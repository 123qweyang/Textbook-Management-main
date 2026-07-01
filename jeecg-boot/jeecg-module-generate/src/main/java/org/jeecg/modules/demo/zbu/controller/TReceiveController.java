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
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.demo.zbu.entity.*;
import org.jeecg.modules.demo.zbu.service.*;

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
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
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
import org.jeecg.modules.demo.zbu.controller.StudentAllBillSummaryController;

/**
 * @Description: 领取表
 * @Author: jeecg-boot
 * @Date: 2026-01-23
 * @Version: V1.0
 */
@Tag(name = "领取表")
@RestController
@RequestMapping("/zbu/tReceive")
@Slf4j
public class TReceiveController extends JeecgController<TReceive, ITReceiveService> {
	@Autowired
	private ITReceiveService tReceiveService;
	@Autowired
	private ITStudentService tStudentService;
	@Autowired
	private IStudentBillService studentBillService;
	@Autowired
	private ITSubscriptionService tSubscriptionService;
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private ITTextbookService tTextbookService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private ITCounselorService tCounselorService;
	@Autowired
	private ITClassService tClassService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private ITCollegeService tCollegeService;
	@Autowired
	private StudentAllBillSummaryController studentAllBillSummaryController;
	@Value("${jeecg.path.upload}")
	private String uploadPath;

	// 角色编码常量
	private static final String ADMIN_ROLE_CODE = "admin";
	private static final String COUNSELOR_ROLE_CODE = "counselor";
	private static final String STUDENT_ROLE_CODE = "student";

	// 操作人级锁，防止同一操作人并发同步账单导致重复创建
	private final ConcurrentHashMap<String, Object> billSyncLocks = new ConcurrentHashMap<>();

	/**
	 * 分页列表查询
	 *
	 * @param tReceive
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	// @AutoLog(value = "领取表-分页列表查询")
	@Operation(summary = "领取表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TReceive>> queryPageList(TReceive tReceive,
												 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
												 HttpServletRequest req) {

		// 1. 获取当前登录用户信息
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		if (loginUser == null) {
			return Result.error("用户未登录，请先登录！");
		}
		String username = loginUser.getUsername();
		SysUser currentUser = sysUserService.getUserByName(username);
		if (currentUser == null) {
			return Result.error("未查询到当前登录用户信息！");
		}

		// 2. 判断当前用户角色
		String userRoleType = getUserRoleType(currentUser.getId());
		log.info("【领取表查询】当前用户：{}，角色类型：{}", username, userRoleType);

		// 3. 自定义查询规则（适配多选筛选）
		Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
		customeRuleMap.put("receiveStatus", QueryRuleEnum.LIKE_WITH_OR);
		QueryWrapper<TReceive> queryWrapper = QueryGenerator.initQueryWrapper(tReceive, req.getParameterMap(),
				customeRuleMap);

		// 学院模糊查询
		String collegeName = req.getParameter("collegeName");
		if (oConvertUtils.isNotEmpty(collegeName)) {
			queryWrapper.inSql("receive_operator",
					"SELECT id FROM t_student WHERE major_id IN (SELECT id FROM t_major WHERE college_id IN (SELECT id FROM t_college WHERE college_name LIKE CONCAT('%', '"
							+ collegeName + "', '%')))");
		}

		// 4. 按角色过滤查询条件
		switch (userRoleType) {
			case ADMIN_ROLE_CODE:
				// 管理员：查询所有，无需过滤
				break;
			case COUNSELOR_ROLE_CODE:
				// 辅导员：查询所有学生的领取记录（可扩展：按辅导员管辖班级/专业过滤）
				// 示例：如果辅导员关联了班级，可添加 queryWrapper.in("class_id", 管辖班级ID列表)
				log.info("【辅导员端】查询所有学生的领取记录");
				break;
			case STUDENT_ROLE_CODE:
				// 学生：仅查询自己的领取记录（receive_operator=学生表ID）
				TStudent student = tStudentService.lambdaQuery()
						.eq(TStudent::getUserId, currentUser.getId())
						.one();
				if (student != null) {
					queryWrapper.eq("receive_operator", student.getId());
				} else {
					// 学生未关联学生表，返回空数据
					Page<TReceive> emptyPage = new Page<>(pageNo, pageSize);
					emptyPage.setRecords(new ArrayList<>());
					emptyPage.setTotal(0);
					return Result.OK(emptyPage);
				}
				break;
			default:
				// 未知角色：返回空数据
				Page<TReceive> emptyPage = new Page<>(pageNo, pageSize);
				emptyPage.setRecords(new ArrayList<>());
				emptyPage.setTotal(0);
				return Result.OK(emptyPage);
		}

		// 5. 执行分页查询
		Page<TReceive> page = new Page<TReceive>(pageNo, pageSize);
		IPage<TReceive> pageList = tReceiveService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param tReceive
	 * @return
	 */
	@AutoLog(value = "领取表-添加")
	@Operation(summary = "领取表-添加")
	@RequiresPermissions("zbu:t_receive:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TReceive tReceive) {
		if ("1".equals(tReceive.getReceiveStatus())) {
			tReceive.setReceiveTime(new Date());
		}
		tReceiveService.save(tReceive);

		return Result.OK("添加成功！");
	}

