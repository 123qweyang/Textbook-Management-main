import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '学院名称',
    align:"center",
    sorter: true,
    dataIndex: 'collegeName_dictText'
   },
   {
    title: '专业名称',
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
    title: '征订学生数',
    align:"center",
    sorter: true,
    dataIndex: 'studentCount'
   },
   {
    title: '征订教材数',
    align:"center",
    sorter: true,
    dataIndex: 'textbookCount'
   },
   {
    title: '原价总价',
    align:"center",
    sorter: true,
    dataIndex: 'originalTotal'
   },
   {
    title: '折后总价',
    align:"center",
    sorter: true,
    dataIndex: 'discountTotal'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
	{
      label: "学院名称",
      field: 'collegeName',
      component: 'Input',
      //colProps: {span: 6},
 	},
	{
      label: "专业名称",
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
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学院名称',
    field: 'collegeName',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'college_name',
     tableName: 't_college',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学院名称!'},
          ];
     },
  },
  {
    label: '专业名称',
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
                 { required: true, message: '请输入专业名称!'},
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
    label: '征订学生数',
    field: 'studentCount',
    component: 'InputNumber',
  },
  {
    label: '征订教材数',
    field: 'textbookCount',
    component: 'InputNumber',
  },
  {
    label: '原价总价',
    field: 'originalTotal',
    component: 'InputNumber',
  },
  {
    label: '折后总价',
    field: 'discountTotal',
    component: 'InputNumber',
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
  collegeName: {title: '学院名称',order: 0,view: 'link_table', type: 'string',},
  majorName: {title: '专业名称',order: 1,view: 'link_table', type: 'string',},
  subscriptionYear: {title: '征订学年',order: 2,view: 'link_table', type: 'string',},
  subscriptionSemester: {title: '征订学期',order: 3,view: 'link_table', type: 'string',},
  studentCount: {title: '征订学生数',order: 4,view: 'number', type: 'number',},
  textbookCount: {title: '征订教材数',order: 5,view: 'number', type: 'number',},
  originalTotal: {title: '原价总价',order: 6,view: 'number', type: 'number',},
  discountTotal: {title: '折后总价',order: 7,view: 'number', type: 'number',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}