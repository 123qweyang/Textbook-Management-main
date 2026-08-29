import {defHttp} from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  list = '/zbu/tSubscription/list',
  save='/zbu/tSubscription/add',
  edit='/zbu/tSubscription/edit',
  deleteOne = '/zbu/tSubscription/delete',
  deleteBatch = '/zbu/tSubscription/deleteBatch',
  importExcel = '/zbu/tSubscription/importExcel',
  exportXls = '/zbu/tSubscription/exportXls',
  getMySubscription = '/zbu/tSubscription/getMySubscription',
  getStudentById = '/zbu/tStudent/queryById', // 后端根据学生主键ID查信息的接口
  getStudentByNo = '/zbu/tStudent/queryByStudentId', // 按学号查询学生
  getTextbookById = '/zbu/tTextbook/queryById', // 查教材
  getMajorById = '/zbu/tMajor/queryById',       // 查专业
  getCollegeById = '/zbu/tCollege/queryById',   // 查学院
  batchUpdateSubscribeStatus = '/zbu/tSubscription/batchUpdateSubscribeStatus',
  markAllSubscribed = '/zbu/tSubscription/markAllSubscribed',
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
      return defHttp.post({url: Api.deleteBatch, data: params}).then(() => {
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

// 新增：学生端获取我的征订记录
export const getMySubscription = (params) =>
  defHttp.get({url: Api.getMySubscription,params});

// 根据学生主键ID查询学号、姓名等信息
export const getStudentById = (studentPrimaryId) =>
  defHttp.get({
    url: Api.getStudentById,
    params: { id: studentPrimaryId } // 传学生主键ID给后端
  });
// 根据教材ID查教材信息
export const getTextbookById = (textbookId) =>
  defHttp.get({url: Api.getTextbookById, params: { id: textbookId }});

// 根据专业ID查专业信息
export const getMajorById = (majorId) =>
  defHttp.get({url: Api.getMajorById, params: { id: majorId }});


/**
 * 批量修改征订状态接口
 * @param params {ids: [], subscribeStatus: '1', studentId: ''}
 */
export const batchUpdateSubscribeStatus = (params: any) => {
  return defHttp.post({url: Api.batchUpdateSubscribeStatus, params});
};

/**
 * 管理员：按当前查询条件将全部未征订记录标记为已征订
 * @param params {筛选参数..., dryRun: boolean} dryRun=true仅返回数量，false执行标记
 */
export const markAllSubscribed = (params: any) => {
  return defHttp.post({url: Api.markAllSubscribed, params});
};


// 根据学号查询学生信息（学生端用）
export const getStudentByNo = (studentNo: string) =>
  defHttp.get({
    url: Api.getStudentByNo,
    params: { studentId: studentNo } // 传学号给后端（参数名和后端保持一致）
  });

// 根据学院ID查学院信息
export const getCollegeById = (collegeId) =>
  defHttp.get({ url: Api.getCollegeById, params: { id: collegeId } });

// 获取当前学年
export const getCurrentSchoolYear = () => {
  return defHttp.get({ url: Api.getCurrentSchoolYear });
};


