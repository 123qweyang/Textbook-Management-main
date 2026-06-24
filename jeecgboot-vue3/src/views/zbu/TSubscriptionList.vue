<template>
  <div>
    <BasicTable
      @register="registerTable"
      :rowSelection="tableRowSelection"
      :form-config="tableFormConfig"
    >
      <template #tableTitle>
        <!--  修改1：所有角色显示批量修改按钮（区分文字） -->
        <div class="batch-btn-container">
          <a-button
            type="primary"
            size="small"
            :loading="subscribing"
            :disabled="subscribing"
            @click="handleBatchUpdateSubscribeStatus"
            v-if="!isAdmin && !isCounselor"
          >
            同意征订
          </a-button>
          <a-button
            type="primary"
            size="small"
            :loading="subscribing"
            :disabled="subscribing"
            @click="handleBatchUpdateSubscribeStatus"
            v-if="(isAdmin || isCounselor) && selectedRowKeys.length > 0"
          >
            批量标记为已征订
          </a-button>
        </div>

        <!-- 管理员/辅导员端：保留原有新增/导出/导入/批量操作 -->
        <template v-if="isAdmin || isCounselor">
          <template v-if="isAdmin">
            <a-button type="primary" v-auth="'zbu:t_subscription:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          </template>
          <a-button  type="primary" v-auth="'zbu:t_subscription:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <!--          <j-upload-button type="primary" v-auth="'zbu:t_subscription:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>-->
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'zbu:t_subscription:deleteBatch'">批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </template>
        <!--        <super-query v-if="isAdmin || isCounselor" :config="superQueryConfig" @search="handleSuperQuery" />-->
      </template>

      <!--  修改2：学生端隐藏操作列 -->
      <template #action="{ record }" v-if="!isStudent">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)"/>
      </template>

      <template v-slot:bodyCell="{ column, record, index, text }">
        <template v-if="column.dataIndex === 'studentNo'">
          {{ record.studentNo || '未知学号' }}
        </template>
        <template v-if="column.dataIndex === 'studentName'">
          {{ record.studentName || '未知姓名' }}
        </template>
        <template v-if="column.dataIndex === 'textbookName'">
          {{ record.textbookName || '未知教材' }}
        </template>
        <template v-if="column.dataIndex === 'majorName'">
          {{ record.majorName || '未知专业' }}
        </template>
        <template v-if="column.dataIndex === 'subscribeStatus_dictText'">
          {{ getSubscribeStatusText(record.subscribeStatus) }}
        </template>
        <template v-if="column.dataIndex === 'subscriptionSemester'">
          {{ record.subscriptionSemester === '1' ? '第一学期' : '第二学期' }}
        </template>
      </template>
    </BasicTable>
    <TSubscriptionModal
      @register="registerModal"
      @success="handleSuccess"
    ></TSubscriptionModal>
  </div>
</template>

<script lang="ts" name="zbu-tSubscription" setup>
import axios from 'axios';
import {ref, reactive, computed, unref, onMounted} from 'vue';
import {BasicTable, useTable, TableAction} from '/@/components/Table';
import {useModal} from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { defHttp } from '/@/utils/http/axios';
import TSubscriptionModal from './components/TSubscriptionModal.vue'
import {columns, searchFormSchema, superQuerySchema} from './TSubscription.data';
import {
  deleteOne, batchDelete, getImportUrl, getExportUrl, getMySubscription,
  getStudentById, getTextbookById, getMajorById, getCollegeById, batchUpdateSubscribeStatus,
  getStudentByNo
} from './TSubscription.api';
import { downloadFile } from '/jeecgboot-vue3/src/utils/common/renderUtils';
import { useUserStore } from '/@/store/modules/user';
import { useMessage } from '/@/hooks/web/useMessage';
import { getDateByPicker } from '/@/utils';

type Recordable = { [key: string]: any };
const fieldPickers = reactive({});
const queryParam = reactive<any>({});
const { createMessage } = useMessage();

