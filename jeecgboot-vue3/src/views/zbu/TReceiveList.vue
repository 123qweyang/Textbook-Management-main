﻿<template>
  <div>
    <BasicTable
      @register="registerTable"
      :rowSelection="rowSelection"
      :form-config="tableFormConfig"
      :class="{ 'hide-search-form': !isAdmin && !isCounselor }"
    >
      <template #tableTitle>
        <!-- 所有角色都显示批量修改按钮（区分文字） -->
        <div class="batch-btn-container">
          <a-button
            type="primary"
            size="small"
            @click="handleBatchUpdateReceiveStatus"
            v-if="!isAdmin && !isCounselor"
          >
            同意领取
          </a-button>
          <a-button
            type="primary"
            size="small"
            @click="handleBatchUpdateReceiveStatus"
            v-if="(isAdmin || isCounselor) && selectedRowKeys.length > 0"
          >
            批量标记为已领取
          </a-button>
          <a-button
            type="primary"
            size="small"
            @click="handleBatchUpdateReceiveStatusUn"
            v-if="(isAdmin || isCounselor) && selectedRowKeys.length > 0"
            style="margin-left: 8px"
          >
            批量标记为未领取
          </a-button>
        </div>

        <!-- 管理员/辅导员端：保留原有新增/导出/导入/批量操作 -->
        <template v-if="isAdmin || isCounselor">
          <template v-if="isAdmin">
            <a-button type="primary" v-auth="'zbu:t_receive:add'" @click="handleAdd" preIcon="ant-design:plus-outlined"> 新增</a-button>
          </template>


          <a-button  type="primary" v-auth="'zbu:t_receive:exportXls'" preIcon="ant-design:export-outlined" @click="onExportXls"> 导出</a-button>
          <!--          <j-upload-button type="primary" v-auth="'zbu:t_receive:importExcel'" preIcon="ant-design:import-outlined" @click="onImportXls">导入</j-upload-button>-->
          <a-dropdown v-if="selectedRowKeys.length > 0">
            <template #overlay>
              <a-menu>
                <a-menu-item key="1" @click="batchHandleDelete">
                  <Icon icon="ant-design:delete-outlined"></Icon>
                  删除
                </a-menu-item>
              </a-menu>
            </template>
            <a-button v-auth="'zbu:t_receive:deleteBatch'">批量操作
              <Icon icon="mdi:chevron-down"></Icon>
            </a-button>
          </a-dropdown>
        </template>
        <!--        <super-query v-if="isAdmin || isCounselor" :config="superQueryConfig" @search="handleSuperQuery" />-->
      </template>

      <!-- 学生端不渲染操作列的模板 -->
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
        <template v-if="column.dataIndex === 'receiveStatus'">
          {{ record.receiveStatus === '1' ? '已领取' : '未领取' }}
        </template>
      </template>
    </BasicTable>

    <TReceiveModal
      @register="registerModal"
      @success="handleSuccess"
    ></TReceiveModal>
  </div>
</template>

<script lang="ts" name="zbu-tReceive" setup>
import {ref, reactive, computed, unref, onMounted} from 'vue';
import {BasicTable, useTable, TableAction} from '/@/components/Table';
import {useModal} from '/@/components/Modal';
import { useListPage } from '/@/hooks/system/useListPage'
import { defHttp } from '/@/utils/http/axios';
import TReceiveModal from './components/TReceiveModal.vue'
import {columns, searchFormSchema, superQuerySchema} from './TReceive.data';
import {
  deleteOne, batchDelete, getImportUrl, getExportUrl, getMyReceive,
  getStudentById, getSubscriptionById, getTextbookById, getMajorById, getCollegeById, getStudentByNo,
  batchUpdateReceiveStatus
} from './TReceive.api';
import { useUserStore } from '/@/store/modules/user';
//  关键修改：只保留JeecgBoot封装的useMessage，放弃所有antd原生导入
import { useMessage } from '/@/hooks/web/useMessage';
import { getDateByPicker } from '/@/utils';

type Recordable = { [key: string]: any };
const fieldPickers = reactive({});
const queryParam = reactive<any>({});
//  恢复JeecgBoot封装的createMessage
const { createMessage } = useMessage();

const [registerModal, {openModal}] = useModal();
const currentStudentId = ref("");
const currentStudentInfo = ref<Recordable>({});

// ========== 核心：角色判断逻辑（不变） ==========
const userStore = useUserStore();
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

