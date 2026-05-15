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
    dataIndex: 'collegeName'
   },
   {
    title: '学院编码',
    align:"center",
    sorter: true,
    dataIndex: 'collegeCode'
   },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "学院名称",
    field: "collegeName",
    component: 'JInput',
  },
	{
      label: "学院编码",
      field: 'collegeCode',
      component: 'Input',
      //colProps: {span: 6},
 	},
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学院名称',
    field: 'collegeName',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学院名称!'},
          ];
     },
  },
  {
    label: '学院编码',
    field: 'collegeCode',
    component: 'Input',
    dynamicRules: ({model,schema}) => {
          return [
                 { required: true, message: '请输入学院编码!'},
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
  collegeName: {title: '学院名称',order: 0,view: 'text', type: 'string',},
  collegeCode: {title: '学院编码',order: 1,view: 'text', type: 'string',},
};

/**
* 流程表单调用这个方法获取formSchema
* @param param
*/
export function getBpmFormSchema(_formData): FormSchema[]{
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}