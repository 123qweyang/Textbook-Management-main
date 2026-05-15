import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
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
    title: '教材',
    align:"center",
    dataIndex: 'textbookId_dictText'
   },
   {
    title: '学年',
    align:"center",
    sorter: true,
    dataIndex: 'schoolYear'
   },
   {
    title: '学期',
    align:"center",
    sorter: true,
    dataIndex: 'semester_dictText'
   },
   {
    title: '生效状态',
    align:"center",
    sorter: true,
    dataIndex: 'selectionStatus_dictText'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
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
      label: "教材",
      field: 'textbookId',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "学年",
      field: 'schoolYear',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "学期",
      field: 'semester',
      component: 'JSelectMultiple',
      componentProps:{
          dictCode:"semester"
      },
      //colProps: {span: 6},
 	},
	{
      label: "生效状态",
      field: 'selectionStatus',
      component: 'JSelectMultiple',
      componentProps:{
          dictCode:"use_state"
      },
      //colProps: {span: 6},
 	},
];
//表单数据
export const formSchema: FormSchema[] = [
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
    label: '教材',
    field: 'textbookId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'textbook_name',
     tableName: 't_textbook',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入教材!'},
          ];
     },
  },
  {
    label: '学年',
    field: 'schoolYear',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学年!'},
          ];
     },
  },
  {
    label: '学期',
    field: 'semester',
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"semester",
     },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学期!'},
          ];
     },
  },
  {
    label: '生效状态',
    field: 'selectionStatus',
    defaultValue: "1",
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"use_state",
     },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入生效状态!'},
          ];
     },
  },
  {
    label: '备注',
    field: 'remark',
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
  majorId: {title: '专业',order: 0,view: 'link_table', type: 'string',},
  classId: {title: '班级',order: 1,view: 'link_table', type: 'string',},
  textbookId: {title: '教材',order: 2,view: 'link_table', type: 'string',},
  schoolYear: {title: '学年',order: 3,view: 'text', type: 'string',},
  semester: {title: '学期',order: 4,view: 'list', type: 'string',dictCode: 'semester',},
  selectionStatus: {title: '生效状态',order: 5,view: 'list', type: 'string',dictCode: 'use_state',},
  remark: {title: '备注',order: 6,view: 'text', type: 'string',},
  createTime: {title: '创建日期',order: 7,view: 'datetime', type: 'string',},
  updateTime: {title: '更新日期',order: 8,view: 'datetime', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}