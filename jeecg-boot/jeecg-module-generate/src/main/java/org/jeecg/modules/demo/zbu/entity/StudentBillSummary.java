package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 账单汇总页面展示实体
 * @Author: jeecg-boot
 * @Date:   2026-04-25
 * @Version: V1.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Schema(description="账单汇总")
public class StudentBillSummary implements Serializable {
    private static final long serialVersionUID = 1L;

    /**学生ID*/
    @TableField(exist = false)
    private java.lang.String studentId;

    /**学号*/
    @Schema(description = "学号")
    private java.lang.String studentNo;

    /**学生姓名*/
    @Schema(description = "学生姓名")
    private java.lang.String studentName;

    /**班级*/
    @Schema(description = "班级")
    private java.lang.String className;

    /**学院*/
    @Schema(description = "学院")
    private java.lang.String collegeName;

    /**专业*/
    @Schema(description = "专业")
    private java.lang.String majorName;

    /**征订学年*/
    @Schema(description = "征订学年")
    private java.lang.String schoolYear;

    /**第一学期费用*/
    @Schema(description = "第一学期费用")
    private java.math.BigDecimal firstSemesterFee;

    /**第二学期费用*/
    @Schema(description = "第二学期费用")
    private java.math.BigDecimal secondSemesterFee;

    /**总费用*/
    @Schema(description = "总费用")
    private java.math.BigDecimal totalFee;
}
