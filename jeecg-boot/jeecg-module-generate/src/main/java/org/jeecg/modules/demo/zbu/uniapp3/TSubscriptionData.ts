import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '学生',
    align:"center",
    sorter: true,
    dataIndex: 'studentId'
   },
   {
    title: '教材',
    align:"center",
    dataIndex: 'textbookId'
   },
   {
    title: '教材选用',
    align:"center",
    sorter: true,
    dataIndex: 'selectionId'
   },
   {
    title: '专业',
    align:"center",
    sorter: true,
    dataIndex: 'majorId'
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
    dataIndex: 'subscriptionSemester_dictText'
   },
   {
    title: '征订状态',
    align:"center",
    sorter: true,
    dataIndex: 'subscribeStatus_dictText'
   },
   {
    title: '征订备注',
    align:"center",
    dataIndex: 'remark'
   },
   {
    title: '征订操作时间',
    align:"center",
    dataIndex: 'subscribeTime'
   },
   {
    title: '征订截止时间',
    align:"center",
    dataIndex: 'deadline'
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