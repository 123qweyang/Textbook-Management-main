import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '标段',
    align:"center",
    sorter: true,
    dataIndex: 'sectionCode'
   },
   {
    title: '编号',
    align:"center",
    sorter: true,
    dataIndex: 'businessCode'
   },
   {
    title: '征订号或书号（ISBN)',
    align:"center",
    sorter: true,
    dataIndex: 'isbn'
   },
   {
    title: '教材名称',
    align:"center",
    dataIndex: 'textbookName'
   },
   {
    title: '编著',
    align:"center",
    dataIndex: 'author'
   },
   {
    title: '出版社',
    align:"center",
    dataIndex: 'publisher'
   },
   {
    title: '出版时间',
    align:"center",
    dataIndex: 'publishDate'
   },
   {
    title: '定价',
    align:"center",
    dataIndex: 'price'
   },
   {
    title: '折扣',
    align:"center",
    dataIndex: 'discount'
   },
   {
    title: '启用学年',
    align:"center",
    sorter: true,
    dataIndex: 'enableYear'
   },
   {
    title: '启用学期',
    align:"center",
    sorter: true,
    dataIndex: 'enableSemester_dictText'
   },
   {
    title: '启用状态',
    align:"center",
    sorter: true,
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
      label: "征订号或书号（ISBN)",
      field: 'isbn',
      component: 'Input',
      //colProps: {span: 6},
 	},
  {
    label: "教材名称",
    field: "textbookName",
    component: 'JInput',
  },
	{
      label: "启用学年",
      field: 'enableYear',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "启用学期",
      field: 'enableSemester',
      component: 'JSelectMultiple',
      componentProps:{
          dictCode:"semester"
      },
      //colProps: {span: 6},
 	},
	{
      label: "启用状态",
      field: 'status',
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
    label: '标段',
    field: 'sectionCode',
    component: 'Input',
  },
  {
    label: '编号',
    field: 'businessCode',
    component: 'Input',
  },
  {
    label: '征订号或书号（ISBN)',
    field: 'isbn',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入征订号或书号（ISBN)!'},
          ];
     },
  },
  {
    label: '教材名称',
    field: 'textbookName',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入教材名称!'},
          ];
     },
  },
  {
    label: '编著',
    field: 'author',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入编著!'},
          ];
     },
  },
  {
    label: '出版社',
    field: 'publisher',
    component: 'Input',
  },
  {
    label: '出版时间',
    field: 'publishDate',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入出版时间!'},
                 { pattern: /^-?\d+$/, message: '请输入整数!'},
          ];
     },
  },
  {
    label: '定价',
    field: 'price',
    component: 'InputNumber',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入定价!'},
          ];
     },
  },
  {
    label: '折扣',
    field: 'discount',
    component: 'InputNumber',
  },
  {
    label: '启用学年',
    field: 'enableYear',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入启用学年!'},
          ];
     },
  },
  {
    label: '启用学期',
    field: 'enableSemester',
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"semester",
     },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入启用学期!'},
          ];
     },
  },
  {
    label: '启用状态',
    field: 'status',
    defaultValue: "1",
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"use_state",
     },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入启用状态!'},
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
  sectionCode: {title: '标段',order: 0,view: 'text', type: 'string',},
  businessCode: {title: '编号',order: 1,view: 'text', type: 'string',},
  isbn: {title: '征订号或书号（ISBN)',order: 2,view: 'text', type: 'string',},
  textbookName: {title: '教材名称',order: 3,view: 'text', type: 'string',},
  author: {title: '编著',order: 4,view: 'text', type: 'string',},
  publisher: {title: '出版社',order: 5,view: 'text', type: 'string',},
  publishDate: {title: '出版时间',order: 6,view: 'text', type: 'string',},
  price: {title: '定价',order: 7,view: 'number', type: 'number',},
  discount: {title: '折扣',order: 8,view: 'number', type: 'number',},
  enableYear: {title: '启用学年',order: 9,view: 'text', type: 'string',},
  enableSemester: {title: '启用学期',order: 10,view: 'list', type: 'string',dictCode: 'semester',},
  status: {title: '启用状态',order: 11,view: 'list', type: 'string',dictCode: 'use_state',},
  createTime: {title: '创建日期',order: 12,view: 'datetime', type: 'string',},
  updateTime: {title: '更新日期',order: 13,view: 'datetime', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}