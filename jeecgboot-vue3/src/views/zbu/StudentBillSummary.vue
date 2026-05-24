<template>
  <div>
    <BasicTable
      @register="registerTable"
      :rowSelection="rowSelection"
      :pagination="{
        pageSize: 20,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total) => `共 ${total} 条记录`
      }"
    >
      <template #tableTitle>
        <a-button  type="primary" v-auth="'zbu:student_bill:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
            </a-menu>
          </template>
          <a-button v-auth="'zbu:student_bill:deleteBatch'">批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>

      <template v-slot:bodyCell="{ column, record, text }">
        <template v-if="column.dataIndex === 'studentId'">
          <span :class="{ 'is-summary-cell': record?.isSummary }">{{ record?.isSummary ? '总计教材费用' : (record?.studentId || '') }}</span>
        </template>
        <template v-if="column.dataIndex === 'totalDiscountPrice'">
          {{ record?.isSummary
          ? `¥${(record?.totalDiscountPrice || 0).toFixed(2)}`
          : `¥${(record?.totalDiscountPrice || 0).toFixed(2)}`
          }}
        </template>
      </template>

      <template #action="{ record }">
        <TableAction
          v-if="!record?.isSummary"
          :actions="getTableAction(record)"
        />
      </template>
    </BasicTable>
  </div>
</template>

<script lang="ts" name="zbu-studentBill-summary" setup>
import {ref, reactive, computed, unref, watch} from 'vue';
import {BasicTable, useTable, TableAction} from '/@/components/Table';
import {useListPage} from '/@/hooks/system/useListPage';
import {columns, searchFormSchema} from './StudentBillSummary.data';
import {summaryList, batchDelete} from './StudentBillSummary.api';
import {useUserStore} from '/@/store/modules/user';
import {useMessage} from '/@/hooks/web/useMessage';
import { filterObj } from '/@/utils/common/compUtils';

type Recordable = {[key: string]: any};
const tableData = ref<Recordable[]>([]);
const totalDiscountPrice = ref<number>(0);
const userStore = useUserStore();
const {createMessage} = useMessage();

const isAdmin = computed(() => {
  const roles = userStore.getRoles || [];
  return roles.includes('admin') || roles.includes('sysadmin');
});

const searchParams = reactive({
  schoolYear: '',
  semester: '',
});

const {prefix, tableContext, onExportXls} = useListPage({
  exportConfig: {
    url: '/zbu/studentBill/exportSummary',
    name: '学生账单汇总',
    params: () => {
      return filterObj(searchParams, (key, value) => {
        return value !== '' && value !== undefined && value !== null;
      });
    },
  },
  tableProps: {
    title: '学生账单汇总',
    api: async (params) => {
      const queryParams = {
        ...params,
        studentNo: params.studentNo || '',
        studentName: params.studentName || '',
        collegeName: params.collegeName || '',
        majorName: params.majorName || '',
        className: params.className || '',
        schoolYear: params.schoolYear || '',
        semester: params.semester || '',
      };
      const res = await summaryList(queryParams);
      if (res) {
        const rawRecords = res.records || [];
        const formattedRecords = rawRecords.map((item, index) => {
          return {
            ...item,
            id: item.id || `summary-row-${index}`,
          };
        });

        const originalTotal = res.total || 0;
        const pageSize = params.pageSize || 10;
        const pageNo = params.pageNo || 1;
        const lastDataPage = Math.ceil(originalTotal / pageSize) || 1;
        const isLastDataPage = pageNo === lastDataPage;
        const isExtraPage = pageNo > lastDataPage;

        // 计算总计金额：需要全量数据，不能只看当前页
        let allTotalPrice = 0;
        if (isExtraPage) {
          // 总计单独一页时，当前页没有数据，需要额外请求全量数据计算金额
          try {
            const allRes = await summaryList({ ...queryParams, pageSize: 99999, pageNo: 1 });
            const allRecords = allRes.records || [];
            allTotalPrice = allRecords.reduce((sum, item) => sum + (Number(item.totalDiscountPrice) || 0), 0);
          } catch (e) {
            allTotalPrice = 0;
          }
        } else {
          // 当前页有数据时，还需要加上前面页的金额
          if (pageNo > 1) {
            try {
              const prevRes = await summaryList({ ...queryParams, pageSize: 99999, pageNo: 1 });
              const prevRecords = prevRes.records || [];
              allTotalPrice = prevRecords.reduce((sum, item) => sum + (Number(item.totalDiscountPrice) || 0), 0);
            } catch (e) {
              allTotalPrice = formattedRecords.reduce((sum, item) => sum + (Number(item.totalDiscountPrice) || 0), 0);
            }
          } else {
            allTotalPrice = formattedRecords.reduce((sum, item) => sum + (Number(item.totalDiscountPrice) || 0), 0);
          }
        }
        totalDiscountPrice.value = allTotalPrice;

        const summaryRow = {
          id: 'summary-row-total',
          isSummary: true,
          studentId: '总计教材费用',
          studentName: '',
          collegeName: '',
          majorName: '',
          className: '',
          schoolYear: '',
          semester: '',
          totalDiscountPrice: allTotalPrice,
        };

        if (isExtraPage) {
          formattedRecords.length = 0;
          formattedRecords.push(summaryRow);
        } else if (isLastDataPage && formattedRecords.length < pageSize) {
          formattedRecords.push(summaryRow);
        }

        tableData.value = formattedRecords;
        return {
          records: formattedRecords,
          total: originalTotal + 1,
        };
      }
      return {records: [], total: 0};
    },
    columns,
    searchInfo: {},
    useSearchForm: true,
    formConfig: {
      schemas: searchFormSchema,
    },
    showIndexColumn: true,
    stripeRows: true,
    bordered: true,
    showActionColumn: false,
    rowKey: 'id',
  },
});

const [registerTable, {reload}, { rowSelection, selectedRowKeys }] = tableContext;

function getTableAction(record) {
  return [
    {
      label: '查看明细',
      onClick: handleViewDetail.bind(null, record),
    },
  ];
}

function handleViewDetail(record: Recordable) {
  createMessage.info(`查看学生 ${record.studentNo} 的账单明细`);
}

async function handleSearch() {
  await reload();
}

async function handleReset() {
  searchParams.schoolYear = '';
  searchParams.semester = '';
  await reload();
}

async function batchHandleDelete() {
  if (selectedRowKeys.value.length === 0) {
    createMessage.warning('请先选择要删除的数据');
    return;
  }

  // 过滤掉自动生成的汇总行
  const validIds = selectedRowKeys.value.filter(key =>
    !key.startsWith('summary-row-') && key !== 'summary-row-total'
  );
  if (validIds.length === 0) {
    createMessage.warning('没有可删除的数据');
    return;
  }

  await batchDelete({ids: validIds}, handleSuccess);
}

async function handleSuccess() {
  selectedRowKeys.value = [];
  reload();
}

watch(() => searchParams, () => {
  reload();
}, {deep: true});
</script>

<style scoped>
.bill-table-container {
  padding: 16px;
}
:deep(.ant-table-tbody tr:has(td .is-summary-cell)) {
  font-weight: bold;
  background-color: #fafafa;
  border-top: 2px solid #f0f0f0;
}
</style>
