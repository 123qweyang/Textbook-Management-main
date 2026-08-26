package org.jeecg.modules.demo.zbu.controller;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.*;
import org.jeecg.modules.demo.zbu.mapper.TStudentMapper;
import org.jeecg.modules.demo.zbu.service.*;

import org.jeecg.common.system.vo.LoginUser;
import org.apache.shiro.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.view.JeecgMapExcelView;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.jeecg.modules.demo.zbu.util.SemesterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
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
 * @Description: 征订表
 * @Author: jeecg-boot
 * @Date: 2026-01-19
 * @Version: V1.0
 */
@Tag(name = "征订表")
@RestController
@RequestMapping("/zbu/tSubscription")
@Slf4j
public class TSubscriptionController extends JeecgController<TSubscription, ITSubscriptionService> {
	@Autowired
	private ITSubscriptionService tSubscriptionService;
	@Autowired
	private ITStudentService tStudentService;
	@Autowired
	private ITReceiveService tReceiveService;
	@Autowired
	private IStudentBillService studentBillService;
	@Autowired
	private ITTextbookService tTextbookService;
	@Autowired
	private ITClassService tClassService;
	@Autowired
	private ITCounselorService tCounselorService;
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private StudentAllBillSummaryController studentAllBillSummaryController;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ITCollegeService tCollegeService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	// 角色编码常量
	private static final String ADMIN_ROLE_CODE = "admin";
	private static final String COUNSELOR_ROLE_CODE = "counselor";
	private static final String STUDENT_ROLE_CODE = "student";

	// 学生级锁，防止同一学生并发创建领取记录导致写偏斜
	private final ConcurrentHashMap<String, Object> receiveCreateLocks = new ConcurrentHashMap<>();

