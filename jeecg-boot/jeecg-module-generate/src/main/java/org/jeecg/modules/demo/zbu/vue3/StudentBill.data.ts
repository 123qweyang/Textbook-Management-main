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
    dataIndex: 'studentId_dictText'
   },
   {
    title: '专业',
    align:"center",
    sorter: true,
    dataIndex: 'majorName_dictText'
   },
   {
    title: '征订学年',
    align:"center",
    sorter: true,
    dataIndex: 'subscriptionYear_dictText'
   },
   {
    title: '征订学期',
    align:"center",
    sorter: true,
    dataIndex: 'subscriptionSemester_dictText'
   },
   {
    title: '教材名称',
    align:"center",
    dataIndex: 'textbookName_dictText'
   },
   {
    title: '教材定价',
    align:"center",
    dataIndex: 'price_dictText'
   },
   {
    title: '教材费用',
    align:"center",
    dataIndex: 'discountPrice'
   },
   {
    title: '征订状态',
    align:"center",
    sorter: true,
    dataIndex: 'subscribeStatus_dictText'
   },
   {
    title: '领取状态',
    align:"center",
    sorter: true,
    dataIndex: 'receiveStatus_dictText'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
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
      label: "专业",
      field: 'majorName',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "征订学年",
      field: 'subscriptionYear',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "征订学期",
      field: 'subscriptionSemester',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "征订状态",
      field: 'subscribeStatus',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "领取状态",
      field: 'receiveStatus',
      component: 'Input',
      //colProps: {span: 6},
 	},
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学号',
    field: 'studentId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'student_id',
     tableName: 't_student',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学号!'},
          ];
     },
  },
  {
    label: '专业',
    field: 'majorName',
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
    label: '征订学年',
    field: 'subscriptionYear',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'subscription_year',
     tableName: 't_subscription',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入征订学年!'},
          ];
     },
  },
  {
    label: '征订学期',
    field: 'subscriptionSemester',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'subscription_semester',
     tableName: 't_subscription',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入征订学期!'},
          ];
     },
  },
  {
    label: '教材名称',
    field: 'textbookName',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'textbook_name',
     tableName: 't_textbook',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入教材名称!'},
          ];
     },
  },
  {
    label: '教材定价',
    field: 'price',
    component: 'InputNumber',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入教材定价!'},
          ];
     },
  },
  {
    label: '教材费用',
    field: 'discountPrice',
    component: 'InputNumber',
  },
  {
    label: '征订状态',
    field: 'subscribeStatus',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'subscribe_status',
     tableName: 't_subscription',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入征订状态!'},
          ];
     },
  },
  {
    label: '领取状态',
    field: 'receiveStatus',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'receive_status',
     tableName: 't_receive',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入领取状态!'},
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
  studentId: {title: '学号',order: 0,view: 'link_table', type: 'string',},
  majorName: {title: '专业',order: 1,view: 'link_table', type: 'string',},
  subscriptionYear: {title: '征订学年',order: 2,view: 'link_table', type: 'string',},
  subscriptionSemester: {title: '征订学期',order: 3,view: 'link_table', type: 'string',},
  textbookName: {title: '教材名称',order: 4,view: 'link_table', type: 'string',},
  price: {title: '教材定价',order: 5,view: 'number', type: 'number',},
  discountPrice: {title: '教材费用',order: 6,view: 'number', type: 'number',},
  subscribeStatus: {title: '征订状态',order: 7,view: 'link_table', type: 'string',},
  receiveStatus: {title: '领取状态',order: 8,view: 'link_table', type: 'string',},
  remark: {title: '备注',order: 9,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}