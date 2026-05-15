package org.jeecg.modules.demo.zbu.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.*;
import org.jeecg.modules.demo.zbu.service.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * @Description: 总账单（数据来源：个人账单表）
 * @Author: jeecg-boot
 * @Date:   2026-01-28
 * @Version: V1.0
 */
@Tag(name="总账单")
@RestController
@RequestMapping("/zbu/studentAllBillSummary")
@Slf4j
public class StudentAllBillSummaryController extends JeecgController<StudentAllBillSummary, IStudentAllBillSummaryService> {
	@Autowired
	private IStudentAllBillSummaryService studentAllBillSummaryService;
	@Autowired
	private IStudentBillService studentBillService;
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private ITCollegeService tCollegeService;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 核心修改：从个人账单表汇总总账单数据
	 * 汇总维度：学院ID + 学院名称 + 专业ID + 专业名称 + 征订学年 + 征订学期
	 * 汇总指标：学生数（去重学号）、教材数（记录数）、原价总价、折后总价
	 */
	@Transactional(rollbackFor = Exception.class)
	public void autoSummarySubscriptionData() {
		try {
			// 1. 清空旧的汇总数据
			studentAllBillSummaryService.remove(new QueryWrapper<>());
			log.info("【自动汇总】已清空旧的总账单数据");

			// 2. 查询所有有效的个人账单记录（过滤关键字段为空的记录）
			QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
			billWrapper.isNotNull("student_id")        // 学号非空
					.isNotNull("major_name")          // 专业名称非空
					.isNotNull("subscription_year")   // 征订学年非空
					.isNotNull("subscription_semester")// 征订学期非空
					.isNotNull("price")                // 教材定价非空
					.isNotNull("discount_price");      // 折后价格非空
			List<StudentBill> billList = studentBillService.list(billWrapper);

			if (billList.isEmpty()) {
				log.warn("【自动汇总】未查询到有效个人账单记录，汇总结束");
				return;
			}
			log.info("【自动汇总】查询到{}条有效个人账单记录", billList.size());

			// 3. 按「学院ID + 专业ID + 学年 + 学期」分组（核心分组逻辑）
			Map<String, List<StudentBill>> groupMap = billList.stream().collect(Collectors.groupingBy(bill -> {
				// 3.1 通过专业名称查询专业信息（获取专业ID和学院ID）
				String majorId = "";
				String collegeId = "";
				if (!oConvertUtils.isEmpty(bill.getMajorName())) {
					QueryWrapper<TMajor> majorWrapper = new QueryWrapper<>();
					majorWrapper.eq("major_name", bill.getMajorName().trim());
					TMajor major = tMajorService.getOne(majorWrapper);
					if (major != null) {
						majorId = major.getId();
						collegeId = major.getCollegeId();
					}
				}

				// 3.2 构造分组key：学院ID_专业ID_学年_学期
				return String.join("_",
						oConvertUtils.isEmpty(collegeId) ? "未知学院" : collegeId,
						oConvertUtils.isEmpty(majorId) ? "未知专业" : majorId,
						oConvertUtils.isEmpty(bill.getSubscriptionYear()) ? "未知学年" : bill.getSubscriptionYear(),
						oConvertUtils.isEmpty(bill.getSubscriptionSemester()) ? "未知学期" : bill.getSubscriptionSemester()
				);
			}));
			log.info("【自动汇总】共分为{}个汇总分组", groupMap.size());

			// 4. 遍历分组，计算汇总数据
			List<StudentAllBillSummary> summaryList = new ArrayList<>();
			for (Map.Entry<String, List<StudentBill>> entry : groupMap.entrySet()) {
				String[] keyArr = entry.getKey().split("_");
				String collegeId = keyArr.length > 0 ? keyArr[0] : "未知学院";
				String majorId = keyArr.length > 1 ? keyArr[1] : "未知专业";
				String year = keyArr.length > 2 ? keyArr[2] : "未知学年";
				String semester = keyArr.length > 3 ? keyArr[3] : "未知学期";
				List<StudentBill> groupBillList = entry.getValue();

				// 4.1 组装基础信息
				StudentAllBillSummary summary = new StudentAllBillSummary();
				summary.setCollegeId(collegeId);
				summary.setMajorId(majorId);
				summary.setSubscriptionYear(year);
				summary.setSubscriptionSemester(semester);

				// 4.2 补充学院名称
				if (!"未知学院".equals(collegeId)) {
					TCollege college = tCollegeService.getById(collegeId);
					summary.setCollegeName(college != null ? college.getCollegeName() : "未知学院");
				} else {
					summary.setCollegeName("未知学院");
				}

				// 4.3 补充专业名称（从分组第一条记录取，同分组专业名称一致）
				if (!"未知专业".equals(majorId)) {
					TMajor major = tMajorService.getById(majorId);
					summary.setMajorName(major != null ? major.getMajorName() : groupBillList.get(0).getMajorName());
				} else {
					summary.setMajorName(groupBillList.get(0).getMajorName());
				}

				// 4.4 计算核心汇总指标
				// ① 学生数：去重学号数量
				Set<String> studentIdSet = groupBillList.stream()
						.map(StudentBill::getStudentId)
						.collect(Collectors.toSet());
				summary.setStudentCount(Long.valueOf(studentIdSet.size()));

				// ② 教材数：该分组下的记录总数（每条记录对应一本教材）
				summary.setTextbookCount(Long.valueOf(groupBillList.size()));

				// ③ 原价总价：price字段求和
				BigDecimal originalTotal = groupBillList.stream()
						.map(StudentBill::getPrice)
						.filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				summary.setOriginalTotal(originalTotal.setScale(2, BigDecimal.ROUND_HALF_UP));

				// ④ 折后总价：discountPrice字段求和
				BigDecimal discountTotal = groupBillList.stream()
						.map(StudentBill::getDiscountPrice)
						.filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				summary.setDiscountTotal(discountTotal.setScale(2, BigDecimal.ROUND_HALF_UP));

				// 4.5 补充时间字段
				summary.setCreateTime(new Date());
				summary.setUpdateTime(new Date());

				summaryList.add(summary);
			}

			// 5. 批量保存汇总数据
			if (!summaryList.isEmpty()) {
				studentAllBillSummaryService.saveBatch(summaryList);
				log.info("【自动汇总】总账单生成完成，共{}条汇总记录", summaryList.size());
			}

		} catch (Exception e) {
			log.error("【自动汇总】总账单生成失败", e);
			throw new RuntimeException("总账单自动汇总失败：" + e.getMessage());
		}
	}

