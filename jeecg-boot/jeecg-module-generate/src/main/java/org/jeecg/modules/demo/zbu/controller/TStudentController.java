package org.jeecg.modules.demo.zbu.controller;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.query.QueryRuleEnum;
import org.jeecg.common.util.PasswordUtil;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.base.service.BaseCommonService;
import org.jeecg.modules.demo.zbu.entity.*;
import org.jeecg.modules.demo.zbu.service.*;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.springframework.beans.factory.annotation.Value;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.util.Md5Util;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUserDepart;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.entity.enmus.ExcelType;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
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
 * @Description: 学生表
 * @Author: jeecg-boot
 * @Date: 2026-01-19
 * @Version: V1.0
 */
@Tag(name = "学生表")
@RestController
@RequestMapping("/zbu/tStudent")
@Slf4j
public class TStudentController extends JeecgController<TStudent, ITStudentService> {
	@Autowired
	private ITStudentService tStudentService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private BaseCommonService baseCommonService;
	// 在TStudentController的@Autowired区域新增以下注入
	@Autowired
	private ITTextbookSelectionService tTextbookSelectionService;
	@Autowired
	private ITSubscriptionService tSubscriptionService;
	@Autowired
	private ITReceiveService tReceiveService;
	@Autowired
	private IStudentBillService studentBillService;
	@Autowired
	private ITTextbookService tTextbookService;
	@Autowired
	private ITMajorService tMajorService;
	@Autowired
	private ITClassService tClassService;
	@Autowired
	private ITCounselorService tCounselorService;
	// 注入JdbcTemplate用于执行原生SQL查询视图
	@Autowired
	private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

	// 学生角色编码
	private static final String STUDENT_ROLE_CODE = "student";

	@Value("${jeecg.path.upload}")
	private String uploadPath;

	/**
	 * 分页列表查询
	 *
	 * @param tStudent
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	// @AutoLog(value = "学生表-分页列表查询")
	@Operation(summary = "学生表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TStudent>> queryPageList(TStudent tStudent,
												 @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
												 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
												 HttpServletRequest req) {

		// // 自定义查询规则
		// Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
		// // 自定义多选的查询规则为：LIKE_WITH_OR
		// customeRuleMap.put("status", QueryRuleEnum.LIKE_WITH_OR);
		// QueryWrapper<TStudent> queryWrapper =
		// QueryGenerator.initQueryWrapper(tStudent,
		// req.getParameterMap(),customeRuleMap);
		//
		// // 学院模糊查询
		// String collegeName = req.getParameter("collegeName");
		// if (oConvertUtils.isNotEmpty(collegeName)) {
		// queryWrapper.inSql("major_id", "SELECT id FROM t_major WHERE college_id IN
		// (SELECT id FROM t_college WHERE college_name LIKE CONCAT('%', '" +
		// collegeName + "', '%'))");
		// }

		// 获取当前登录用户
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String username = loginUser.getUsername();

		// 判断是否是管理员
		boolean isAdmin = "admin".equals(username) || "sysadmin".equals(username);

		// 判断是否是辅导员
		boolean isCounselor = false;
		List<SysUserRole> userRoleList = sysUserRoleService
				.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, loginUser.getId()));
		if (!userRoleList.isEmpty()) {
			List<String> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
			List<SysRole> roleList = sysRoleService.listByIds(roleIds);
			for (SysRole role : roleList) {
				if ("counselor".equals(role.getRoleCode())) {
					isCounselor = true;
					break;
				}
			}
		}

		// 自定义查询规则
		Map<String, QueryRuleEnum> customeRuleMap = new HashMap<>();
		// 自定义多选的查询规则为：LIKE_WITH_OR
		customeRuleMap.put("status", QueryRuleEnum.LIKE_WITH_OR);

		// 移除majorId和classId参数，避免QueryGenerator创建错误的查询条件
		Map<String, String[]> paramMap = new HashMap<>(req.getParameterMap());
		String[] majorNameParams = paramMap.remove("majorName");
		String[] classNameParams = paramMap.remove("className");

		QueryWrapper<TStudent> queryWrapper = QueryGenerator.initQueryWrapper(tStudent, paramMap,
				customeRuleMap);

		// 学院模糊查询
		String collegeName = req.getParameter("collegeName");
		if (oConvertUtils.isNotEmpty(collegeName)) {
			queryWrapper.inSql("major_id",
					"SELECT id FROM t_major WHERE college_id IN (SELECT id FROM t_college WHERE college_name LIKE CONCAT('%', '"
							+ collegeName + "', '%'))");
		}

		// 专业模糊查询
		if (majorNameParams != null && majorNameParams.length > 0) {
			String majorName = majorNameParams[0];
			if (oConvertUtils.isNotEmpty(majorName)) {
				queryWrapper.inSql("major_id",
						"SELECT id FROM t_major WHERE major_name LIKE CONCAT('%', '" + majorName + "', '%')");
			}
		}

		// 班级模糊查询
		if (classNameParams != null && classNameParams.length > 0) {
			String className = classNameParams[0];
			if (oConvertUtils.isNotEmpty(className)) {
				queryWrapper.inSql("class_id",
						"SELECT id FROM t_class WHERE class_name LIKE CONCAT('%', '" + className + "', '%')");
			}
		}

		// 辅导员逻辑：只显示自己管理的班级学生
		if (isCounselor && !isAdmin) {
			// 优化：使用视图查询，减少多次关联查询
			String sql = "SELECT student_id FROM v_student_with_counselor WHERE counselor_user_id = ?";
			List<Map<String, Object>> resultList = jdbcTemplate.queryForList(sql, loginUser.getId());

			if (!resultList.isEmpty()) {
				// 提取学生ID列表
				List<String> studentIds = resultList.stream()
						.map(item -> item.get("student_id").toString())
						.collect(Collectors.toList());
				// 过滤学生表，只显示这些学生
				queryWrapper.in("id", studentIds);
			} else {
				// 没有学生数据，返回空结果
				Page<TStudent> page = new Page<TStudent>(pageNo, pageSize);
				IPage<TStudent> pageList = new Page<>(pageNo, pageSize);
				pageList.setRecords(new ArrayList<>());
				pageList.setTotal(0);
				return Result.OK(pageList);
			}
		}

		Page<TStudent> page = new Page<TStudent>(pageNo, pageSize);

		IPage<TStudent> pageList = tStudentService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 添加
	 *
	 * @param tStudent
	 * @return
	 */
	@AutoLog(value = "学生表-添加")
	@Operation(summary = "学生表-添加")
	@RequiresPermissions("zbu:t_student:add")
	@PostMapping(value = "/add")
	public Result<String> add(@RequestBody TStudent tStudent) {
		try {
			String studentNo = tStudent.getStudentId();
			if (oConvertUtils.isEmpty(studentNo)) {
				return Result.error("学生学号不能为空！");
			}

			studentNo = studentNo.trim();

			// 检查学号是否已存在于学生表
			QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
			studentWrapper.eq("student_id", studentNo);
			if (tStudentService.count(studentWrapper) > 0) {
				return Result.error("添加失败：学号已存在！");
			}

			// 检查学号是否已存在于系统用户表（因为学号用作用户名）
			QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
			userWrapper.eq("username", studentNo);
			if (sysUserService.count(userWrapper) > 0) {
				return Result.error("添加失败：学号已存在系统账号！");
			}

			// 获取当前登录用户
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			String username = loginUser.getUsername();

			// 判断是否是管理员
			boolean isAdmin = "admin".equals(username) || "sysadmin".equals(username);

			// 判断是否是辅导员
			boolean isCounselor = false;
			List<SysUserRole> userRoleList = sysUserRoleService
					.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, loginUser.getId()));
			if (!userRoleList.isEmpty()) {
				List<String> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
				List<SysRole> roleList = sysRoleService.listByIds(roleIds);
				for (SysRole role : roleList) {
					if ("counselor".equals(role.getRoleCode())) {
						isCounselor = true;
						break;
					}
				}
			}

