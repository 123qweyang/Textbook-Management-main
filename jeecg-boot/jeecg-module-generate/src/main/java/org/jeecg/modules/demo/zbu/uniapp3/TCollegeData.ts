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
    title: '学院编码',
    align:"center",
    sorter: true,
    dataIndex: 'collegeCode'
   },
];