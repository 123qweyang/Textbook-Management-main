import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/zbu/tStudent/list',
  save='/zbu/tStudent/add',
  edit='/zbu/tStudent/edit',
  deleteOne = '/zbu/tStudent/delete',
  deleteBatch = '/zbu/tStudent/deleteBatch',
  importExcel = '/zbu/tStudent/importExcel',
  exportXls = '/zbu/tStudent/exportXls',
  getCurrentStudent = '/zbu/tStudent/getCurrentStudent',
  getMajorList = '/zbu/tStudent/getMajorList',
  getClassList = '/zbu/tStudent/getClassList',
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

// 新增：调用后端获取当前登录学生信息的接口
export const getCurrentStudent = () =>
  defHttp.get({url: Api.getCurrentStudent});

// 新增：专业/班级下拉框方法（前端直接调用）
export const getMajorList = () => defHttp.get({ url: Api.getMajorList });
export const getClassList = () => defHttp.get({ url: Api.getClassList });
export const getClassListByMajor = (majorId) => defHttp.get({ url: '/zbu/tStudent/getClassListByMajor', params: { majorId } });
