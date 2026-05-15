package org.jeecg.modules.demo.zbu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入结果 VO（用于返回给前端 201 弹窗）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultVO {
    /** 摘要信息 */
    private String msg;
    /** 错误文件下载相对路径 */
    private String fileUrl;
    /** 错误文件名 */
    private String fileName;
}
