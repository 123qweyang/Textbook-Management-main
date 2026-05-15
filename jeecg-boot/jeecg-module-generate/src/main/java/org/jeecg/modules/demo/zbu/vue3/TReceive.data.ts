import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '领取操作人',
    align:"center",
    sorter: true,
    dataIndex: 'receiveOperator_dictText'
   },
   {
    title: '教材',
    align:"center",
    dataIndex: 'subscriptionId_dictText'
   },
   {
    title: '领取状态',
    align:"center",
    sorter: true,
    dataIndex: 'receiveStatus_dictText'
   },
   {
    title: '领取时间',
    align:"center",
    dataIndex: 'receiveTime'
   },
   {
    title: '领取备注',
    align:"center",
    dataIndex: 'receiveRemark'
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
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '领取操作人',
    field: 'receiveOperator',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'student_id,student_name',
     tableName: 't_student',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入领取操作人!'},
          ];
     },
  },
  {
    label: '教材',
    field: 'subscriptionId',
    component: 'JLinkTableCard',
    componentProps: {
     valueField: 'id',
     textField: 'textbook_id',
     tableName: 't_subscription',
     multi: false
    },
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入教材!'},
          ];
     },
  },
  {
    label: '领取状态',
    field: 'receiveStatus',
    component: 'JDictSelectTag',
    componentProps:{
        dictCode:"receive_status",
     },
  },
  {
    label: '领取备注',
    field: 'receiveRemark',
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
  receiveOperator: {title: '领取操作人',order: 0,view: 'link_table', type: 'string',},
  subscriptionId: {title: '教材',order: 1,view: 'link_table', type: 'string',},
  receiveStatus: {title: '领取状态',order: 2,view: 'list', type: 'string',dictCode: 'receive_status',},
  receiveTime: {title: '领取时间',order: 3,view: 'datetime', type: 'string',},
  receiveRemark: {title: '领取备注',order: 4,view: 'text', type: 'string',},
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