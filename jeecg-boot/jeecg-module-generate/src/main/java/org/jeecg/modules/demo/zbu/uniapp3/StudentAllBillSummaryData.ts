import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '学院名称',
    align:"center",
    sorter: true,
    dataIndex: 'collegeName'
   },
   {
    title: '专业名称',
    align:"center",
    sorter: true,
    dataIndex: 'majorName'
   },
   {
    title: '征订学年',
    align:"center",
    sorter: true,
    dataIndex: 'subscriptionYear'
   },
   {
    title: '征订学期',
    align:"center",
    sorter: true,
    dataIndex: 'subscriptionSemester'
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