	/**
	 * 分页列表查询
	 *
	 * @param tSubscription
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	// @AutoLog(value = "征订表-分页列表查询")
	@Operation(summary = "征订表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TSubscription>> queryPageList(TSubscription tSubscription,
													  @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
													  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
													  HttpServletRequest req) {

		// 调试：打印前端传入的征订状态参数
		String subscribeStatusParam = req.getParameter("subscribeStatus");
		log.info("征订列表查询 - 前端传入subscribeStatus参数值：{}", subscribeStatusParam);
		log.info("征订列表查询 - 实体类subscribeStatus字段值：{}", tSubscription.getSubscribeStatus());

		// 自定义查询规则
		Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
		// 自定义多选的查询规则为：LIKE_WITH_OR
		customeRuleMap.put("subscriptionSemester", QueryRuleEnum.LIKE_WITH_OR);
		// 征订状态改为精确匹配（EQ），避免 LIKE 匹配错误
		customeRuleMap.put("subscribeStatus", QueryRuleEnum.EQ);
		QueryWrapper<TSubscription> queryWrapper = QueryGenerator.initQueryWrapper(tSubscription, req.getParameterMap(),
				customeRuleMap);

		// 调试：打印生成的SQL
		log.info("征订列表查询 - 生成的SQL条件：{}", queryWrapper.getCustomSqlSegment());

		// 学院模糊查询
		String collegeName = req.getParameter("collegeName");
		if (oConvertUtils.isNotEmpty(collegeName)) {
			// 仿照学生表的查询逻辑：通过 major_id 关联专业表，然后关联学院表
			queryWrapper.inSql("major_id",
					"SELECT id FROM t_major WHERE college_id IN (SELECT id FROM t_college WHERE college_name LIKE CONCAT('%', '"
							+ collegeName + "', '%'))");
		}

		Page<TSubscription> page = new Page<TSubscription>(pageNo, pageSize);
		IPage<TSubscription> pageList = tSubscriptionService.page(page, queryWrapper);
		log.info("征订列表查询 - 总记录数：{}", pageList.getTotal());
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param tSubscription
	 * @return
	 */
	@AutoLog(value = "征订表-添加")
	@Operation(summary = "征订表-添加")
	@RequiresPermissions("zbu:t_subscription:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TSubscription tSubscription) {
		// 1. 保存征订表数据
		tSubscriptionService.save(tSubscription);

		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param tSubscription
	 * @return
	 */
	@AutoLog(value = "征订表-编辑")
	@Operation(summary = "征订表-编辑")
	@RequiresPermissions("zbu:t_subscription:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	@Transactional(rollbackFor = Exception.class)
	public Result<String> edit(@RequestBody TSubscription tSubscription) {
		try {
			String subscriptionId = tSubscription.getId();
			TSubscription oldSubscription = tSubscriptionService.getById(subscriptionId);
			if (oldSubscription == null) {
				return Result.error("未找到待编辑的征订记录，ID：" + subscriptionId);
			}

			String oldStatus = oldSubscription.getSubscribeStatus();
			String newStatus = tSubscription.getSubscribeStatus();

			boolean isUpdateSuccess = tSubscriptionService.updateById(tSubscription);
			if (!isUpdateSuccess) {
				return Result.error("更新失败");
			}

			// 同步更新个人账单中的征订状态
			TStudent student = tStudentService.getById(oldSubscription.getStudentId());
			String studentNo = student != null ? student.getStudentId() : null;

			TTextbook textbook = tTextbookService.getById(oldSubscription.getTextbookId());
			String textbookName = textbook != null ? textbook.getTextbookName() : null;

			if (studentNo != null && textbookName != null) {
				// 根据学号、学年、学期、教材名称查询个人账单
				QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
				billWrapper.eq("student_id", studentNo)
						.eq("subscription_year", oldSubscription.getSubscriptionYear())
						.eq("subscription_semester", oldSubscription.getSubscriptionSemester())
						.eq("textbook_name", textbookName);

				StudentBill existingBill = studentBillService.getOne(billWrapper);
				if (existingBill != null) {
					// 更新个人账单的征订状态
					StudentBill billUpdate = new StudentBill();
					billUpdate.setId(existingBill.getId());
					billUpdate.setSubscribeStatus(newStatus);
					billUpdate.setUpdateTime(new Date());
					studentBillService.updateById(billUpdate);
					log.info("编辑征订记录时同步更新个人账单征订状态，账单ID={}，新状态={}", existingBill.getId(), newStatus);
				}
			}

			// 当征订状态从非"已征订"改为"已征订"时，生成领取记录
			if (!"1".equals(oldStatus) && "1".equals(newStatus)) {
				QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
				receiveWrapper.eq("subscription_id", subscriptionId);
				if (tReceiveService.count(receiveWrapper) == 0) {
					TReceive receive = new TReceive();
					receive.setReceiveOperator(tSubscription.getStudentId());
					receive.setSubscriptionId(subscriptionId);
					receive.setReceiveStatus("未领取");
					receive.setReceiveRemark("");
					receive.setCreateTime(new Date());
					receive.setUpdateTime(new Date());
					TMajor major = tMajorService.getById(tSubscription.getMajorId());
					if (major != null) {
						TCollege college = tCollegeService.getById(major.getCollegeId());
						if (college != null) {
							receive.setCollegeName(college.getCollegeName());
						}
					}
			receive.setSubscriptionYear(tSubscription.getSubscriptionYear());
			receive.setSubscriptionSemester(tSubscription.getSubscriptionSemester());
			tReceiveService.save(receive);
					log.info("编辑征订状态为已征订，创建领取记录成功，征订ID：{}", subscriptionId);
					return Result.OK("编辑成功！并已创建领取记录，同步更新个人账单");
				}
			}

			// Cascade delete: when subscribeStatus changes from 1 to 0, delete receive and bill
			if ("1".equals(oldStatus) && !"1".equals(newStatus)) {
				QueryWrapper<TReceive> receiveDelWrapper = new QueryWrapper<>();
				receiveDelWrapper.eq("subscription_id", subscriptionId);
				tReceiveService.remove(receiveDelWrapper);
				if (studentNo != null && textbookName != null) {
					QueryWrapper<StudentBill> billDelWrapper = new QueryWrapper<>();
					billDelWrapper.eq("student_id", studentNo)
							.eq("subscription_year", oldSubscription.getSubscriptionYear())
							.eq("subscription_semester", oldSubscription.getSubscriptionSemester())
							.eq("textbook_name", textbookName);
					// Get bill data for summary update before deletion
					StudentBill billToDelete = studentBillService.getOne(billDelWrapper);
					studentBillService.remove(billDelWrapper);
					// Update summary table after bill deletion
					if (billToDelete != null) {
						studentAllBillSummaryController.incrementSummary(billToDelete, true);
					}
				}
				log.info("Status changed to unsubscribed, deleted receive and bill. subId: {}", subscriptionId);
								return Result.OK("编辑成功！已删除领取记录和账单");
			}

			return Result.OK("编辑成功！已同步更新个人账单");
		} catch (Exception e) {
			log.error("编辑征订记录失败", e);
			return Result.error("编辑失败：" + e.getMessage());
		}
	}

	@AutoLog(value = "征订表-通过id删除")
	@Operation(summary = "征订表-通过id删除")
	@RequiresPermissions("zbu:t_subscription:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		tSubscriptionService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "征订表-批量删除")
	@Operation(summary = "征订表-批量删除")
	@RequiresPermissions("zbu:t_subscription:deleteBatch")
	@PostMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestBody Map<String, Object> params) {
		String ids = (String) params.get("ids");
		this.tSubscriptionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	// @AutoLog(value = "征订表-通过id查询")
	@Operation(summary = "征订表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TSubscription> queryById(@RequestParam(name = "id", required = true) String id) {
		TSubscription tSubscription = tSubscriptionService.getById(id);
		if (tSubscription == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tSubscription);
	}

	/**
	 * 导出excel（流式：SXSSFWorkbook + 分批查询，支持几十万级数据）
	 */
	@RequiresPermissions("zbu:t_subscription:exportXls")
	@RequestMapping(value = "/exportXls")
		public void exportXls(HttpServletRequest request, HttpServletResponse response) {
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			String exporter = (sysUser != null) ? sysUser.getRealname() : "未知";
			long startTime = System.currentTimeMillis();

			// 1. 构建权限过滤与筛选条件
			StringBuilder filterSql = new StringBuilder();
			if (sysUser != null) {
				SysUser currentUser = sysUserService.getUserByName(sysUser.getUsername());
				String userRoleType = getUserRoleType(currentUser.getId());
				if (STUDENT_ROLE_CODE.equals(userRoleType)) {
					TStudent student = tStudentService.lambdaQuery().eq(TStudent::getUserId, currentUser.getId()).one();
					if (student != null) {
						filterSql.append(" AND s.student_id = '").append(student.getId()).append("'");
					}
				} else if (COUNSELOR_ROLE_CODE.equals(userRoleType)) {
					TCounselor counselor = tCounselorService.lambdaQuery().eq(TCounselor::getUserId, currentUser.getId()).one();
					if (counselor != null) {
						List<TClass> classList = tClassService.lambdaQuery().eq(TClass::getCounselorId, counselor.getId()).list();
						if (!classList.isEmpty()) {
							List<String> studentIds = tStudentService.lambdaQuery()
								.in(TStudent::getClassId, classList.stream().map(TClass::getId).toList())
								.list().stream().map(TStudent::getId).toList();
							if (!studentIds.isEmpty()) {
								filterSql.append(" AND s.student_id IN ('").append(String.join("','", studentIds)).append("')");
							}
						}
					}
				}
			}

			// 通用筛选条件
			String subscriptionYear = request.getParameter("subscriptionYear");
			if (oConvertUtils.isNotEmpty(subscriptionYear)) {
				filterSql.append(" AND s.subscription_year = '").append(subscriptionYear).append("'");
			}
			String subscriptionSemester = request.getParameter("subscriptionSemester");
			if (oConvertUtils.isNotEmpty(subscriptionSemester)) {
				filterSql.append(" AND s.subscription_semester = '").append(subscriptionSemester).append("'");
			}
			String subscribeStatus = request.getParameter("subscribeStatus");
			if (oConvertUtils.isNotEmpty(subscribeStatus)) {
				filterSql.append(" AND s.subscribe_status = '").append(subscribeStatus).append("'");
			}
			String studentIdPrefix = request.getParameter("studentIdPrefix");
			if (oConvertUtils.isNotEmpty(studentIdPrefix)) {
				filterSql.append(" AND st.student_id LIKE '").append(studentIdPrefix.trim()).append("%'");
			}
			String studentId = request.getParameter("studentId");
			if (oConvertUtils.isNotEmpty(studentId)) {
				String key = studentId.trim();
				filterSql.append(" AND (st.student_id LIKE '%").append(key)
					.append("%' OR st.student_name LIKE '%").append(key).append("%')");
			}
			String collegeName = request.getParameter("collegeName");
			if (oConvertUtils.isNotEmpty(collegeName)) {
				filterSql.append(" AND c.college_name LIKE '%").append(collegeName.trim()).append("%'");
			}
			String majorName = request.getParameter("majorName");
			if (oConvertUtils.isNotEmpty(majorName)) {
				filterSql.append(" AND m.major_name LIKE '%").append(majorName.trim()).append("%'");
			}
			String className = request.getParameter("className");
			if (oConvertUtils.isNotEmpty(className)) {
				filterSql.append(" AND cl.class_name LIKE '%").append(className.trim()).append("%'");
			}

			// 2. 基础 FROM
			String baseFrom = " FROM t_subscription s"
				+ " LEFT JOIN t_student st ON s.student_id = st.id"
				+ " LEFT JOIN t_textbook tb ON s.textbook_id = tb.id"
				+ " LEFT JOIN t_major m ON s.major_id = m.id"
				+ " LEFT JOIN t_college c ON m.college_id = c.id"
				+ " LEFT JOIN t_class cl ON st.class_id = cl.id"
				+ " WHERE 1=1" + filterSql.toString();

			// 3. 计数
			long totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*)" + baseFrom, Long.class);
			log.info("导出征订表：共 {} 条记录，导出人：{}", totalCount, exporter);

			if (totalCount == 0) {
				try {
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"success\":false,\"message\":\"没有符合条件的数据可导出\"}");
				} catch (IOException e) {}
				return;
			}

			// 4. 设置响应头
			try {
				String fileName = java.net.URLEncoder.encode("征订表_" + exporter + "_"
					+ new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()), "UTF-8") + ".xlsx";
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				// 5. 创建流式工作簿（内存窗口100行）
				org.apache.poi.xssf.streaming.SXSSFWorkbook workbook = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100);
				org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("征订表");

				// 6. 创建表头
				String[] headers = {"学号", "学生姓名", "教材", "ISBN", "专业", "学院", "班级",
					"征订学年", "征订学期", "征订状态", "征订备注", "征订操作时间"};
				org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(0);
				org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
				org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
				headerFont.setBold(true);
				headerStyle.setFont(headerFont);
				for (int i = 0; i < headers.length; i++) {
					org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
					cell.setCellValue(headers[i]);
					cell.setCellStyle(headerStyle);
				}

				// 7. 分批查询写入（每批5000条）
				int batchSize = 5000;
				int rowIdx = 1;
				String dataSql = "SELECT st.student_id AS studentNo, st.student_name AS studentName,"
					+ " tb.textbook_name AS textbookName, tb.isbn,"
					+ " m.major_name AS majorName, c.college_name AS collegeName, cl.class_name AS className,"
					+ " s.subscription_year AS subscriptionYear, s.subscription_semester AS subscriptionSemester,"
					+ " s.subscribe_status AS subscribeStatus, s.remark, s.subscribe_time AS subscribeTime"
					+ baseFrom + " ORDER BY s.create_time DESC";

				for (int offset = 0; offset < totalCount; offset += batchSize) {
					String pageSql = dataSql + " LIMIT " + batchSize + " OFFSET " + offset;
					List<Map<String, Object>> batch = jdbcTemplate.queryForList(pageSql);
					for (Map<String, Object> row : batch) {
						org.apache.poi.ss.usermodel.Row excelRow = sheet.createRow(rowIdx++);
						excelRow.createCell(0).setCellValue(str(row.get("studentNo")));
						excelRow.createCell(1).setCellValue(str(row.get("studentName")));
						excelRow.createCell(2).setCellValue(str(row.get("textbookName")));
						excelRow.createCell(3).setCellValue(str(row.get("isbn")));
						excelRow.createCell(4).setCellValue(str(row.get("majorName")));
						excelRow.createCell(5).setCellValue(str(row.get("collegeName")));
						excelRow.createCell(6).setCellValue(str(row.get("className")));
						excelRow.createCell(7).setCellValue(str(row.get("subscriptionYear")));
						// 学期标准化
						String sem = str(row.get("subscriptionSemester"));
						if ("1".equals(sem) || "一".equals(sem) || "第一学期".equals(sem)) sem = "第一学期";
						else if ("2".equals(sem) || "二".equals(sem) || "第二学期".equals(sem)) sem = "第二学期";
						excelRow.createCell(8).setCellValue(sem);
						// 状态标准化
						String status = str(row.get("subscribeStatus"));
						excelRow.createCell(9).setCellValue("1".equals(status) ? "已征订" : "未征订");
						excelRow.createCell(10).setCellValue(str(row.get("remark")));
						Object timeObj = row.get("subscribeTime");
						excelRow.createCell(11).setCellValue(timeObj != null ? timeObj.toString() : "");
					}
					log.info("导出进度：{}/{}", rowIdx - 1, totalCount);
				}

				// 8. 自动列宽（SXSSFSheet 需先 track 列）
				((org.apache.poi.xssf.streaming.SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
				for (int i = 0; i < headers.length; i++) {
					sheet.autoSizeColumn(i);
					int width = sheet.getColumnWidth(i);
					sheet.setColumnWidth(i, Math.min(width, 6000));
				}

				// 9. 写出
				workbook.write(response.getOutputStream());
				workbook.dispose();
				workbook.close();
				long elapsed = System.currentTimeMillis() - startTime;
				log.info("导出完成：{} 条记录，耗时 {} 秒", totalCount, elapsed / 1000.0);

			} catch (Exception e) {
				log.error("导出征订表失败", e);
				try {
					response.setContentType("application/json;charset=UTF-8");
					response.getWriter().write("{\"success\":false,\"message\":\"导出失败：" + e.getMessage() + "\"}");
				} catch (IOException ignored) {}
			}
		}

		private String str(Object obj) {
			return obj == null ? "" : obj.toString();
		}


	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_subscription:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要导入的Excel文件！");
			}

