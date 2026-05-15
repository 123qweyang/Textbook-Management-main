<template>
  <div>
    <!--引用表格-->
    <BasicTable @register="registerTable" :rowSelection="rowSelection">
      <!--插槽:table标题-->
      <template #tableTitle>
        <template v-if="isAdmin">
          <a-button type="primary" preIcon="ant-design:plus-outlined" @click="handleCreate"> 新增</a-button>
        </template>
        <a-button type="primary" preIcon="ant-design:export-outlined" @click="onExportXls" > 导出</a-button>
        <!--        <j-upload-button type="primary" preIcon="ant-design:import-outlined" @click="onImportXls" v-auth="'system:user:import'">导入</j-upload-button>-->
<!--        <import-excel-progress :upload-url="getImportUrl" @success="reload"></import-excel-progress>-->
        <template v-if="isAdmin">
          <a-button type="primary" @click="openModal(true, {})" preIcon="ant-design:hdd-outlined"> 回收站</a-button>
        </template>

        <a-dropdown v-if="selectedRowKeys.length > 0">
          <template #overlay>
            <a-menu>
              <a-menu-item key="1" @click="batchHandleDelete">
                <Icon icon="ant-design:delete-outlined"></Icon>
                删除
              </a-menu-item>
              <a-menu-item key="2" @click="batchFrozen(2)">
                <Icon icon="ant-design:lock-outlined"></Icon>
                冻结
              </a-menu-item>
              <a-menu-item key="3" @click="batchFrozen(1)">
                <Icon icon="ant-design:unlock-outlined"></Icon>
                解冻
              </a-menu-item>
              <a-menu-item v-if="hasPermission('system:user:resetPassword')" key="4" @click="batchResetPassword()">
                <Icon icon="ant-design:reload-outlined"></Icon>
                重置密码
              </a-menu-item>
            </a-menu>
          </template>
          <a-button
          >批量操作
            <Icon icon="mdi:chevron-down"></Icon>
          </a-button>
        </a-dropdown>
      </template>
      <!--操作栏-->
      <template #action="{ record }">
        <TableAction :actions="getTableAction(record)" :dropDownActions="getDropDownAction(record)" />
      </template>
    </BasicTable>
    <!--用户抽屉-->
    <UserDrawer @register="registerDrawer" @success="handleSuccess" />
    <!--修改密码-->
    <PasswordModal @register="registerPasswordModal" @success="reload" />
    <!--回收站-->
    <UserRecycleBinModal @register="registerModal" @success="reload" />
    <!-- 离职人员列弹窗 -->
    <UserQuitModal @register="registerQuitModal" @success="reload" />
  </div>
</template>

<script lang="ts" name="system-user" setup>
//ts语法
import { ref, computed, unref, onMounted, watch } from 'vue';
import { defHttp } from '/@/utils/http/axios';
import { BasicTable, TableAction, ActionItem } from '/@/components/Table';
import UserDrawer from './UserDrawer.vue';
import UserRecycleBinModal from './UserRecycleBinModal.vue';
import PasswordModal from './PasswordModal.vue';
import JThirdAppButton from '/@/components/jeecg/thirdApp/JThirdAppButton.vue';
import UserQuitModal from './UserQuitModal.vue';
import { useDrawer } from '/@/components/Drawer';
import { useListPage } from '/@/hooks/system/useListPage';
import { useModal } from '/@/components/Modal';
import { useMessage } from '/@/hooks/web/useMessage';
import { columns, searchFormSchema } from './user.data';
import { listNoCareTenant, deleteUser, batchDeleteUser, getImportUrl, getExportUrl, frozenBatch, resetPassword } from './user.api';
import { usePermission } from '/@/hooks/web/usePermission';
import ImportExcelProgress from './components/ImportExcelProgress.vue';
import { useUserStore } from '/@/store/modules/user';

const { createMessage, createConfirm } = useMessage();
const { isDisabledAuth, hasPermission } = usePermission();



