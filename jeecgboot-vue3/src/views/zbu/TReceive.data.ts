import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { rules} from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { defHttp } from '/@/utils/http/axios';

//列表数据（保留你的配置）
export const columns: BasicColumn[] = [
  {
    title: '学号',
    align:"center",
    sorter: true,
    dataIndex: 'studentNo'
  },
  {
    title: '姓名',
    align:"center",
    dataIndex: 'studentName'
  },
  {
    title: '学院',
    align:"center",
    sorter: true,
    dataIndex: 'collegeName'
  },
  {
    title: '教材',
    align:"center",
    dataIndex: 'textbookName'
  },
  {
    title: 'ISBN',
    align:"center",
    dataIndex: 'isbn'
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
  // {
  //   title: '创建日期',
  //   align:"center",
  //   dataIndex: 'createTime'
  // },
  // {
  //   title: '更新日期',
  //   align:"center",
  //   dataIndex: 'updateTime'
  // },
];

// ========== 正确的搜索表单配置（FormSchema） ==========
export const searchFormSchema: FormSchema[] = [
  // 学号（模糊搜索）
  {
    label: '学号', // 搜索框显示的标签
    field: 'studentNo', // 对应表格的dataIndex，也是接口查询参数
    component: 'Input', // 搜索组件：输入框
    colProps: { span: 8 }, // 布局占比（24格布局，一行显示3个）
  },
  // 姓名（模糊搜索）
  {
    label: '姓名',
    field: 'studentName',
    component: 'Input',
    colProps: { span: 8 },
  },
  {
    label: "学院",
    field: 'collegeName',
    component: 'ApiSelect',
    componentProps: {
      api: () => defHttp.get({ url: '/zbu/tCollege/list', params: { pageSize: 999, pageNo: 1 } }).then(res => {
        const records = res.records || [];
        return records.map(item => ({
          label: item.collegeName,
          value: item.collegeName
        }));
      }),
      placeholder: '请选择学院'
    },
    //colProps: {span: 6},
  },
  {
    label: "专业",
    field: 'majorName',
    component: 'ApiSelect',
    componentProps: ({ formModel }) => {
      const collegeName = formModel.collegeName;
      return {
        api: () => defHttp.get({ url: '/zbu/tMajor/list', params: { pageSize: 9999, pageNo: 1 } }).then(res => {
          const records = res.records || [];
          if (!collegeName) return records.map(item => ({ label: item.majorName, value: item.majorName }));
          return defHttp.get({ url: '/zbu/tCollege/list', params: { pageSize: 999, pageNo: 1, collegeName } }).then(colRes => {
            const colleges = colRes.records || [];
            const college = colleges.find(c => c.collegeName === collegeName);
            const collegeId = college ? college.id : null;
            return records
              .filter(item => !collegeId || item.collegeId === collegeId)
              .map(item => ({ label: item.majorName, value: item.majorName }));
          });
        }),
        placeholder: collegeName ? '请选择专业' : '请先选择学院',
        disabled: !collegeName,
        immediate: true
      };
    },
  },
  {
    label: "班级",
    field: 'className',
    component: 'ApiSelect',
    componentProps: ({ formModel }) => {
      const majorName = formModel.majorName;
      return {
        api: () => defHttp.get({ url: '/zbu/tClass/list', params: { pageSize: 9999, pageNo: 1 } }).then(res => {
          const records = res.records || [];
          if (!majorName) return records.map(item => ({ label: item.className, value: item.className }));
          return defHttp.get({ url: '/zbu/tMajor/list', params: { pageSize: 999, pageNo: 1, majorName } }).then(majRes => {
            const majors = majRes.records || [];
            const major = majors.find(m => m.majorName === majorName);
            const majorId = major ? major.id : null;
            return records
              .filter(item => !majorId || item.majorId === majorId)
              .map(item => ({ label: item.className, value: item.className }));
          });
        }),
        placeholder: majorName ? '请选择班级' : '请先选择专业',
        disabled: !majorName,
        immediate: false
      };
    },
    ifShow: ({ values }) => !!values.majorName,
  },
  {
    label: '教材',
    field: 'textbookName',
    component: 'Input',
    colProps: { span: 8 },
  },
  // 领取状态（下拉选择）
  {
    label: '领取状态',
    field: 'receiveStatus', // 对应字典码，和superQuerySchema一致
    component: 'JDictSelectTag', // 字典下拉组件
    componentProps: {
      dictCode: 'receive_status', // 和表单里的字典码一致
    },
    colProps: { span: 8 },
  },
  // // 领取时间（日期范围）
  // {
  //   label: '领取时间',
  //   field: 'receiveTime',
  //   component: 'RangePicker', // 时间范围选择器
  //   componentProps: {
  //     format: 'YYYY-MM-DD HH:mm:ss',
  //     placeholder: ['开始时间', '结束时间'],
  //   },
  //   colProps: { span: 8 },
  // },
];

//表单数据（保留你的配置）
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

// 学生专用Schema（保留你的配置）
export const studentEditFormSchema: FormSchema[] = [
  {
    label: '领取状态',
    field: 'receiveStatus', // 字段名和原表单一致
    component: 'JDictSelectTag', // 和原表单组件一致
    componentProps:{
      dictCode:"receive_status", // 字典码和原表单一致
    },
    dynamicRules: () => [{ required: true, message: '请选择领取状态!' }],
    colProps: { span: 24 } // 占满宽度，确保显示
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false // 隐藏ID，仅提交用
  }
];

// 高级查询数据（保留你的配置）
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
