import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/zbu/tReceive/list',
  save='/zbu/tReceive/add',
  edit='/zbu/tReceive/edit',
  deleteOne = '/zbu/tReceive/delete',
  deleteBatch = '/zbu/tReceive/deleteBatch',
  importExcel = '/zbu/tReceive/importExcel',
  exportXls = '/zbu/tReceive/exportXls',
  getMyReceive = '/zbu/tReceive/getMyReceive',
  getStudentById = '/zbu/tStudent/queryById',
  getSubscriptionById = '/zbu/tSubscription/queryById',
  getTextbookById = '/zbu/tTextbook/queryById',
  getMajorById = '/zbu/tMajor/queryById',
  getCollegeById = '/zbu/tCollege/queryById',
  getStudentByNo= '/zbu/tReceive/getStudentByNo',
  batchUpdateReceiveStatus = '/zbu/tReceive/batchUpdateReceiveStatus',
}

// 导出/导入URL
export const getExportUrl = Api.exportXls;
export const getImportUrl = Api.importExcel;

// 列表接口（保留原有，兼容旧逻辑）
export const list = (params) => defHttp.get({url: Api.list, params});

// 新增：对应征订表的getMySubscription，核心数据接口
export const getMyReceive = (params) => defHttp.get({url: Api.getMyReceive, params});

// 删除单个（保留你原有逻辑，不修改）
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

// 批量删除（保留你原有逻辑，不修改）
export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.delete({url: Api.deleteBatch, data: params}, {joinParamsToUrl: true}).then(() => {
        handleSuccess();
      });
    }
  });
}

// 保存/更新（保留）
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}

// 根据学生ID查询信息（保留）
export const getStudentById = (id) => {
  return defHttp.get({url: Api.getStudentById, params: {id}});
};

// 根据征订ID查询信息（保留）
export const getSubscriptionById = (id) => {
  return defHttp.get({url: Api.getSubscriptionById, params: {id}});
};

// 根据教材ID查询信息（保留）
export const getTextbookById = (id) => {
  return defHttp.get({url: Api.getTextbookById, params: {id}});
}

// 按学号查询学生（保留）
export const getStudentByNo = (studentNo) => {
  return defHttp.get({url: Api.getStudentByNo, params: {studentNo}});
};

// 批量修改领取状态（保留）
export const batchUpdateReceiveStatus = (params) => {
  return defHttp.post({url: Api.batchUpdateReceiveStatus, params});
};

// 根据专业ID查询信息
export const getMajorById = (id) => {
  return defHttp.get({ url: Api.getMajorById, params: { id } });
};

// 根据学院ID查询信息
export const getCollegeById = (id) => {
  return defHttp.get({ url: Api.getCollegeById, params: { id } });
};