// ========== 角色判断逻辑（参考征订表）==========
const userStore = useUserStore();
const userRoleType = computed(() => {
  const userInfo = userStore.getUserInfo || {};
  console.log('userInfo:', userInfo);
  console.log('userInfo.roleCode:', userInfo.roleCode);
  console.log('userInfo.username:', userInfo.username);

  // 双重兜底 + 转小写去空格，兼容后端返回的各种格式
  const roleCode = (userInfo.roleCode || "").toLowerCase().trim();
  const username = (userInfo.username || "").toLowerCase().trim();

  console.log('roleCode:', roleCode);
  console.log('username:', username);

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

//注册drawer
const [registerDrawer, { openDrawer }] = useDrawer();
//回收站model
const [registerModal, { openModal }] = useModal();
//密码model
const [registerPasswordModal, { openModal: openPasswordModal }] = useModal();
//代理人model
const [registerAgentModal, { openModal: openAgentModal }] = useModal();
//离职代理人model
const [registerQuitAgentModal, { openModal: openQuitAgentModal }] = useModal();
//离职用户列表model
const [registerQuitModal, { openModal: openQuitModal }] = useModal();

// 存储辅导员管理的学生用户ID列表
const counselorStudentUserIds = ref<string[]>([]);
// 标志位：学生列表是否已加载
const counselorStudentsLoaded = ref(false);

// 获取辅导员管理的学生用户ID列表
const fetchCounselorStudents = async () => {
  console.log('fetchCounselorStudents called, isCounselor:', unref(isCounselor));
  if (!unref(isCounselor)) return;
  try {
    // 调用后端接口获取辅导员管理的学生用户ID列表
    const res = await defHttp.get({ url: '/sys/user/getCounselorStudents' });
    console.log('getCounselorStudents response:', res);
    console.log('getCounselorStudents response is array:', Array.isArray(res));

    // 处理后端返回的结果，兼容defHttp.get的处理
    let studentIds = [];
    if (Array.isArray(res)) {
      // 如果直接返回的是数组，说明defHttp已经处理了Result对象
      studentIds = res;
    } else if (res && res.success && Array.isArray(res.result)) {
      // 如果返回的是Result对象
      studentIds = res.result;
    }

    console.log('studentIds:', studentIds);
    console.log('studentIds.length:', studentIds.length);

    // 设置响应式变量值
    counselorStudentUserIds.value = studentIds;
    counselorStudentsLoaded.value = true;

    console.log('counselorStudentUserIds:', counselorStudentUserIds);
    console.log('counselorStudentUserIds.length:', counselorStudentUserIds.length);

    // 获取到学生列表后，重新加载表格数据
    // reload();
  } catch (error) {
    console.error('获取辅导员管理的学生列表失败:', error);
  }
};


// 包装API函数，添加过滤逻辑
const wrappedApi = async (params) => {
  console.log('wrappedApi called, isCounselor:', unref(isCounselor));

  // 如果是辅导员，需要获取所有数据后再过滤
  if (unref(isCounselor)) {
    // 确保学生列表已加载
    if (counselorStudentUserIds.value.length === 0) {
      console.log('Student list not loaded, fetching...');
      await fetchCounselorStudents();
      console.log('After fetchCounselorStudents, studentUserIds:', counselorStudentUserIds.value);
      console.log('counselorStudentUserIds.length:', counselorStudentUserIds.value.length);
    }

    console.log('filtering data with studentUserIds:', counselorStudentUserIds.value);
    console.log('counselorStudentUserIds.length:', counselorStudentUserIds.value.length);

    // 获取所有数据（不分页）
    const allRes = await listNoCareTenant({ pageNo: 1, pageSize: 1000 });
    console.log('all data response:', allRes);

    if (counselorStudentUserIds.value.length > 0) {
      const allFilteredRecords = allRes.records.filter((item) => counselorStudentUserIds.value.includes(item.id));
      console.log('all filtered records length:', allFilteredRecords.length);

      // 手动进行分页
      const pageNo = params.pageNo || 1;
      const pageSize = params.pageSize || 10;
      const start = (pageNo - 1) * pageSize;
      const end = start + pageSize;
      const paginatedRecords = allFilteredRecords.slice(start, end);

      console.log('paginated records length:', paginatedRecords.length);
      return {
        ...allRes,
        records: paginatedRecords,
        total: allFilteredRecords.length // 更新total为过滤后的总数量
      };
    }
    // 如果没有学生，返回空数据
    return {
      ...allRes,
      records: [],
      total: 0 // 更新total为0
    };
  }

  // 其他情况使用原始分页数据
  const res = await listNoCareTenant(params);
  console.log('original API response:', res);
  return res;
};


// 监听isCounselor变化，当确定为辅导员时获取学生列表
watch(isCounselor, (newVal) => {
  console.log('isCounselor changed:', newVal);
  if (newVal) {
    fetchCounselorStudents();
  }
}, { immediate: true });

// 在组件挂载时也尝试获取
onMounted(() => {
  console.log('onMounted, isCounselor:', unref(isCounselor));
  if (unref(isCounselor)) {
    fetchCounselorStudents();
  }
});

// 列表页面公共参数、方法
const { prefixCls, tableContext, onExportXls, onImportXls } = useListPage({
  designScope: 'user-list',
  tableProps: {
    title: '用户列表',
    api: wrappedApi,
    columns: columns,
    size: 'small',
    formConfig: {
      // labelWidth: 200,
      schemas: searchFormSchema,
    },
    actionColumn: {
      width: 120,
    },
    beforeFetch: (params) => {
      return Object.assign({ column: 'createTime', order: 'desc' }, params);
    },
    afterFetch: (data) => {
      console.log('afterFetch called, isCounselor:', unref(isCounselor), 'data length:', data.length);
      // 辅导员的过滤逻辑已经在wrappedApi中实现，这里不需要重复过滤
      return data;
    },
    defSort: {
      column: "",
      order: ""
    },
  },
  exportConfig: {
    name: '用户列表',
    url: getExportUrl,
  },
  importConfig: {
    url: getImportUrl,
  },
});

//注册table数据
const [registerTable, { reload, updateTableDataRecord, clearSelectedRowKeys }, { rowSelection, selectedRows, selectedRowKeys }] = tableContext;

/**
 * 新增事件
 */
function handleCreate() {
  openDrawer(true, {
    isUpdate: false,
    showFooter: true,
    tenantSaas: false,
  });
}
/**
 * 编辑事件
 */
async function handleEdit(record: Recordable) {
  openDrawer(true, {
    record,
    isUpdate: true,
    showFooter: true,
    tenantSaas: false,
  });
}
/**
 * 详情
 */
async function handleDetail(record: Recordable) {
  openDrawer(true, {
    record,
    isUpdate: true,
    showFooter: false,
    tenantSaas: false,
  });
}
/**
 * 删除事件
 */
async function handleDelete(record) {
  if ('admin' == record.username) {
    createMessage.warning('管理员账号不允许此操作！');
    return;
  }
  await deleteUser({ id: record.id }, reload);
}
/**
 * 批量删除事件
 */
async function batchHandleDelete() {
  let hasAdmin = unref(selectedRows).filter((item) => item.username == 'admin');
  if (unref(hasAdmin).length > 0) {
    createMessage.warning('管理员账号不允许此操作！');
    return;
  }
  await batchDeleteUser({ ids: selectedRowKeys.value }, () => {
    selectedRowKeys.value = [];
    reload();
  });
}
/**
 * 成功回调
 */
function handleSuccess() {
  reload();
}

/**
 * 打开修改密码弹窗
 */
function handleChangePassword(username) {
  openPasswordModal(true, { username });
}
/**
 * 冻结解冻
 */
async function handleFrozen(record, status) {
  if ('admin' == record.username) {
    createMessage.warning('管理员账号不允许此操作！');
    return;
  }
  await frozenBatch({ ids: record.id, status: status }, reload);
}
/**
 * 批量冻结解冻
 */
function batchFrozen(status) {
  let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
  if (unref(hasAdmin).length > 0) {
    createMessage.warning('管理员账号不允许此操作！');
    return;
  }
  createConfirm({
    iconType: 'warning',
    title: '确认操作',
    content: '是否' + (status == 1 ? '解冻' : '冻结') + '选中账号?',
    onOk: async () => {
      await frozenBatch({ ids: unref(selectedRowKeys).join(','), status: status }, reload);
    },
  });
}
/**
 * 批量重置密码
 */
function batchResetPassword() {
  let hasAdmin = selectedRows.value.filter((item) => item.username == 'admin');
  if (unref(hasAdmin).length > 0) {
    createMessage.warning('所选用户中包含管理员，管理员账号不允许重置密码！！');
    return;
  }
  if (selectedRows.value.length > 0) {
    createConfirm({
      iconType: 'warning',
      title: '确认操作',
      content: '是否重置选中的账号密码?',
      onOk: async () => {
        const usernames = selectedRows.value.map((item) => item.username).join(',');
        await resetPassword({ usernames: usernames }, ()=>{reload();clearSelectedRowKeys();});
      },
    });
  }
}


/**
 *同步钉钉和微信回调
 */
function onSyncFinally({ isToLocal }) {
  // 同步到本地时刷新下数据
  if (isToLocal) {
    reload();
  }
}

/**
 * 操作栏
 */
function getTableAction(record): ActionItem[] {
  const actions = [];
  // 只有管理员可以看到编辑按钮
  if (isAdmin) {
    actions.push({
      label: '编辑',
      onClick: handleEdit.bind(null, record),
      auth: 'zbu:t_counselor:edit'
    });
  }
  return actions;
}
/**
 * 下拉操作栏
 */
function getDropDownAction(record): ActionItem[] {
  return [
    {
      label: '详情',
      onClick: handleDetail.bind(null, record),
    },
    {
      label: '密码',
      //auth: 'user:changepwd',
      onClick: handleChangePassword.bind(null, record.username),
    },
    {
      label: '删除',
      popConfirm: {
        title: '是否确认删除',
        confirm: handleDelete.bind(null, record),
      },
    },
    {
      label: '冻结',
      ifShow: record.status == 1,
      popConfirm: {
        title: '确定冻结吗?',
        confirm: handleFrozen.bind(null, record, 2),
      },
    },
    {
      label: '解冻',
      ifShow: record.status == 2,
      popConfirm: {
        title: '确定解冻吗?',
        confirm: handleFrozen.bind(null, record, 1),
      },
    },
  ];
}

</script>

<style scoped></style>
