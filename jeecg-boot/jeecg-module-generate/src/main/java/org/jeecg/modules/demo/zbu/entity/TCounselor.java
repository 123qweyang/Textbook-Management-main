package org.jeecg.modules.demo.zbu.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
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
 * @Description: 辅导员表
 * @Author: jeecg-boot
 * @Date:   2026-02-02
 * @Version: V1.0
 */
@Data
@TableName("t_counselor")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description="辅导员表")
public class TCounselor implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @Schema(description = "主键")
    private java.lang.String id;
	/**辅导员工号*/
	@Excel(name = "辅导员工号", width = 15)
    @Schema(description = "辅导员工号")
    private java.lang.String counselorId;
	/**辅导员姓名*/
	@Excel(name = "辅导员姓名", width = 15)
    @Schema(description = "辅导员姓名")
    private java.lang.String counselorName;
	/**所属学院*/
	@Excel(name = "所属学院", width = 15, dictTable = "t_college", dicCode = "id", dicText = "college_name")
	@Dict(dictTable = "t_college", dicCode = "id", dicText = "college_name")
    @Schema(description = "所属学院")
    private java.lang.String collegeId;
	/**联系方式*/
	@Excel(name = "联系方式", width = 15)
    @Schema(description = "联系方式")
    private java.lang.String contact;
	/**状态*/
	@Excel(name = "状态", width = 15, dicCode = "employed")
	@Dict(dicCode = "employed")
    @Schema(description = "状态")
    private java.lang.String status;
	/**账号ID*/
    @Schema(description = "账号ID")
    private java.lang.String userId;
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
