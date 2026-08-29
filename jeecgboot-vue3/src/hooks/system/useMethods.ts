import { defHttp } from '/@/utils/http/axios';
import { useMessage } from '/@/hooks/web/useMessage';
import { useGlobSetting } from '/@/hooks/setting';

const { createMessage, createWarningModal } = useMessage();
const glob = useGlobSetting();

/**
 * 导出文件xlsx的mime-type
 */
export const XLSX_MIME_TYPE = 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet';
/**
 * 导出文件xlsx的文件后缀
 */
export const XLSX_FILE_SUFFIX = '.xlsx';

export function useMethods() {
  /**
   * 导出xls
   * @param name
   * @param url
   * @param params
   * @param isXlsx
   * @param timeout 超时时间（毫秒），默认 60000
   */
  async function exportXls(name, url, params, isXlsx = false, timeout = 900000) {
    // 修改为返回原生 response，便于获取 headers
    const response = await defHttp.get(
      { url: url, params: params, responseType: 'blob', timeout: timeout },
      { isTransformResponse: false, isReturnNativeResponse: true }
    );
    if (!response || !response.data) {
      createMessage.warning('文件下载失败');
      return;
    }
    // 判断 header 中 content-disposition 是否包含 .xlsx
    let isXlsxByHeader = isXlsx;
    const disposition = response.headers && response.headers['content-disposition'];
    if (disposition && disposition.indexOf('.xlsx') !== -1) {
      isXlsxByHeader = true;
    }
    const data = response.data;
    // 代码逻辑说明: 导出excel失败提示，不进行导出---
    let reader = new FileReader()
    reader.readAsText(data, 'utf-8')
    reader.onload = async () => {
      if(reader.result){
        if(reader.result.toString().indexOf("success") !=-1){
          // 代码逻辑说明: 【issues/7738】文件中带"success"导出报错 ---
          try {
            const { success, message } = JSON.parse(reader.result.toString());
            if (!success) {
              createMessage.warning('导出失败，失败原因：' + message);
            } else {
              exportExcel(name, isXlsxByHeader, data);
            }
            return;
          } catch (error) {
            exportExcel(name, isXlsxByHeader, data);
          }
        }
      }
      exportExcel(name, isXlsxByHeader, data);
    }
  }

  /**
   * 导入xls
   * @param data 导入的数据
   * @param url
   * @param success 成功后的回调
   */
  async function importXls(data, url, success) {
    const isReturn = (fileInfo) => {
      try {
        // 导入错误（201部分成功 / 500导入失败）统一弹窗+下载处理
        if (fileInfo.code === 201 || fileInfo.code === 500 || fileInfo.code === 510) {
          const msg = fileInfo.result?.msg || fileInfo.message || '导入出现错误';
          const fileUrl = fileInfo.result?.fileUrl;
          const fileName = fileInfo.result?.fileName;
          // uploadUrl 可能为空（webvpn 相对路径场景），兜底用 /jeecgboot 前缀，
          // 避免下载地址落成 /sys/common/static/... 被 nginx 当 SPA 路由回退成 HTML
          const href = fileUrl ? (glob.uploadUrl || '/jeecgboot') + fileUrl : '';

          if (href && fileName) {
            // 有错误文件可下载：弹窗+自动下载
            createWarningModal({
              title: fileInfo.message || '导入结果',
              centered: false,
              width: 500,
              content: `<div>
                <p><strong>${msg}</strong></p>
                <p>错误详情文件<a href="${href}" download="${fileName}"> ${fileName} </a>已自动下载，也可点击链接重新下载。</p>
              </div>`,
            });
            try {
              fetch(href)
                .then(res => res.blob())
                .then(blob => {
                  const url = window.URL.createObjectURL(blob);
                  const a = document.createElement('a');
                  a.href = url;
                  a.download = fileName;
                  document.body.appendChild(a);
                  a.click();
                  document.body.removeChild(a);
                  window.URL.revokeObjectURL(url);
                })
                .catch(e => console.warn('自动下载导入错误文件失败:', e));
            } catch (e) {
              console.warn('自动下载导入错误文件失败:', e);
            }
          } else {
            // 无错误文件（如意外异常）：弹窗展示错误信息
            createWarningModal({
              title: fileInfo.message || '导入失败',
              centered: false,
              width: 500,
              content: `<div><p><strong>${msg}</strong></p></div>`,
            });
          }
        } else {
          createMessage.success(fileInfo.message || `${data.file.name} 文件上传成功`);
        }
      } catch (error) {
        console.log('导入的数据异常', error);
      } finally {
        typeof success === 'function' ? success(fileInfo) : '';
      }
    };
    await defHttp.uploadFile({ url }, { file: data.file }, { success: isReturn });
  }

  return {
    handleExportXls: (name: string, url: string, params?: object, timeout?: number) => exportXls(name, url, params, false, timeout),
    handleImportXls: (data, url, success) => importXls(data, url, success),
    handleExportXlsx: (name: string, url: string, params?: object, timeout?: number) => exportXls(name, url, params, true, timeout),
  };

  /**
   * 导出excel
   * @param name
   * @param isXlsx
   * @param data
   */
  function exportExcel(name, isXlsx, data) {
    if (!name || typeof name != 'string') {
      name = '导出文件';
    }
    let blobOptions = { type: 'application/vnd.ms-excel' };
    let fileSuffix = '.xls';
    if (isXlsx) {
      blobOptions['type'] = XLSX_MIME_TYPE;
      fileSuffix = XLSX_FILE_SUFFIX;
    }
    if (typeof window.navigator.msSaveBlob !== 'undefined') {
      window.navigator.msSaveBlob(new Blob([data], blobOptions), name + fileSuffix);
    } else {
      let url = window.URL.createObjectURL(new Blob([data], blobOptions));
      let link = document.createElement('a');
      link.style.display = 'none';
      link.href = url;
      link.setAttribute('download', name + fileSuffix);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link); //下载完成移除元素
      window.URL.revokeObjectURL(url); //释放掉blob对象
    }
  }
}
