package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;
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
 * @Description: 领取表
 * @Author: jeecg-boot
 * @Date:   2026-01-23
 * @Version: V1.0
 */
@Data
@TableName("t_receive")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="领取表")
public class TReceive implements Serializable {
    private static final long serialVersionUID = 1L;

    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
    /**领取操作人*/
    @Excel(name = "领取操作人", width = 15, dictTable = "t_student", dicCode = "id", dicText = "student_id")
    @Dict(dictTable = "t_student", dicCode = "id", dicText = "student_id")
    @Schema(description = "领取操作人")
    private java.lang.String receiveOperator;
    /**学生姓名*/
    @TableField(exist = false)
    @Excel(name = "学生姓名", width = 15)
    @Schema(description = "学生姓名")
    private java.lang.String studentName;
    /**教材*/
    @Excel(name = "教材", width = 15, dictTable = "t_subscription", dicCode = "id", dicText = "textbook_id")
    @Dict(dictTable = "t_subscription", dicCode = "id", dicText = "textbook_id")
    @Schema(description = "教材")
    private java.lang.String subscriptionId;
    /**教材名称*/
    @TableField(exist = false)
    @Excel(name = "教材名称", width = 15)
    @Schema(description = "教材名称")
    private java.lang.String textbookName;
    /**领取状态*/
    @Excel(name = "领取状态", width = 15, dicCode = "receive_status")
    @Dict(dicCode = "receive_status")
    @Schema(description = "领取状态")
    private java.lang.String receiveStatus;
    /**领取时间*/
    @Excel(name = "领取时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @Schema(description = "领取时间")
    private java.util.Date receiveTime;
    /**领取备注*/
    @Excel(name = "领取备注", width = 15)
    @Schema(description = "领取备注")
    private java.lang.String receiveRemark;
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
    //    @TableField(exist = false)
    @Excel(name = "学院", width = 20)
    private String collegeName;
}
