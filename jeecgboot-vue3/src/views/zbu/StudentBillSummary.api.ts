import { defHttp } from '/@/utils/http/axios';
import { useMessage } from "/@/hooks/web/useMessage";

const { createConfirm } = useMessage();

enum Api {
  summaryList = '/zbu/studentBill/summary',
  exportXls = '/zbu/studentBill/exportSummary',
  deleteBatch = '/zbu/studentBill/deleteSummaryBatch',
  getStudentByNo = '/zbu/tStudent/queryByStudentNo',
  getCurrentSchoolYear = '/zbu/studentBill/getCurrentSchoolYear',
}

/**
 * 汇总列表查询
 * @param params
 */
export const summaryList = (params) => {
  return defHttp.get({ url: Api.summaryList, params }, { joinParamsToUrl: true });
};

export const getStudentByNo = (studentNo) => {
  return defHttp.get({ url: Api.getStudentByNo, params: { studentNo } });
};

/**
 * 获取当前学年
 */
export const getCurrentSchoolYear = () => {
  return defHttp.get({ url: Api.getCurrentSchoolYear });
};

/**
 * 导出api
 * @param params
 */
export const getExportUrl = Api.exportXls;

export const batchDelete = (params, handleSuccess) => {
  createConfirm({
    iconType: 'warning',
    title: '确认删除',
    content: '是否删除选中数据',
    okText: '确认',
    cancelText: '取消',
    onOk: () => {
      return defHttp.post({ url: Api.deleteBatch, data: params }).then(() => {
        handleSuccess();
      });
    }
  });
}