			// 辅导员逻辑：检查学生的班级是否属于该辅导员管理
			if (isCounselor && !isAdmin) {
				// 步骤1：通过sys_user.id查询辅导员信息
				QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
				counselorWrapper.eq("user_id", loginUser.getId());
				TCounselor counselor = tCounselorService.getOne(counselorWrapper);
				if (counselor != null) {
					// 步骤2：查询该辅导员管理的所有班级
					QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
					classWrapper.eq("counselor_id", counselor.getId());
					List<TClass> classList = tClassService.list(classWrapper);
					if (!classList.isEmpty()) {
						// 提取班级ID列表
						List<String> classIds = classList.stream().map(TClass::getId).collect(Collectors.toList());
						// 步骤3：检查学生的班级是否在该辅导员管理的班级列表中
						if (!classIds.contains(tStudent.getClassId())) {
							return Result.error("您只能添加自己管理班级的学生！");
						}
					}
				}
			}

			// 1. 先创建系统用户（sys_user）的登录账号
			SysUser sysUser = new SysUser();
			sysUser.setUsername(studentNo);
			// 姓名去空格
			sysUser.setRealname(tStudent.getStudentName() != null ? tStudent.getStudentName().trim() : "");

			// 2. Jeecg官方逻辑：生成8位随机salt + 原生加密（核心修正）
			String plainPwd = studentNo + "Zbu1"; // 明文密码写死Zbu1
			String salt = oConvertUtils.randomGen(8); // 生成8位随机salt（和admin/jeecg账号逻辑一致）
			sysUser.setSalt(salt); // 设置随机salt到用户表
			// 用Jeecg原生加密：encrypt(用户名, 明文密码, 随机salt)，和登录验证逻辑完全匹配
			String encryptedPwd = PasswordUtil.encrypt(sysUser.getUsername(), plainPwd, salt);
			sysUser.setPassword(encryptedPwd); // 设置加密后的密码

			// 3. 补充系统默认字段（修复重复设置status问题）
			sysUser.setDepartIds("1"); // 赋值为你系统中存在的部门ID（如根部门ID，多个用,分隔）
			sysUser.setStatus(1); // 账号启用（仅设置一次）
			sysUser.setDelFlag(CommonConstant.DEL_FLAG_0); // 0=未删除
			sysUser.setCreateTime(new Date()); // 创建时间
			sysUser.setLastPwdUpdateTime(new Date()); // 最后修改密码时间
			sysUser.setOrgCode(null); // 对齐系统逻辑，org_code设为null

			// 4. 保存sys_user并校验
			boolean saveSysUserSuccess = sysUserService.save(sysUser);
			if (!saveSysUserSuccess) {
				return Result.error("创建登录账号失败！");
			}
			System.out.println("sys_user保存结果：" + saveSysUserSuccess);

			// 5. 通过角色编码（student）查询角色ID
			QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
			roleWrapper.eq("role_code", STUDENT_ROLE_CODE); // 按角色编码查询
			SysRole studentRole = sysRoleService.getOne(roleWrapper); // 获取唯一角色
			if (studentRole == null) {
				return Result.error("未找到编码为【student】的学生角色，请先在角色管理中创建！");
			}

			// 6. 绑定用户-角色关系（核心：给新增用户分配学生角色）
			SysUserRole userRole = new SysUserRole();
			userRole.setUserId(sysUser.getId()); // 新创建的用户ID
			userRole.setRoleId(studentRole.getId()); // 学生角色ID
			sysUserRoleService.save(userRole);

			// 7. 把sys_user的ID关联到学生表并保存
			tStudent.setUserId(sysUser.getId());
			tStudentService.save(tStudent);

			generateStudentSubscription(tStudent);