const getSubscribeStatusText = (status: string) => {
  if (status === '1') return '已征订';
  if (status === '2') return '未征订';
  return '未征订';
};

const normalizeSubscribeStatus = (status: string | undefined) => {
  if (!status) return '0';
  if (status === '1' || status === '2') return status;
  if (status === '未设置' || status === '未征订' || status === '' || status === '0') return '0';
  return '0';
};

const [registerModal, {openModal}] = useModal();
const currentStudentId = ref("");
const currentStudentInfo = ref<Recordable>({});
const subscribing = ref(false);

// ========== 核心优化：精准定义角色类型，增强兜底和兼容性 ==========
const userStore = useUserStore();
const userRoleType = computed(() => {
  const userInfo = userStore.getUserInfo || {};
  // 双重兜底 + 转小写去空格，兼容后端返回的各种格式（大写/带空格/多角色码）
  const roleCode = (userInfo.roleCode || "").toLowerCase().trim();
  const username = (userInfo.username || "").toLowerCase().trim();

  // 管理员判断（优先级最高）
  if (['admin', 'sysadmin'].includes(username) || roleCode.includes('admin')) {
    return 'admin';
  }
  // 辅导员判断（兼容counselor/daoyuan等别名）
  if (roleCode.includes('counselor') || roleCode.includes('daoyuan')) {
    return 'counselor';
  }
  // 默认学生
  return 'student';
});

// 简化角色判断（基于统一的roleType）
const isAdmin = computed(() => unref(userRoleType) === 'admin');
const isCounselor = computed(() => unref(userRoleType) === 'counselor');
const isStudent = computed(() => unref(userRoleType) === 'student');

// ========== 响应式控制表单显示（兼容setFormProps报错） ==========
const studentSchemaFields = ['subscriptionYear', 'subscriptionSemester', 'subscribeStatus'];
const tableFormConfig = computed(() => ({
  schemas: unref(isStudent)
    ? searchFormSchema.filter(s => studentSchemaFields.includes(s.field))
    : searchFormSchema,
  show: true, // 所有角色均可使用搜索框
  showAdvancedButton: unref(isAdmin) || unref(isCounselor),
  showSearchButton: true,
  showResetButton: true,
  autoSubmitOnEnter: true,
  submitButtonProps: { label: '查询' },
  resetButtonProps: { label: '重置' }
}));

// ========== 封装：安全获取学生信息（避免报错） ==========
const getStudentInfoSafely = async (studentNo: string) => {
  try {
    if (!studentNo) return null;
    const res = await getStudentByNo(studentNo);
    // 兼容后端返回格式（判断success）
    return res?.success ? res.result : null;
  } catch (e) {
    // 静默失败，仅日志（不抛到控制台）
    console.debug("【非关键错误】按学号查询学生失败（预期行为）：", e.message);
    return null;
  }
};

const allSubscriptionKeys = ref<string[]>([]);

