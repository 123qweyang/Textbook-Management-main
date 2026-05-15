package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import org.jeecg.common.constant.ProvinceCityArea;
import org.jeecg.common.util.SpringContextUtils;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 班级表
 * @Author: jeecg-boot
 * @Date:   2026-01-26
 * @Version: V1.0
 */
@Data
@TableName("t_class")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="班级表")
public class TClass implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
    /**班级名称*/
    @Excel(name = "班级名称", width = 15)
    @Schema(description = "班级名称")
    private java.lang.String className;
    /**班级编码*/
    @Excel(name = "班级编码", width = 15)
    @Schema(description = "班级编码")
    private java.lang.String classCode;
    /**所属专业*/
    @Excel(name = "所属专业", width = 15, dictTable = "t_major", dicCode = "id", dicText = "major_name")
    @Dict(dictTable = "t_major", dicCode = "id", dicText = "major_name")
    @Schema(description = "所属专业")
    private java.lang.String majorId;
    /**辅导员*/
    @Excel(name = "辅导员", width = 15, dictTable = "t_counselor", dicCode = "id", dicText = "counselor_name")
    @Dict(dictTable = "t_counselor", dicCode = "id", dicText = "counselor_name")
    @Schema(description = "辅导员")
    private java.lang.String counselorId;
    /**辅导员名称（用于搜索，不映射数据库）*/
    @TableField(exist = false)
    @Schema(description = "辅导员名称")
    private java.lang.String counselorName;
    /**辅导员工号*/
    @TableField(exist = false)
    @Schema(description = "辅导员工号")
    private java.lang.String counselorNo;
    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private java.util.Date createTime;
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private java.util.Date updateTime;
}
