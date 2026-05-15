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
    title: '姓名',
    align: "center",
    sorter: false,
    dataIndex: 'studentName' // 匹配api返回的studentName字段，无需异步渲染
  },
  {
    title: '专业',
    align: "center",
    sorter: true,
    dataIndex: 'majorName'
  },
  {
    title: '班级',
    align: "center",
    sorter: true,
    dataIndex: 'className'
  },
  {
    title: '征订学年',
    align: "center",
    sorter: true,
    dataIndex: 'subscriptionYear' // 关键：删掉_dictText，后端返回的是subscriptionYear
  },
  {
    title: '征订学期',
    align: "center",
    sorter: true,
    dataIndex: 'subscriptionSemester', // 关键：删掉_dictText，后端返回的是subscriptionSemester
    // 新增：把数字1/2转成中文（可选，优化显示）
    customRender: ({ text }) => {
      return text === '1' ? '第一学期' : text === '2' ? '第二学期' : text;
    }
  },
  {
    title: '教材名称',
    align: "center",
    dataIndex: 'textbookName' // 关键：删掉_dictText，后端返回的是textbookName
  },
  {
    title: 'ISBN',
    align: "center",
    dataIndex: 'isbn'
  },
  {
    title: '教材定价',
    align: "center",
    dataIndex: 'price' // 关键：删掉_dictText，后端返回的是price
  },
  {
    title: '教材费用',
    align: "center",
    dataIndex: 'discountPrice' // 原本就对，不用改
  },
  {
    title: '征订状态',
    align: "center",
    sorter: true,
    dataIndex: 'subscribeStatus',
    customRender: ({ text }) => {
      if (text === '1') return '已征订';
      if (text === '2') return '未征订';
      return text || '未征订';
    }
  },
  {
    title: '领取状态',
    align: "center",
    sorter: true,
    dataIndex: 'receiveStatus',
    customRender: ({ text }) => {
      if (text === '1') return '已领取';
      if (text === '0') return '未领取';
      return text || '未领取';
    }
  },
  {
    title: '备注',
    align: "center",
    dataIndex: 'remark' // 原本就对，不用改
  },
];
//查询数据
export const searchFormSchema: FormSchema[] = [
  {
    label: "学号",
    field: 'studentId',
    component: 'Input',
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
          // 先找到专业
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
    label: "征订学年",
    field: 'subscriptionYear',
    component: 'JDictSelectTag',
    componentProps: () => {
      const now = new Date();
      const currentYear = now.getFullYear();
      const currentMonth = now.getMonth() + 1; // 1-12

      // 9月之前（1-8月）：最新一届是去年；9月之后（9-12月）：最新一届是本年
      const latestGradeYear = currentMonth < 9 ? currentYear - 1 : currentYear;

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
  },
  {
    label: "征订学期",
    field: 'subscriptionSemester',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "semester"
    },
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
  },
  {
    label: "领取状态",
    field: 'receiveStatus',
    component: 'JDictSelectTag',
    componentProps: {
      options: [
        { label: '已领取', value: '1' },
        { label: '未领取', value: '0' }
      ],
      placeholder: '请选择领取状态'
    },
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学号',
    field: 'studentId',
    component: 'Input',
    disabled: true,
  },
  {
    label: '专业',
    field: 'majorName',
    component: 'Input',
    disabled: true,
  },
  {
    label: '征订学年',
    field: 'subscriptionYear',
    component: 'Input',
    disabled: true,
  },
  {
    label: '征订学期',
    field: 'subscriptionSemester',
    component: 'Input',
    disabled: true,
  },
  {
    label: '教材名称',
    field: 'textbookName',
    component: 'Input',
    disabled: true,
  },
  {
    label: '教材定价',
    field: 'price',
    component: 'InputNumber',
  },
  {
    label: '教材费用',
    field: 'discountPrice',
    component: 'InputNumber',
  },
  {
    label: '征订状态',
    field: 'subscribeStatus',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "subscribe_status"
    },
  },
  {
    label: '领取状态',
    field: 'receiveStatus',
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "receive_status"
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
  studentId: { title: '学号', order: 0, view: 'link_table', type: 'string', },
  majorName: { title: '专业', order: 1, view: 'link_table', type: 'string', },
  subscriptionYear: { title: '征订学年', order: 2, view: 'link_table', type: 'string', },
  subscriptionSemester: { title: '征订学期', order: 3, view: 'link_table', type: 'string', },
  textbookName: { title: '教材名称', order: 4, view: 'link_table', type: 'string', },
  price: { title: '教材定价', order: 5, view: 'number', type: 'number', },
  discountPrice: { title: '教材费用', order: 6, view: 'number', type: 'number', },
  subscribeStatus: { title: '征订状态', order: 7, view: 'link_table', type: 'string', },
  receiveStatus: { title: '领取状态', order: 8, view: 'link_table', type: 'string', },
  remark: { title: '备注', order: 9, view: 'text', type: 'string', },
};

/**
 * 流程表单调用这个方法获取formSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}