			return Result.OK("添加成功", encryptedPwd);
		} catch (Exception e) {
			log.error("新增学生失败：", e);
			return Result.error("添加失败：" + e.getMessage());
		}
	}

	/**
	 * 编辑
	 *
	 * @param tStudent
	 * @return
	 */
	@AutoLog(value = "学生表-编辑")
	@Operation(summary = "学生表-编辑")
	@RequiresPermissions("zbu:t_student:edit")
	@RequestMapping(value = "/edit", method = { RequestMethod.PUT, RequestMethod.POST })
	@Transactional(rollbackFor = Exception.class)
	public Result<String> edit(@RequestBody TStudent tStudent) {
		try {
			// 1. 查询修改前的学生信息
			TStudent oldStudent = tStudentService.getById(tStudent.getId());
			if (oldStudent == null) {
				return Result.error("学生记录不存在！");
			}

			String oldMajorId = oldStudent.getMajorId();
			String oldClassId = oldStudent.getClassId();
			String newMajorId = tStudent.getMajorId();
			String newClassId = tStudent.getClassId();

			// 2. 执行更新
			tStudentService.updateById(tStudent);

			// 3. 判断专业或班级是否发生变更
			boolean majorChanged = (oldMajorId == null && newMajorId != null)
					|| (oldMajorId != null && !oldMajorId.equals(newMajorId));
			boolean classChanged = (oldClassId == null && newClassId != null)
					|| (oldClassId != null && !oldClassId.equals(newClassId));

			if (majorChanged || classChanged) {
				log.info("学生{}（学号{}）专业或班级变更：专业 {} -> {}，班级 {} -> {}，开始生成当前学年当前学期征订记录",
						tStudent.getId(), oldStudent.getStudentId(),
						oldMajorId, newMajorId, oldClassId, newClassId);

				// 4. 重新查询更新后的完整学生信息（确保拿到最新数据）
				TStudent updatedStudent = tStudentService.getById(tStudent.getId());

				// 5. 仅为当前学年（上下学期）生成新征订记录（旧记录保留不删除）
				generateCurrentYearSubscription(updatedStudent);

				log.info("学生{}（学号{}）转专业/班级后当前学年当前学期征订记录生成完成", updatedStudent.getId(), updatedStudent.getStudentId());
			}

			return Result.OK("编辑成功!");
		} catch (Exception e) {
			log.error("编辑学生失败：", e);
			return Result.error("编辑失败：" + e.getMessage());
		}
	}

	/**
	 * 通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "学生表-通过id删除")
	@Operation(summary = "学生表-通过id删除")
	@RequiresPermissions("zbu:t_student:delete")
	@DeleteMapping(value = "/delete")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> delete(@RequestParam(name = "id", required = true) String id) {
		try {
			log.info("开始删除学生：id={}", id);
			// 前置步骤：查询学生获取关联的userId（学生删除特有，其余步骤完全复刻）
			TStudent student = tStudentService.getById(id);
			if (student == null) {
				log.warn("删除失败：学生记录不存在，id={}", id);
				return Result.error("学生记录不存在！");
			}
			log.info("查询到学生信息：{}", JSON.toJSONString(student));

			// ========== 以下完全复刻你提供的单个用户删除逻辑（仅把id换成student.getUserId()） ==========
			if (student.getUserId() != null && !student.getUserId().isEmpty()) {
				String userId = student.getUserId(); // 学生关联的用户ID，替代单个删除的id

				// 步骤1：先删关联数据（和你提供的代码完全一致）
				int delRole = sysUserRoleService.getBaseMapper().delete(
						new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
				// 注意：如果你的项目没有sysUserDepartService，直接注释这两行！！！
				// int delDepart = sysUserDepartService.getBaseMapper().delete(
				// new LambdaQueryWrapper<SysUserDepart>().eq(SysUserDepart::getUserId, userId)
				// );
				int delDepart = 0; // 无sysUserDepartService时，赋值0避免报错
				log.info("删除关联数据：用户-角色({}条)、用户-部门({}条)", delRole, delDepart);

				// 步骤2：核心修复！用LambdaUpdateWrapper更新del_flag（和你提供的代码完全一致）
				SysUserMapper sysUserMapper = (SysUserMapper) sysUserService.getBaseMapper();
				int updateDelFlag = sysUserMapper.update(
						new SysUser(),
						new LambdaUpdateWrapper<SysUser>()
								.set(SysUser::getDelFlag, "1")
								.eq(SysUser::getId, userId));
				log.info("原生SQL更新del_flag：id={}，影响行数={}", userId, updateDelFlag);

				if (updateDelFlag <= 0) {
					log.error("强制更新del_flag失败：ID={}（可能ID不存在/字段映射错误）", userId);
					// 直接跳过del_flag，执行终极删除（和你提供的代码完全一致）
					log.warn("跳过del_flag标记，直接物理删除");
					int delUser = sysUserMapper.delete(
							new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, userId));
					if (delUser <= 0) {
						return Result.error("删除失败：用户ID不存在或字段映射错误！");
					}
					baseCommonService.addLog("直接物理删除用户：id=" + userId, CommonConstant.LOG_TYPE_2, 3);
					// 不直接return，继续删除学生表（学生删除特有）
				} else {
					// 步骤3：调用deleteLogicDeleted删除（和你提供的代码完全一致）
					List<String> userIds = Collections.singletonList(userId);
					int delUser = sysUserMapper.deleteLogicDeleted(userIds);
					if (delUser <= 0) {
						// 兜底：再次直接删除（和你提供的代码完全一致）
						delUser = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, userId));
					}
					log.info("彻底删除用户成功：id={}，影响行数={}", userId, delUser);
					baseCommonService.addLog("彻底删除用户：id=" + userId, CommonConstant.LOG_TYPE_2, 3);
				}
			}

			// 级联删除学生的征订记录、领取记录、账单记录
			String studentId = student.getId();
			String studentNo = student.getStudentId(); // 学号，账单表中存储的是学号
			log.info("开始级联删除学生的业务记录：学生id={}，学号={}", studentId, studentNo);

			// 1. 删除征订表记录（按student_id，即学生主键ID）
			int delSubscription = tSubscriptionService.getBaseMapper().delete(
					new LambdaQueryWrapper<TSubscription>().eq(TSubscription::getStudentId, studentId));
			log.info("删除征订记录：{}条", delSubscription);

			// 2. 删除领取表记录（按receive_operator，即学生id）
			int delReceive = tReceiveService.getBaseMapper().delete(
					new LambdaQueryWrapper<TReceive>().eq(TReceive::getReceiveOperator, studentId));
			log.info("删除领取记录：{}条", delReceive);

			// 3. 删除个人账单记录（按student_id，但账单表存储的是学号studentNo）
			int delBill = studentBillService.getBaseMapper().delete(
					new LambdaQueryWrapper<StudentBill>().eq(StudentBill::getStudentId, studentNo));
			log.info("删除个人账单记录：{}条", delBill);

			// 学生删除特有步骤：物理删除学生表（保持原有校验逻辑）
			int delStudent = tStudentService.getBaseMapper().deleteById(id);
			log.info("物理删除学生表记录：id={}，影响行数={}", id, delStudent);
			if (delStudent <= 0) {
				throw new RuntimeException("学生表记录物理删除失败（无记录被影响）");
			}

			return Result.OK("删除成功! 数据库中已无该学生及关联用户、征订、领取、账单记录");
		} catch (Exception e) {
			log.error("删除学生失败：id={}", id, e);
			return Result.error("删除失败：" + e.getMessage());
		}
	}

	/**
	 * 批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "学生表-批量删除")
	@Operation(summary = "学生表-批量删除")
	@RequiresPermissions("zbu:t_student:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> deleteBatch(@RequestParam(name = "ids", required = true) String ids) {

		try {
			log.info("开始批量删除学生：ids={}", ids);
			// 1. 拆分批量删除的学生ID（处理空格、空字符串）
			List<String> studentIds = Arrays.stream(ids.split(","))
					.map(String::trim)
					.filter(id -> !id.isEmpty())
					.collect(Collectors.toList());
			if (studentIds.isEmpty()) {
				return Result.error("批量删除的ID不能为空！");
			}
			log.info("处理后待删除的学生ID：{}", studentIds);

			// 2. 批量查询这些学生的userId
			List<TStudent> students = tStudentService.listByIds(studentIds);
			log.info("查询到待删除的学生数量：{}", students.size());
			if (students.isEmpty()) {
				return Result.error("未找到对应学生记录！");
			}

			// 3. 提取所有关联的userId（过滤空值）
			List<String> userIds = students.stream()
					.filter(student -> student.getUserId() != null && !student.getUserId().isEmpty())
					.map(TStudent::getUserId)
					.collect(Collectors.toList());
			log.info("提取到学生关联的用户ID数量：{}", userIds.size());

			// 4. 批量处理关联用户（完全复刻单个用户删除的核心逻辑，适配批量）
			if (!userIds.isEmpty()) {
				// 步骤1：批量删除用户关联数据（角色/部门，和单个删除逻辑一致）
				int delRole = sysUserRoleService.getBaseMapper().delete(
						new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds));
				// 注意：无sysUserDepartService则注释以下两行，赋值delDepart=0
				// int delDepart = sysUserDepartService.getBaseMapper().delete(
				// new LambdaQueryWrapper<SysUserDepart>().in(SysUserDepart::getUserId, userIds)
				// );
				int delDepart = 0; // 无用户部门表时赋值0，避免报错
				log.info("批量删除用户关联数据：角色({}条)、部门({}条)", delRole, delDepart);

				// 步骤2：批量更新用户del_flag=1（和单个删除的LambdaUpdateWrapper逻辑一致）
				SysUserMapper sysUserMapper = (SysUserMapper) sysUserService.getBaseMapper();
				int updateDelFlag = sysUserMapper.update(
						new SysUser(),
						new LambdaUpdateWrapper<SysUser>()
								.set(SysUser::getDelFlag, "1")
								.in(SysUser::getId, userIds) // 批量匹配ID
				);
				log.info("批量更新用户del_flag=1：待更新数量={}, 实际更新行数={}", userIds.size(), updateDelFlag);

				if (updateDelFlag <= 0) {
					log.error("批量更新用户del_flag失败：无用户被标记为逻辑删除");
					// 兜底：直接批量物理删除用户（和单个删除的兜底逻辑一致）
					log.warn("跳过del_flag标记，直接批量物理删除用户");
					int delUsers = sysUserMapper.delete(
							new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds));
					if (delUsers <= 0) {
						log.warn("批量兜底删除用户失败：无用户记录被删除");
					} else {
						log.info("批量兜底物理删除用户成功：影响行数={}", delUsers);
						baseCommonService.addLog("直接批量物理删除用户：ids=" + userIds, CommonConstant.LOG_TYPE_2, 3);
					}
				} else {
					// 步骤3：调用deleteLogicDeleted批量删除用户（和单个删除逻辑一致）
					int delUser = sysUserMapper.deleteLogicDeleted(userIds);
					log.info("调用deleteLogicDeleted批量删除用户：待删数量={}, 实际删除行数={}", userIds.size(), delUser);

					if (delUser <= 0) {
						// 兜底：再次批量物理删除用户
						log.warn("deleteLogicDeleted批量删除失败，执行兜底删除");
						delUser = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds));
						log.info("批量兜底删除用户成功：影响行数={}", delUser);
					}
					baseCommonService.addLog("彻底批量删除用户：ids=" + userIds, CommonConstant.LOG_TYPE_2, 3);
				}
			}

			// 级联删除学生的征订记录、领取记录、账单记录
			log.info("开始批量级联删除学生的业务记录：学生ids={}", studentIds);

			// 提取所有学生的学号（账单表存储的是学号）
			List<String> studentNos = students.stream()
					.map(TStudent::getStudentId)
					.filter(s -> oConvertUtils.isNotEmpty(s))
					.collect(Collectors.toList());
			log.info("待删除学生的学号列表：{}", studentNos);

			// 1. 批量删除征订表记录（按student_id，即学生主键ID）
			int delSubscription = tSubscriptionService.getBaseMapper().delete(
					new LambdaQueryWrapper<TSubscription>().in(TSubscription::getStudentId, studentIds));
			log.info("批量删除征订记录：{}条", delSubscription);

			// 2. 批量删除领取表记录（按receive_operator）
			int delReceive = tReceiveService.getBaseMapper().delete(
					new LambdaQueryWrapper<TReceive>().in(TReceive::getReceiveOperator, studentIds));
			log.info("批量删除领取记录：{}条", delReceive);

			// 3. 批量删除个人账单记录（按student_id，但账单表存储的是学号）
			if (!studentNos.isEmpty()) {
				int delBill = studentBillService.getBaseMapper().delete(
						new LambdaQueryWrapper<StudentBill>().in(StudentBill::getStudentId, studentNos));
				log.info("批量删除个人账单记录：{}条", delBill);
			}

			// 5. 批量物理删除学生表（彻底删库记录，替换原逻辑删除）
			int delStudents = tStudentService.getBaseMapper().deleteBatchIds(studentIds);
			log.info("批量物理删除学生表记录：待删数量={}, 实际删除行数={}", studentIds.size(), delStudents);

			if (delStudents <= 0) {
				throw new RuntimeException("学生表批量物理删除失败（无记录被影响）");
			}

			return Result.OK("批量删除成功! 数据库中已无该批学生及关联用户、征订、领取、账单记录");
		} catch (Exception e) {
			log.error("批量删除学生失败：ids={}", ids, e);
			return Result.error("批量删除失败：" + e.getMessage());
		}
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	// @AutoLog(value = "学生表-通过id查询")
	@Operation(summary = "学生表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TStudent> queryById(@RequestParam(name = "id", required = true) String id) {
		TStudent tStudent = tStudentService.getById(id);
		if (tStudent == null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tStudent);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tStudent
	 */
	@RequiresPermissions("zbu:t_student:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TStudent tStudent) {
		// 处理非TStudent实体的搜索字段（学院/专业/班级通过关联表查询）
		String collegeName = request.getParameter("collegeName");
		String majorName = request.getParameter("majorName");
		String className = request.getParameter("className");
		Map<String, String[]> paramMap = new HashMap<>(request.getParameterMap());
		if (majorName != null && !majorName.isEmpty()) {
			paramMap.remove("majorName");
		}
		if (className != null && !className.isEmpty()) {
			paramMap.remove("className");
		}
		QueryWrapper<TStudent> queryWrapper = QueryGenerator.initQueryWrapper(tStudent, paramMap);
		// 学院筛选（通过 major_id → college_id）
		if (collegeName != null && !collegeName.isEmpty()) {
			queryWrapper.inSql("major_id",
				"SELECT id FROM t_major WHERE college_id IN (SELECT id FROM t_college WHERE college_name LIKE '%" + collegeName + "%')");
		}
		// 专业筛选
		if (majorName != null && !majorName.isEmpty()) {
			queryWrapper.inSql("major_id",
				"SELECT id FROM t_major WHERE major_name LIKE '%" + majorName + "%'");
		}
		// 班级筛选
		if (className != null && !className.isEmpty()) {
			queryWrapper.inSql("class_id",
				"SELECT id FROM t_class WHERE class_name LIKE '%" + className + "%'");
		}
		List<TStudent> exportList = tStudentService.list(queryWrapper);

		LoginUser sysUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		ModelAndView mv = new ModelAndView(new JeecgEntityExcelView());
		mv.addObject(NormalExcelConstants.FILE_NAME, "学生表");
		mv.addObject(NormalExcelConstants.CLASS, TStudent.class);
		mv.addObject(NormalExcelConstants.PARAMS,
				new ExportParams("学生表报表", "导出人:" + (sysUser != null ? sysUser.getRealname() : "未知"), "学生表", ExcelType.XSSF));
		mv.addObject(NormalExcelConstants.DATA_LIST, exportList);
		return mv;
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_student:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 1. 获取上传的Excel文件
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要导入的Excel文件！");
			}

			// 获取当前登录用户
			LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
			String username = loginUser.getUsername();

			// 判断是否是管理员
			boolean isAdmin = "admin".equals(username) || "sysadmin".equals(username);

			// 判断是否是辅导员
			boolean isCounselor = false;
			List<SysUserRole> userRoleList = sysUserRoleService
					.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, loginUser.getId()));
			if (!userRoleList.isEmpty()) {
				List<String> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
				List<SysRole> roleList = sysRoleService.listByIds(roleIds);
				for (SysRole role : roleList) {
					if ("counselor".equals(role.getRoleCode())) {
						isCounselor = true;
						break;
					}
				}
			}

			// 辅导员逻辑：获取管理的班级列表
			List<String> counselorClassIds = new ArrayList<>();
			if (isCounselor && !isAdmin) {
				// 步骤1：通过sys_user.id查询辅导员信息
				QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
				counselorWrapper.eq("user_id", loginUser.getId());
				TCounselor counselor = tCounselorService.getOne(counselorWrapper);
				if (counselor != null) {
					// 步骤2：查询该辅导员管理的所有班级
					QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
					classWrapper.eq("counselor_id", counselor.getId());
					List<TClass> classList = tClassService.list(classWrapper);
					if (!classList.isEmpty()) {
						// 提取班级ID列表
						counselorClassIds = classList.stream().map(TClass::getId).collect(Collectors.toList());
					}
				}
			}

			// 预查询学生角色（只查一次）
			SysRole studentRole = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>()
					.eq(SysRole::getRoleCode, STUDENT_ROLE_CODE));
			if (studentRole == null) {
				return Result.error("系统未配置【student】角色，请先在角色管理中创建！");
			}

			// 基础导入参数配置
			ImportParams importParams = new ImportParams();
			importParams.setTitleRows(2);
			importParams.setHeadRows(1);
			importParams.setNeedSave(false);

			// 存储有效数据和失败信息
			List<TStudent> validStudentList = new ArrayList<>(); // 仅存合法数据
			List<String> failMsgList = new ArrayList<>();
			int totalRow = 0; // 记录Excel总行数（跳过标题）

			// 3. 遍历解析上传的Excel文件
			for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
				MultipartFile file = entry.getValue();
				if (file.isEmpty()) {
					continue;
				}

				// 4. 解析Excel为学生列表
				List<TStudent> tempList = ExcelImportUtil.importExcel(
						file.getInputStream(),
						TStudent.class,
						importParams);

				// 5. 逐行校验+过滤（核心：跳过错误/重复数据，继续导入正确的）
				for (int i = 0; i < tempList.size(); i++) {
					totalRow = i + 2; // Excel行号（标题行1行+表头1行，数据从第2行开始）
					TStudent student = tempList.get(i);

					// ========== 强制空值校验 ==========
					// 5.1 学号空值校验
					String studentId = student.getStudentId();
					if (oConvertUtils.isEmpty(studentId) || studentId.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：学号为空，跳过导入");
						continue;
					}
					student.setStudentId(studentId.trim()); // 去除学号空格

					// 5.2 学生姓名空值校验
					String studentName = student.getStudentName();
					if (oConvertUtils.isEmpty(studentName) || studentName.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：学生姓名为空（学号：" + studentId + "），跳过导入");
						continue;
					}
					student.setStudentName(studentName.trim()); // 去除姓名空格

					// 5.2.1 班级空值/存在性校验（支持按ID或名称查找）
					String classId = student.getClassId();

					if (oConvertUtils.isEmpty(classId) || classId.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：班级为空（学号：" + studentId + "），跳过导入");
						continue;
					}

					TClass existClass = tClassService.getById(classId);
					if (existClass == null) {
						// 按ID没找到，尝试按班级名称查找
						existClass = tClassService.getOne(new LambdaQueryWrapper<TClass>()
								.eq(TClass::getClassName, classId.trim()));
					}

					if (existClass == null) {
						failMsgList.add("第" + totalRow + "行：班级【" + classId + "】不存在（学号：" + studentId + "），跳过导入");
						continue;
					}
					// 更新classId为实际找到的班级ID
					classId = existClass.getId();
					student.setClassId(classId);

					// 5.2.2 专业空值/存在性校验（支持按ID或名称查找）
					String majorIdInput = student.getMajorId(); // Excel中原始输入值

					if (oConvertUtils.isNotEmpty(majorIdInput)) {
						// Excel中填写了专业，必须校验该专业是否存在
						TMajor inputMajor = tMajorService.getById(majorIdInput);
						if (inputMajor == null) {
							// 按ID没找到，尝试按专业名称查找
							inputMajor = tMajorService.getOne(new LambdaQueryWrapper<TMajor>()
									.eq(TMajor::getMajorName, majorIdInput.trim()));
						}

						if (inputMajor == null) {
							failMsgList
									.add("第" + totalRow + "行：专业【" + majorIdInput + "】不存在（学号：" + studentId + "），跳过导入");
							continue;
						}
						// 更新majorId为实际找到的专业ID
						majorIdInput = inputMajor.getId();
						student.setMajorId(majorIdInput);

						// 额外校验：Excel中的专业是否与班级关联的专业一致
						String classMajorId = existClass.getMajorId();
						if (oConvertUtils.isNotEmpty(classMajorId) && !classMajorId.equals(majorIdInput)) {
							TMajor classMajor = tMajorService.getById(classMajorId);
							failMsgList.add("第" + totalRow + "行：学号【" + studentId + "】的专业【"
									+ inputMajor.getMajorName() + "】与班级【" + existClass.getClassName()
									+ "】关联的专业【" + (classMajor != null ? classMajor.getMajorName() : classMajorId)
									+ "】不一致，跳过导入");
							continue;
						}
					} else {
						// Excel中没有填写专业，使用班级关联的专业
						String majorIdFromClass = existClass.getMajorId();
						if (oConvertUtils.isEmpty(majorIdFromClass)) {
							failMsgList.add("第" + totalRow + "行：班级【" + existClass.getClassName() + "】未关联专业且未指定专业（学号："
									+ studentId + "），跳过导入");
							continue;
						}
						student.setMajorId(majorIdFromClass);
					}

					// 5.3 辅导员权限校验：检查学生的班级是否属于该辅导员管理
					if (isCounselor && !isAdmin) {
						if (!counselorClassIds.contains(student.getClassId())) {
							failMsgList.add("第" + totalRow + "行：学号【" + studentId + "】不属于您管理的班级，跳过导入");
							continue;
						}
					}

					// 5.4 校验学号是否已存在于学生表
					QueryWrapper<TStudent> studentWrapper = new QueryWrapper<>();
					studentWrapper.eq("student_id", student.getStudentId());
					if (tStudentService.count(studentWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：学号【" + studentId + "】已存在，跳过导入");
						continue;
					}

					// 5.5 校验学号是否已存在于系统用户表
					SysUser existUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
							.eq(SysUser::getUsername, student.getStudentId())
							.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0));
					if (existUser != null) {
						failMsgList.add("第" + totalRow + "行：学号【" + studentId + "】已存在系统账号，跳过导入");
						continue;
					}

					// 5.6 创建系统用户
					SysUser sysUser = new SysUser();
					sysUser.setUsername(student.getStudentId()); // 学号作为登录名
					sysUser.setRealname(student.getStudentName()); // 姓名作为真实名
					// 密码加密
					String salt = oConvertUtils.randomGen(8);
					String plainPwd = student.getStudentId() + "Zbu1";
					String encryptedPwd = PasswordUtil.encrypt(sysUser.getUsername(), plainPwd, salt);
					sysUser.setSalt(salt);
					sysUser.setPassword(encryptedPwd);
					// 补充系统用户字段
					sysUser.setDepartIds("1"); // 根部门ID
					sysUser.setStatus(1); // 账号启用
					sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
					sysUser.setCreateTime(new Date());
					sysUser.setLastPwdUpdateTime(new Date());
					sysUser.setOrgCode(null);

					// 5.7 保存系统用户
					boolean saveUserOk = sysUserService.save(sysUser);
					if (!saveUserOk) {
						failMsgList.add("第" + totalRow + "行：学号【" + studentId + "】创建系统账号失败，跳过导入");
						continue;
					}

					// 5.8 绑定用户-角色关系
					SysUserRole userRole = new SysUserRole();
					userRole.setUserId(sysUser.getId());
					userRole.setRoleId(studentRole.getId());
					sysUserRoleService.save(userRole);

					// 5.9 关联用户ID到学生表
					student.setUserId(sysUser.getId());
					student.setCreateTime(new Date());

					// 加入有效列表
					validStudentList.add(student);
				}
			}

			// 6. 批量保存有效数据
			if (!validStudentList.isEmpty()) {
				tStudentService.saveBatch(validStudentList);

				// 7. 为每个成功导入的学生生成征订记录
				for (TStudent student : validStudentList) {
					try {
						generateStudentSubscription(student);
					} catch (Exception e) {
						log.warn("学生{}（学号：{}）生成征订记录失败：{}",
								student.getStudentName(), student.getStudentId(), e.getMessage());
					}
				}
			}

			// 8. 构建返回结果
			String successMsg = "导入完成！成功导入【" + validStudentList.size() + "】条有效数据";

			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "学生表");

		} catch (Exception e) {
			log.error("Excel导入学生数据失败", e);
			List<String> failMsgList = new ArrayList<>(); failMsgList.add("导入异常：" + e.getMessage()); return ImportErrorExportUtil.buildErrorResult(failMsgList, "导入完成！成功导入0条有效数据", uploadPath, "学生表");
		}
	}

	/**
	 * 通过Excel更新学生信息（按学号匹配）
	 */
	@RequiresPermissions("zbu:t_student:importExcel")
	@RequestMapping(value = "/updateByExcel", method = RequestMethod.POST)
	public Result<?> updateByExcel(HttpServletRequest request) {
		try {
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要上传的Excel文件！");
			}

			int updateCount = 0;
			List<String> failMsgList = new ArrayList<>();

			for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
				MultipartFile file = entry.getValue();
				if (file.isEmpty()) continue;

				ImportParams params = new ImportParams();
				params.setTitleRows(2);
				params.setHeadRows(1);
				params.setNeedSave(false);

				List<TStudent> list = ExcelImportUtil.importExcel(file.getInputStream(), TStudent.class, params);

				for (int i = 0; i < list.size(); i++) {
					TStudent tb = list.get(i);
					int rowNum = i + 3;

					// 学号必填
					if (oConvertUtils.isEmpty(tb.getStudentId())) {
						failMsgList.add("第" + rowNum + "行：学号为空，跳过更新");
						continue;
					}
					tb.setStudentId(tb.getStudentId().trim());

					// 按学号查找已有记录
					QueryWrapper<TStudent> queryWrapper = new QueryWrapper<>();
					queryWrapper.eq("student_id", tb.getStudentId());
					TStudent exist = tStudentService.getOne(queryWrapper);
					if (exist == null) {
						failMsgList.add("第" + rowNum + "行：学号【" + tb.getStudentId() + "】不存在，无法更新");
						continue;
					}

					boolean updated = false;
					if (oConvertUtils.isNotEmpty(tb.getStudentName())) {
						exist.setStudentName(tb.getStudentName().trim());
						updated = true;
					}

					// 专业校验（兼容名称/ID）
					String majorIdInput = tb.getMajorId();
					String resolvedMajorId = null;
					if (oConvertUtils.isNotEmpty(majorIdInput)) {
						TMajor major = tMajorService.getById(majorIdInput);
						if (major == null) {
							major = tMajorService.getOne(new LambdaQueryWrapper<TMajor>()
									.eq(TMajor::getMajorName, majorIdInput.trim()));
						}
						if (major == null) {
							failMsgList.add("第" + rowNum + "行：专业【" + majorIdInput + "】不存在（学号：" + tb.getStudentId() + "），跳过更新");
							continue;
						}
						resolvedMajorId = major.getId();
						updated = true;
					}

					// 班级校验（兼容名称/ID）
					String classIdInput = tb.getClassId();
					String resolvedClassId = null;
					if (oConvertUtils.isNotEmpty(classIdInput)) {
						TClass clazz = tClassService.getById(classIdInput);
						if (clazz == null) {
							clazz = tClassService.getOne(new LambdaQueryWrapper<TClass>()
									.eq(TClass::getClassName, classIdInput.trim()));
						}
						if (clazz == null) {
							failMsgList.add("第" + rowNum + "行：班级【" + classIdInput + "】不存在（学号：" + tb.getStudentId() + "），跳过更新");
							continue;
						}
						resolvedClassId = clazz.getId();
						updated = true;
					}

					// 记录变更前的专业/班级
					String oldMajorId = exist.getMajorId();
					String oldClassId = exist.getClassId();

					// 应用专业/班级更新
					if (resolvedMajorId != null) exist.setMajorId(resolvedMajorId);
					if (resolvedClassId != null) exist.setClassId(resolvedClassId);
					if (oConvertUtils.isNotEmpty(tb.getStatus())) {
						exist.setStatus(tb.getStatus());
						updated = true;
					}
					if (oConvertUtils.isNotEmpty(tb.getAdmissionYear())) {
						exist.setAdmissionYear(tb.getAdmissionYear());
						updated = true;
					}

					if (updated) {
						tStudentService.updateById(exist);
						updateCount++;

						// 检测专业/班级是否变更，若变更则生成当前学年征订记录
						String newMajorId = exist.getMajorId();
						String newClassId = exist.getClassId();
						boolean majorChanged = (oldMajorId == null && newMajorId != null)
								|| (oldMajorId != null && !oldMajorId.equals(newMajorId));
						boolean classChanged = (oldClassId == null && newClassId != null)
								|| (oldClassId != null && !oldClassId.equals(newClassId));

						if (majorChanged || classChanged) {
							log.info("学生{}（学号{}）通过Excel更新后专业/班级变更：专业 {} -> {}，班级 {} -> {}",
									exist.getId(), exist.getStudentId(),
									oldMajorId, newMajorId, oldClassId, newClassId);
							try {
								generateCurrentYearSubscription(exist);
							} catch (Exception e) {
								log.warn("更新学生{}后生成当前学年当前学期征订记录失败：{}", exist.getStudentId(), e.getMessage());
							}
						}
					} else {
						failMsgList.add("第" + rowNum + "行：学号【" + tb.getStudentId() + "】未提供任何要更新的字段，跳过");
					}
				}
			}

			String successMsg = "更新完成！成功更新【" + updateCount + "】条记录";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "学生表");

		} catch (Exception e) {
			log.error("通过Excel更新学生失败", e);
			return Result.error("更新失败：" + e.getMessage());
		}
	}

	/**
	 * 登录后获取当前用户的学生信息
	 */
	@Operation(summary = "获取当前登录学生的信息")
	@GetMapping("/getCurrentStudent")
	public Result<TStudent> getCurrentStudent() {
		// 1. 获取Shiro当前登录的Subject（核心）
		Subject subject = org.apache.shiro.SecurityUtils.getSubject();
		if (subject == null || !subject.isAuthenticated()) {
			return Result.error("用户未登录，请先登录");
		}

		// 2. 直接从Principal获取SysUser（绕过LoginUser，万能写法）
		Object principal = subject.getPrincipal();
		if (principal == null) {
			return Result.error("未获取到当前登录用户信息");
		}

		// 3. 适配不同版本：如果Principal是LoginUser，转成SysUser；如果直接是SysUser，直接用
		SysUser currentUser = null;
		if (principal instanceof SysUser) {
			// 版本1：Principal直接是SysUser（部分JeecgBoot版本）
			currentUser = (SysUser) principal;
		} else {
			// 版本2：Principal是LoginUser，用反射/强制转换获取SysUser（兼容所有版本）
			try {
				// 尝试调用getUser()（替代getSysUser()，多数版本用这个）
				Method getUserMethod = principal.getClass().getMethod("getUser");
				currentUser = (SysUser) getUserMethod.invoke(principal);
			} catch (Exception e) {
				// 兜底：如果getUser()也没有，提示版本适配问题
				return Result.error("当前JeecgBoot版本的LoginUser无getSysUser/getUser方法，请核对类结构");
			}
		}

		// 4. 根据sys_user.id查询学生表
		if (currentUser == null) {
			return Result.error("未解析到当前登录的系统用户信息");
		}
		TStudent student = tStudentService.lambdaQuery()
				.eq(TStudent::getUserId, currentUser.getId())
				.one();

		if (student == null) {
			return Result.error("当前登录用户不是学生，未找到对应信息");
		}
		return Result.OK(student);
	}

	/**
	 * 按学号查询学生信息
	 */
	@GetMapping(value = "/queryByStudentId")
	public Result<TStudent> queryByStudentId(@RequestParam(name = "studentId") String studentId) {
		QueryWrapper<TStudent> wrapper = new QueryWrapper<>();
		wrapper.eq("student_id", studentId);
		TStudent student = tStudentService.getOne(wrapper);
		if (student == null) {
			return Result.error("未找到对应学生");
		}
		return Result.OK(student);
	}

	/**
	 * 按学号查询学生信息
	 */
	@Operation(summary = "按学号查询学生信息")
	@GetMapping(value = "/queryByNo")
	public Result<TStudent> queryByNo(@RequestParam(name = "studentNo") String studentNo) {
		// 核心：查询条件用数据库字段student_id，参数名用studentNo（匹配前端）
		QueryWrapper<TStudent> wrapper = new QueryWrapper<>();
		wrapper.eq("student_id", studentNo);
		TStudent student = tStudentService.getOne(wrapper);
		if (student == null) {
			return Result.error("未找到对应学生");
		}
		return Result.OK(student);
	}

	/**
	 * 为单个学生生成征订/领取/账单记录（适配教材选用表已有记录时新增学生的场景）
	 *
	 * @param student 新增/导入的学生对象
	 */
	private void generateStudentSubscription(TStudent student) {
		try {
			// 1. 校验学生核心字段（班级ID不能为空）
			if (oConvertUtils.isEmpty(student.getClassId())) {
				log.warn("学生{}（学号{}）无班级信息，跳过征订记录生成", student.getId(), student.getStudentId());
				return;
			}

			// 2. 查询该学生班级的有效教材选用记录（生效状态=1）
			QueryWrapper<TTextbookSelection> selectionWrapper = new QueryWrapper<>();
			selectionWrapper.eq("class_id", student.getClassId())
					.eq("selection_status", "1"); // 仅处理生效的教材选用记录
			List<TTextbookSelection> selectionList = tTextbookSelectionService.list(selectionWrapper);
			if (selectionList.isEmpty()) {
				log.info("学生{}（学号{}）所在班级{}无有效教材选用记录，跳过征订记录生成",
						student.getId(), student.getStudentId(), student.getClassId());
				return;
			}

			// 3. 遍历教材选用记录，为该学生生成关联记录
			for (TTextbookSelection selection : selectionList) {
				// 3.1 防重复：检查该学生+该教材+该学年是否已存在征订记录（不限学期）
				QueryWrapper<TSubscription> subExistWrapper = new QueryWrapper<>();
				subExistWrapper.eq("student_id", student.getId())
						.eq("textbook_id", selection.getTextbookId())
						.eq("subscription_year", selection.getSchoolYear());
				if (tSubscriptionService.count(subExistWrapper) > 0) {
					log.warn("学生{}（学号{}）已存在教材{}的征订记录，跳过",
							student.getId(), student.getStudentId(), selection.getTextbookId());
					continue;
				}

				// ========== 生成征订记录 ==========
				TSubscription subscription = new TSubscription();
				subscription.setStudentId(student.getId()); // 学生主键ID
				subscription.setTextbookId(selection.getTextbookId()); // 教材ID
				subscription.setSelectionId(selection.getId()); // 关联教材选用记录ID
				subscription.setMajorId(selection.getMajorId()); // 专业ID
				subscription.setSubscriptionYear(selection.getSchoolYear()); // 征订学年
				// 统一学期格式为字典码
					String semester = selection.getSemester() != null ? selection.getSemester().trim() : "";
					if ("第一学期".equals(semester) || "一".equals(semester)) semester = "1";
					else if ("第二学期".equals(semester) || "二".equals(semester)) semester = "2";
				subscription.setSubscriptionSemester(semester); // 征订学期
				subscription.setSubscribeStatus("0"); // 初始征订状态（未征订）
				subscription.setRemark("");
				subscription.setCreateTime(new Date());
				subscription.setUpdateTime(new Date());
				tSubscriptionService.save(subscription);
				log.info("为学生{}生成征订记录：{}", student.getStudentId(), subscription.getId());

			}
		} catch (Exception e) {
			log.error("为学生{}生成征订记录失败", student.getStudentId(), e);
			throw new RuntimeException("学生新增成功，但生成教材征订相关记录失败：" + e.getMessage());
		}
	}

	/**
	 * 为转专业/班级的学生仅生成当前学年（上下学期）的征订记录
	 * 旧专业/班级的征订记录保留不删除
	 *
	 * @param student 变更后的学生对象
	 */
	private void generateCurrentYearSubscription(TStudent student) {
		try {
			// 1. 校验学生核心字段（班级ID不能为空）
			if (oConvertUtils.isEmpty(student.getClassId())) {
				log.warn("学生{}（学号{}）无班级信息，跳过征订记录生成", student.getId(), student.getStudentId());
				return;
			}

			// 2. 计算当前学年和当前学期
			// 6月~11月为第一学期，12月~5月为第二学期
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
			log.info("当前学年：{}，当前学期：{}", currentSchoolYear, currentSemester);

			// 3. 查询该学生新班级在当前学年+当前学期的有效教材选用记录（生效状态=1）
			QueryWrapper<TTextbookSelection> selectionWrapper = new QueryWrapper<>();
			selectionWrapper.eq("class_id", student.getClassId())
					.eq("selection_status", "1")
					.eq("school_year", currentSchoolYear)
					.eq("semester", currentSemester);
			List<TTextbookSelection> selectionList = tTextbookSelectionService.list(selectionWrapper);
			if (selectionList.isEmpty()) {
				log.info("学生{}（学号{}）新班级{}在当前学年{}第{}学期无有效教材选用记录，跳过征订记录生成",
						student.getId(), student.getStudentId(), student.getClassId(), currentSchoolYear, currentSemester);
				return;
			}

			// 4. 遍历教材选用记录，为该学生生成当前学年当前学期的征订记录
			for (TTextbookSelection selection : selectionList) {
				// 4.1 防重复：检查该学生+该教材+该学年是否已存在征订记录（不限学期）
				QueryWrapper<TSubscription> subExistWrapper = new QueryWrapper<>();
				subExistWrapper.eq("student_id", student.getId())
						.eq("textbook_id", selection.getTextbookId())
						.eq("subscription_year", selection.getSchoolYear());
				if (tSubscriptionService.count(subExistWrapper) > 0) {
					log.warn("学生{}（学号{}）已存在教材{}在{}学年的征订记录，跳过",
							student.getId(), student.getStudentId(), selection.getTextbookId(),
							selection.getSchoolYear(), selection.getSemester());
					continue;
				}

				// ========== 生成征订记录 ==========
				TSubscription subscription = new TSubscription();
				subscription.setStudentId(student.getId());
				subscription.setTextbookId(selection.getTextbookId());
				subscription.setSelectionId(selection.getId());
				subscription.setMajorId(selection.getMajorId());
				subscription.setSubscriptionYear(selection.getSchoolYear());
				// 统一学期格式为字典码
					String semester = selection.getSemester() != null ? selection.getSemester().trim() : "";
					if ("第一学期".equals(semester) || "一".equals(semester)) semester = "1";
					else if ("第二学期".equals(semester) || "二".equals(semester)) semester = "2";
				subscription.setSubscriptionSemester(semester);
				subscription.setSubscribeStatus("0"); // 初始征订状态（未征订）
				subscription.setRemark("");
				subscription.setCreateTime(new Date());
				subscription.setUpdateTime(new Date());
				tSubscriptionService.save(subscription);
				log.info("为学生{}生成当前学年征订记录：教材{}，{}学年第{}学期",
						student.getStudentId(), selection.getTextbookId(),
						selection.getSchoolYear(), selection.getSemester());
			}
		} catch (Exception e) {
			log.error("为学生{}生成当前学年当前学期征订记录失败", student.getStudentId(), e);
			throw new RuntimeException("转专业/班级成功，但生成当前学年当前学期征订记录失败：" + e.getMessage());
		}
	}

	/**
	 * 获取所有专业（下拉框专用，无权限拦截）
	 * 辅导员只能获取自己管理班级的专业
	 */
	@GetMapping("/getMajorList")
	public Result<List<TMajor>> getMajorList() {
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String username = loginUser.getUsername();
		boolean isAdmin = "admin".equals(username) || "sysadmin".equals(username);

		if (isAdmin) {
			List<TMajor> list = tMajorService.list();
			return Result.OK(list);
		}

		boolean isCounselor = false;
		List<SysUserRole> userRoleList = sysUserRoleService
				.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, loginUser.getId()));
		if (!userRoleList.isEmpty()) {
			List<String> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
			List<SysRole> roleList = sysRoleService.listByIds(roleIds);
			for (SysRole role : roleList) {
				if ("counselor".equals(role.getRoleCode())) {
					isCounselor = true;
					break;
				}
			}
		}

		if (isCounselor) {
			QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
			counselorWrapper.eq("user_id", loginUser.getId());
			TCounselor counselor = tCounselorService.getOne(counselorWrapper);
			if (counselor != null) {
				QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
				classWrapper.eq("counselor_id", counselor.getId());
				List<TClass> classList = tClassService.list(classWrapper);
				if (!classList.isEmpty()) {
					List<String> majorIds = classList.stream()
							.map(TClass::getMajorId)
							.filter(m -> oConvertUtils.isNotEmpty(m))
							.distinct()
							.collect(Collectors.toList());
					if (!majorIds.isEmpty()) {
						QueryWrapper<TMajor> majorWrapper = new QueryWrapper<>();
						majorWrapper.in("id", majorIds);
						List<TMajor> majorList = tMajorService.list(majorWrapper);
						return Result.OK(majorList);
					}
				}
			}
			return Result.OK(new ArrayList<>());
		}

		List<TMajor> list = tMajorService.list();
		return Result.OK(list);
	}

	/**
	 * 根据专业ID获取班级列表（下拉框专用，辅导员只能获取自己管理的班级）
	 */
	@GetMapping("/getClassListByMajor")
	public Result<List<TClass>> getClassListByMajor(@RequestParam(name = "majorId") String majorId) {
		LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String username = loginUser.getUsername();
		boolean isAdmin = "admin".equals(username) || "sysadmin".equals(username);

		QueryWrapper<TClass> classWrapper = new QueryWrapper<>();
		classWrapper.eq("major_id", majorId);

		if (!isAdmin) {
			boolean isCounselor = false;
			List<SysUserRole> userRoleList = sysUserRoleService
					.list(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, loginUser.getId()));
			if (!userRoleList.isEmpty()) {
				List<String> roleIds = userRoleList.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
				List<SysRole> roleList = sysRoleService.listByIds(roleIds);
				for (SysRole role : roleList) {
					if ("counselor".equals(role.getRoleCode())) {
						isCounselor = true;
						break;
					}
				}
			}

			if (isCounselor) {
				QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
				counselorWrapper.eq("user_id", loginUser.getId());
				TCounselor counselor = tCounselorService.getOne(counselorWrapper);
				if (counselor != null) {
					classWrapper.eq("counselor_id", counselor.getId());
				} else {
					return Result.OK(new ArrayList<>());
				}
			}
		}

		List<TClass> list = tClassService.list(classWrapper);
		return Result.OK(list);
	}

	/**
	 * 获取所有班级（下拉框专用，无权限拦截）
	 */
	@GetMapping("/getClassList")
	public Result<List<TClass>> getClassList() {
		List<TClass> list = tClassService.list();
		return Result.OK(list);
	}

}
