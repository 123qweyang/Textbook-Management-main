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
    title: '专业',
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
    title: '教材名称',
    align:"center",
    dataIndex: 'textbookName'
   },
   {
    title: '教材定价',
    align:"center",
    dataIndex: 'price'
   },
   {
    title: '教材费用',
    align:"center",
    dataIndex: 'discountPrice'
   },
   {
    title: '征订状态',
    align:"center",
    sorter: true,
    dataIndex: 'subscribeStatus'
   },
   {
    title: '领取状态',
    align:"center",
    sorter: true,
    dataIndex: 'receiveStatus'
   },
   {
    title: '备注',
    align:"center",
    dataIndex: 'remark'
   },
];