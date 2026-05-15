import { BasicColumn } from '/@/components/Table';
import { FormSchema } from '/@/components/Table';
import { rules } from '/@/utils/helper/validator';
import { render } from '/@/utils/common/renderUtils';
import { getWeekMonthQuarterYear } from '/@/utils';
import { getMajorList, getClassListByMajor } from './TStudent.api';
import { defHttp } from '/@/utils/http/axios';
//列表数据
export const columns: BasicColumn[] = [
  {
    title: '学号',
    align: "center",
    sorter: true,
    dataIndex: 'studentId'
  },
  {
    title: '学生姓名',
    align: "center",
    dataIndex: 'studentName'
  },
  {
    title: '专业',
    align: "center",
    sorter: true,
    dataIndex: 'majorId_dictText'
  },
  {
    title: '班级',
    align: "center",
    sorter: true,
    dataIndex: 'classId_dictText'
  },
  {
    title: '状态',
    align: "center",
    sorter: true,
    dataIndex: 'status_dictText'
  },
  {
    title: '入学年份',
    align: "center",
    sorter: true,
    dataIndex: 'admissionYear'
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
    label: "学生姓名",
    field: "studentName",
    component: 'JInput',
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
    label: "状态",
    field: 'status',
    component: 'JSelectMultiple',
    componentProps: {
      dictCode: "use_state"
    },
  },
  {
    label: "入学年份",
    field: 'admissionYear',
    component: 'Input',
  },
];
//表单数据
export const formSchema: FormSchema[] = [
  {
    label: '学号',
    field: 'studentId',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入学号!' },
      ];
    },
  },
  {
    label: '学生姓名',
    field: 'studentName',
    component: 'Input',
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请输入学生姓名!' },
      ];
    },
  },
  {
    label: '专业',
    field: 'majorId',
    component: 'ApiSelect',
    componentProps: ({ formActionType }) => {
      return {
        api: getMajorList,
        labelField: 'majorName',
        valueField: 'id',
        placeholder: '请选择专业',
        onChange: async (value, formModel) => {
          if (value) {
            const result = await getClassListByMajor(value);
            if (result && result.length > 0) {
              const classOptions = result.map((item: any) => ({
                label: item.className,
                value: item.id,
              }));
              formActionType.updateSchema([
                {
                  field: 'classId',
                  component: 'Select',
                  componentProps: {
                    options: classOptions,
                    placeholder: '请选择班级',
                  },
                }]);
            } else {
              formActionType.updateSchema([
                {
                  field: 'classId',
                  component: 'Select',
                  componentProps: {
                    options: [],
                    placeholder: '该专业下无班级',
                  },
                }]);
            }
          } else {
            formActionType.updateSchema([
              {
                field: 'classId',
                component: 'Select',
                componentProps: {
                  options: [],
                  placeholder: '请先选择专业',
                },
              }]);
          }
          formModel.classId = null;
        },
      };
    },
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请选择专业!' },
      ];
    },
  },
  {
    label: '班级',
    field: 'classId',
    component: 'Select',
    componentProps: {
      placeholder: '请先选择专业',
      options: [],
    },
    dynamicRules: ({ model, schema }) => {
      return [
        { required: true, message: '请选择班级!' },
      ];
    },
  },
  {
    label: '状态',
    field: 'status',
    defaultValue: "1",
    component: 'JDictSelectTag',
    componentProps: {
      dictCode: "use_state",
    },
  },
  {
    label: '入学年份',
    field: 'admissionYear',
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
  studentId: { title: '学号', order: 0, view: 'text', type: 'string', },
  studentName: { title: '学生姓名', order: 1, view: 'text', type: 'string', },
  majorId: { title: '专业', order: 2, view: 'link_table', type: 'string', },
  classId: { title: '班级', order: 3, view: 'link_table', type: 'string', },
  status: { title: '状态', order: 4, view: 'list', type: 'string', dictCode: 'use_state', },
  admissionYear: { title: '入学年份', order: 5, view: 'text', type: 'string', },
};

/**
 * 流程表单调用这个方法获取formSchema
 * @param param
 */
export function getBpmFormSchema(_formData): FormSchema[] {
  // 默认和原始表单保持一致 如果流程中配置了权限数据，这里需要单独处理formSchema
  return formSchema;
}
