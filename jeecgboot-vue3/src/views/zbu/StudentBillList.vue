<template>
  <div>
    <div class="bill-table-container">
      <BasicTable
        @register="registerTable"
        :rowSelection="billRowSelection"
        :form-config="tableFormConfig"
        :pagination="{
          pageSize: 10,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total) => `共 ${total} 条记录`
        }"
      >
        <template #tableTitle>
          <template v-if="isAdmin">
            <a-button type="primary" v-auth="'zbu:student_bill:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
            <a-button type="primary" v-auth="'zbu:student_bill:sync'" @click="handleSync" preIcon="ant-design:sync-outlined" style="margin-left: 8px;"> 同步征订数据</a-button>
            <a-button  type="primary" v-auth="'zbu:student_bill:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
            <!--            <j-upload-button type="primary" v-auth="'zbu:student_bill:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>-->
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
            <!--            <super-query :config="superQueryConfig" @search="handleSuperQuery" />-->
          </template>
        </template>

        <template v-slot:bodyCell="{ column, record, text }">
          <template v-if="column.dataIndex === 'studentNo'">
            <span :class="{ 'is-summary-cell': record?.isSummary }">{{ record?.isSummary ? '总计教材费用' : (record?.studentNo || '未知学号') }}</span>
          </template>
          <template v-if="column.dataIndex === 'studentName'">
            {{ record?.isSummary ? '' : (record?.studentName || '未知姓名') }}
          </template>
          <template v-if="column.dataIndex === 'textbookName'">
            {{ record?.isSummary ? '' : (record?.textbookName || '未知教材') }}
          </template>
          <template v-if="column.dataIndex === 'majorName'">
            {{ record?.isSummary ? '' : (record?.majorName || '未知专业') }}
          </template>
          <template v-if="column.dataIndex === 'discountPrice'">
            {{ record?.isSummary ? `¥${(record?.discountPrice || 0).toFixed(2)}` : `¥${(record?.discountPrice || 0).toFixed(2)}` }}
          </template>
          <template v-if="column.dataIndex === 'subscribeStatus'">
            {{ record?.isSummary ? '' : text }}
          </template>
          <template v-if="column.dataIndex === 'receiveStatus'">
            {{ record?.isSummary ? '' : text }}
          </template>
        </template>
      </BasicTable>
    </div>

    <StudentBillModal
      @register="registerModal"
      @success="handleSuccess"
    ></StudentBillModal>
  </div>
</template>

<script lang="ts" name="zbu-studentBill" setup>
import {ref, reactive, computed, unref, watch, onMounted} from 'vue';
import {BasicTable, useTable, TableAction} from '/@/components/Table';
import {useModal} from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import StudentBillModal from './components/StudentBillModal.vue'
import {columns, searchFormSchema, superQuerySchema} from './StudentBill.data';
import {
  list, deleteOne, batchDelete, getImportUrl, getExportUrl,
  syncFromSubscription
} from './StudentBill.api';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDateByPicker } from '/@/utils';

type Recordable = { [key: string]: any };
const tableData = ref<Recordable[]>([]);
const totalDiscountPrice = ref<number>(0);

const fieldPickers = reactive({});
const queryParam = reactive<any>({});
const userStore = useUserStore();
const { createMessage } = useMessage();

const [registerModal, {openModal}] = useModal();

const userRoleType = computed(() => {
  const userInfo = userStore.getUserInfo || {};
  const roleCode = (userInfo.roleCode || "").toLowerCase().trim();
  const username = (userInfo.username || "").toLowerCase().trim();

  if (['admin', 'sysadmin'].includes(username) || roleCode.includes('admin')) {
    return 'admin';
  }
  if (roleCode.includes('counselor') || roleCode.includes('daoyuan')) {
    return 'counselor';
  }
  return 'student';
});

const isAdmin = computed(() => unref(userRoleType) === 'admin');
const isCounselor = computed(() => unref(userRoleType) === 'counselor');
const isStudent = computed(() => unref(userRoleType) === 'student');
const isFirstQuery = ref(true);

const tableFormConfig = computed(() => ({
  schemas: searchFormSchema,
  show: true, // 所有角色均可使用搜索框
  showAdvancedButton: true,
  showSearchButton: true,
  showResetButton: true,
  autoSubmitOnEnter: true,
  submitButtonProps: { label: '查询' },
  resetButtonProps: { label: '重置' }
}));

