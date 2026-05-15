package org.jeecg.modules.demo.zbu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导入错误详情 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImportErrorVO {
    /** 行号 */
    private Integer rowNum;
    /** 列名 */
    private String columnName;
    /** 字段名 */
    private String fieldName;
    /** 错误原因 */
    private String errorReason;
}
