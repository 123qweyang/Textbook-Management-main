import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/zbu/tTextbook/list',
  save='/zbu/tTextbook/add',
  edit='/zbu/tTextbook/edit',
  deleteOne = '/zbu/tTextbook/delete',
  deleteBatch = '/zbu/tTextbook/deleteBatch',
  importExcel = '/zbu/tTextbook/importExcel',
  updateByExcel = '/zbu/tTextbook/updateByExcel',
  exportXls = '/zbu/tTextbook/exportXls',
  editBatch = '/zbu/tTextbook/editBatch',
  getCurrentSchoolYear = '/zbu/studentBill/getCurrentSchoolYear',
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
 * 更新api（按ISBN+学年+学期匹配更新）
 */
export const getUpdateUrl = Api.updateByExcel;
/**
 * 列表接口
 * @param params
 */
export const list = (params) =>
  defHttp.get({url: Api.list, params});

/**
 * 获取所有记录ID（用于全选）
 * @param params 查询条件
 */
export const getAllIds = (params) =>
  defHttp.get({url: '/zbu/tTextbook/getAllIds', params}, {isTransformResponse: false});

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
      return defHttp.post({url: Api.deleteBatch, data: params}, {isTransformResponse: false}).then((res) => {
        if (res.success) {
          handleSuccess();
        }
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

/**
 * 批量修改
 * @param params 包含ids、sectionCode、businessCode、discount
 */
export const batchEdit = (params) => {
  // 和征订表接口写法完全一致，简洁且无报错
  return defHttp.post({ url: Api.editBatch, params });
};

/**
 * 获取当前学年
 */
export const getCurrentSchoolYear = () => {
  return defHttp.get({ url: Api.getCurrentSchoolYear });
};


