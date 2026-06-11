<template>
  <div>
    <!-- 表格组件：移除所有可能的非法属性，简化绑定 -->
    <BasicTable @register="registerTable" :row-selection="rowSelection">
      <!-- 表格标题栏 -->
      <template #tableTitle>
        <div class="table-title-bar">
          <a-button type="primary" v-auth="'zbu:t_textbook:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          <a-button type="primary" v-auth="'zbu:t_textbook:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls" class="ml-2"> 导出</a-button>
          <j-upload-button type="primary" v-auth="'zbu:t_textbook:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls" class="ml-2">导入</j-upload-button>
          <j-upload-button type="primary" v-auth="'zbu:t_textbook:importExcel'" preIcon="ant-design:edit-outlined" @click="onUpdateXls" class="ml-2">更新</j-upload-button>

          <a-dropdown v-if="selectedRowKeys.length > 0" class="ml-2">
            <template #overlay>
              <a-menu>
                <a-menu-item key="del" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'zbu:t_textbook:deleteBatch'">批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </div>
      </template>

      <!-- 操作栏 -->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :drop-down-actions="getDropDownAction(record)"/>
      </template>

      <!-- 字段回显插槽 -->
      <template v-slot:bodyCell="{ column, record, index, text }">
      </template>
    </BasicTable>

    <!-- 原有弹窗 -->
    <TTextbookModal @register="registerModal" @success="handleSuccess"></TTextbookModal>
  </div>
</template>

<script lang="ts" name="zbu-tTextbook" setup>
import { reactive, ref } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { useMethods } from '/@/hooks/system/useMethods'
import TTextbookModal from './components/TTextbookModal.vue'
import { columns, searchFormSchema, superQuerySchema } from './TTextbook.data';
import { list, deleteOne, batchDelete, getImportUrl, getUpdateUrl, getExportUrl } from './TTextbook.api';
import { getDateByPicker } from '/@/utils';

// 基础变量
const fieldPickers = reactive({});
const queryParam = reactive<any>({});
const isFirstQuery = ref(true);

// 注册原有Modal
const [registerModal, { openModal }] = useModal();

// 注册表格
const { tableContext, onExportXls, onImportXls } = useListPage({
  tableProps: {
    title: '教材表',
    api: list,
    columns,
    canResize: true,
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter: true,
      showAdvancedButton: true,
      fieldMapToNumber: [],
      fieldMapToTime: [],
    },
    actionColumn: {
      width: 120,
      fixed: 'right'
    },
    beforeFetch: async (params) => {
      if (params && fieldPickers) {
        for (let key in fieldPickers) {
          if (params[key]) {
            params[key] = getDateByPicker(params[key], fieldPickers[key]);
          }
        }
      }

      const merged = Object.assign({}, queryParam, params);
      // 仅首次加载时，默认查询当前学年；取消勾选后不再强制回填
      if (!merged.enableYear && isFirstQuery.value) {
        const now = new Date();
        const currentYear = now.getFullYear();
        const currentMonth = now.getMonth() + 1;
        merged.enableYear = (currentMonth < 6) ? `${currentYear - 1}-${currentYear}` : `${currentYear}-${currentYear + 1}`;
      }
      isFirstQuery.value = false;
      // 同步到 queryParam，确保导出时能带上当前搜索条件
      Object.keys(queryParam).forEach(key => {
        if (!(key in merged)) delete queryParam[key];
      });
      Object.keys(merged).forEach(key => {
        queryParam[key] = merged[key];
      });

      loadAllKeys(merged);

      return merged;
    },
  },
  exportConfig: {
    name: "教材表",
    url: getExportUrl,
    params: queryParam,
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess
  },
})

// 预加载全量ID，用于跨页全选
const allTableKeys = ref<string[]>([]);
const loadAllKeys = async (searchParams?: any) => {
  try {
    const res = await list({ ...(searchParams || queryParam), pageSize: 99999, pageNo: 1 });
    allTableKeys.value = (res?.records || []).map((r: any) => r.id).filter(Boolean);
  } catch (e) { /* ignore */ }
};

const [registerTable, { reload }, { rowSelection, selectedRowKeys }] = tableContext;

// 全选时选择当前筛选条件下的所有数据（同步，使用预加载ID）
rowSelection.onSelectAll = (selected) => {
  if (selected && allTableKeys.value.length > 0) {
    selectedRowKeys.value = [...allTableKeys.value];
  } else {
    selectedRowKeys.value = [];
  }
};

// 更新Excel上传（复用导入的上传处理逻辑）
const { handleImportXls } = useMethods();
function onUpdateXls(file) {
  return handleImportXls(file, getUpdateUrl, reload);
}

// 高级查询配置
const superQueryConfig = reactive(superQuerySchema);

// 高级查询事件
function handleSuperQuery(params) {
  Object.keys(params).map((k) => {
    queryParam[k] = params[k];
  });
  reload();
}

// 原有方法（不变）
function handleAdd() {
  openModal(true, { isUpdate: false, showFooter: true });
}
function handleEdit(record: Recordable) {
  openModal(true, { record, isUpdate: true, showFooter: true });
}
function handleDetail(record: Recordable) {
  openModal(true, { record, isUpdate: true, showFooter: false });
}
async function handleDelete(record) {
  await deleteOne({ id: record.id }, handleSuccess);
}
async function batchHandleDelete() {
  await batchDelete({ ids: selectedRowKeys.value }, handleSuccess);
}

// 成功回调
function handleSuccess() {
  selectedRowKeys.value = [];
  reload();
}

// 操作栏
function getTableAction(record) {
  return [{ label: '编辑', onClick: () => handleEdit(record), auth: 'zbu:t_textbook:edit' }];
}

// 下拉操作栏
function getDropDownAction(record) {
  return [
    { label: '详情', onClick: () => handleDetail(record) },
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: () => handleDelete(record),
        placement: 'topLeft',
      },
      auth: 'zbu:t_textbook:delete'
    }
  ];
}
</script>

<style lang="less" scoped>
/* 简化样式，避免组件样式冲突 */
.table-title-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
  margin-bottom: 10px;
}
.ml-2 {
  margin-left: 8px;
}
.flex-1 {
  flex: 1;
}
.min-w-[200px] {
  min-width: 200px;
}

/* 批量修改表单样式 */
.batch-edit-form {
  padding: 10px 0;
}
.form-item {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}
.form-label {
  width: 60px;
  text-align: right;
  margin-right: 8px;
  font-weight: 500;
}
.form-input {
  flex: 1;
  padding: 4px 8px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  height: 32px;
  box-sizing: border-box;
}
.form-input:focus {
  outline: none;
  border-color: #1890ff;
}

:deep(.ant-picker), :deep(.ant-input-number) {
  width: 100%;
}
</style>