const studentSearchSchema = computed(() => [
  {
    field: 'subscriptionYear',
    label: '征订年份',
    component: 'Input',
    componentProps: {
      placeholder: '请输入征订年份（如2025）',
      style: { width: '180px' }
    },
  },
  {
    field: 'subscriptionSemester',
    label: '征订学期',
    component: 'Select',
    componentProps: {
      style: { width: '180px' },
      placeholder: '请选择学期',
      options: [
        { label: '第一学期', value: '1' },
        { label: '第二学期', value: '2' }
      ]
    },
  },
  {
    field: 'textbookName',
    label: '教材名称',
    component: 'Input',
    componentProps: {
      placeholder: '请输入教材名称',
      style: { width: '200px' }
    },
  },
]);

const fetchBillList = async (params = {}) => {
  try {
    const requestParams = { ...params, ...queryParam };
    const userInfo = userStore.getUserInfo || {};
    const loginUsername = userInfo.username || "";

    if (unref(isStudent)) {
      if (!loginUsername) {
        createMessage.warning("未获取到你的登录学号，请重新登录");
        return { records: [], total: 0 };
      }
      requestParams.studentNo = loginUsername;
      requestParams.student_id = loginUsername;
    }

    // 仅首次加载时，默认查询当前学年；取消勾选后不再强制回填
    if (!requestParams.subscriptionYear && isFirstQuery.value) {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;
      requestParams.subscriptionYear = (currentMonth < 6) ? `${currentYear - 1}-${currentYear}` : `${currentYear}-${currentYear + 1}`;
    }
    isFirstQuery.value = false;

    const res = await list(requestParams);
    console.log('【账单列表】后端返回原始数据：', JSON.stringify(res).substring(0, 500));
    console.log('【账单列表】当前页码/每页条数：', requestParams.pageNo, requestParams.pageSize);
    const rawRecords = res.records || [];
    console.log('【账单列表】原始记录数量：', rawRecords.length);
    const formattedRecords: Recordable[] = [];

    for (const item of rawRecords) {
      const studentNo = item.studentNo || item.student_id || item.studentId || "未知学号";
      const studentName = item.studentName || "未知姓名";
      const majorName = item.majorName || "未知专业";
      const textbookName = item.textbookName || "未知教材";

      formattedRecords.push({
        ...item,
        id: item.id || `${item.studentId || ''}-${item.textbookName || ''}-${item.subscriptionYear || ''}`,
        studentId: item.student_id || item.studentId,
        subscriptionId: item.subscription_id || item.subscriptionId,
        textbookId: item.textbook_id || item.textbookId,
        majorId: item.major_id || item.majorId,
        receiveId: item.receive_id || item.receiveId,
        studentNo,
        studentName,
        majorName,
        textbookName,
        isbn: item.isbn || '',
        collegeName: item.collegeName || item.college_name || '',
        className: item.className || item.class_name || '',
        subscriptionYear: item.subscriptionYear || item.subscription_year,
        subscriptionSemester: item.subscriptionSemester || item.subscription_semester,
        subscribeStatus: item.subscribeStatus || item.subscribe_status,
        receiveStatus: item.receiveStatus || item.receive_status,
        price: item.price,
        discountPrice: item.discountPrice,
        remark: item.remark,
      });
    }

    let finalRecords = [...formattedRecords];
    if (unref(isStudent) && Object.keys(requestParams).length > 0) {
      if (requestParams.subscriptionYear) {
        const year = requestParams.subscriptionYear.trim();
        finalRecords = finalRecords.filter(item => (item.subscriptionYear || '').includes(year));
      }
      if (requestParams.subscriptionSemester) {
        finalRecords = finalRecords.filter(item => item.subscriptionSemester === requestParams.subscriptionSemester);
      }
      if (requestParams.textbookName) {
        const textbook = requestParams.textbookName.trim().toLowerCase();
        finalRecords = finalRecords.filter(item => (item.textbookName || '').toLowerCase().includes(textbook));
      }
    }
    // 管理员筛选：学院/专业/班级（直接字段匹配）
    if (unref(isAdmin)) {
      if (requestParams.collegeName) {
        const searchKey = requestParams.collegeName.trim().toLowerCase();
        finalRecords = finalRecords.filter(item =>
          (item.collegeName || '').toLowerCase().includes(searchKey)
        );
      }
      if (requestParams.majorName) {
        const searchKey = requestParams.majorName.trim().toLowerCase();
        finalRecords = finalRecords.filter(item =>
          (item.majorName || '').toLowerCase().includes(searchKey)
        );
      }
      if (requestParams.className) {
        const searchKey = requestParams.className.trim().toLowerCase();
        finalRecords = finalRecords.filter(item =>
          (item.className || '').toLowerCase().includes(searchKey)
        );
      }
    }

    totalDiscountPrice.value = finalRecords.reduce((sum, item) => {
      return sum + (Number(item.discountPrice) || 0);
    }, 0);

    const originalTotal = res.total || 0;

    // 管理员模式：最后一行加总计
    if (unref(isAdmin) && originalTotal > 0) {
      // 额外请求全量数据，计算筛选后的真实总数和总计金额
      let allTotalPrice = 0;
      let filteredTotal = originalTotal;
      try {
        const allRes = await list({ ...requestParams, pageSize: 99999, pageNo: 1 });
        const allRaw = allRes.records || [];
        let allFiltered = [...allRaw];
        if (requestParams.collegeName) {
          const key = requestParams.collegeName.trim().toLowerCase();
          allFiltered = allFiltered.filter(item => (item.collegeName || item.college_name || '').toLowerCase().includes(key));
        }
        if (requestParams.majorName) {
          const key = requestParams.majorName.trim().toLowerCase();
          allFiltered = allFiltered.filter(item => (item.majorName || item.major_name || '').toLowerCase().includes(key));
        }
        if (requestParams.className) {
          const key = requestParams.className.trim().toLowerCase();
          allFiltered = allFiltered.filter(item => (item.className || item.class_name || '').toLowerCase().includes(key));
        }
        filteredTotal = allFiltered.length;
        allTotalPrice = allFiltered.reduce((sum, item) => sum + (Number(item.discountPrice) || 0), 0);
      } catch (e) {
        allTotalPrice = totalDiscountPrice.value;
      }
      totalDiscountPrice.value = allTotalPrice;

      const pageSize = requestParams.pageSize || 10;
      const pageNo = requestParams.pageNo || 1;
      const lastDataPage = Math.ceil(filteredTotal / pageSize) || 1;
      const isLastDataPage = pageNo === lastDataPage;
      const isExtraPage = pageNo > lastDataPage;

      const summaryRow = {
        id: 'summary-row',
        isSummary: true,
        studentNo: '总计教材费用',
        studentName: '',
        majorName: '',
        className: '',
        subscriptionYear: '',
        subscriptionSemester: '',
        textbookName: '',
        isbn: '',
        price: '',
        discountPrice: allTotalPrice,
        subscribeStatus: '',
        receiveStatus: '',
        remark: '',
      };

      if (isExtraPage) {
        // 总计单独一页
        finalRecords = [summaryRow];
      } else if (isLastDataPage && finalRecords.length < pageSize) {
        // 最后一页没满，直接追加总计行
        finalRecords.push(summaryRow);
      }
      // 如果最后一页满了，总计会自动出现在下一页（isExtraPage时显示）

      tableData.value = finalRecords;
      return { records: finalRecords, total: filteredTotal + 1 };
    }

    tableData.value = finalRecords;
    return { records: finalRecords, total: originalTotal };
  } catch (e) {
    console.error("【获取账单列表失败】", e);
    createMessage.error("获取账单记录失败，请刷新重试");
    tableData.value = [];
    totalDiscountPrice.value = 0;
    return { records: [], total: 0 };
  }
};

