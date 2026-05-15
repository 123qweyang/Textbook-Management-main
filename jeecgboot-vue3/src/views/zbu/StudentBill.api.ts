import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/zbu/studentBill/list',
  save='/zbu/studentBill/add',
  edit='/zbu/studentBill/edit',
  deleteOne = '/zbu/studentBill/delete',
  deleteBatch = '/zbu/studentBill/deleteBatch',
  importExcel = '/zbu/studentBill/importExcel',
  exportXls = '/zbu/studentBill/exportXls',
  syncFromSubscription = '/zbu/studentBill/syncFromSubscription',
  getStudentByNo = '/zbu/tStudent/queryByNo',
  getStudentById = '/zbu/tStudent/queryById', // 对齐领取表：新增学生ID查询接口
  getMajorInfo = '/zbu/tMajor/queryById',     // 对齐领取表：专业查询接口
  getTextbookById = '/zbu/tTextbook/queryById',// 对齐领取表：教材查询接口
  batchUpdateReceiveStatus = '/zbu/studentBill/batchUpdateReceiveStatus',// 批量修改领取状态接口
  getCurrentSchoolYear = '/zbu/studentBill/getCurrentSchoolYear', // 获取当前学年
}

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;
/**
 * 导入api
 */
export const getImportUrl = Api.importExcel;
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * 删除单个
 */
export const deleteOne = (params,handleSuccess) => {
  return defHttp.delete({url: Api.deleteOne, params}, {joinParamsToUrl: true}).then(() => {
    handleSuccess();
  });
}

/**
 * 批量删除
 * @param params
 */
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

/**
 * 保存或者更新
 * @param params
 */
export const saveOrUpdate = (params, isUpdate) => {
  let url = isUpdate ? Api.edit : Api.save;
  return defHttp.post({url: url, params});
}

// 同步征订数据接口
export const syncFromSubscription = () =>
  defHttp.post({url: Api.syncFromSubscription});

/**
 * 根据学生ID查询学生信息（学号/姓名）- 完全对齐领取表
 * @param id 学生ID
 */
export const getStudentById = (id) => {
  return defHttp.get({url: Api.getStudentById, params: {id}});
};

/**
 * 按学号查询学生信息 - 完全对齐领取表写法
 * @param studentNo 学号
 */
export const getStudentByNo = (studentNo) => {
  return defHttp.get({url: Api.getStudentByNo, params: {studentNo}});
};

/**
 * 根据专业ID查询专业名称 - 对齐领取表风格
 * @param majorId 专业ID
 */
export const getMajorInfo = (majorId) => {
  return defHttp.get({url: Api.getMajorInfo, params: {id: majorId}});
};

/**
 * 根据教材ID查询教材名称 - 完全对齐领取表
 * @param id 教材ID
 */
export const getTextbookById = (id) => {
  return defHttp.get({url: Api.getTextbookById, params: {id}});
};

// 批量修改领取状态接口函数
export const batchUpdateReceiveStatus = (params) => {
  return defHttp.post({url: Api.batchUpdateReceiveStatus, params});
};

// 获取当前学年
export const getCurrentSchoolYear = () => {
  return defHttp.get({url: Api.getCurrentSchoolYear});
};
