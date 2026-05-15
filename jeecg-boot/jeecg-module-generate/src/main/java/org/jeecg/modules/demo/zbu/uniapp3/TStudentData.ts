import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '学号',
    align:"center",
    sorter: true,
    dataIndex: 'studentId'
   },
   {
    title: '学生姓名',
    align:"center",
    dataIndex: 'studentName'
   },
   {
    title: '专业',
    align:"center",
    sorter: true,
    dataIndex: 'majorId'
   },
   {
    title: '班级',
    align:"center",
    sorter: true,
    dataIndex: 'classId'
   },
   {
    title: '状态',
    align:"center",
    sorter: true,
    dataIndex: 'status_dictText'
   },
   {
    title: '入学年份',
    align:"center",
    sorter: true,
    dataIndex: 'admissionYear'
   },
];