	// ========== 定时任务：每天凌晨0点自动汇总（兜底保障） ==========
	@Scheduled(cron = "0 0 0 * * ?")
	public void scheduleSummary() {
		log.info("【定时汇总】开始执行总账单定时汇总任务");
		autoSummarySubscriptionData();
	}

	// ========== 手动触发汇总接口（用于测试/手动兜底） ==========
	@AutoLog(value = "总账单-手动汇总个人账单数据")
	@Operation(summary="总账单-手动汇总个人账单数据")
	@GetMapping(value = "/summarySubscriptionData")
	public Result<String> manualSummary() {
		autoSummarySubscriptionData();
		return Result.OK("手动汇总总账单成功！");
	}

	// ========== 以下为原有基础接口（无需修改） ==========
	@Operation(summary="总账单-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<StudentAllBillSummary>> queryPageList(StudentAllBillSummary studentAllBillSummary,
															  @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
															  @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
															  HttpServletRequest req) {
		QueryWrapper<StudentAllBillSummary> queryWrapper = QueryGenerator.initQueryWrapper(studentAllBillSummary, req.getParameterMap());
		Page<StudentAllBillSummary> page = new Page<>(pageNo, pageSize);
		IPage<StudentAllBillSummary> pageList = studentAllBillSummaryService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	@AutoLog(value = "总账单-添加")
	@Operation(summary="总账单-添加")
	@RequiresPermissions("zbu:student_all_bill_summary:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody StudentAllBillSummary studentAllBillSummary) {
		studentAllBillSummaryService.save(studentAllBillSummary);
		return Result.OK("添加成功！");
	}

	@AutoLog(value = "总账单-编辑")
	@Operation(summary="总账单-编辑")
	@RequiresPermissions("zbu:student_all_bill_summary:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody StudentAllBillSummary studentAllBillSummary) {
		studentAllBillSummaryService.updateById(studentAllBillSummary);
		return Result.OK("编辑成功!");
	}

	@AutoLog(value = "总账单-通过id删除")
	@Operation(summary="总账单-通过id删除")
	@RequiresPermissions("zbu:student_all_bill_summary:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		studentAllBillSummaryService.removeById(id);
		return Result.OK("删除成功!");
	}

	@AutoLog(value = "总账单-批量删除")
	@Operation(summary="总账单-批量删除")
	@RequiresPermissions("zbu:student_all_bill_summary:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.studentAllBillSummaryService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	@Operation(summary="总账单-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<StudentAllBillSummary> queryById(@RequestParam(name="id",required=true) String id) {
		StudentAllBillSummary studentAllBillSummary = studentAllBillSummaryService.getById(id);
		if(studentAllBillSummary==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(studentAllBillSummary);
	}

	@RequiresPermissions("zbu:student_all_bill_summary:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, StudentAllBillSummary studentAllBillSummary) {
		return super.exportXls(request, studentAllBillSummary, StudentAllBillSummary.class, "总账单");
	}

	@RequiresPermissions("zbu:student_all_bill_summary:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<StudentAllBillSummary> list = ExcelImportUtil.importExcel(file.getInputStream(), StudentAllBillSummary.class, params);
				long start = System.currentTimeMillis();
				service.saveBatch(list);
				log.info("消耗时间" + (System.currentTimeMillis() - start) + "毫秒");
				return Result.ok("文件导入成功！数据行数：" + list.size());
			} catch (Exception e) {
				String msg = e.getMessage();
				log.error(msg, e);
				List<String> failMsgList = new ArrayList<>();
				if (msg != null && msg.indexOf("Duplicate entry") >= 0) {
					failMsgList.add("存在重复数据：" + msg);
				} else {
					failMsgList.add("导入异常：" + msg);
				}
				return ImportErrorExportUtil.buildErrorResult(failMsgList, "导入完成！成功导入0条有效数据", uploadPath, "总账单");
			} finally {
				try { file.getInputStream().close(); } catch (IOException e) { e.printStackTrace(); }
			}
		}
		return Result.error("文件导入失败！");
	}


	@Transactional(rollbackFor = Exception.class)
	public void incrementSummary(StudentBill bill, boolean isDelete) {
		try {
			// 1. 获取当前个人账单的核心分组维度（学院ID、专业ID、学年、学期）
			String majorName = bill.getMajorName();
			String subscriptionYear = bill.getSubscriptionYear();
			String subscriptionSemester = bill.getSubscriptionSemester();

			// 2. 通过专业名称查询专业和学院信息（和全量汇总逻辑一致）
			String majorId = "";
			String collegeId = "";
			if (!oConvertUtils.isEmpty(majorName)) {
				QueryWrapper<TMajor> majorWrapper = new QueryWrapper<>();
				majorWrapper.eq("major_name", majorName.trim());
				TMajor major = tMajorService.getOne(majorWrapper);
				if (major != null) {
					majorId = major.getId();
					collegeId = major.getCollegeId();
				}
			}

			// 3. 构造分组Key（和全量汇总的分组Key一致，确保对应同一个汇总记录）
			String groupKey = String.join("_",
					oConvertUtils.isEmpty(collegeId) ? "未知学院" : collegeId,
					oConvertUtils.isEmpty(majorId) ? "未知专业" : majorId,
					oConvertUtils.isEmpty(subscriptionYear) ? "未知学年" : subscriptionYear,
					oConvertUtils.isEmpty(subscriptionSemester) ? "未知学期" : subscriptionSemester
			);

			// 4. 如果是删除操作：先删除该分组的旧汇总记录（避免删除后汇总数据残留）
			if (isDelete) {
				QueryWrapper<StudentAllBillSummary> delWrapper = new QueryWrapper<>();
				delWrapper.eq("college_id", oConvertUtils.isEmpty(collegeId) ? "未知学院" : collegeId)
						.eq("major_id", oConvertUtils.isEmpty(majorId) ? "未知专业" : majorId)
						.eq("subscription_year", subscriptionYear)
						.eq("subscription_semester", subscriptionSemester);
				studentAllBillSummaryService.remove(delWrapper);
				log.info("【增量汇总-删除】已删除分组Key:{}的旧汇总记录", groupKey);
			}

			// 5. 查询该分组下的所有个人账单，重新计算汇总数据
			QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
			billWrapper.eq("major_name", majorName)
					.eq("subscription_year", subscriptionYear)
					.eq("subscription_semester", subscriptionSemester)
					.isNotNull("student_id")
					.isNotNull("price")
					.isNotNull("discount_price");
			List<StudentBill> groupBillList = studentBillService.list(billWrapper);

			// 6. 如果该分组还有个人账单，重新生成汇总记录；否则不生成（避免空分组）
			if (!groupBillList.isEmpty()) {
				StudentAllBillSummary summary = new StudentAllBillSummary();
				summary.setCollegeId(oConvertUtils.isEmpty(collegeId) ? "未知学院" : collegeId);
				summary.setMajorId(oConvertUtils.isEmpty(majorId) ? "未知专业" : majorId);
				summary.setSubscriptionYear(subscriptionYear);
				summary.setSubscriptionSemester(subscriptionSemester);

				// 补充学院/专业名称
				if (!"未知学院".equals(collegeId)) {
					TCollege college = tCollegeService.getById(collegeId);
					summary.setCollegeName(college != null ? college.getCollegeName() : "未知学院");
				} else {
					summary.setCollegeName("未知学院");
				}
				if (!"未知专业".equals(majorId)) {
					TMajor major = tMajorService.getById(majorId);
					summary.setMajorName(major != null ? major.getMajorName() : majorName);
				} else {
					summary.setMajorName(majorName);
				}

				// 重新计算汇总指标
				Set<String> studentIdSet = groupBillList.stream().map(StudentBill::getStudentId).collect(Collectors.toSet());
				summary.setStudentCount(Long.valueOf(studentIdSet.size()));
				summary.setTextbookCount(Long.valueOf(groupBillList.size()));

				BigDecimal originalTotal = groupBillList.stream()
						.map(StudentBill::getPrice)
						.filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				summary.setOriginalTotal(originalTotal.setScale(2, BigDecimal.ROUND_HALF_UP));

				BigDecimal discountTotal = groupBillList.stream()
						.map(StudentBill::getDiscountPrice)
						.filter(Objects::nonNull)
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				summary.setDiscountTotal(discountTotal.setScale(2, BigDecimal.ROUND_HALF_UP));

				summary.setCreateTime(new Date());
				summary.setUpdateTime(new Date());

				// 保存汇总记录（如果已存在则更新，不存在则新增）
				QueryWrapper<StudentAllBillSummary> existWrapper = new QueryWrapper<>();
				existWrapper.eq("college_id", summary.getCollegeId())
						.eq("major_id", summary.getMajorId())
						.eq("subscription_year", summary.getSubscriptionYear())
						.eq("subscription_semester", summary.getSubscriptionSemester());
				if (studentAllBillSummaryService.count(existWrapper) > 0) {
					// 更新已有记录
					StudentAllBillSummary oldSummary = studentAllBillSummaryService.getOne(existWrapper);
					summary.setId(oldSummary.getId()); // 复用主键ID
					studentAllBillSummaryService.updateById(summary);
					log.info("【增量汇总-更新】分组Key:{}的汇总记录已更新", groupKey);
				} else {
					// 新增记录
					studentAllBillSummaryService.save(summary);
					log.info("【增量汇总-新增】分组Key:{}的汇总记录已生成", groupKey);
				}
			} else {
				log.info("【增量汇总】分组Key:{}已无个人账单，不生成汇总记录", groupKey);
			}

		} catch (Exception e) {
			log.error("【增量汇总】实时更新总账单失败", e);
			throw new RuntimeException("实时更新总账单失败：" + e.getMessage());
		}
	}

	/**
	 * 批量删除时调用：传入分组维度，批量重新计算
	 */
	@Transactional(rollbackFor = Exception.class)
	public void batchIncrementSummary(String majorName, String subscriptionYear, String subscriptionSemester) {
		StudentBill dummyBill = new StudentBill();
		dummyBill.setMajorName(majorName);
		dummyBill.setSubscriptionYear(subscriptionYear);
		dummyBill.setSubscriptionSemester(subscriptionSemester);
		this.incrementSummary(dummyBill, true);
	}
}