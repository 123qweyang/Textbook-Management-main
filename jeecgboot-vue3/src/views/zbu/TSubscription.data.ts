import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { defHttp } from '/@/utils/http/axios';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '学号',
    align: "center",
    sorter: true,
    dataIndex: 'studentNo'
  },
  {
    title: '学生姓名',
    align: "center",
    dataIndex: 'studentName'
  },
  {
    title: '教材',
    align: "center",
    dataIndex: 'textbookName'
  },
  {
    title: 'ISBN',
    align: "center",
    dataIndex: 'isbn'
  },
  // {
  //  title: '教材选用',
  //  align:"center",
  //  sorter: true,
  //  dataIndex: 'selectionId_dictText'
  // },
  {
    title: '专业',
    align: "center",
    sorter: true,
    dataIndex: 'majorName'
  },
  {
    title: '学院',
    align: "center",
    sorter: true,
    dataIndex: 'collegeName'
  },
  {
    title: '征订学年',
    align: "center",
    sorter: true,
    dataIndex: 'subscriptionYear'
  },
  {
    title: '征订学期',
    align: "center",
    sorter: true,
    dataIndex: 'subscriptionSemester_dictText'
  },
  {
    title: '征订状态',
    align: "center",
    sorter: true,
    dataIndex: 'subscribeStatus_dictText'
  },

  // {
  //  title: '征订截止时间',
  //  align:"center",
  //  dataIndex: 'deadline'
  // },
  // {
  //  title: '创建日期',
  //  align:"center",
  //  dataIndex: 'createTime'
  // },
  // {
  //  title: '更新日期',
  //  align:"center",
  //  dataIndex: 'updateTime'
  // },
  {
    title: '征订备注',
    align: "center",
    dataIndex: 'remark'
  },
  {
    title: '征订操作时间',
    align: "center",
    dataIndex: 'subscribeTime'
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "新生届号",
    field: 'studentIdPrefix',
    component: 'JDictSelectTag',
    componentProps: () => {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1; // 1-12
      // 9月之前：最新一届是前一年（如2026年9月前，最新是25届）
      // 9月及之后：最新一届是当年（如2026年9月后，最新是26届）
      let startYear = currentMonth < 6 ? currentYear - 1 : currentYear;
      const options = [];
      for (let i = 0; i < 4; i++) {
        const year = startYear - i;
        const label = String(year).slice(-2); // 取后两位，如 26、25
        options.push({ label, value: label });
      }
      return {
        options,
        placeholder: '请选择新生届号'
      };
    },
    //colProps: {span: 6},
  },
  {
    label: "学生",
    field: 'studentId',
    component: 'Input',
    //colProps: {span: 6},
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
          // 先找到学院
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
    //colProps: {span: 6},
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
    //colProps: {span: 6},
  },
  {
    label: "征订学年",
    field: 'subscriptionYear',
    component: 'JDictSelectTag',
    defaultValue: (() => {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1;
      return (currentMonth < 6) ? `${currentYear - 1}-${currentYear}` : `${currentYear}-${currentYear + 1}`;
    })(),
    componentProps: () => {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1; // 1-12

      // 9月之前（1-8月）：最新一届是去年；9月之后（9-12月）：最新一届是本年
      const latestGradeYear = currentMonth < 6 ? currentYear - 1 : currentYear;

      // 生成4个选项：从最新一届往前推4年（包括最新一届，用于查看）
      const options = [];
      for (let i = 0; i <= 3; i++) {
        const year = latestGradeYear - i;
        if (year >= 2020) { // 只显示2020年及之后的
          options.push({
            label: `${year}-${year + 1}`,
            value: `${year}-${year + 1}`
          });
        }
      }

      return {
        options,
        placeholder: '请选择征订学年'
      };
    },
    //colProps: {span: 6},
  },
  {
    label: "征订学期",
    field: 'subscriptionSemester',
    component: 'JDictSelectTag',
    defaultValue: (() => {
      const month = new Date().getMonth() + 1;
      return (month >= 6 && month <= 11) ? '1' : '2';
    })(),
    componentProps: {
      dictCode: "semester"
    },
    //colProps: {span: 6},
  },
  {
    label: "征订状态",
    field: 'subscribeStatus',
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: '已征订', value: '1' },
        { label: '未征订', value: '0' }
      ],
      placeholder: '请选择征订状态'
    },
    //colProps: {span: 6},
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学生',
    field: 'studentId',
    component: 'JLinkTableCard',
    componentProps: {
      valueField: 'id',
      textField: 'student_id,student_name',
      tableName: 't_student',
      multi: false
    },
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入学生!' },
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
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入教材!' },
      ];
    },
  },
  {
    label: '教材选用',
    field: 'selectionId',
    component: 'JLinkTableCard',
    componentProps: {
      valueField: 'id',
      textField: 'selection_status',
      tableName: 't_textbook_selection',
      multi: false
    },
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入教材选用!' },
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
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入专业!' },
      ];
    },
  },
  {
    label: '征订学年',
    field: 'subscriptionYear',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入征订学年!' },
      ];
    },
  },
  {
    label: '征订学期',
    field: 'subscriptionSemester',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "semester",
    },
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入征订学期!' },
      ];
    },
  },
  {
    label: '征订状态',
    field: 'subscribeStatus',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "subscribe_status",
    },
  },
  {
    label: '征订备注',
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


// 学生专用表单（仅2个字段，确保无拼写错误）
export const studentEditFormSchema: FormSchema[] = [
  {
    label: '征订状态',
    field: 'subscribeStatus', // 字段名必须和列表的dataIndex一致
    component: 'JDictSelectTag', // 确保组件名正确
    componentProps: {
      dictCode: "subscribe_status", // 确保字典码在系统中存在
    },
    dynamicRules: () => [{ required: true, message: '请选择征订状态!' }],
    colProps: { span: 24 } // 占满宽度，确保显示
  },
  {
    label: '',
    field: 'id',
    component: 'Input',
    show: false // 隐藏ID，仅提交用
  }
];

// 高级查询数据
export const superQuerySchema = {
  studentId: { title: '学生', order: 0, view: 'link_table', type: 'string', },
  textbookId: { title: '教材', order: 1, view: 'link_table', type: 'string', },
  selectionId: { title: '教材选用', order: 2, view: 'link_table', type: 'string', },
  majorId: { title: '专业', order: 3, view: 'link_table', type: 'string', },
  subscriptionYear: { title: '征订学年', order: 4, view: 'text', type: 'string', },
  subscriptionSemester: { title: '征订学期', order: 5, view: 'list', type: 'string', dictCode: 'semester', },
  subscribeStatus: { title: '征订状态', order: 6, view: 'list', type: 'string', dictCode: 'subscribe_status', },
  remark: { title: '征订备注', order: 7, view: 'text', type: 'string', },
  subscribeTime: { title: '征订操作时间', order: 8, view: 'datetime', type: 'string', },
  // deadline: {title: '征订截止时间',order: 9,view: 'datetime', type: 'string',},
  createTime: { title: '创建日期', order: 10, view: 'datetime', type: 'string', },
  updateTime: { title: '更新日期', order: 11, view: 'datetime', type: 'string', },
};

/**
 * 流程表单调用这个方法获取formSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}