// ========== 表单配置（不变） ==========
const tableFormConfig = computed(() => ({
  schemas: unref(isAdmin) || unref(isCounselor) ? searchFormSchema : [],
  show: unref(isAdmin) || unref(isCounselor),
  showAdvancedButton: unref(isAdmin) || unref(isCounselor),
  showSearchButton: unref(isAdmin) || unref(isCounselor),
  showResetButton: unref(isAdmin) || unref(isCounselor),
  autoSubmitOnEnter: true,
  submitButtonProps: { label: '查询' },
  resetButtonProps: { label: '重置' }
}));

// ========== 安全获取学生信息（不变） ==========
const getStudentInfoSafely = async (studentNo: string) => {
  try {
    if (!studentNo) return null;
    const res = await getStudentByNo(studentNo);
    return res?.success ? res.result : null;
  } catch (e) {
    console.debug("【非关键错误】按学号查询学生失败：", e.message);
    return null;
  }
};

// ========== 重构fetchTableData，使用视图数据 ==========
const fetchTableData = async (params = {}) => {
  try {
    const roleType = unref(userRoleType);
    console.log(`【领取表-${roleType}端】开始获取数据，参数：`, params);

    const res = await getMyReceive(params);
    const rawRecords = res?.success ? res.result : (Array.isArray(res) ? res : []);

    if (rawRecords.length === 0) {
      return {records: [], total: 0};
    }

    if (unref(isStudent)) {
      const userInfo = userStore.getUserInfo || {};
      const studentNo = userInfo.username || "";
      if (studentNo) {
        const studentInfo = await getStudentInfoSafely(studentNo);
        if (studentInfo) {
          currentStudentInfo.value = studentInfo;
          currentStudentId.value = studentInfo.id || studentInfo.studentId || studentNo;
        } else {
          currentStudentId.value = rawRecords[0]?.receiveOperator || studentNo;
        }
      }
    }

    // 直接使用视图返回的数据，移除串行异步请求
    const formattedRecords: Recordable[] = rawRecords.map(item => ({
      ...item,
      id: item.id,
      subscriptionId: item.subscription_id || item.subscriptionId,
      studentId: item.student_id || item.studentId,
      textbookId: item.textbook_id || item.textbookId,
      receiveOperator: item.receive_operator || item.receiveOperator,
      studentNo: item.studentNo || item.student_no || '未知学号',
      studentName: item.studentName || item.student_name || '未知姓名',
      textbookName: item.textbookName || item.textbook_name || '未知教材',
      isbn: item.isbn || '',
      collegeName: item.collegeName || item.college_name || '未知学院',
      majorName: item.majorName || item.major_name || '未知专业',
      receiveStatus: item.receiveStatus || item.receive_status || '0',
      receiveStatus_dictText: (item.receiveStatus || item.receive_status) === '1' ? '已领取' : '未领取',
      key: item.id || Math.random().toString(36).substr(2, 9)
    }));

    let filteredRecords = [...formattedRecords];
    console.log('【领取表筛选】参数：', params);
    if ((unref(isAdmin) || unref(isCounselor)) && Object.keys(params).length > 0) {
      if (params.studentNo) {
        const searchKey = params.studentNo.trim().toLowerCase();
        filteredRecords = filteredRecords.filter(item =>
          item.studentNo.toLowerCase().includes(searchKey)
        );
      }
      if (params.studentName) {
        const searchKey = params.studentName.trim().toLowerCase();
        filteredRecords = filteredRecords.filter(item =>
          item.studentName.toLowerCase().includes(searchKey)
        );
      }
      if (params.textbookName) {
        const searchKey = params.textbookName.trim().toLowerCase();
        filteredRecords = filteredRecords.filter(item =>
          item.textbookName.toLowerCase().includes(searchKey)
        );
      }
      if (params.receiveStatus) {
        console.log('【领取表筛选】receiveStatus参数：', params.receiveStatus, typeof params.receiveStatus);

        // 处理不同类型的 receiveStatus 参数
        let statusList = [];
        if (Array.isArray(params.receiveStatus)) {
          statusList = params.receiveStatus;
        } else if (typeof params.receiveStatus === 'object' && params.receiveStatus !== null) {
          // 如果是对象，尝试获取其值
          if (params.receiveStatus.value !== undefined) {
            statusList = [params.receiveStatus.value];
          } else if (params.receiveStatus.label !== undefined) {
            statusList = [params.receiveStatus.label];
          } else {
            statusList = [params.receiveStatus];
          }
        } else {
          statusList = [params.receiveStatus];
        }

        console.log('【领取表筛选】statusList：', statusList);

        // 调试：查看过滤前的记录
        console.log('【领取表筛选】过滤前记录数：', filteredRecords.length);
        console.log('【领取表筛选】过滤前记录状态：', filteredRecords.map(item => ({id: item.id, receiveStatus: item.receiveStatus, receiveStatus_dictText: item.receiveStatus_dictText})));

        filteredRecords = filteredRecords.filter(item => {
          // 处理不同的状态值格式
          const itemStatus = item.receiveStatus || '0';
          console.log('【领取表筛选】当前记录状态：', item.id, itemStatus, item.receiveStatus_dictText);

          const match = statusList.some(status => {
            // 处理状态值
            let statusValue = status;
            if (typeof status === 'object' && status !== null) {
              if (status.value !== undefined) {
                statusValue = status.value;
              } else if (status.label !== undefined) {
                statusValue = status.label;
              }
            }

            console.log('【领取表筛选】比较状态：', statusValue, typeof statusValue, 'vs', itemStatus);

            // 处理后端返回的文本状态值
            if (itemStatus === '未领取' || itemStatus === '未领') {
              // 如果记录状态是未领取，检查参数是否匹配
              return statusValue === '0' || statusValue === '未领' || statusValue === '未领取';
            } else if (itemStatus === '已领取' || itemStatus === '已领') {
              // 如果记录状态是已领取，检查参数是否匹配
              return statusValue === '1' || statusValue === '已领' || statusValue === '已领取';
            } else {
              // 其他情况，直接比较
              return statusValue === itemStatus;
            }
          });

          console.log('【领取表筛选】记录匹配结果：', item.id, match);
          return match;
        });

        // 调试：查看过滤后的记录
        console.log('【领取表筛选】过滤后记录数：', filteredRecords.length);
        console.log('【领取表筛选】过滤后记录状态：', filteredRecords.map(item => ({id: item.id, receiveStatus: item.receiveStatus, receiveStatus_dictText: item.receiveStatus_dictText})));
      }
      // 学院筛选（通过学院查找专业，再查找学生ID来过滤）
      if (params.collegeName || params.majorName || params.className) {
        // 收集所有需要匹配的学生ID
        let targetStudentIds = null;

        // 优先按班级筛选
        if (params.className) {
          try {
            const classRes = await defHttp.get({ url: '/zbu/tClass/list', params: { pageSize: 999, pageNo: 1, className: params.className } });
            const classRecords = classRes.records || [];
            const classIds = classRecords.map((c: any) => c.id);
            if (classIds.length > 0) {
              const stuRes = await defHttp.get({ url: '/zbu/tStudent/list', params: { pageSize: 9999, pageNo: 1 } });
              const stuRecords = stuRes.records || [];
              targetStudentIds = stuRecords
                .filter((s: any) => classIds.includes(s.classId))
                .map((s: any) => s.id);
            } else {
              targetStudentIds = [];
            }
          } catch (e) {
            console.error('班级筛选失败：', e);
          }
        }
        // 其次按专业筛选
        else if (params.majorName) {
          try {
            const majorRes = await defHttp.get({ url: '/zbu/tMajor/list', params: { pageSize: 999, pageNo: 1, majorName: params.majorName } });
            const majorRecords = majorRes.records || [];
            const majorIds = majorRecords.map((m: any) => m.id);
            if (majorIds.length > 0) {
              const stuRes = await defHttp.get({ url: '/zbu/tStudent/list', params: { pageSize: 9999, pageNo: 1 } });
              const stuRecords = stuRes.records || [];
              targetStudentIds = stuRecords
                .filter((s: any) => majorIds.includes(s.majorId))
                .map((s: any) => s.id);
            } else {
              targetStudentIds = [];
            }
          } catch (e) {
            console.error('专业筛选失败：', e);
          }
        }
        // 只按学院筛选
        else if (params.collegeName) {
          try {
            const collegeRes = await defHttp.get({ url: '/zbu/tCollege/list', params: { pageSize: 999, pageNo: 1, collegeName: params.collegeName } });
            const collegeRecords = collegeRes.records || [];
            const collegeIds = collegeRecords.map((c: any) => c.id);
            if (collegeIds.length > 0) {
              const majorRes = await defHttp.get({ url: '/zbu/tMajor/list', params: { pageSize: 9999, pageNo: 1 } });
              const majorRecords = majorRes.records || [];
              const majorIdsInCollege = majorRecords
                .filter((m: any) => collegeIds.includes(m.collegeId))
                .map((m: any) => m.id);
              if (majorIdsInCollege.length > 0) {
                const stuRes = await defHttp.get({ url: '/zbu/tStudent/list', params: { pageSize: 9999, pageNo: 1 } });
                const stuRecords = stuRes.records || [];
                targetStudentIds = stuRecords
                  .filter((s: any) => majorIdsInCollege.includes(s.majorId))
                  .map((s: any) => s.id);
              } else {
                targetStudentIds = [];
              }
            } else {
              targetStudentIds = [];
            }
          } catch (e) {
            console.error('学院筛选失败：', e);
          }
        }

        // 应用学生ID过滤
        if (targetStudentIds !== null) {
          if (targetStudentIds.length === 0) {
            filteredRecords = [];
          } else {
            filteredRecords = filteredRecords.filter(item =>
              targetStudentIds.includes(item.receiveOperator || item.studentId)
            );
          }
        }
      }
    }

    // 处理排序
    if (params.column && params.order) {
      filteredRecords.sort((a, b) => {
        const field = params.column;
        const order = params.order === 'asc' ? 1 : -1;

        // 处理不同类型字段的排序
        if (typeof a[field] === 'string' && typeof b[field] === 'string') {
          return a[field].localeCompare(b[field]) * order;
        } else if (typeof a[field] === 'number' && typeof b[field] === 'number') {
          return (a[field] - b[field]) * order;
        } else if (a[field] instanceof Date && b[field] instanceof Date) {
          return (a[field].getTime() - b[field].getTime()) * order;
        } else if (a[field] && b[field]) {
          // 处理其他类型的排序
          return String(a[field]).localeCompare(String(b[field])) * order;
        }
        return 0;
      });
    }

    return {
      records: filteredRecords,
      total: filteredRecords.length
    };
  } catch (e) {
    console.error('【获取领取数据异常】：', e);
    createMessage.error('获取领取记录失败：' + (e.message || '网络异常'));
    return {records: [], total: 0};
  }
};

