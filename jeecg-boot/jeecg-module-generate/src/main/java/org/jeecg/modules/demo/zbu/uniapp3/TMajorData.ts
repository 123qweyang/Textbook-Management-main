import { render } from '@/common/renderUtils';
//列表数据
export const columns = [
    {
    title: '专业名称',
    align:"center",
    sorter: true,
    dataIndex: 'majorName'
   },
   {
    title: '专业编码',
    align:"center",
    sorter: true,
    dataIndex: 'majorCode'
   },
   {
    title: '所属学院',
    align:"center",
    dataIndex: 'collegeId'
   },
];