// ========== fetchTableData，服务端分页/筛选/排序 ==========
  const fetchTableData = async (params = {}) => {
    selectedRowKeys.value = []; // 翻页/筛选时清空上一页勾选
    try {
      const roleType = unref(userRoleType);
      console.log(`【${roleType}端】开始获取数据，参数：`, params);

      // 1. 调用后端分页接口（defHttp已unwrap: res = response.data.result）
      const res = await getMySubscription(params);
      // 兼容新旧后端：新后端返回{records,total,...}，旧后端返回数组
      const rawRecords = Array.isArray(res) ? res : (res?.records || []);
      const total = Array.isArray(res) ? res.length : (res?.total || 0);

      if (rawRecords.length === 0) {
        return {records: [], total: 0};
      }

      // 2. 学生端：获取当前登录学生ID
      if (unref(isStudent)) {
        currentStudentId.value = rawRecords[0]?.studentId || rawRecords[0]?.student_id;
        const userInfo = userStore.getUserInfo || {};
        const studentNo = userInfo.username || '';
        if (studentNo) {
          try {
            const studentInfo = await getStudentInfoSafely(studentNo);
            if (studentInfo) {
              currentStudentInfo.value = studentInfo;
              currentStudentId.value = studentInfo.id;
            }
          } catch (e) {
            console.debug('获取学生信息失败：', e.message);
          }
        }
      }

      // 3. 格式化数据（后端已做筛选排序分页，此处仅做字段兼容）
      const formattedRecords: Recordable[] = rawRecords.map(item => ({
        ...item,
        id: item.id,
        studentId: item.student_id || item.studentId,
        textbookId: item.textbook_id || item.textbookId,
        majorId: item.major_id || item.majorId,
        selectionId: item.selection_id || item.selectionId,
        studentNo: item.studentNo || item.student_no || '未知学号',
        studentName: item.studentName || item.student_name || '未知姓名',
        textbookName: item.textbookName || item.textbook_name || '未知教材',
        isbn: item.isbn || '',
        majorName: item.majorName || item.major_name || '未知专业',
        collegeName: item.collegeName || item.college_name || '未知学院',
        className: item.className || item.class_name || '',
        subscriptionSemester: item.subscriptionSemester || item.subscription_semester || '',
        subscriptionSemester_dictText: (item.subscriptionSemester || item.subscription_semester) === '1' ? '第一学期' : '第二学期',
        subscribeStatus: normalizeSubscribeStatus(item.subscribeStatus || item.subscribe_status),
        subscribeStatus_dictText: getSubscribeStatusText(normalizeSubscribeStatus(item.subscribeStatus || item.subscribe_status)),
        key: item.id || Math.random().toString(36).substr(2, 9)
      }));

      allSubscriptionKeys.value = formattedRecords.map((r: any) => r.id);

      console.log(`【${roleType}端】获取到${formattedRecords.length}条记录（总数: ${total}）`);

      return {
        records: formattedRecords,
        total: total
      };
    } catch (e) {
      console.error('【获取征订数据异常】：', e);
      createMessage.error('获取征订记录失败：' + (e.message || '网络异常'));
      return {records: [], total: 0};
    }
  };


// 注册表格（ 修改3：学生端隐藏操作列）
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  tableProps:{
    title: '征订表',
    api: fetchTableData,
    columns,
    canResize:true,
    rowKey: 'id',
    formConfig: {
      schemas: unref(isStudent) ? searchFormSchema.filter(s => studentSchemaFields.includes(s.field)) : searchFormSchema,
      autoSubmitOnEnter:true,
      showAdvancedButton: unref(isAdmin) || unref(isCounselor),
      fieldMapToNumber: [],
      fieldMapToTime: [],
    },
    useSearchForm: true, // 所有角色均可使用搜索框
    //  学生端不渲染操作列
    actionColumn: unref(isAdmin) || unref(isCounselor) ? {
      width: 120,
      fixed:'right'
    } : undefined,
    beforeFetch: (params) => {
      if (params && fieldPickers) {
        for (let key in fieldPickers) {
          if (params[key]) {
            params[key] = getDateByPicker(params[key], fieldPickers[key]);
          }
        }
      }
      // 合并参数：当前表单值优先，queryParam 兜底
      const mergedParams = Object.assign({}, queryParam, params);
      // 过滤掉空字符串和 undefined 的参数
      Object.keys(mergedParams).forEach(key => {
        if (mergedParams[key] === '' || mergedParams[key] === undefined) {
          delete mergedParams[key];
        }
      });
      // 清理 queryParam 中已被用户清空的字段，然后同步当前值
      Object.keys(queryParam).forEach(key => {
        if (!(key in mergedParams)) delete queryParam[key];
      });
      Object.keys(mergedParams).forEach(key => {
        queryParam[key] = mergedParams[key];
      });
      console.log('【beforeFetch】合并后的参数:', mergedParams);
      return mergedParams;
    },
  },
  exportConfig: {
    name:"征订表",
    url: getExportUrl,
    params: queryParam,
    show: unref(isAdmin) || unref(isCounselor)
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
    show: false //隐藏导入 unref(isAdmin) || unref(isCounselor)
  },
});

