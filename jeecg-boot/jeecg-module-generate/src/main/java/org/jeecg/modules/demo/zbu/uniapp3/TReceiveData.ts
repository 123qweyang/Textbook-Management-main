import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '领取操作人',
    align:"center",
    sorter: true,
    dataIndex: 'receiveOperator'
   },
   {
    title: '教材',
    align:"center",
    dataIndex: 'subscriptionId'
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