const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps:{
    title: '个人账单',
    api: fetchBillList,
    columns: columns,
    canResize:true,
    showActionColumn: false,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [],
      fieldMapToTime: [],
    },
    useSearchForm: true, // 所有角色均可使用搜索框
    beforeFetch: async (params) => {
      if (params && fieldPickers) {
        for (let key in fieldPickers) {
          if (params[key]) {
            params[key] = getDateByPicker(params[key], fieldPickers[key]);
          }
        }
      }

      const merged = Object.assign({}, params, queryParam);

      // 仅首次加载时，默认查询当前学年；取消勾选后不再强制回填
      if (!merged.subscriptionYear && isFirstQuery.value) {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;
        merged.subscriptionYear = (currentMonth < 6) ? `${currentYear - 1}-${currentYear}` : `${currentYear}-${currentYear + 1}`;
      }

      loadAllKeys(merged);

      return merged;
    },
  },
  exportConfig: {
    name:"个人账单",
    url: getExportUrl,
    params: queryParam,
    show: unref(isAdmin)
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
    show: false //隐藏导入 unref(isAdmin)
  },
});

// 预加载全量ID，用于跨页全选
const allTableKeys = ref<string[]>([]);
const loadAllKeys = async (searchParams?: any) => {
  try {
    const res = await list({ ...(searchParams || queryParam), pageSize: 99999, pageNo: 1 });
    allTableKeys.value = (res?.records || []).map((r: any) => r.id).filter(Boolean);
  } catch (e) { /* ignore */ }
};

const [registerTable, {reload}, { rowSelection, selectedRowKeys }] = tableContext;