// ========== 注册表格（不变） ==========
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  tableProps:{
    title: '领取表',
    api: fetchTableData,
    columns,
    canResize:true,
    rowKey: 'id',
    formConfig: {
      schemas: searchFormSchema,
      autoSubmitOnEnter:true,
      showAdvancedButton: true,
      fieldMapToNumber: [],
      fieldMapToTime: [],
    },
    useSearchForm: unref(isAdmin) || unref(isCounselor),
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
      return Object.assign(params, queryParam);
    },
  },
  exportConfig: {
    name:"领取表",
    url: getExportUrl,
    params: queryParam,
    show: unref(isAdmin) || unref(isCounselor)
  },
  importConfig: {
    url: getImportUrl,
    success: handleSuccess,
    show: false // 隐藏导入
  },
});

const [registerTable, {reload}, { rowSelection, selectedRowKeys }] = tableContext;
const superQueryConfig = reactive(superQuerySchema);

// ========== 批量修改领取状态： 核心修改（去掉所有loading，彻底无报错） ==========
const handleBatchUpdateReceiveStatus = async () => {
  try {
    let receiveIds: string[] = [];
    const tableData = await fetchTableData();

    // 1. 检查是否有选中的记录
    if (selectedRowKeys.value.length === 0) {
      createMessage.info("请先选中要修改的记录！");
      return;
    }

    // 2. 筛选选中的未领取记录
    const validRecords = tableData.records.filter(item => {
      const isUnReceived = item.receiveStatus !== '1' && item.receiveStatus !== '已领取';
      const isSelected = selectedRowKeys.value.includes(item.id);
      return isUnReceived && isSelected;
    });
    receiveIds = validRecords.map(item => item.id).filter(Boolean);

    // 3. 无数据判断
    if (receiveIds.length === 0) {
      createMessage.info("暂无选中的未领取记录需要修改！");
      return;
    }

    //  关键修改：去掉所有loading相关代码，直接调用接口
    const res = await batchUpdateReceiveStatus({
      ids: receiveIds,
      receiveStatus: '1',
      receiveOperator: userStore.getUserInfo.id
    });
    createMessage.success(res.msg || `成功修改${receiveIds.length}条记录的领取状态！`);

    reload();
  } catch (e: any) {
    console.error("【批量修改领取状态失败】：", e);
    const errorMsg = e.msg || e.message || "";
    if (errorMsg.includes("无权限")) {
      createMessage.error("你没有权限修改该记录！");
    } else if (errorMsg.includes("无数据被更新")) {
      createMessage.info("暂无未领取的记录需要修改！");
    } else {
      createMessage.error(errorMsg || '领取状态修改失败，请重试！');
    }
  }
};