			List<TSubscription> validList = new ArrayList<>();
			List<String> errorMsgList = new ArrayList<>();

			for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
				MultipartFile file = entity.getValue();
				if (file.isEmpty()) continue;
				ImportParams params = new ImportParams();
				params.setTitleRows(2);
				params.setHeadRows(1);
				params.setNeedSave(false);

				List<TSubscription> list = ExcelImportUtil.importExcel(file.getInputStream(), TSubscription.class, params);

				for (int i = 0; i < list.size(); i++) {
					TSubscription sub = list.get(i);
					int rowNum = i + 4;

					// 校验教材
					String textbookInput = sub.getTextbookId();
					if (oConvertUtils.isEmpty(textbookInput)) {
						errorMsgList.add("第" + rowNum + "行，教材字段不能为空，请检查。");
						continue;
					}

					// 解析教材（按ID、ISBN、名称依次查找）
					TTextbook textbook = tTextbookService.getById(textbookInput);
					if (textbook == null) {
						textbook = tTextbookService.lambdaQuery()
								.eq(TTextbook::getIsbn, textbookInput.trim()).one();
					}
					if (textbook == null) {
						textbook = tTextbookService.lambdaQuery()
								.eq(TTextbook::getTextbookName, textbookInput.trim()).one();
					}
					if (textbook == null) {
						errorMsgList.add("第" + rowNum + "行，教材「" + textbookInput + "」不存在，请检查。");
						continue;
					}
					sub.setTextbookId(textbook.getId());

					// 校验学年
					if (oConvertUtils.isEmpty(sub.getSubscriptionYear())) {
						errorMsgList.add("第" + rowNum + "行，征订学年字段不能为空，请检查。");
						continue;
					}

					// 校验学期
					if (oConvertUtils.isEmpty(sub.getSubscriptionSemester())) {
						sub.setSubscriptionSemester("1");
					} else {
						// 统一学期格式为字典码（兼容导入Excel中的"1/一/第一学期"等写法）
						sub.setSubscriptionSemester(SemesterUtil.normalizeCode(sub.getSubscriptionSemester()));
					}

					// 补默认值
					if (oConvertUtils.isEmpty(sub.getSubscribeStatus())) {
						sub.setSubscribeStatus("0");
					}
					sub.setCreateTime(new Date());
					sub.setUpdateTime(new Date());

					// 查重：教材+学年+学期
					QueryWrapper<TSubscription> dupWrapper = new QueryWrapper<>();
					dupWrapper.eq("textbook_id", sub.getTextbookId())
							.eq("subscription_year", sub.getSubscriptionYear())
							.eq("subscription_semester", sub.getSubscriptionSemester());
					if (tSubscriptionService.count(dupWrapper) > 0) {
						String semesterLabel = "1".equals(sub.getSubscriptionSemester()) ? "第一学期" :
								"2".equals(sub.getSubscriptionSemester()) ? "第二学期" : sub.getSubscriptionSemester();
						errorMsgList.add("第" + rowNum + "行，ISBN【" + textbook.getIsbn() + "】已存在（学年："
								+ sub.getSubscriptionYear() + "，学期：" + semesterLabel + "），跳过导入。");
						continue;
					}

					validList.add(sub);
				}
			}

			if (!validList.isEmpty()) {
				service.saveBatch(validList);
			}

			String successMsg = "导入完成！成功导入【" + validList.size() + "】条有效数据";
			return ImportErrorExportUtil.buildErrorResult(errorMsgList, successMsg, uploadPath, "征订表");
		} catch (Exception e) {
			log.error("Excel导入征订数据失败", e);
			return Result.error("导入失败：" + e.getMessage());
		}
	}

	/**
	 * 获取当前登录用户的征订记录（核心接口，支持服务端分页/排序/筛选）
	 */
	@AutoLog(value = "征订表-获取我的征订记录")
	@Operation(summary = "获取当前登录用户的征订记录", description = "按角色返回：管理员全量分页、辅导员仅管班级、学生仅本人")
	@GetMapping(value = "/getMySubscription")
	public Result<Map<String, Object>> getMySubscription(
		@RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
		@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
		@RequestParam(name = "subscriptionYear", required = false) String subscriptionYear,
		@RequestParam(name = "subscriptionSemester", required = false) String subscriptionSemester,
		@RequestParam(name = "className", required = false) String className,
		@RequestParam(name = "majorName", required = false) String majorName,
		@RequestParam(name = "collegeName", required = false) String collegeName,
		@RequestParam(name = "studentId", required = false) String studentId,
		@RequestParam(name = "studentIdPrefix", required = false) String studentIdPrefix,
		@RequestParam(name = "subscribeStatus", required = false) String subscribeStatus,
		@RequestParam(name = "column", required = false) String column,
		@RequestParam(name = "order", required = false) String order) {
		try {
			// 1. 获取当前登录用户
			Subject subject = SecurityUtils.getSubject();
			if (subject == null || !subject.isAuthenticated()) {
				log.warn("用户未登录，无法获取征订记录");
				return Result.error("用户未登录，无法获取征订记录");
			}
			LoginUser loginUser = (LoginUser) subject.getPrincipal();
			if (loginUser == null) {
				log.warn("未获取到当前登录用户信息");
				return Result.error("未获取到当前登录用户信息");
			}

			log.info("当前登录用户: {}，角色码: {}", loginUser.getUsername(), loginUser.getRoleCode());

			// 2. 解析角色
			String roleCodeStr = loginUser.getRoleCode();
			boolean isAdmin = false;
			boolean isCounselor = false;
			if (roleCodeStr != null && !roleCodeStr.isEmpty()) {
				for (String code : roleCodeStr.split(",")) {
					code = code.trim();
					if ("admin".equals(code)) { isAdmin = true; break; }
					if ("counselor".equals(code)) { isCounselor = true; }
				}
			}
			if (!isAdmin && "admin".equals(loginUser.getUsername())) {
				isAdmin = true;
			}

			// 3. 构建角色限制条件
			String roleFilter = "";
			if (isCounselor) {
				TCounselor counselor = tCounselorService.lambdaQuery()
					.eq(TCounselor::getUserId, loginUser.getId()).one();
				if (counselor == null) {
					return mapResult(Collections.emptyList(), 0, pageNo, pageSize);
				}
				List<TClass> classList = tClassService.lambdaQuery()
					.eq(TClass::getCounselorId, counselor.getId()).list();
				if (classList.isEmpty()) {
					return mapResult(Collections.emptyList(), 0, pageNo, pageSize);
				}
				List<String> classIds = classList.stream().map(TClass::getId).collect(Collectors.toList());
				List<String> studentIds = tStudentService.lambdaQuery()
					.in(TStudent::getClassId, classIds).list()
					.stream().map(TStudent::getId).collect(Collectors.toList());
				if (studentIds.isEmpty()) {
					return mapResult(Collections.emptyList(), 0, pageNo, pageSize);
				}
				roleFilter = " AND s.student_id IN ('" + String.join("','", studentIds) + "')";
			} else if (!isAdmin) {
				// 学生：仅看自己
				TStudent student = tStudentService.lambdaQuery()
					.eq(TStudent::getStudentId, loginUser.getUsername()).one();
				if (student == null) {
					return mapResult(Collections.emptyList(), 0, pageNo, pageSize);
				}
				roleFilter = " AND s.student_id = '" + student.getId() + "'";
			}

			// 4. 构建通用筛选条件
			StringBuilder filterSql = new StringBuilder();

			if (oConvertUtils.isNotEmpty(subscriptionYear)) {
				filterSql.append(" AND s.subscription_year = '").append(subscriptionYear).append("'");
			}
			if (oConvertUtils.isNotEmpty(subscriptionSemester)) {
				filterSql.append(" AND s.subscription_semester = '").append(subscriptionSemester).append("'");
			}
			if (oConvertUtils.isNotEmpty(subscribeStatus)) {
				filterSql.append(" AND s.subscribe_status = '").append(subscribeStatus).append("'");
			}
			if (oConvertUtils.isNotEmpty(studentIdPrefix)) {
				filterSql.append(" AND st.student_id LIKE '").append(studentIdPrefix.trim()).append("%'");
			}
			if (oConvertUtils.isNotEmpty(studentId)) {
				String key = studentId.trim();
				filterSql.append(" AND (st.student_id LIKE '%").append(key)
					.append("%' OR st.student_name LIKE '%").append(key).append("%')");
			}
			if (oConvertUtils.isNotEmpty(collegeName)) {
				filterSql.append(" AND c.college_name LIKE '%").append(collegeName.trim()).append("%'");
			}
			if (oConvertUtils.isNotEmpty(majorName)) {
				filterSql.append(" AND m.major_name LIKE '%").append(majorName.trim()).append("%'");
			}
			if (oConvertUtils.isNotEmpty(className)) {
				filterSql.append(" AND cl.class_name LIKE '%").append(className.trim()).append("%'");
			}

			// 5. 基础 FROM（完整 JOIN，覆盖视图所有字段 + className）
			String baseFrom = " FROM t_subscription s"
				+ " LEFT JOIN t_student st ON s.student_id = st.id"
				+ " LEFT JOIN t_textbook tb ON s.textbook_id = tb.id"
				+ " LEFT JOIN t_major m ON s.major_id = m.id"
				+ " LEFT JOIN t_college c ON m.college_id = c.id"
				+ " LEFT JOIN t_class cl ON st.class_id = cl.id"
				+ " WHERE 1=1" + roleFilter + filterSql.toString();

			// 6. COUNT 查询
			long totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*)" + baseFrom, Long.class);

			// 7. 排序（前端列名 → 数据库列名）
			String orderClause;
			java.util.Map<String, String> sortMap = new java.util.HashMap<>();
			sortMap.put("studentNo", "st.student_id");
			sortMap.put("studentName", "st.student_name");
			sortMap.put("textbookName", "tb.textbook_name");
			sortMap.put("isbn", "tb.isbn");
			sortMap.put("majorName", "m.major_name");
			sortMap.put("collegeName", "c.college_name");
			sortMap.put("className", "cl.class_name");
			sortMap.put("subscriptionYear", "s.subscription_year");
			sortMap.put("subscriptionSemester", "s.subscription_semester");
			sortMap.put("subscribeStatus", "s.subscribe_status");
			sortMap.put("subscribeTime", "s.subscribe_time");
			sortMap.put("createTime", "s.create_time");
			if (oConvertUtils.isNotEmpty(column) && sortMap.containsKey(column)) {
				String dir = "asc".equalsIgnoreCase(order != null ? order : "") ? "ASC" : "DESC";
				orderClause = " ORDER BY " + sortMap.get(column) + " " + dir;
			} else {
				orderClause = " ORDER BY s.create_time DESC";
			}

			// 8. 数据查询（LIMIT/OFFSET，与视图别名兼容）
			String dataSql = "SELECT s.id, s.student_id, st.student_id AS studentNo, st.student_name AS studentName,"
				+ " s.textbook_id, tb.textbook_name AS textbook_name, tb.isbn AS isbn,"
				+ " s.selection_id, s.major_id, m.major_name AS majorName,"
				+ " m.college_id, c.college_name AS collegeName,"
				+ " s.subscription_year AS subscriptionYear, s.subscription_semester AS subscriptionSemester,"
				+ " s.subscribe_status AS subscribeStatus, s.remark,"
				+ " s.subscribe_time AS subscribeTime, s.create_time AS createTime, s.update_time AS updateTime,"
				+ " cl.class_name AS className"
				+ baseFrom + orderClause
				+ " LIMIT " + pageSize + " OFFSET " + ((pageNo - 1) * pageSize);

			List<Map<String, Object>> records = jdbcTemplate.queryForList(dataSql);

			log.info("查询到{}条征订记录（总数: {}，第{}页/每页{}条）", records.size(), totalCount, pageNo, pageSize);

			return mapResult(records, totalCount, pageNo, pageSize);
		} catch (Exception e) {
			log.error("获取征订记录失败", e);
			return Result.error("获取失败：" + e.getMessage());
		}
	}

	/** 构建分页结果 */
	private Result<Map<String, Object>> mapResult(List<Map<String, Object>> records, long total, int pageNo, int pageSize) {
		Map<String, Object> result = new HashMap<>();
		result.put("records", records);
		result.put("total", total);
		result.put("pageNo", pageNo);
		result.put("pageSize", pageSize);
		return Result.OK(result);
	}

	/**
	 * 核心新增：批量修改征订状态（适配前端“是否已全部征订”按钮）
	 *
	 * @param params 请求参数（ids: 征订记录ID数组, subscribeStatus: 征订状态, studentId: 学生ID）
	 * @return Result<String>
	 */
	@Transactional(rollbackFor = Exception.class)
	@AutoLog(value = "征订表-批量修改征订状态")
	@Operation(summary = "批量修改征订状态", description = "学生只能修改自己的征订记录，管理员可修改所有")
	@PostMapping(value = "/batchUpdateSubscribeStatus")
	public Result<String> batchUpdateSubscribeStatus(@RequestBody Map<String, Object> params) {
		try {
			// 1. 解析参数
			List<String> ids = (List<String>) params.get("ids");
			String subscribeStatus = (String) params.get("subscribeStatus"); // 征订表数字状态：0/1
			String studentId = (String) params.get("studentId"); // 学生表主键ID

			// 2. 参数校验
			if (ids == null || ids.isEmpty()) {
				log.warn("批量修改征订状态失败：未传入需要修改的记录ID");
				return Result.error("未传入需要修改的记录ID");
			}
			if (oConvertUtils.isEmpty(subscribeStatus)) {
				log.warn("批量修改征订状态失败：未传入征订状态");
				return Result.error("未传入征订状态");
			}
			if (oConvertUtils.isEmpty(studentId)) {
				log.warn("批量修改征订状态失败：未传入学生ID");
				return Result.error("未传入学生ID");
			}

			// 3. 获取当前登录用户，校验权限
			Subject subject = SecurityUtils.getSubject();
			LoginUser loginUser = (LoginUser) subject.getPrincipal();
			boolean isAdmin = false;
			boolean isCounselor = false;
			String roleCodeStr = loginUser.getRoleCode();
			if (roleCodeStr != null && !roleCodeStr.isEmpty()) {
				String[] roleCodes = roleCodeStr.split(",");
				for (String code : roleCodes) {
					if ("admin".equals(code.trim())) {
						isAdmin = true;
						break;
					}
					if ("counselor".equals(code.trim())) {
						isCounselor = true;
					}
				}
			}
			// 管理员用户名兜底判断
			if (!isAdmin && "admin".equals(loginUser.getUsername())) {
				isAdmin = true;
			}

			// 4. 权限校验
			if (!isAdmin) {
				if (isCounselor) {
					// 辅导员：可以修改自己管理的班级下的学生的记录
					// 步骤1：通过sys_user.id查询辅导员信息
					QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
					counselorWrapper.eq("user_id", loginUser.getId());
					TCounselor counselor = tCounselorService.getOne(counselorWrapper);
					if (counselor == null) {
						log.warn("当前登录用户未关联辅导员信息，用户ID: {}", loginUser.getId());
						return Result.error("当前登录用户未关联辅导员信息");
					}

					// 步骤2：查询该辅导员管理的所有班级
					QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
					classWrapper.eq("counselor_id", counselor.getId());
					List<TClass> classList = tClassService.list(classWrapper);
					if (classList.isEmpty()) {
						log.info("辅导员{}暂无管理的班级，无征订记录", counselor.getCounselorName());
						return Result.error("你暂无管理的班级，无征订记录");
					}
					// 提取班级ID列表
					List<String> classIds = classList.stream().map(TClass::getId).collect(Collectors.toList());

					// 步骤3：查询这些班级下的所有学生
					QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
					studentWrapper.in("class_id", classIds);
					List<TStudent> studentList = tStudentService.list(studentWrapper);
					if (studentList.isEmpty()) {
						log.info("辅导员{}管理的班级暂无学生，无征订记录", counselor.getCounselorName());
						return Result.error("你管理的班级暂无学生，无征订记录");
					}
					// 提取学生ID列表
					List<String> counselorStudentIds = studentList.stream().map(TStudent::getId)
							.collect(Collectors.toList());

					// 步骤4：检查要修改的征订记录是否属于这些学生
					QueryWrapper<TSubscription> wrapper = new QueryWrapper<>();
					wrapper.in("id", ids)
							.notIn("student_id", counselorStudentIds);
					List<TSubscription> noAuthRecords = tSubscriptionService.list(wrapper);
					if (!noAuthRecords.isEmpty()) {
						log.warn("辅导员{}尝试修改非管理班级学生的征订记录，非法ID: {}", counselor.getCounselorName(),
								noAuthRecords.stream().map(TSubscription::getId).collect(Collectors.joining(",")));
						return Result.error("你只能修改自己管理班级下学生的征订记录，无法操作其他班级记录！");
					}
				} else {
					// 学生：只能修改自己的记录
					QueryWrapper<TSubscription> wrapper = new QueryWrapper<>();
					wrapper.in("id", ids)
							.ne("student_id", studentId);
					List<TSubscription> noAuthRecords = tSubscriptionService.list(wrapper);
					if (!noAuthRecords.isEmpty()) {
						log.warn("学生{}尝试修改他人征订记录，非法ID: {}", studentId,
								noAuthRecords.stream().map(TSubscription::getId).collect(Collectors.joining(",")));
						return Result.error("你只能修改自己的征订记录，无法操作他人记录！");
					}
				}
			}

			// 5. 学生端：校验学年学期，仅允许操作当前学年当前学期的数据
			if (!isAdmin && !isCounselor) {
				Calendar cal = Calendar.getInstance();
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH) + 1;
				String currentSchoolYear;
				String currentSemester;
				if (month >= 6 && month <= 11) {
					currentSchoolYear = year + "-" + (year + 1);
					currentSemester = "1";
				} else if (month == 12) {
					currentSchoolYear = year + "-" + (year + 1);
					currentSemester = "2";
				} else {
					currentSchoolYear = (year - 1) + "-" + year;
					currentSemester = "2";
				}
				QueryWrapper<TSubscription> yearSemesterWrapper = new QueryWrapper<>();
				yearSemesterWrapper.in("id", ids);
				List<TSubscription> yearSemesterList = tSubscriptionService.list(yearSemesterWrapper);
				for (TSubscription sub : yearSemesterList) {
					boolean yearMatch = currentSchoolYear.equals(sub.getSubscriptionYear());
					boolean semesterMatch = currentSemester.equals(sub.getSubscriptionSemester());
					if (!yearMatch || !semesterMatch) {
						log.warn("学生{}尝试操作非当前学年学期数据，征订ID={}，学年={}，学期={}，期望学年={}，期望学期={}",
								studentId, sub.getId(), sub.getSubscriptionYear(), sub.getSubscriptionSemester(),
								currentSchoolYear, currentSemester);
						return Result.error("仅允许操作当前学年（" + currentSchoolYear + "）第" + currentSemester + "学期的数据，无法操作其他学年学期的征订记录！");
					}
				}
			}

			// 6. 批量更新征订表状态
			List<TSubscription> updateList = new ArrayList<>();
			Date now = new Date();
			for (String id : ids) {
				TSubscription subscription = new TSubscription();
				subscription.setId(id);
				subscription.setSubscribeStatus(subscribeStatus);
				// 当同意征订时，设置征订操作时间
				if ("1".equals(subscribeStatus)) {
					subscription.setSubscribeTime(now);
				}
				subscription.setUpdateTime(now); // 更新修改时间
				updateList.add(subscription);
			}
			boolean subUpdateSuccess = tSubscriptionService.updateBatchById(updateList);
			if (!subUpdateSuccess) {
				log.warn("征订表状态修改失败：无匹配的征订记录（ids={}）", ids);
				return Result.error("征订表状态修改失败：无匹配的记录！");
			}

			// ========== 修复核心：仅创建领取记录，不创建/更新个人账单 ==========
			// 6.1 通过征订表ID查询征订记录（获取关联信息）
			QueryWrapper<TSubscription> subQuery = new QueryWrapper<>();
			subQuery.in("id", ids);
			List<TSubscription> subList = tSubscriptionService.list(subQuery);
			if (subList.isEmpty()) {
				log.warn("未查询到征订记录（ids={}）", ids);
				return Result.OK("征订表状态修改成功，但无匹配的征订记录！");
			}
			// 7. 当同意征订时，为每条征订记录创建领取记录（同步保护，防止并发写偏斜）
			int receiveCreateCount = 0;
			if ("1".equals(subscribeStatus)) { // 1表示已征订/同意征订
				Object lock = receiveCreateLocks.computeIfAbsent(studentId, k -> new Object());
				synchronized (lock) {
					List<TReceive> receiveList = new ArrayList<>();
					for (TSubscription subscription : subList) {
						// 检查是否已存在领取记录
						QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
						receiveWrapper.eq("subscription_id", subscription.getId());
						if (tReceiveService.count(receiveWrapper) == 0) {
							// 创建领取记录
							TReceive receive = new TReceive();
							receive.setReceiveOperator(subscription.getStudentId());
							receive.setSubscriptionId(subscription.getId());
							receive.setReceiveStatus("未领取");
							receive.setReceiveRemark("");
							receive.setCreateTime(new Date());
							receive.setUpdateTime(new Date());
							TMajor major = tMajorService.getById(subscription.getMajorId());
							if (major != null) {
								TCollege college = tCollegeService.getById(major.getCollegeId());
								if (college != null) {
									receive.setCollegeName(college.getCollegeName());
								}
							}
							receive.setSubscriptionYear(subscription.getSubscriptionYear());
							receive.setSubscriptionSemester(subscription.getSubscriptionSemester());
							receiveList.add(receive);
							receiveCreateCount++;
						}
					}
					if (!receiveList.isEmpty()) {
						tReceiveService.saveBatch(receiveList);
						log.info("批量创建领取记录成功，共创建{}条", receiveList.size());
					}
				}
			}

			// 8. 同步更新个人账单表中的征订状态
			for (TSubscription subscription : subList) {
				// 查询该征订记录对应的个人账单
				TStudent student = tStudentService.getById(subscription.getStudentId());
				String studentNo = student != null ? student.getStudentId() : null;
				if (studentNo == null) {
					continue;
				}

				TTextbook textbook = tTextbookService.getById(subscription.getTextbookId());
				String textbookName = textbook != null ? textbook.getTextbookName() : null;
				if (textbookName == null) {
					continue;
				}

				// 根据学号、学年、学期、教材名称查询个人账单
				QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
				billWrapper.eq("student_id", studentNo)
						.eq("subscription_year", subscription.getSubscriptionYear())
						.eq("subscription_semester", subscription.getSubscriptionSemester())
						.eq("textbook_name", textbookName);

				StudentBill existingBill = studentBillService.getOne(billWrapper);
				if (existingBill != null) {
					// 更新个人账单的征订状态
					StudentBill billUpdate = new StudentBill();
					billUpdate.setId(existingBill.getId());
					billUpdate.setSubscribeStatus(subscribeStatus);
					billUpdate.setUpdateTime(new Date());
					studentBillService.updateById(billUpdate);
					log.info("同步更新个人账单征订状态，账单ID={}，新状态={}", existingBill.getId(), subscribeStatus);
				}
			}

			// 9. 返回最终结果
			String msg = String.format("成功修改%d条征订记录状态（创建%d条领取记录）！", ids.size(), receiveCreateCount);
			return Result.OK(msg);

		} catch (Exception e) {
			log.error("批量修改征订状态失败", e);
			// 区分权限错误和通用错误
			if (e.getMessage() != null && e.getMessage().contains("无权限")) {
				return Result.error("你只能修改自己的征订记录，无法操作他人记录！");
			} else {
				return Result.error("征订状态修改失败：" + e.getMessage());
			}
		}
	}

	/**
	 * 学生同意征订
	 * 学生点击同意征订后，更新征订状态为"已确认"并创建领取记录
	 *
	 * @param subscriptionId 征订记录ID
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	@AutoLog(value = "征订表-学生同意征订")
	@Operation(summary = "征订表-学生同意征订")
	@PostMapping(value = "/agreeSubscription")
	public Result<String> agreeSubscription(@RequestParam String subscriptionId) {
		try {
			// 1. 查询征订记录
			TSubscription subscription = tSubscriptionService.getById(subscriptionId);
			if (subscription == null) {
				return Result.error("征订记录不存在");
			}

			// 2. 校验当前登录用户（只要登录即可操作，不管角色）
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (loginUser == null) {
				return Result.error("请先登录");
			}

			// 记录操作人信息
			String currentUserId = loginUser.getId();
			log.info("用户{}（角色：{}）操作学生征订同意，征订记录ID：{}",
					loginUser.getUsername(), loginUser.getRoleCode(), subscriptionId);

			// 3. 校验征订状态是否为"待确认"
			if (!"待确认".equals(subscription.getSubscribeStatus())) {
				return Result.error("该征订记录状态不是待确认，无法同意征订");
			}

			// 获取学生信息用于日志和账单更新
			TStudent student = tStudentService.getById(subscription.getStudentId());
			String studentNo = student != null ? student.getStudentId() : "未知学生";

			// 4. 更新征订状态为"已征订"(1)
			subscription.setSubscribeStatus("1");
			subscription.setSubscribeTime(new Date());
			subscription.setUpdateTime(new Date());
			tSubscriptionService.updateById(subscription);
			log.info("学生{}同意征订,征订记录ID:{}", studentNo, subscriptionId);

			// 5. 检查是否已存在领取记录，防止重复创建
			QueryWrapper<TReceive> receiveWrapper = new QueryWrapper<>();
			receiveWrapper.eq("subscription_id", subscriptionId);
			if (tReceiveService.count(receiveWrapper) > 0) {
				log.warn("征订记录{}已存在领取记录，跳过重复创建", subscriptionId);
				return Result.OK("同意征订成功！领取记录已存在，无需重复创建");
			}

			// 6. 创建领取记录
			TReceive receive = new TReceive();
			receive.setReceiveOperator(subscription.getStudentId());
			receive.setSubscriptionId(subscription.getId());
			receive.setReceiveStatus("未领取");
			receive.setReceiveRemark("");
			receive.setCreateTime(new Date());
			receive.setUpdateTime(new Date());

			TMajor major = tMajorService.getById(subscription.getMajorId());
			if (major != null) {
				// 2. 根据专业ID查询学院
				TCollege college = tCollegeService.getById(major.getCollegeId());
				if (college != null) {
					// 3. 赋值学院名称到领取表
					receive.setCollegeName(college.getCollegeName());
				}
			}
			log.info("准备创建领取记录：receiveOperator={}, subscriptionId={}, receiveStatus={}",
					receive.getReceiveOperator(), receive.getSubscriptionId(), receive.getReceiveStatus());
			receive.setSubscriptionYear(subscription.getSubscriptionYear());
			receive.setSubscriptionSemester(subscription.getSubscriptionSemester());
			boolean saveResult = tReceiveService.save(receive);
			log.info("创建领取记录结果：{}，领取记录ID：{}", saveResult, receive.getId());
			if (!saveResult) {
				log.error("创建领取记录失败，subscriptionId：{}", subscriptionId);
				return Result.error("创建领取记录失败");
			}
			log.info("为学生{}创建领取记录成功，关联征订记录ID：{}，领取记录ID：{}",
					studentNo, subscriptionId, receive.getId());

			return Result.OK("同意征订成功！已创建领取记录（个人账单将在领取教材后生成）");
		} catch (Exception e) {
			log.error("同意征订失败", e);
			return Result.error("同意征订失败：" + e.getMessage());
		}
	}

	private String getUserRoleType(String userId) {
		// 1. 查询用户关联的角色
		QueryWrapper<SysUserRole> userRoleWrapper = new QueryWrapper<>();
		userRoleWrapper.eq("user_id", userId);
		List<SysUserRole> userRoleList = sysUserRoleService.list(userRoleWrapper);
		if (userRoleList.isEmpty()) {
			return "";
		}

		// 2. 提取角色编码
		List<String> roleIds = userRoleList.stream()
				.map(SysUserRole::getRoleId)
				.collect(Collectors.toList());
		List<SysRole> roleList = sysRoleService.listByIds(roleIds);

		// 3. 判断角色优先级：管理员 > 辅导员 > 学生
		for (SysRole role : roleList) {
			if (ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
				return ADMIN_ROLE_CODE;
			}
			if (COUNSELOR_ROLE_CODE.equals(role.getRoleCode())) {
				return COUNSELOR_ROLE_CODE;
			}
			if (STUDENT_ROLE_CODE.equals(role.getRoleCode())) {
				return STUDENT_ROLE_CODE;
			}
		}
		return "";
	}

}
