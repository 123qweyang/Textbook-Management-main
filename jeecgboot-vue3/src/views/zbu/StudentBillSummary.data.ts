import {BasicColumn} from '/@/components/Table';
import {FormSchema} from '/@/components/Table';
import { defHttp } from '/@/utils/http/axios';

export const columns: BasicColumn[] = [
  {
    title: '学号',
    align:"center",
    dataIndex: 'studentId',
    width: 120
  },
  {
    title: '姓名',
    align:"center",
    dataIndex: 'studentName',
    width: 100
  },
  {
    title: '学院',
    align:"center",
    dataIndex: 'collegeName',
    width: 150
  },
  {
    title: '专业',
    align:"center",
    dataIndex: 'majorName',
    width: 150
  },
  {
    title: '班级',
    align:"center",
    dataIndex: 'className',
    width: 150
  },
  {
    title: '征订学年',
    align:"center",
    dataIndex: 'schoolYear',
    width: 100
  },
  {
    title: '征订学期',
    align:"center",
    dataIndex: 'semester',
    width: 100
  },
  {
    title: '教材费用',
    align:"center",
    dataIndex: 'totalDiscountPrice',
    width: 120,
    customRender: ({ text }) => text ? `¥${Number(text).toFixed(2)}` : '¥0.00'
  },
];

export const searchFormSchema: FormSchema[] = [
  {
    label: "学号",
    field: 'studentNo',
    component: 'Input',
  },
  {
    label: "姓名",
    field: 'studentName',
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
    label: "征订学年",
    field: 'schoolYear',
    component: 'ApiSelect',
    componentProps: {
      api: () => defHttp.get({ url: '/zbu/tSubscription/list', params: { pageSize: 99999, pageNo: 1 } }).then(res => {
        const records = res.records || [];
        const years = [...new Set(records.map(item => item.subscriptionYear).filter(Boolean))];
        return years.sort().map(year => ({ label: year, value: year }));
      }),
      placeholder: '请选择征订学年'
    },
  },
  {
    label: "征订学期",
    field: 'semester',
    component: 'Select',
    componentProps: {
      options: [
        { label: '第一学期', value: '1' },
        { label: '第二学期', value: '2' },
      ],
    },
  },
];