	/**
	 * 编辑
	 *
	 * @param tReceive
	 * @return
	 */
	@AutoLog(value = "领取表-编辑")
	@Operation(summary = "领取表-编辑")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT, RequestMethod.POST})
	@Transactional(rollbackFor = Exception.class)
	public Result<String> edit(@RequestBody TReceive tReceive) {
		try {
			TReceive existingReceive = tReceiveService.getById(tReceive.getId());
			if (existingReceive == null) {
				return Result.error("未找到对应的领取记录！");
			}
			String oldStatus = existingReceive.getReceiveStatus();
			String newStatus = tReceive.getReceiveStatus();

			// 如果改为已领取，设置领取时间
			if ("1".equals(newStatus)) {
				tReceive.setReceiveTime(new Date());
			}
			tReceiveService.updateById(tReceive);

			// 如果改为未领取，删除对应的账单
			if ("0".equals(newStatus) && !"0".equals(oldStatus)) {
				deleteStudentBillForReceive(tReceive);
			}

			// 如果从非已领取改为已领取，创建账单记录
			if ("1".equals(newStatus) && !"1".equals(oldStatus)) {
				createStudentBillForReceive(tReceive);
			}

			return Result.OK("编辑成功！");
		} catch (Exception e) {
			log.error("编辑领取记录失败", e);
			throw new RuntimeException("编辑失败：" + e.getMessage());
		}
	}

	/**
	 * 为已领取的记录创建个人账单
	 */
	private void createStudentBillForReceive(TReceive receive) {
		try {
			String subscriptionId = receive.getSubscriptionId();
			if (oConvertUtils.isEmpty(subscriptionId)) {
				log.warn("【创建账单失败】领取记录ID={} 的subscriptionId为空", receive.getId());
				return;
			}

			TSubscription subscription = tSubscriptionService.getById(subscriptionId);
			if (subscription == null) {
				log.warn("【创建账单失败】征订ID={} 不存在", subscriptionId);
				return;
			}

			// 获取学号
			String studentNo = subscription.getStudentId();
			TStudent student = tStudentService.getById(studentNo);
			if (student != null) {
				studentNo = student.getStudentId();
			}

			// 获取教材信息
			TTextbook textbook = tTextbookService.getById(subscription.getTextbookId());
			String textbookName = textbook != null ? textbook.getTextbookName() : "未知教材";

			// 检查账单是否已存在
			QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
			billWrapper.eq("student_id", studentNo)
					.eq("subscription_year", subscription.getSubscriptionYear())
					.eq("subscription_semester", subscription.getSubscriptionSemester())
					.eq("textbook_name", textbookName);

			StudentBill existingBill = studentBillService.getOne(billWrapper);
			if (existingBill != null) {
				// 账单已存在，更新领取状态为已领取
				StudentBill billUpdate = new StudentBill();
				billUpdate.setId(existingBill.getId());
				billUpdate.setReceiveStatus("1");
				billUpdate.setUpdateTime(new Date());
				boolean updateSuccess = studentBillService.updateById(billUpdate);
				if (updateSuccess) {
					log.info("【更新账单成功】领取记录ID={} → 账单（学号={}，教材={}）状态改为已领取",
							receive.getId(), studentNo, textbookName);
				}
				return;
			}

			// 创建新账单
			// 从学生当前信息获取专业和班级
					TStudent billStudent = tStudentService.lambdaQuery()
							.eq(TStudent::getStudentId, studentNo).one();
					TMajor major = billStudent != null
							? tMajorService.getById(billStudent.getMajorId()) : null;
					String className = "";
					if (billStudent != null && oConvertUtils.isNotEmpty(billStudent.getClassId())) {
						TClass tClass = tClassService.getById(billStudent.getClassId());
						if (tClass != null) className = tClass.getClassName();
					}
			BigDecimal price = textbook != null && textbook.getPrice() != null ? textbook.getPrice() : BigDecimal.ZERO;
			BigDecimal discount = textbook != null && textbook.getDiscount() != null ? textbook.getDiscount() : new BigDecimal("1");
			BigDecimal discountPrice = price.multiply(discount).setScale(2, java.math.RoundingMode.HALF_UP);

			StudentBill newBill = new StudentBill();
			newBill.setStudentId(studentNo);
			newBill.setMajorName(major != null ? major.getMajorName() : "");
			newBill.setSubscriptionYear(subscription.getSubscriptionYear());
			newBill.setSubscriptionSemester(subscription.getSubscriptionSemester());
			newBill.setTextbookName(textbookName);
				newBill.setIsbn(textbook.getIsbn() != null ? textbook.getIsbn() : "");
			newBill.setClassName(className);
			String collegeName = "";
				if (major != null) {
					TCollege college = tCollegeService.getById(major.getCollegeId());
					if (college != null) collegeName = college.getCollegeName();
				}
				newBill.setCollegeName(collegeName);
			newBill.setPrice(price);
			newBill.setDiscountPrice(discountPrice);
			newBill.setSubscribeStatus(subscription.getSubscribeStatus());
			newBill.setReceiveStatus("1");
			newBill.setRemark("");
			newBill.setCreateTime(new Date());
			newBill.setUpdateTime(new Date());

			boolean createSuccess = studentBillService.save(newBill);
			if (createSuccess) {
				log.info("【创建账单成功】领取记录ID={} → 账单（学号={}，教材={}）创建成功",
						receive.getId(), studentNo, textbookName);
			}
		} catch (Exception e) {
			log.error("【创建账单异常】领取记录ID={}", receive.getId(), e);
		}
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "领取表-通过id删除")
	@Operation(summary = "领取表-通过id删除")
	@RequiresPermissions("zbu:t_receive:delete")
	@DeleteMapping(value = "/delete")
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		tReceiveService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "领取表-批量删除")
	@Operation(summary = "领取表-批量删除")
	@RequiresPermissions("zbu:t_receive:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
		this.tReceiveService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	// @AutoLog(value = "领取表-通过id查询")
	@Operation(summary = "领取表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TReceive> queryById(@RequestParam(name = "id", required = true) String id) {
		TReceive tReceive = tReceiveService.getById(id);
		if (tReceive == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tReceive);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tReceive
	 */
	@RequiresPermissions("zbu:t_receive:exportXls")
	@RequestMapping(value = "/exportXls")
		public void exportXls(HttpServletRequest request, HttpServletResponse response) {
			LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			String exporter = (sysUser != null) ? sysUser.getRealname() : "未知";
			long startTime = System.currentTimeMillis();

			// 1. 角色判断 + 构建筛选 SQL
			Subject subject = SecurityUtils.getSubject();
			LoginUser loginUser = (LoginUser) subject.getPrincipal();
			String roleCodeStr = loginUser.getRoleCode();
			boolean isAdmin = false, isCounselor = false;
			if (roleCodeStr != null && !roleCodeStr.isEmpty()) {
				for (String code : roleCodeStr.split(",")) {
					if ("admin".equals(code.trim())) { isAdmin = true; break; }
					if ("counselor".equals(code.trim())) { isCounselor = true; }
				}
			}
			if (!isAdmin && "admin".equals(loginUser.getUsername())) isAdmin = true;

			StringBuilder filterSql = new StringBuilder();
			// 角色过滤
			if (isCounselor) {
				TCounselor counselor = tCounselorService.lambdaQuery().eq(TCounselor::getUserId, loginUser.getId()).one();
				if (counselor != null) {
					List<String> sids = tStudentService.lambdaQuery()
						.in(TStudent::getClassId, tClassService.lambdaQuery().eq(TClass::getCounselorId, counselor.getId()).list()
						.stream().map(TClass::getId).toList()).list().stream().map(TStudent::getId).toList();
					filterSql.append(sids.isEmpty() ? " AND 1=0" : " AND r.receive_operator IN ('" + String.join("','", sids) + "')");
				}
			} else if (!isAdmin) {
				TStudent student = tStudentService.lambdaQuery().eq(TStudent::getStudentId, loginUser.getUsername()).one();
				filterSql.append(student != null ? " AND r.receive_operator = '" + student.getId() + "'" : " AND 1=0");
			}
			// 筛选条件
			addParamFilter(request, filterSql, "subscriptionYear", "r.subscription_id IN (SELECT id FROM t_subscription WHERE subscription_year = '", "')");
			addParamFilter(request, filterSql, "subscriptionSemester", "r.subscription_id IN (SELECT id FROM t_subscription WHERE subscription_semester = '", "')");
			addParamLikeFilter(request, filterSql, "receiveStatus", "r.receive_status");
			addParamLikeFilter(request, filterSql, "textbookName", "r.subscription_id IN (SELECT s.id FROM t_subscription s JOIN t_textbook t ON s.textbook_id=t.id WHERE t.textbook_name LIKE '%')");
			addParamLikeFilter(request, filterSql, "studentNo", "r.receive_operator IN (SELECT id FROM t_student WHERE student_id LIKE '%')");
			addParamLikeFilter(request, filterSql, "studentName", "r.receive_operator IN (SELECT id FROM t_student WHERE student_name LIKE '%')");
			addParamLikeFilter(request, filterSql, "collegeName", "r.college_name");
			addParamLikeFilter(request, filterSql, "majorName", "r.subscription_id IN (SELECT s.id FROM t_subscription s JOIN t_major m ON s.major_id=m.id WHERE m.major_name LIKE '%')");
			addParamLikeFilter(request, filterSql, "className", "r.receive_operator IN (SELECT st.id FROM t_student st JOIN t_class cl ON st.class_id=cl.id WHERE cl.class_name LIKE '%')");

			// 2. JOIN 查询（一次性拿到所有显示字段，无 N+1）
			String baseFrom = " FROM t_receive r"
				+ " LEFT JOIN t_student st ON r.receive_operator = st.id"
				+ " LEFT JOIN t_subscription s ON r.subscription_id = s.id"
				+ " LEFT JOIN t_textbook tb ON s.textbook_id = tb.id"
				+ " WHERE 1=1" + filterSql.toString();

			long totalCount = jdbcTemplate.queryForObject("SELECT COUNT(*)" + baseFrom, Long.class);
			log.info("导出领取表：共 {} 条，导出人：{}", totalCount, exporter);
			if (totalCount == 0) { writeJson(response, "没有符合条件的数据可导出"); return; }

			// 3. 流式导出
			try {
				String fileName = java.net.URLEncoder.encode("领取表_" + exporter + "_" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()), "UTF-8") + ".xlsx";
				response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
				response.setCharacterEncoding("UTF-8");
				response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

				org.apache.poi.xssf.streaming.SXSSFWorkbook wb = new org.apache.poi.xssf.streaming.SXSSFWorkbook(100);
				org.apache.poi.ss.usermodel.Sheet sheet = wb.createSheet("领取表");
				String[] headers = {"学号","学生姓名","教材名称","领取状态","征订学年","征订学期","领取时间","领取备注","学院"};
				org.apache.poi.ss.usermodel.Row hr = sheet.createRow(0);
				org.apache.poi.ss.usermodel.CellStyle hs = wb.createCellStyle();
				org.apache.poi.ss.usermodel.Font hf = wb.createFont(); hf.setBold(true); hs.setFont(hf);
				for (int i = 0; i < headers.length; i++) { org.apache.poi.ss.usermodel.Cell c = hr.createCell(i); c.setCellValue(headers[i]); c.setCellStyle(hs); }

				String dataSql = "SELECT st.student_id AS studentNo, st.student_name AS studentName,"
					+ " tb.textbook_name AS textbookName, r.receive_status AS receiveStatus,"
					+ " s.subscription_year AS subscriptionYear, s.subscription_semester AS subscriptionSemester,"
					+ " r.receive_time AS receiveTime, r.receive_remark AS receiveRemark, r.college_name AS collegeName"
					+ baseFrom + " ORDER BY r.create_time DESC";
				int batchSize = 5000, rowIdx = 1;
				for (int off = 0; off < totalCount; off += batchSize) {
					for (Map<String, Object> row : jdbcTemplate.queryForList(dataSql + " LIMIT " + batchSize + " OFFSET " + off)) {
						org.apache.poi.ss.usermodel.Row er = sheet.createRow(rowIdx++);
						er.createCell(0).setCellValue(str(row.get("studentNo")));
						er.createCell(1).setCellValue(str(row.get("studentName")));
						er.createCell(2).setCellValue(str(row.get("textbookName")));
						er.createCell(3).setCellValue(str(row.get("receiveStatus")));
						er.createCell(4).setCellValue(str(row.get("subscriptionYear")));
						String sem = str(row.get("subscriptionSemester"));
						if ("1".equals(sem)||"一".equals(sem)||"第一学期".equals(sem)) sem="第一学期"; else if ("2".equals(sem)||"二".equals(sem)||"第二学期".equals(sem)) sem="第二学期";
						er.createCell(5).setCellValue(sem);
						Object rt = row.get("receiveTime"); er.createCell(6).setCellValue(rt!=null?rt.toString():"");
						er.createCell(7).setCellValue(str(row.get("receiveRemark")));
						er.createCell(8).setCellValue(str(row.get("collegeName")));
					}
					log.info("领取表导出进度：{}/{}", rowIdx-1, totalCount);
				}
				((org.apache.poi.xssf.streaming.SXSSFSheet)sheet).trackAllColumnsForAutoSizing();
				for (int i = 0; i < headers.length; i++) { sheet.autoSizeColumn(i); sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i), 6000)); }
				wb.write(response.getOutputStream()); wb.dispose(); wb.close();
				log.info("领取表导出完成：{} 条，耗时 {} 秒", totalCount, (System.currentTimeMillis()-startTime)/1000.0);
			} catch (Exception e) { log.error("导出领取表失败", e); writeJson(response, "导出失败：" + e.getMessage()); }
		}

	/**
	 * 通过excel导入数据
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_receive:importExcel")
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
				List<TReceive> list = ExcelImportUtil.importExcel(file.getInputStream(), TReceive.class, params);
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
				return ImportErrorExportUtil.buildErrorResult(failMsgList, "导入完成！成功导入0条有效数据", uploadPath, "领取表");
			} finally {
				try { file.getInputStream().close(); } catch (IOException e) { e.printStackTrace(); }
			}
		}
		return Result.error("文件导入失败！");
	}

	/**
	 * 通过学号查询学生信息
	 *
	 * @param studentNo 学生学号
	 * @return
	 */
	// @AutoLog(value = "领取表-通过学号查询学生信息")
	@Operation(summary = "领取表-通过学号查询学生信息")
	@GetMapping(value = "/getStudentByNo")
	public Result<TStudent> getStudentByNo(@RequestParam(name = "studentNo", required = true) String studentNo) {
		// 构造查询条件：根据学号查询学生表
		QueryWrapper<TStudent> queryWrapper = new QueryWrapper<>();
		queryWrapper.eq("student_id", studentNo);
		TStudent student = tStudentService.getOne(queryWrapper);

		if (student == null) {
			return Result.error("未找到学号为【" + studentNo + "】的学生信息");
		}
		return Result.OK(student);
	}

	/**
	 * 原有按ID查学生的接口
	 *
	 * @param id 学生表ID
	 * @return
	 */
	// @AutoLog(value = "领取表-通过ID查询学生信息")
	@Operation(summary = "领取表-通过ID查询学生信息")
	@GetMapping(value = "/getStudentById")
	public Result<TStudent> getStudentById(@RequestParam(name = "id", required = true) String id) {
		TStudent student = tStudentService.getById(id);
		if (student == null) {
			return Result.error("未找到ID为【" + id + "】的学生信息");
		}
		return Result.OK(student);
	}

	/**
	 * 批量修改领取状态（学生端仅改自己的，管理员改所有）
	 */
	@Transactional(rollbackFor = Exception.class)
	@AutoLog(value = "领取表-批量修改领取状态")
	@PostMapping("/batchUpdateReceiveStatus")
	public Result<String> batchUpdateReceiveStatus(@RequestBody Map<String, Object> params) {
		try {
			// 1. 获取当前登录用户及角色
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			if (loginUser == null) {
				return Result.error("用户未登录，请先登录！");
			}
			String username = loginUser.getUsername();
			SysUser currentUser = sysUserService.getUserByName(username);
			String userRoleType = getUserRoleType(currentUser.getId());

			// 2. 获取参数
			List<String> ids = (List<String>) params.get("ids");
			String receiveStatus = (String) params.get("receiveStatus");
			String receiveOperator = (String) params.get("receiveOperator");

			// 3. 参数校验
			if (ids == null || ids.isEmpty()) {
				return Result.error("暂无需要修改的领取记录！");
			}
			if (oConvertUtils.isEmpty(receiveStatus)) {
				return Result.error("领取状态不能为空！");
			}
			// 非管理员/辅导员必须传receiveOperator（学生仅能修改自己）
			if (!ADMIN_ROLE_CODE.equals(userRoleType) && !COUNSELOR_ROLE_CODE.equals(userRoleType)) {
				if (oConvertUtils.isEmpty(receiveOperator)) {
					return Result.error("缺少学生标识，无权限修改他人记录！");
				}
			}

			// 4. 构建更新条件（按角色过滤）
			QueryWrapper<TReceive> updateWrapper = new QueryWrapper<>();
			updateWrapper.in("id", ids);
			// 学生仅能修改自己的记录
			if (STUDENT_ROLE_CODE.equals(userRoleType)) {
				TStudent student = tStudentService.lambdaQuery()
						.eq(TStudent::getUserId, currentUser.getId())
						.one();
				if (student != null) {
					updateWrapper.eq("receive_operator", student.getId());
				} else {
					return Result.error("未查询到当前学生信息，无法修改！");
				}
			}

			// 5. 学生端：校验学年学期，仅允许操作当前学年当前学期的数据
			if (STUDENT_ROLE_CODE.equals(userRoleType)) {
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
				// 查询领取记录关联的征订信息，获取学年学期
				QueryWrapper<TReceive> receiveQueryForCheck = new QueryWrapper<>();
				receiveQueryForCheck.in("id", ids);
				List<TReceive> receiveCheckList = tReceiveService.list(receiveQueryForCheck);
				List<String> subscriptionIds = receiveCheckList.stream()
						.map(TReceive::getSubscriptionId)
						.filter(id -> oConvertUtils.isNotEmpty(id))
						.collect(Collectors.toList());
				if (!subscriptionIds.isEmpty()) {
					QueryWrapper<TSubscription> subCheckWrapper = new QueryWrapper<>();
					subCheckWrapper.in("id", subscriptionIds);
					List<TSubscription> subCheckList = tSubscriptionService.list(subCheckWrapper);
					for (TSubscription sub : subCheckList) {
						boolean yearMatch = currentSchoolYear.equals(sub.getSubscriptionYear());
						boolean semesterMatch = currentSemester.equals(sub.getSubscriptionSemester());
						if (!yearMatch || !semesterMatch) {
							log.warn("学生{}尝试操作非当前学年学期数据，领取记录关联征订ID={}，学年={}，学期={}，期望学年={}，期望学期={}",
									receiveOperator, sub.getId(), sub.getSubscriptionYear(), sub.getSubscriptionSemester(),
									currentSchoolYear, currentSemester);
							return Result.error("仅允许操作当前学年（" + currentSchoolYear + "）第" + currentSemester + "学期的数据，无法操作其他学年学期的领取记录！");
						}
					}
				}
			}

			// 6. 构造更新对象
			TReceive updateReceive = new TReceive();
			updateReceive.setReceiveStatus(receiveStatus);
			if ("1".equals(receiveStatus)) {
				updateReceive.setReceiveTime(new Date());
			}

			// 6. 执行更新
			boolean receiveUpdateSuccess = tReceiveService.update(updateReceive, updateWrapper);
			if (!receiveUpdateSuccess) {
				return Result.error("领取表状态修改失败：无匹配的领取记录！");
			}

			// 7. 同步更新个人账单（同步保护，防止并发重复创建账单）
			if (receiveUpdateSuccess) {
				String lockKey = oConvertUtils.isNotEmpty(receiveOperator) ? receiveOperator : loginUser.getId();
				Object lock = billSyncLocks.computeIfAbsent(lockKey, k -> new Object());
				synchronized (lock) {
				String billReceiveStatus = receiveStatus;

				QueryWrapper<TReceive> receiveQuery = new QueryWrapper<>();
				receiveQuery.in("id", ids);
				List<TReceive> receiveList = tReceiveService.list(receiveQuery);
				if (receiveList.isEmpty()) {
					log.warn("【同步账单失败】未查询到领取记录（ids={}）", ids);
					return Result.OK("领取表状态修改成功，但未同步到账单（无匹配领取记录）！");
				}

				int billUpdateCount = 0;
				int billCreateCount = 0;
				int billDeleteCount = 0;
				for (TReceive receive : receiveList) {
					String subscriptionId = receive.getSubscriptionId();
					if (oConvertUtils.isEmpty(subscriptionId)) {
						log.warn("【同步账单失败】领取记录ID={} 的subscriptionId为空", receive.getId());
						continue;
					}

					TSubscription subscription = tSubscriptionService.getById(subscriptionId);
					if (subscription == null) {
						log.warn("【同步账单失败】征订ID={} 不存在", subscriptionId);
						continue;
					}

					String studentNo = subscription.getStudentId();
					TStudent student = tStudentService.getById(studentNo);
					if (student != null) {
						studentNo = student.getStudentId();
					}

					TTextbook textbook = tTextbookService.getById(subscription.getTextbookId());
					String textbookName = textbook != null ? textbook.getTextbookName() : "未知教材";

					QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
					billWrapper.eq("student_id", studentNo)
							.eq("subscription_year", subscription.getSubscriptionYear())
							.eq("subscription_semester", subscription.getSubscriptionSemester())
							.eq("textbook_name", textbookName);

					StudentBill existingBill = studentBillService.getOne(billWrapper);

					if ("0".equals(receiveStatus) && existingBill != null) {
						boolean deleteSuccess = studentBillService.removeById(existingBill.getId());
				// Update summary after bill deletion
				studentAllBillSummaryController.incrementSummary(existingBill, true);
						if (deleteSuccess) {
							billDeleteCount++;
							log.info("【删除账单成功】领取记录ID={} → 账单（学号={}，教材={}）已删除",
									receive.getId(), studentNo, textbookName);
						}
					} else if (existingBill != null) {
						StudentBill billUpdate = new StudentBill();
						billUpdate.setId(existingBill.getId());
						billUpdate.setReceiveStatus(billReceiveStatus);
						billUpdate.setUpdateTime(new Date());
						boolean singleBillSuccess = studentBillService.updateById(billUpdate);
						if (singleBillSuccess) {
							billUpdateCount++;
							log.info("【同步账单成功】领取记录ID={} → 账单（学号={}，教材={}）状态改为{}",
									receive.getId(), studentNo, textbookName, billReceiveStatus);
						}
					} else if ("1".equals(receiveStatus) && textbook != null) {
						// 从学生当前信息获取专业和班级
					TStudent billStudent = tStudentService.lambdaQuery()
							.eq(TStudent::getStudentId, studentNo).one();
					TMajor major = billStudent != null
							? tMajorService.getById(billStudent.getMajorId()) : null;
					String className = "";
					if (billStudent != null && oConvertUtils.isNotEmpty(billStudent.getClassId())) {
						TClass tClass = tClassService.getById(billStudent.getClassId());
						if (tClass != null) className = tClass.getClassName();
					}
						BigDecimal price = textbook.getPrice() != null ? textbook.getPrice() : BigDecimal.ZERO;
						BigDecimal discount = textbook.getDiscount() != null ? textbook.getDiscount()
								: new BigDecimal("1");
						BigDecimal discountPrice = price.multiply(discount).setScale(2, java.math.RoundingMode.HALF_UP);

						StudentBill newBill = new StudentBill();
						newBill.setStudentId(studentNo);
						newBill.setMajorName(major != null ? major.getMajorName() : "");
						newBill.setSubscriptionYear(subscription.getSubscriptionYear());
						newBill.setSubscriptionSemester(subscription.getSubscriptionSemester());
						newBill.setTextbookName(textbookName);
					newBill.setIsbn(textbook.getIsbn() != null ? textbook.getIsbn() : "");
			newBill.setClassName(className);
			String collegeName = "";
				if (major != null) {
					TCollege college = tCollegeService.getById(major.getCollegeId());
					if (college != null) collegeName = college.getCollegeName();
				}
				newBill.setCollegeName(collegeName);
						newBill.setPrice(price);
						newBill.setDiscountPrice(discountPrice);
						newBill.setSubscribeStatus(subscription.getSubscribeStatus());
						newBill.setReceiveStatus(billReceiveStatus);
						newBill.setRemark("");
						newBill.setCreateTime(new Date());
						newBill.setUpdateTime(new Date());

						boolean createSuccess = studentBillService.save(newBill);
						if (createSuccess) {
							billCreateCount++;
							log.info("【创建账单成功】领取记录ID={} → 账单（学号={}，教材={}）创建成功",
									receive.getId(), studentNo, textbookName);
						}
					}
				}
				log.info("同步个人账单结果：更新{}条，创建{}条，删除{}条，领取状态改为{}", billUpdateCount, billCreateCount, billDeleteCount, billReceiveStatus);
				} // synchronized
			}

			return Result.OK("领取表状态修改成功（已同步个人账单）！");
		} catch (Exception e) {
			log.error("批量修改领取状态失败", e);
			return Result.error("修改失败：" + e.getMessage());
		}
	}

	private void deleteStudentBillForReceive(TReceive receive) {
		try {
			String subscriptionId = receive.getSubscriptionId();
			if (oConvertUtils.isEmpty(subscriptionId)) {
				log.warn("【删除账单失败】领取记录ID={} 的subscriptionId为空", receive.getId());
				return;
			}

			TSubscription subscription = tSubscriptionService.getById(subscriptionId);
			if (subscription == null) {
				log.warn("【删除账单失败】征订ID={} 不存在", subscriptionId);
				return;
			}

			String studentNo = subscription.getStudentId();
			TStudent student = tStudentService.getById(studentNo);
			if (student != null) {
				studentNo = student.getStudentId();
			}

			TTextbook textbook = tTextbookService.getById(subscription.getTextbookId());
			String textbookName = textbook != null ? textbook.getTextbookName() : "未知教材";

			QueryWrapper<StudentBill> billWrapper = new QueryWrapper<>();
			billWrapper.eq("student_id", studentNo)
					.eq("subscription_year", subscription.getSubscriptionYear())
					.eq("subscription_semester", subscription.getSubscriptionSemester())
					.eq("textbook_name", textbookName);

			StudentBill existingBill = studentBillService.getOne(billWrapper);
			if (existingBill != null) {
				boolean deleteSuccess = studentBillService.removeById(existingBill.getId());
				if (deleteSuccess) {
					log.info("【删除账单成功】领取记录ID={} → 账单（学号={}，教材={}）已删除",
							receive.getId(), studentNo, textbookName);
				}
			}
		} catch (Exception e) {
			log.error("【删除账单异常】领取记录ID={}", receive.getId(), e);
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

	@AutoLog(value = "领取表-获取我的领取记录")
	@Operation(summary = "获取当前登录用户的领取记录", description = "管理员查所有、辅导员查管理班级、学生查自己")
	@GetMapping(value = "/getMyReceive")
	public Result<List<Map<String, Object>>> getMyReceive(HttpServletRequest request) {
		try {
			// 1. 获取当前登录用户（和征订表完全一致）
			Subject subject = SecurityUtils.getSubject();
			if (subject == null || !subject.isAuthenticated()) {
				log.warn("用户未登录，无法获取领取记录");
				return Result.error("用户未登录，无法获取领取记录");
			}
			LoginUser loginUser = (LoginUser) subject.getPrincipal();
			if (loginUser == null) {
				log.warn("未获取到当前登录用户信息");
				return Result.error("未获取到当前登录用户信息");
			}

			log.info("当前登录用户: {}，角色码: {}", loginUser.getUsername(), loginUser.getRoleCode());

			// 2. 解析角色编码，判断用户类型（兼容多角色逗号分隔，和征订表一致）
			String roleCodeStr = loginUser.getRoleCode();
			boolean isAdmin = false;
			boolean isCounselor = false;
			if (roleCodeStr != null && !roleCodeStr.isEmpty()) {
				String[] roleCodes = roleCodeStr.split(",");
				for (String code : roleCodes) {
					code = code.trim();
					if ("admin".equals(code)) {
						isAdmin = true;
						break; // 管理员优先级最高
					}
					if ("counselor".equals(code)) {
						isCounselor = true;
					}
				}
			}
			// 管理员用户名兜底判断（和征订表一致）
			if (!isAdmin && "admin".equals(loginUser.getUsername())) {
				isAdmin = true;
			}

			List<Map<String, Object>> receiveList = new ArrayList<>();
			if (isAdmin) {
				// 管理员：查询所有领取记录（使用视图）
				receiveList = jdbcTemplate
						.queryForList("SELECT * FROM v_receive_with_details ORDER BY receiveTime DESC");
				log.info("管理员模式，查询到{}条领取记录", receiveList.size());
			} else if (isCounselor) {
				// ========== 辅导员逻辑（完全仿照征订表） ==========
				// 步骤1：通过sys_user.id查询辅导员信息
				QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
				counselorWrapper.eq("user_id", loginUser.getId()); // t_counselor的userId关联sys_user.id
				TCounselor counselor = tCounselorService.getOne(counselorWrapper);
				if (counselor == null) {
					log.warn("当前登录用户未关联辅导员信息，用户ID: {}", loginUser.getId());
					return Result.error("当前登录用户未关联辅导员信息");
				}

				// 步骤2：查询该辅导员管理的所有班级
				QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
				classWrapper.eq("counselor_id", counselor.getId()); // t_class的counselorId关联t_counselor.id
				List<TClass> classList = tClassService.list(classWrapper);
				if (classList.isEmpty()) {
					log.info("辅导员{}暂无管理的班级，无领取记录", counselor.getCounselorName());
					return Result.OK("你暂无管理的班级，无领取记录", receiveList);
				}
				// 提取班级ID列表
				List<String> classIds = classList.stream().map(TClass::getId).collect(Collectors.toList());

				// 步骤3：查询这些班级下的所有学生
				QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
				studentWrapper.in("class_id", classIds); // t_student的classId关联t_class.id
				List<TStudent> studentList = tStudentService.list(studentWrapper);
				if (studentList.isEmpty()) {
					log.info("辅导员{}管理的班级暂无学生，无领取记录", counselor.getCounselorName());
					return Result.OK("你管理的班级暂无学生，无领取记录", receiveList);
				}
				// 提取学生ID列表（t_receive的receive_operator关联t_student.id）
				List<String> studentIds = studentList.stream().map(TStudent::getId).collect(Collectors.toList());

				// 步骤4：查询这些学生的所有领取记录（使用视图）
				String studentIdInClause = String.join(",",
						studentIds.stream().map(id -> "'" + id + "'").collect(Collectors.toList()));
				receiveList = jdbcTemplate
						.queryForList("SELECT * FROM v_receive_with_details WHERE receiveOperator IN ("
								+ studentIdInClause + ") ORDER BY receiveTime DESC");
				log.info("辅导员{}模式，查询到管理班级下{}条领取记录", counselor.getCounselorName(), receiveList.size());
			} else {
				// ========== 学生逻辑（仿照征订表，替换为领取表字段） ==========
				String username = loginUser.getUsername();
				if (username == null || username.isEmpty()) {
					log.warn("当前登录用户无用户名（学号）信息");
					return Result.error("当前登录用户无用户名（学号）信息");
				}

				// 通过学号查询学生信息
				QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
				studentWrapper.eq("student_id", username);
				TStudent student = tStudentService.getOne(studentWrapper);
				if (student == null) {
					log.warn("当前登录用户未关联学生信息，用户名: {}", username);
					return Result.error("当前登录用户未关联学生信息，用户名: " + username);
				}

				// 查询该学生的领取记录（receive_operator=学生id）（使用视图）
				receiveList = jdbcTemplate.queryForList(
						"SELECT * FROM v_receive_with_details WHERE receiveOperator = ? ORDER BY receiveTime DESC",
						student.getId());
				log.info("学生模式，查询到{}条领取记录", receiveList.size());
			}

		// 征订学年和学期已由视图提供，用于筛选

			// 征订学年和学期筛选（基于原始字典码）
			String reqYear = request.getParameter("subscriptionYear");
			String reqSemester = request.getParameter("subscriptionSemester");
			if (oConvertUtils.isNotEmpty(reqYear) || oConvertUtils.isNotEmpty(reqSemester)) {
				receiveList.removeIf(record -> {
					if (oConvertUtils.isNotEmpty(reqYear)) {
						Object yearObj = record.get("subscriptionYear");
						if (yearObj == null || !reqYear.equals(yearObj.toString())) {
							return true;
						}
					}
					if (oConvertUtils.isNotEmpty(reqSemester)) {
						Object semObj = record.get("subscriptionSemester");
						if (semObj == null || !reqSemester.equals(semObj.toString())) {
							return true;
						}
					}
					return false;
				});
			}
		// 征订学期归一化为显示文字（筛选后执行）
			for (Map<String, Object> record : receiveList) {
				Object semObj = record.get("subscriptionSemester");
				if (semObj != null) {
					String s = semObj.toString().trim();
					if ("1".equals(s) || "一".equals(s) || "第一学期".equals(s)) {
						record.put("subscriptionSemester", "第一学期");
					} else if ("2".equals(s) || "二".equals(s) || "第二学期".equals(s)) {
						record.put("subscriptionSemester", "第二学期");
					}
				}
			}
			return Result.OK("", receiveList);
		} catch (Exception e) {
			log.error("获取领取记录失败", e);
			return Result.error("获取失败：" + e.getMessage());
		}
	}


	private void addParamFilter(HttpServletRequest request, StringBuilder sql, String param, String prefix, String suffix) {
		String val = request.getParameter(param);
		if (oConvertUtils.isNotEmpty(val)) sql.append(" AND ").append(prefix).append(val).append(suffix);
	}

	private void addParamLikeFilter(HttpServletRequest request, StringBuilder sql, String param, String col) {
		String val = request.getParameter(param);
		if (oConvertUtils.isNotEmpty(val)) {
			String key = val.trim();
			if (col.contains("IN (SELECT"))
				sql.append(" AND ").append(col.replace("%", key));
			else if (col.contains("%"))
				sql.append(" AND ").append(col.replace("%", key));
			else
				sql.append(" AND ").append(col).append(" LIKE '%").append(key).append("%'");
		}
	}

	private void writeJson(HttpServletResponse response, String msg) {
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write("{\"success\":false,\"message\":\"" + msg + "\"}");
		} catch (Exception ignored) {}
	}

	private String str(Object obj) {
		return obj == null ? "" : obj.toString();
	}

}