const handleBatchUpdateReceiveStatusUn = async () => {
  try {
    let receiveIds: string[] = [];
    const tableData = await fetchTableData();

    if (selectedRowKeys.value.length === 0) {
      createMessage.info("请先选中要修改的记录！");
      return;
    }

    const validRecords = tableData.records.filter(item => {
      const isReceived = item.receiveStatus === '1' || item.receiveStatus === '已领取';
      const isSelected = selectedRowKeys.value.includes(item.id);
      return isReceived && isSelected;
    });
    receiveIds = validRecords.map(item => item.id).filter(Boolean);

    if (receiveIds.length === 0) {
      createMessage.info("暂无选中的已领取记录需要修改！");
      return;
    }

    const res = await batchUpdateReceiveStatus({
      ids: receiveIds,
      receiveStatus: '0',
      receiveOperator: userStore.getUserInfo.id
    });
    createMessage.success(res.msg || `成功修改${receiveIds.length}条记录的领取状态！`);

    reload();
  } catch (e: any) {
    console.error("【批量修改领取状态失败】：", e);
    const errorMsg = e.msg || e.message || "";
    if (errorMsg.includes("无权限")) {
      createMessage.error("你没有权限修改该记录！");
    } else if (errorMsg.includes("无数据被更新")) {
      createMessage.info("暂无已领取的记录需要修改！");
    } else {
      createMessage.error(errorMsg || '领取状态修改失败，请重试！');
    }
  }
};

