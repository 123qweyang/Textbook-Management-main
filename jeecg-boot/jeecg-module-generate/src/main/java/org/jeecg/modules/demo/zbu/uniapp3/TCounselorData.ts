import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '辅导员工号',
    align:"center",
    sorter: true,
    dataIndex: 'counselorId'
   },
   {
    title: '辅导员姓名',
    align:"center",
    sorter: true,
    dataIndex: 'counselorName'
   },
   {
    title: '所属学院',
    align:"center",
    dataIndex: 'collegeId'
   },
   {
    title: '联系方式',
    align:"center",
    dataIndex: 'contact'
   },
   {
    title: '状态',
    align:"center",
    dataIndex: 'status_dictText'
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