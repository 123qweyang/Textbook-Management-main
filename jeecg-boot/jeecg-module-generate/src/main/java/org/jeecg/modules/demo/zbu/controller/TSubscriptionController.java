package org.jeecg.modules.demo.zbu.controller;

import java.math.BigDecimal;
import java.util.*;
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
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecgframework.poi.excel.view.JeecgMapExcelView;
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
					tReceiveService.save(receive);
					log.info("编辑征订状态为已征订，创建领取记录成功，征订ID：{}", subscriptionId);
					return Result.OK("编辑成功！并已创建领取记录，同步更新个人账单");
				}
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
	@DeleteMapping(value = "/deleteBatch")
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {
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
	 * 导出excel
	 *
	 * @param request
	 * @param tSubscription
	 */
	@RequiresPermissions("zbu:t_subscription:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TSubscription tSubscription) {
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		ExportParams exportParams = new ExportParams("征订表", "征订表数据");
		mv.addObject(NormalExcelConstants.PARAMS, exportParams);
		mv.addObject(NormalExcelConstants.CLASS, TSubscription.class);

		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		List<TSubscription> list = new ArrayList<>();

		if (loginUser != null) {
			SysUser currentUser = sysUserService.getUserByName(loginUser.getUsername());
			String userRoleType = getUserRoleType(currentUser.getId());
			StringBuilder sql = new StringBuilder("SELECT * FROM v_subscription_with_details WHERE 1=1");

			// --------- 权限过滤（查询视图时直接拼接条件）---------
			// 1. 学生：只看自己
			if (STUDENT_ROLE_CODE.equals(userRoleType)) {
				TStudent student = tStudentService.lambdaQuery().eq(TStudent::getUserId, currentUser.getId()).one();
				if (student != null) {
					sql.append(" AND student_id = '").append(student.getId()).append("'");
				}
			}
			// 2. 辅导员：只看自己管理的班级
			else if (COUNSELOR_ROLE_CODE.equals(userRoleType)) {
				TCounselor counselor = tCounselorService.lambdaQuery().eq(TCounselor::getUserId, currentUser.getId())
						.one();
				if (counselor != null) {
					List<TClass> classList = tClassService.lambdaQuery().eq(TClass::getCounselorId, counselor.getId())
							.list();
					if (!classList.isEmpty()) {
						List<String> studentIds = tStudentService.lambdaQuery()
								.in(TStudent::getClassId, classList)
								.list().stream().map(TStudent::getId).toList();
						if (!studentIds.isEmpty()) {
							String ids = String.join("','", studentIds);
							sql.append(" AND student_id IN ('").append(ids).append("')");
						}
					}
				}
			}
			// 3. 管理员：查询全部视图数据

			// --------- 直接查询视图（自动带学院名称）---------
			list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(TSubscription.class));
		}

		// 填充学生姓名
		for (TSubscription subscription : list) {
			if (oConvertUtils.isNotEmpty(subscription.getStudentId())) {
				try {
					TStudent student = tStudentService.getById(subscription.getStudentId());
					if (student != null) {
						subscription.setStudentName(student.getStudentName());
					}
				} catch (Exception e) {
					log.warn("查询学生姓名失败：{}", e.getMessage());
				}
			}
		}

		mv.addObject(NormalExcelConstants.DATA_LIST, list);
		return mv;
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
		MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
		for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
			MultipartFile file = entity.getValue();
			ImportParams params = new ImportParams();
			params.setTitleRows(2);
			params.setHeadRows(1);
			params.setNeedSave(true);
			try {
				List<TSubscription> list = ExcelImportUtil.importExcel(file.getInputStream(), TSubscription.class, params);
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
				return ImportErrorExportUtil.buildErrorResult(failMsgList, "导入完成！成功导入0条有效数据", uploadPath, "征订表");
			} finally {
				try { file.getInputStream().close(); } catch (IOException e) { e.printStackTrace(); }
			}
		}
		return Result.error("文件导入失败！");
	}

	/**
	 * 获取当前登录学生的征订记录（核心接口）
	 */
	@AutoLog(value = "征订表-获取我的征订记录")
	@Operation(summary = "获取当前登录学生的征订记录", description = "仅返回当前登录学生本人的征订记录")
	@GetMapping(value = "/getMySubscription")
	public Result<List<Map<String, Object>>> getMySubscription(
			@RequestParam(name = "subscriptionYear", required = false) String subscriptionYear,
			@RequestParam(name = "subscriptionSemester", required = false) String subscriptionSemester,
			@RequestParam(name = "className", required = false) String className,
			@RequestParam(name = "majorName", required = false) String majorName,
			@RequestParam(name = "studentId", required = false) String studentId) {
		try {
			// 1. 获取当前登录用户（LoginUser）
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

			// 2. 解析角色编码，判断用户类型（admin > 辅导员 > 学生）
			String roleCodeStr = loginUser.getRoleCode();
			boolean isAdmin = false;
			boolean isCounselor = false;
			// 角色判断（兼容多角色逗号分隔的情况）
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
			// 管理员用户名兜底判断
			if (!isAdmin && "admin".equals(loginUser.getUsername())) {
				isAdmin = true;
			}

			// 3. 构建WHERE条件（注意：视图中的列名是驼峰格式）
			List<String> whereConditions = new ArrayList<>();

			// 征订学年筛选
			if (oConvertUtils.isNotEmpty(subscriptionYear)) {
				whereConditions.add("subscriptionYear = '" + subscriptionYear + "'");
			}

			// 征订学期筛选
			if (oConvertUtils.isNotEmpty(subscriptionSemester)) {
				whereConditions.add("subscriptionSemester = '" + subscriptionSemester + "'");
			}

			String whereClause = whereConditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", whereConditions);

			List<Map<String, Object>> subList = new ArrayList<>();
			if (isAdmin) {
				// 管理员：查询所有征订记录（使用视图）
				subList = jdbcTemplate
						.queryForList("SELECT * FROM v_subscription_with_details" + whereClause
								+ " ORDER BY createTime DESC");
				log.info("管理员模式，查询到{}条征订记录", subList.size());
			} else if (isCounselor) {
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
					log.info("辅导员{}暂无管理的班级，无征订记录", counselor.getCounselorName());
					return Result.OK("你暂无管理的班级，无征订记录", subList);
				}
				// 提取班级ID列表
				List<String> classIds = classList.stream().map(TClass::getId).collect(Collectors.toList());

				// 步骤3：查询这些班级下的所有学生
				QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
				studentWrapper.in("class_id", classIds); // t_student的classId关联t_class.id
				List<TStudent> studentList = tStudentService.list(studentWrapper);
				if (studentList.isEmpty()) {
					log.info("辅导员{}管理的班级暂无学生，无征订记录", counselor.getCounselorName());
					return Result.OK("你管理的班级暂无学生，无征订记录", subList);
				}
				// 提取学生ID列表（t_subscription的studentId关联t_student.id）
				List<String> studentIds = studentList.stream().map(TStudent::getId).collect(Collectors.toList());

				// 步骤4：查询这些学生的所有征订记录（使用视图）
				String studentIdInClause = String.join(",",
						studentIds.stream().map(id -> "'" + id + "'").collect(Collectors.toList()));
				subList = jdbcTemplate.queryForList("SELECT * FROM v_subscription_with_details WHERE student_id IN ("
						+ studentIdInClause + ")" + (whereClause.isEmpty() ? "" : whereClause)
						+ " ORDER BY createTime DESC");
				log.info("辅导员{}模式，查询到管理班级下{}条征订记录", counselor.getCounselorName(), subList.size());
			} else {
				// 学生逻辑（保持原有不变）
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

				// 查询该学生的征订记录（使用视图）
				StringBuilder studentSql = new StringBuilder(
						"SELECT * FROM v_subscription_with_details WHERE student_id = ?");
				List<Object> params = new ArrayList<>();
				params.add(student.getId());

				if (oConvertUtils.isNotEmpty(subscriptionYear)) {
					studentSql.append(" AND subscriptionYear = ?");
					params.add(subscriptionYear);
				}
				if (oConvertUtils.isNotEmpty(subscriptionSemester)) {
					studentSql.append(" AND subscriptionSemester = ?");
					params.add(subscriptionSemester);
				}
				studentSql.append(" ORDER BY subscribeTime DESC");

				Object[] paramArray = params.toArray();
				subList = jdbcTemplate.queryForList(studentSql.toString(), paramArray);
				log.info("学生模式，查询到{}条征订记录", subList.size());
			}

			return Result.OK("", subList);
		} catch (Exception e) {
			log.error("获取征订记录失败", e);
			return Result.error("获取失败：" + e.getMessage());
		}
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

			// 5. 批量更新征订表状态
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
			// 7. 当同意征订时，为每条征订记录创建领取记录
			int receiveCreateCount = 0;
			if ("1".equals(subscribeStatus)) { // 1表示已征订/同意征订
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
						receiveList.add(receive);
						receiveCreateCount++;
					}
				}
				if (!receiveList.isEmpty()) {
					tReceiveService.saveBatch(receiveList);
					log.info("批量创建领取记录成功，共创建{}条", receiveList.size());
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

			// 5. 创建领取记录
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
