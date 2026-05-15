import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '班级名称',
    align:"center",
    sorter: true,
    dataIndex: 'className'
   },
   {
    title: '班级编码',
    align:"center",
    sorter: true,
    dataIndex: 'classCode'
   },
   {
    title: '所属专业',
    align:"center",
    dataIndex: 'majorId_dictText'
   },
   {
    title: '辅导员',
    align:"center",
    dataIndex: 'counselorId_dictText'
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
    label: "班级名称",
    field: "className",
    component: 'JInput',
  },
	{
      label: "班级编码",
      field: 'classCode',
      component: 'Input',
      //colProps: {span: 6},
 	},
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '班级名称',
    field: 'className',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入班级名称!'},
                 {...rules.duplicateCheckRule('t_class', 'class_name',model,schema)[0]},
          ];
     },
  },
  {
    label: '班级编码',
    field: 'classCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入班级编码!'},
                 {...rules.duplicateCheckRule('t_class', 'class_code',model,schema)[0]},
          ];
     },
  },
  {
    label: '所属专业',
    field: 'majorId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'major_name',
     tableName: 't_major',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入所属专业!'},
          ];
     },
  },
  {
    label: '辅导员',
    field: 'counselorId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'counselor_name',
     tableName: 't_counselor',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入辅导员!'},
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
  className: {title: '班级名称',order: 0,view: 'text', type: 'string',},
  classCode: {title: '班级编码',order: 1,view: 'text', type: 'string',},
  majorId: {title: '所属专业',order: 2,view: 'link_table', type: 'string',},
  counselorId: {title: '辅导员',order: 3,view: 'link_table', type: 'string',},
  createTime: {title: '创建日期',order: 4,view: 'datetime', type: 'string',},
  updateTime: {title: '更新日期',order: 5,view: 'datetime', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}