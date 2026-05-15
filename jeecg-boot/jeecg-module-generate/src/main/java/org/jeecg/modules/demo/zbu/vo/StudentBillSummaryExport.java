package org.jeecg.modules.demo.zbu.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 账单汇总导出实体
 * @Author: jeecg-boot
 * @Date:   2026-04-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="账单汇总导出")
public class StudentBillSummaryExport implements Serializable {
    private static final long serialVersionUID = 1L;

    /**学号*/
    @Excel(name = "学号", width = 15)
    @Schema(description = "学号")
    private java.lang.String studentNo;

    /**学生姓名*/
    @Excel(name = "姓名", width = 15)
    @Schema(description = "学生姓名")
    private java.lang.String studentName;

    /**学院*/
    @Excel(name = "学院", width = 15)
    @Schema(description = "学院")
    private java.lang.String collegeName;

    /**专业*/
    @Excel(name = "专业", width = 15)
    @Schema(description = "专业")
    private java.lang.String majorName;

    /**班级*/
    @Excel(name = "班级", width = 15)
    @Schema(description = "班级")
    private java.lang.String className;

    /**第一学期费用*/
    @Excel(name = "第一学期（费用）", width = 20)
    @Schema(description = "第一学期费用")
    private java.math.BigDecimal firstSemesterFee;

    /**第二学期费用*/
    @Excel(name = "第二学期（费用）", width = 20)
    @Schema(description = "第二学期费用")
    private java.math.BigDecimal secondSemesterFee;

    /**总费用*/
    @Excel(name = "总费用", width = 15)
    @Schema(description = "总费用")
    private java.math.BigDecimal totalFee;
}
