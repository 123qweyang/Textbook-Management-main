import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '辅导员工号',
    align:"center",
    sorter: true,
    dataIndex: 'counselorId'
   },
   {
    title: '辅导员姓名',
    align:"center",
    sorter: true,
    dataIndex: 'counselorName'
   },
   {
    title: '所属学院',
    align:"center",
    dataIndex: 'collegeId_dictText'
   },
   {
    title: '联系方式',
    align:"center",
    dataIndex: 'contact'
   },
   {
    title: '状态',
    align:"center",
    dataIndex: 'status_dictText'
   },
   {
    title: '创建日期',
    align:"center",
    dataIndex: 'createTime'
   },
   {
    title: '更新日期',
    align:"center",
    dataIndex: 'updateTime'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
	{
      label: "辅导员工号",
      field: 'counselorId',
      component: 'Input',
      //colProps: {span: 6},
 	},
  {
    label: "辅导员姓名",
    field: "counselorName",
    component: 'JInput',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '辅导员工号',
    field: 'counselorId',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入辅导员工号!'},
          ];
     },
  },
  {
    label: '辅导员姓名',
    field: 'counselorName',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入辅导员姓名!'},
          ];
     },
  },
  {
    label: '所属学院',
    field: 'collegeId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'college_name',
     tableName: 't_college',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入所属学院!'},
          ];
     },
  },
  {
    label: '联系方式',
    field: 'contact',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入联系方式!'},
                 { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码!'},
          ];
     },
  },
  {
    label: '状态',
    field: 'status',
    defaultValue: "1",
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"employed",
     },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入状态!'},
          ];
     },
  },
	// TODO 主键隐藏字段，目前写死为ID
	{
	  label: '',
	  field: 'id',
	  component: 'Input',
	  show: false
	},
];

// 高级查询数据
export const superQuerySchema = {
  counselorId: {title: '辅导员工号',order: 0,view: 'text', type: 'string',},
  counselorName: {title: '辅导员姓名',order: 1,view: 'text', type: 'string',},
  collegeId: {title: '所属学院',order: 2,view: 'link_table', type: 'string',},
  contact: {title: '联系方式',order: 3,view: 'text', type: 'string',},
  status: {title: '状态',order: 4,view: 'list', type: 'string',dictCode: 'employed',},
  createTime: {title: '创建日期',order: 5,view: 'datetime', type: 'string',},
  updateTime: {title: '更新日期',order: 6,view: 'datetime', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}