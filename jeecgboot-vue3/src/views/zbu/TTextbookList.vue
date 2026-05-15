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

          <a-button type="primary" @click="handleSelectAll" class="ml-2">全选</a-button>
          <a-button type="primary" @click="handleClearSelect" class="ml-2" v-if="selectedRowKeys.length > 0">清空选择</a-button>

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

          <!-- 高级查询 -->
          <!--          <super-query :config="superQueryConfig" @search="handleSuperQuery" class="ml-2 flex-1 min-w-[200px]" />-->

          <!-- 批量修改按钮 -->
          <a-button
            type="primary"
            v-if="selectedRowKeys.length > 0"
            @click="batchHandleEdit"
            preIcon="ant-design:edit-outlined"
            class="ml-2"
          >
            批量修改（标段/编号/折扣）
          </a-button>
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

    <!-- 批量修改弹窗：彻底简化，无任何非法属性 -->
    <a-modal
      v-model:open="batchEditModalVisible"
      title="批量修改教材信息"
      ok-text="确认修改"
      cancel-text="取消"
      @ok="doBatchEdit"
      width="500px"
      :confirm-loading="batchEditLoading"
    >
      <div class="batch-edit-form">
        <div class="form-item">
          <label class="form-label">标段：</label>
          <input v-model="batchEditForm.sectionCode" placeholder="请输入标段（选填）" class="form-input" />
        </div>
        <div class="form-item">
          <label class="form-label">编号：</label>
          <input v-model="batchEditForm.businessCode" placeholder="请输入编号（选填）" class="form-input" />
        </div>
        <div class="form-item">
          <label class="form-label">折扣：</label>
          <input
            v-model="batchEditForm.discount"
            placeholder="请输入折扣（如0.85=85折）"
            class="form-input"
            type="number"
            min="0.01"
            max="1"
            step="0.01"
          />
        </div>
      </div>
    </a-modal>

    <!-- 原有弹窗 -->
    <TTextbookModal @register="registerModal" @success="handleSuccess"></TTextbookModal>
  </div>
</template>

<script lang="ts" name="zbu-tTextbook" setup>
import { ref, reactive } from 'vue';
import { BasicTable, useTable, TableAction } from '/@/components/Table';
import { useModal } from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import TTextbookModal from './components/TTextbookModal.vue'
import { columns, searchFormSchema, superQuerySchema } from './TTextbook.data';
import { list, deleteOne, batchDelete, getImportUrl, getExportUrl, batchEdit, getAllIds, getCurrentSchoolYear } from './TTextbook.api';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDateByPicker } from '/@/utils';

// 基础变量
const fieldPickers = reactive({});
const queryParam = reactive<any>({});
const { createMessage } = useMessage();

// 批量修改弹窗变量（明确初始化）
const batchEditModalVisible = ref(false);
const batchEditForm = reactive({
  sectionCode: '',
  businessCode: '',
  discount: '',
});
// 新增：批量修改加载状态（防止重复提交）
const batchEditLoading = ref(false);

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

      const merged = Object.assign({}, params, queryParam);

      // 如果没有传启用学年参数，调用接口获取当前学年
      if (!merged.enableYear) {
        try {
          const yearRes = await getCurrentSchoolYear();
          if (yearRes && yearRes.currentSchoolYear) {
            merged.enableYear = yearRes.currentSchoolYear;
          }
        } catch (e) {
          console.warn('获取当前学年失败', e);
        }
      }

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

const [registerTable, { reload, getDataSource, getForm }, { rowSelection, selectedRowKeys }] = tableContext;

// 全选所有搜索结果
async function handleSelectAll() {
  try {
    const form = getForm();
    const formValues = form?.getFieldsValue() || {};
    console.log('表单值:', formValues);
    console.log('queryParam:', queryParam);

    const params = { ...queryParam, ...formValues };
    console.log('合并参数:', params);
    params.pageNo = 1;
    params.pageSize = 999999999;
    console.log('最终参数:', params);

    const res = await getAllIds(params);
    console.log('API响应:', res);

    if (res.success && Array.isArray(res.result)) {
      selectedRowKeys.value = res.result;
      createMessage.success('已选中 ' + res.result.length + ' 条记录');
    } else if (res.success) {
      selectedRowKeys.value = [];
      createMessage.warning('没有找到任何记录');
    } else {
      createMessage.warning('获取记录失败: ' + (res.message || '未知错误'));
    }
  } catch (err: any) {
    console.error('错误:', err);
    createMessage.error('获取记录失败: ' + (err.message || '未知错误'));
  }
}

// 清空选择
function handleClearSelect() {
  selectedRowKeys.value = [];
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

// 批量修改方法（简化）
function batchHandleEdit() {
  console.log('批量修改按钮点击，弹窗状态：', batchEditModalVisible.value);
  // 重置表单
  batchEditForm.sectionCode = '';
  batchEditForm.businessCode = '';
  batchEditForm.discount = '';
  // 打开弹窗
  batchEditModalVisible.value = true;
}

// 批量修改提交（核心修复）
async function doBatchEdit() {
  // 1. 基础校验
  if (!batchEditForm.sectionCode && !batchEditForm.businessCode && !batchEditForm.discount) {
    createMessage.warning('请至少填写一个修改字段');
    return;
  }

  // 2. 构造参数（优化折扣处理，避免空字符串转数字报错）
  const params = {
    ids: selectedRowKeys.value.join(','),
    sectionCode: batchEditForm.sectionCode.trim() || undefined,
    businessCode: batchEditForm.businessCode.trim() || undefined,
    discount: batchEditForm.discount ? Number(batchEditForm.discount) : undefined,
  };

  try {
    // 3. 开启加载状态，防止重复提交
    batchEditLoading.value = true;

    // 4. 调用批量修改接口（修复：只传1个参数，符合API定义）
    await batchEdit(params);

    // 5. 成功提示+刷新列表
    createMessage.success('批量修改成功！');
    handleSuccess();
  } catch (err: any) {
    // 6. 失败提示
    createMessage.error(`批量修改失败：${err.message || '服务器处理异常'}`);
  } finally {
    // 7. 关闭弹窗+关闭加载状态
    batchEditModalVisible.value = false;
    batchEditLoading.value = false;
  }
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