// ========== 工具函数（不变） ==========
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
  await batchDelete({ids: selectedRowKeys.value}, handleSuccess);
}

function handleSuccess() {
  selectedRowKeys.value = [];
  reload();
}

function getTableAction(record) {
  if (unref(isAdmin) || unref(isCounselor)) {
    return [{label: '编辑',onClick: handleEdit.bind(null, record),auth: 'zbu:t_receive:edit'}];
  } else {
    return [];
  }
}

function getDropDownAction(record) {
  if (unref(isAdmin) || unref(isCounselor)) {
    return [
      {label: '详情',onClick: handleDetail.bind(null, record),},
      {label: '删除',popConfirm: {title: '是否确认删除',confirm: handleDelete.bind(null, record),placement: 'topLeft',},auth: 'zbu:t_receive:delete'}
    ];
  } else {
    return [];
  }
}

// ========== 初始化（不变） ==========
onMounted(async () => {
  let waitCount = 0;
  while (!userStore.getUserInfo?.roleCode && waitCount < 10) {
    await new Promise(resolve => setTimeout(resolve, 100));
    waitCount++;
  }

  console.log('【领取表-完整用户信息】', userStore.getUserInfo);
  console.log('【领取表-用户名】', userStore.getUserInfo?.username);
  console.log('【领取表-角色码原始值】', userStore.getUserInfo?.roleCode);
  console.log('【领取表-最终识别角色类型】', unref(userRoleType));

  reload();
});
</script>

<style lang="less" scoped>
:deep(.ant-picker),:deep(.ant-input-number),:deep(.ant-select-selector),:deep(.ant-input){width: 100%;}
:deep(.ant-table-actions .ant-btn-primary) {padding: 0 8px;margin-right: 4px;}
.batch-btn-container {display: flex;justify-content: flex-start;width: 100%;padding-left: 8px;margin-right: 12px;}
:deep(.ant-table-title) {display: flex;justify-content: flex-start;align-items: center;padding: 16px 24px !important;}
:deep(.ant-form-item-control-input-content) {display: flex;gap: 8px;}

:deep(.ant-select) {
  width: 130px !important; /* 固定下拉框宽度（可自行调整数值） */
  min-width: 130px !important;
  flex: none !important; /* 禁止弹性收缩 */
}
:deep(.ant-select-selector) {
  width: 100% !important;
  min-width: 130px !important;
  white-space: nowrap; /* 禁止文字换行 */
}

.hide-search-form :deep(.ant-table-form-container) {
  display: none !important;
}
</style>