// 解构tableContext
const [registerTable, {reload}, { rowSelection, selectedRowKeys }] = tableContext;

rowSelection.onSelectAll = (selected) => {
  if (selected) {
    selectedRowKeys.value = [...allSubscriptionKeys.value];
  } else {
    selectedRowKeys.value = [];
  }
};

// 学生端隐藏复选框
const tableRowSelection = computed(() => {
  if (unref(isStudent)) {
    return undefined;
  }
  return rowSelection;
});
const superQueryConfig = reactive(superQuerySchema);

// ==========  修改4：批量修改征订状态（支持所有角色） ==========
const handleBatchUpdateSubscribeStatus = async () => {
  if (subscribing.value) return;
  subscribing.value = true;
  try {
    let subscriptionIds: string[] = [];

    // 获取当前表格的筛选参数
    const tableInstance = tableContext[2];
    const currentParams = tableInstance?.params || queryParam;

    // 1. 获取筛选后的数据
    const tableData = await fetchTableData(currentParams);

    // 2. 分角色处理数据筛选
    if (unref(isStudent)) {
      // 学生端：筛选自己的未征订记录
      const validRecords = tableData.records.filter(item => {
        const isUnSubscribed = item.subscribeStatus !== '1' && item.subscribeStatus !== '已征订' && item.subscribeStatus !== '已确认';
        // 直接使用记录中的student_id进行匹配
        const isOwnRecord = true; // 学生端只显示自己的记录，所以直接返回true
        return isUnSubscribed && isOwnRecord;
      });
      subscriptionIds = validRecords.map(item => item.id).filter(Boolean);
    } else {
      // 管理员/辅导员端：筛选选中的未征订记录
      if (selectedRowKeys.value.length === 0) {
        createMessage.info("请先选中要修改的记录！");
        return;
      }
      const validRecords = tableData.records.filter(item => {
        const isUnSubscribed = item.subscribeStatus !== '1' && item.subscribeStatus !== '已征订' && item.subscribeStatus !== '已确认';
        const isSelected = selectedRowKeys.value.includes(item.id);
        return isUnSubscribed && isSelected;
      });
      subscriptionIds = validRecords.map(item => item.id).filter(Boolean);
    }

    // 3. 无数据判断
    if (subscriptionIds.length === 0) {
      const tipMsg = unref(isStudent)
        ? "暂无属于你的未征订记录需要修改！"
        : "暂无选中的未征订记录需要修改！";
      createMessage.info(tipMsg);
      return;
    }

    // 4. 执行批量操作
    // 对于学生端，使用第一条记录的student_id作为studentId参数
    let studentIdForRequest = unref(isStudent)
      ? tableData.records[0]?.student_id || currentStudentId.value
      : userStore.getUserInfo.id;

    const res = await batchUpdateSubscribeStatus({
      ids: subscriptionIds,
      subscribeStatus: '1',
      studentId: studentIdForRequest
    });
    createMessage.success(res.msg || `成功修改${subscriptionIds.length}条记录的征订状态！`);

    // 5. 刷新表格
    reload();
  } catch (e: any) {
    console.error("【批量修改失败】：", e);
    const errorMsg = e.msg || e.message || "";
    if (errorMsg.includes("无权限")) {
      createMessage.error(unref(isStudent) ? "你只能修改自己的征订记录！" : "你没有权限修改该记录！");
    } else if (errorMsg.includes("无数据被更新")) {
      createMessage.info("暂无未征订的记录需要修改！");
    } else {
      createMessage.error(errorMsg || '征订状态修改失败，请重试！');
    }
  } finally {
    subscribing.value = false;
  }
};

