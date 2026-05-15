import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
//列表数据
export const columns: BasicColumn[] = [
   {
    title: '专业名称',
    align:"center",
    sorter: true,
    dataIndex: 'majorName'
   },
   {
    title: '专业编码',
    align:"center",
    sorter: true,
    dataIndex: 'majorCode'
   },
   {
    title: '所属学院',
    align:"center",
    dataIndex: 'collegeId_dictText'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "专业名称",
    field: "majorName",
    component: 'JInput',
  },
	{
      label: "专业编码",
      field: 'majorCode',
      component: 'Input',
      //colProps: {span: 6},
 	},
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '专业名称',
    field: 'majorName',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入专业名称!'},
                 {...rules.duplicateCheckRule('t_major', 'major_name',model,schema)[0]},
          ];
     },
  },
  {
    label: '专业编码',
    field: 'majorCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入专业编码!'},
                 {...rules.duplicateCheckRule('t_major', 'major_code',model,schema)[0]},
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
  majorName: {title: '专业名称',order: 0,view: 'text', type: 'string',},
  majorCode: {title: '专业编码',order: 1,view: 'text', type: 'string',},
  collegeId: {title: '所属学院',order: 2,view: 'link_table', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}