// 全选时选择当前筛选条件下的所有数据（同步，使用预加载ID）
rowSelection.onSelectAll = (selected) => {
  if (selected && allTableKeys.value.length > 0) {
    selectedRowKeys.value = [...allTableKeys.value];
  } else {
    selectedRowKeys.value = [];
  }
};

const billRowSelection = computed(() => {
  if (unref(isStudent)) {
    return undefined;
  }
  return rowSelection;
});

const superQueryConfig = reactive(superQuerySchema);

watch([tableData], () => {
  totalDiscountPrice.value = tableData.value.reduce((sum, item) => {
    return sum + (Number(item.discountPrice) || 0);
  }, 0);
}, { deep: true });

async function handleSync() {
  if (!unref(isAdmin)) {
    createMessage.warning("仅管理员可执行同步操作");
    return;
  }
  try {
    createMessage.loading('正在同步征订数据，请稍候...');
    const res = await syncFromSubscription();
    createMessage.success(res.msg || '同步征订数据成功！');
    reload();
  } catch (e: any) {
    createMessage.error(e.msg || '同步征订数据时发生错误，请检查后端日志！');
  }
}

function handleSuperQuery(params) {
  Object.assign(queryParam, params);
  reload();
}

function handleAdd() {
  if (!unref(isAdmin)) {
    createMessage.warning("仅管理员可新增账单");
    return;
  }
  openModal(true, {
    isUpdate: false,
    showFooter: true,
  });
}

function handleEdit(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: true,
  });
}

function handleDetail(record: Recordable) {
  openModal(true, {
    record,
    isUpdate: true,
    showFooter: false,
  });
}

async function handleDelete(record) {
  if (!unref(isAdmin)) {
    createMessage.warning("仅管理员可删除账单");
    return;
  }
  await deleteOne({id: record.id}, handleSuccess);
}

async function batchHandleDelete() {
  if (!unref(isAdmin)) {
    createMessage.warning("仅管理员可批量删除账单");
    return;
  }
  const validIds = selectedRowKeys.value.filter(key => key !== 'summary-row');
  if (validIds.length === 0) {
    createMessage.warning("请选择要删除的账单记录");
    return;
  }
  await batchDelete({ids: validIds}, handleSuccess);
}

function handleSuccess() {
  selectedRowKeys.value = [];
  reload();
}

function getTableAction(record) {
  if (unref(isAdmin)) {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        auth: 'zbu:student_bill:edit'
      }
    ];
  } else {
    return [
      {
        label: '编辑',
        onClick: handleEdit.bind(null, record),
        type: 'primary'
      }
    ];
  }
}

function getDropDownAction(record) {
  if (unref(isAdmin)) {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      }, {
        label: '删除',
        popConfirm: {
          title: '是否确认删除',
          confirm: handleDelete.bind(null, record),
          placement: 'topLeft',
        },
        auth: 'zbu:student_bill:delete'
      }
    ];
  } else {
    return [
      {
        label: '详情',
        onClick: handleDetail.bind(null, record),
      }
    ];
  }
}
</script>

<style lang="less" scoped>
:deep(.ant-input), :deep(.ant-picker), :deep(.ant-select-selector), :deep(.ant-input-number) {
  width: 200px;
  box-sizing: border-box;
}

:deep(.ant-select) {
  width: 200px !important;
  min-width: 200px !important;
  flex: none !important;
  display: inline-flex;
  align-items: center;
}

:deep(.ant-select-selection-item) {
  white-space: nowrap !important;
}

:deep(.ant-table-actions .ant-btn-primary) {padding: 0 8px;margin-right: 4px;}

:deep(.ant-form-item-control-input-content) {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

:deep(.ant-form-item-label) {
  width: 80px;
  text-align: right;
}

:deep(.ant-form) {
  padding: 8px 0;
}

.bill-table-container {
  position: relative;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  margin: 10px 0;
}

.bill-summary-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  padding: 12px 24px;
  background: #fafafa;
  border-top: 2px solid #f0f0f0;
  margin-top: -1px;

  .summary-label {
    font-weight: bold;
    color: #333;
    margin-right: 8px;
    margin-left: 16px;
  }

  .summary-value {
    font-weight: bold;
    color: #165DFF;
    font-size: 14px;
  }
}

:deep(.ant-table-tbody tr:has(td .is-summary-cell)) {
  font-weight: bold;
  background-color: #fafafa;
  border-top: 2px solid #f0f0f0;
}

:deep(.ant-table-pagination) {
  margin: 16px !important;
  text-align: right;
}

.hide-search-form :deep(.ant-table-form-container) {
  display: none !important;
}
</style>