// 其余工具函数（保留原有逻辑）
function handleSuperQuery(params) {
  Object.keys(params).map((k) => {
    queryParam[k] = params[k];
  });
  reload();
}
function handleAdd() {
  openModal(true, {isUpdate: false, showFooter: true,});
}
function handleEdit(record: Recordable) {
  openModal(true, {record, isUpdate: true, showFooter: true,});
}
function handleDetail(record: Recordable) {
  openModal(true, {record, isUpdate: true, showFooter: false,});
}
async function handleDelete(record) {
  await deleteOne({id: record.id}, handleSuccess);
}
async function batchHandleDelete() {
  await batchDelete({ids: selectedRowKeys.value.join(',')}, handleSuccess);
}
function handleSuccess() {
  selectedRowKeys.value = [];
  reload();
}
function getTableAction(record) {
  if (unref(isAdmin) || unref(isCounselor)) {
    return [{label: '编辑',onClick: handleEdit.bind(null, record),auth: 'zbu:t_subscription:edit'}];
  } else {
    return [{label: '编辑',onClick: handleEdit.bind(null, record),type: 'primary'}];
  }
}
function getDropDownAction(record) {
  if (unref(isAdmin) || unref(isCounselor)) {
    return [
      {label: '详情',onClick: handleDetail.bind(null, record),},
      {label: '删除',popConfirm: {title: '是否确认删除',confirm: handleDelete.bind(null, record),placement: 'topLeft',},auth: 'zbu:t_subscription:delete'}
    ];
  } else {
    return [{label: '详情',onClick: handleDetail.bind(null, record),}];
  }
}

// ========== onMounted ==========
onMounted(async () => {
  // 等待用户信息加载完成（最多1秒）
  let waitCount = 0;
  while (!userStore.getUserInfo?.roleCode && waitCount < 10) {
    await new Promise(resolve => setTimeout(resolve, 100));
    waitCount++;
  }

  console.log('【用户角色】', unref(userRoleType));

  // 刷新表格（fetchTableData会自动带上当前学年参数）
  reload();
});
</script>

<style lang="less" scoped>
// 核心：统一所有搜索组件的宽度和高度
:deep(.ant-input), :deep(.ant-picker), :deep(.ant-select-selector), :deep(.ant-input-number) {
  width: 200px; // 统一宽度（可根据需要调整）
  height: 20px; // 统一高度
  padding: 0 11px; // 统一内边距
  box-sizing: border-box;
}


:deep(.ant-select) {
  width: 200px !important;
  min-width: 200px !important;
  height: 20px;
  flex: none !important;     /* 核心：禁止弹性收缩 */
  display: inline-flex;
  align-items: center;
}

// 禁止下拉框文字换行，保持样式稳定
:deep(.ant-select-selection-item) {
  white-space: nowrap !important;
}

// 按钮样式
:deep(.ant-table-actions .ant-btn-primary) {padding: 0 8px;margin-right: 4px;}
.batch-btn-container {display: flex;justify-content: flex-start;width: 100%;padding-left: 8px;margin-right: 12px;}
:deep(.ant-table-title) {display: flex;justify-content: flex-start;align-items: center;padding: 16px 24px !important;}

// 搜索框容器：开启自动换行 + 调整间距
:deep(.ant-form-item-control-input-content) {
  display: flex;
  gap: 8px; // 组件之间的间距（比原来的8px更宽松）
  flex-wrap: wrap; // 关键：超出容器宽度时自动换行
  align-items: center; // 垂直居中对齐
}

// 可选：让标签固定宽度，对齐更整齐
:deep(.ant-form-item-label) {
  width: 80px; // 标签固定宽度，避免长短不一导致错位
  text-align: right; // 标签右对齐
}

// 可选：调整整个搜索区域的上下内边距
:deep(.ant-form) {
  padding: 8px 0;
}

// 学生端隐藏整个搜索区域
.hide-search-form :deep(.ant-table-form-container) {
  display: none !important;
}
</style>
