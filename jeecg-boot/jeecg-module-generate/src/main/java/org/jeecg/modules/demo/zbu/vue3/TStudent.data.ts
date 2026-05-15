import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '学号',
    align:"center",
    sorter: true,
    dataIndex: 'studentId'
   },
   {
    title: '学生姓名',
    align:"center",
    dataIndex: 'studentName'
   },
   {
    title: '专业',
    align:"center",
    sorter: true,
    dataIndex: 'majorId_dictText'
   },
   {
    title: '班级',
    align:"center",
    sorter: true,
    dataIndex: 'classId_dictText'
   },
   {
    title: '状态',
    align:"center",
    sorter: true,
    dataIndex: 'status_dictText'
   },
   {
    title: '入学年份',
    align:"center",
    sorter: true,
    dataIndex: 'admissionYear'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
	{
      label: "学号",
      field: 'studentId',
      component: 'Input',
      //colProps: {span: 6},
 	},
  {
    label: "学生姓名",
    field: "studentName",
    component: 'JInput',
  },
	{
      label: "专业",
      field: 'majorId',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "班级",
      field: 'classId',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "状态",
      field: 'status',
      component: 'JSelectMultiple',
      componentProps:{
          dictCode:"use_state"
      },
      //colProps: {span: 6},
 	},
	{
      label: "入学年份",
      field: 'admissionYear',
      component: 'Input',
      //colProps: {span: 6},
 	},
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学号',
    field: 'studentId',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学号!'},
          ];
     },
  },
  {
    label: '学生姓名',
    field: 'studentName',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学生姓名!'},
          ];
     },
  },
  {
    label: '专业',
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
                 { required: true, message: '请输入专业!'},
          ];
     },
  },
  {
    label: '班级',
    field: 'classId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'class_name',
     tableName: 't_class',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入班级!'},
          ];
     },
  },
  {
    label: '状态',
    field: 'status',
    defaultValue: "1",
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"use_state",
     },
  },
  {
    label: '入学年份',
    field: 'admissionYear',
    component: 'Input',
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
  studentId: {title: '学号',order: 0,view: 'text', type: 'string',},
  studentName: {title: '学生姓名',order: 1,view: 'text', type: 'string',},
  majorId: {title: '专业',order: 2,view: 'link_table', type: 'string',},
  classId: {title: '班级',order: 3,view: 'link_table', type: 'string',},
  status: {title: '状态',order: 4,view: 'list', type: 'string',dictCode: 'use_state',},
  admissionYear: {title: '入学年份',order: 5,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}