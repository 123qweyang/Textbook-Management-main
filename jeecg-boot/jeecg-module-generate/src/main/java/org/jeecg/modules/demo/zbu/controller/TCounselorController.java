package org.jeecg.modules.demo.zbu.controller;

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
import org.jeecg.modules.demo.zbu.entity.TCounselor;
import org.jeecg.modules.demo.zbu.entity.TCollege;
import org.jeecg.modules.demo.zbu.service.ITCounselorService;
import org.jeecg.modules.demo.zbu.service.ITCollegeService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.system.entity.SysRole;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.entity.SysUserRole;
import org.jeecg.modules.system.mapper.SysUserMapper;
import org.jeecg.modules.system.service.ISysRoleService;
import org.jeecg.modules.system.service.ISysUserRoleService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.demo.zbu.vo.ImportErrorExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
 * @Description: 辅导员表
 * @Author: jeecg-boot
 * @Date:   2026-01-19
 * @Version: V1.0
 */
@Tag(name="辅导员表")
@RestController
@RequestMapping("/zbu/tCounselor")
@Slf4j
public class TCounselorController extends JeecgController<TCounselor, ITCounselorService> {
	@Autowired
	private ITCounselorService tCounselorService;
	@Autowired
	private ITCollegeService tCollegeService;
	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysRoleService sysRoleService;
	@Autowired
	private ISysUserRoleService sysUserRoleService;
	@Autowired
	private BaseCommonService baseCommonService;
	@Value("${jeecg.path.upload}")
	private String uploadPath;
	// 辅导员角色编码
	private static final String COUNSELOR_ROLE_CODE = "counselor";

	/**
	 * 分页列表查询
	 *
	 * @param tCounselor
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	//@AutoLog(value = "辅导员表-分页列表查询")
	@Operation(summary="辅导员表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<IPage<TCounselor>> queryPageList(TCounselor tCounselor,
												   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
												   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
												   HttpServletRequest req) {


		QueryWrapper<TCounselor> queryWrapper = QueryGenerator.initQueryWrapper(tCounselor, req.getParameterMap());
		Page<TCounselor> page = new Page<TCounselor>(pageNo, pageSize);
		IPage<TCounselor> pageList = tCounselorService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param tCounselor
	 * @return
	 */
	@AutoLog(value = "辅导员表-添加")
	@Operation(summary="辅导员表-添加")
	@RequiresPermissions("zbu:t_counselor:add")
	@PostMapping(value = "/add")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> add(@RequestBody TCounselor tCounselor) {
		try {
			// 1. 校验核心字段
			if (oConvertUtils.isEmpty(tCounselor.getCounselorId())
					|| oConvertUtils.isEmpty(tCounselor.getCounselorName())) {
				return Result.error("辅导员工号和姓名不能为空！");
			}
			String counselorId = tCounselor.getCounselorId().trim();

			// 2. 检查工号是否已存在于辅导员表
			QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
			counselorWrapper.eq("counselor_id", counselorId);
			if (tCounselorService.count(counselorWrapper) > 0) {
				return Result.error("添加失败：工号已存在！");
			}

			// 3. 检查工号是否已存在于系统用户表（因为工号用作用户名）
			QueryWrapper<SysUser> userWrapper = new QueryWrapper<>();
			userWrapper.eq("username", counselorId);
			if (sysUserService.count(userWrapper) > 0) {
				return Result.error("添加失败：工号已存在系统账号！");
			}

			// 2. 创建系统用户（sys_user）的登录账号
			SysUser sysUser = new SysUser();
			// 辅导员工号去空格（避免登录时用户名带空格导致匹配失败）
			sysUser.setUsername(tCounselor.getCounselorId().trim());
			// 辅导员姓名去空格
			sysUser.setRealname(tCounselor.getCounselorName().trim());

			// 3. Jeecg官方逻辑：生成8位随机salt + 原生加密
			String plainPwd = counselorId + "Zbu1"; // 明文密码写死Zbu1
			String salt = oConvertUtils.randomGen(8); // 生成8位随机salt
			sysUser.setSalt(salt); // 设置随机salt到用户表
			// 用Jeecg原生加密：encrypt(用户名, 明文密码, 随机salt)
			String encryptedPwd = PasswordUtil.encrypt(sysUser.getUsername(), plainPwd, salt);
			sysUser.setPassword(encryptedPwd); // 设置加密后的密码

			// 4. 补充系统默认字段
			sysUser.setDepartIds("1"); // 赋值为你系统中存在的部门ID（如根部门ID）
			sysUser.setStatus(1); // 账号启用
			sysUser.setDelFlag(CommonConstant.DEL_FLAG_0); // 0=未删除
			sysUser.setCreateTime(new Date()); // 创建时间
			sysUser.setLastPwdUpdateTime(new Date()); // 最后修改密码时间
			sysUser.setOrgCode(null); // 对齐系统逻辑

			// 5. 保存sys_user并校验
			boolean saveSysUserSuccess = sysUserService.save(sysUser);
			if (!saveSysUserSuccess) {
				return Result.error("创建登录账号失败！");
			}
			log.info("sys_user保存结果：{}，用户ID：{}", saveSysUserSuccess, sysUser.getId());

			// 6. 通过角色编码（counselor）查询角色ID
			QueryWrapper<SysRole> roleWrapper = new QueryWrapper<>();
			roleWrapper.eq("role_code", COUNSELOR_ROLE_CODE); // 按角色编码查询
			SysRole counselorRole = sysRoleService.getOne(roleWrapper); // 获取唯一角色
			if (counselorRole == null) {
				return Result.error("未找到编码为【counselor】的辅导员角色，请先在角色管理中创建！");
			}

			// 7. 绑定用户-角色关系（给新增用户分配辅导员角色）
			SysUserRole userRole = new SysUserRole();
			userRole.setUserId(sysUser.getId()); // 新创建的用户ID
			userRole.setRoleId(counselorRole.getId()); // 辅导员角色ID
			sysUserRoleService.save(userRole);

			// 8. 把sys_user的ID关联到辅导员表并保存
			tCounselor.setUserId(sysUser.getId()); // 需确保TCounselor实体类有userId字段
			tCounselorService.save(tCounselor);


			return Result.OK("添加成功", encryptedPwd);
		} catch (Exception e) {
			log.error("新增辅导员失败：", e);
			return Result.error("添加失败：" + e.getMessage());
		}
	}

	/**
	 *  编辑
	 *
	 * @param tCounselor
	 * @return
	 */
	@AutoLog(value = "辅导员表-编辑")
	@Operation(summary="辅导员表-编辑")
	@RequiresPermissions("zbu:t_counselor:edit")
	@RequestMapping(value = "/edit", method = {RequestMethod.PUT,RequestMethod.POST})
	public Result<String> edit(@RequestBody TCounselor tCounselor) {
		tCounselorService.updateById(tCounselor);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "辅导员表-通过id删除")
	@Operation(summary="辅导员表-通过id删除")
	@RequiresPermissions("zbu:t_counselor:delete")
	@DeleteMapping(value = "/delete")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> delete(@RequestParam(name="id",required=true) String id) {
		try {
			log.info("开始删除辅导员：id={}", id);
			// 1. 查询辅导员获取关联的userId
			TCounselor counselor = tCounselorService.getById(id);
			if (counselor == null) {
				log.warn("删除失败：辅导员记录不存在，id={}", id);
				return Result.error("辅导员记录不存在！");
			}
			log.info("查询到辅导员信息：{}", JSON.toJSONString(counselor));

			// 2. 级联删除系统用户相关数据
			if (counselor.getUserId() != null && !counselor.getUserId().isEmpty()) {
				String userId = counselor.getUserId();

				// 步骤1：删除用户-角色关联
				int delRole = sysUserRoleService.getBaseMapper().delete(
						new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId)
				);
				log.info("删除用户-角色关联：{}条", delRole);

				// 步骤2：更新sys_user的del_flag=1（逻辑删除）
				SysUserMapper sysUserMapper = (SysUserMapper) sysUserService.getBaseMapper();
				int updateDelFlag = sysUserMapper.update(
						new SysUser(),
						new LambdaUpdateWrapper<SysUser>()
								.set(SysUser::getDelFlag, "1")
								.eq(SysUser::getId, userId)
				);
				log.info("更新用户del_flag：id={}，影响行数={}", userId, updateDelFlag);

				if (updateDelFlag <= 0) {
					log.error("强制更新del_flag失败：ID={}", userId);
					// 兜底：直接物理删除用户
					int delUser = sysUserMapper.delete(
							new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, userId)
					);
					if (delUser <= 0) {
						return Result.error("删除失败：用户ID不存在或字段映射错误！");
					}
					baseCommonService.addLog("直接物理删除用户：id=" + userId, CommonConstant.LOG_TYPE_2, 3);
				} else {
					// 步骤3：调用deleteLogicDeleted彻底删除
					List<String> userIds = Collections.singletonList(userId);
					int delUser = sysUserMapper.deleteLogicDeleted(userIds);
					if (delUser <= 0) {
						delUser = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().eq(SysUser::getId, userId));
					}
					log.info("彻底删除用户成功：id={}，影响行数={}", userId, delUser);
					baseCommonService.addLog("彻底删除用户：id=" + userId, CommonConstant.LOG_TYPE_2, 3);
				}
			}

			// 3. 物理删除辅导员表记录
			int delCounselor = tCounselorService.getBaseMapper().deleteById(id);
			log.info("物理删除辅导员表记录：id={}，影响行数={}", id, delCounselor);
			if (delCounselor <= 0) {
				throw new RuntimeException("辅导员表记录物理删除失败");
			}

			return Result.OK("删除成功! 数据库中已无该辅导员及关联用户记录");
		} catch (Exception e) {
			log.error("删除辅导员失败：id={}", id, e);
			return Result.error("删除失败：" + e.getMessage());
		}
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "辅导员表-批量删除")
	@Operation(summary="辅导员表-批量删除")
	@RequiresPermissions("zbu:t_counselor:deleteBatch")
	@DeleteMapping(value = "/deleteBatch")
	@Transactional(rollbackFor = Exception.class)
	public Result<String> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		try {
			log.info("开始批量删除辅导员：ids={}", ids);
			// 1. 拆分批量删除的辅导员ID
			List<String> counselorIds = Arrays.stream(ids.split(","))
					.map(String::trim)
					.filter(id -> !id.isEmpty())
					.collect(Collectors.toList());
			if (counselorIds.isEmpty()) {
				return Result.error("批量删除的ID不能为空！");
			}

			// 2. 批量查询这些辅导员的userId
			List<TCounselor> counselors = tCounselorService.listByIds(counselorIds);
			log.info("查询到待删除的辅导员数量：{}", counselors.size());
			if (counselors.isEmpty()) {
				return Result.error("未找到对应辅导员记录！");
			}

			// 3. 提取所有关联的userId
			List<String> userIds = counselors.stream()
					.filter(counselor -> counselor.getUserId() != null && !counselor.getUserId().isEmpty())
					.map(TCounselor::getUserId)
					.collect(Collectors.toList());
			log.info("提取到辅导员关联的用户ID数量：{}", userIds.size());

			// 4. 批量处理关联用户
			if (!userIds.isEmpty()) {
				// 步骤1：批量删除用户-角色关联
				int delRole = sysUserRoleService.getBaseMapper().delete(
						new LambdaQueryWrapper<SysUserRole>().in(SysUserRole::getUserId, userIds)
				);
				log.info("批量删除用户-角色关联：{}条", delRole);

				// 步骤2：批量更新用户del_flag=1
				SysUserMapper sysUserMapper = (SysUserMapper) sysUserService.getBaseMapper();
				int updateDelFlag = sysUserMapper.update(
						new SysUser(),
						new LambdaUpdateWrapper<SysUser>()
								.set(SysUser::getDelFlag, "1")
								.in(SysUser::getId, userIds)
				);
				log.info("批量更新用户del_flag=1：待更新数量={}, 实际更新行数={}", userIds.size(), updateDelFlag);

				if (updateDelFlag <= 0) {
					log.error("批量更新用户del_flag失败");
					// 兜底：直接批量物理删除用户
					int delUsers = sysUserMapper.delete(
							new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds)
					);
					log.info("批量兜底物理删除用户成功：影响行数={}", delUsers);
					baseCommonService.addLog("直接批量物理删除用户：ids=" + userIds, CommonConstant.LOG_TYPE_2, 3);
				} else {
					// 步骤3：调用deleteLogicDeleted批量删除
					int delUser = sysUserMapper.deleteLogicDeleted(userIds);
					log.info("调用deleteLogicDeleted批量删除用户：待删数量={}, 实际删除行数={}", userIds.size(), delUser);

					if (delUser <= 0) {
						delUser = sysUserMapper.delete(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, userIds));
						log.info("批量兜底删除用户成功：影响行数={}", delUser);
					}
					baseCommonService.addLog("彻底批量删除用户：ids=" + userIds, CommonConstant.LOG_TYPE_2, 3);
				}
			}

			// 5. 批量物理删除辅导员表
			int delCounselors = tCounselorService.getBaseMapper().deleteBatchIds(counselorIds);
			log.info("批量物理删除辅导员表记录：待删数量={}, 实际删除行数={}", counselorIds.size(), delCounselors);

			if (delCounselors <= 0) {
				throw new RuntimeException("辅导员表批量物理删除失败");
			}

			return Result.OK("批量删除成功! 数据库中已无该批辅导员及关联用户记录");
		} catch (Exception e) {
			log.error("批量删除辅导员失败：ids={}", ids, e);
			return Result.error("批量删除失败：" + e.getMessage());
		}
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	//@AutoLog(value = "辅导员表-通过id查询")
	@Operation(summary="辅导员表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<TCounselor> queryById(@RequestParam(name="id",required=true) String id) {
		TCounselor tCounselor = tCounselorService.getById(id);
		if(tCounselor==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(tCounselor);
	}

	/**
	 * 导出excel
	 *
	 * @param request
	 * @param tCounselor
	 */
	@RequiresPermissions("zbu:t_counselor:exportXls")
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, TCounselor tCounselor) {
		return super.exportXls(request, tCounselor, TCounselor.class, "辅导员表");
	}

	/**
	 * 通过excel导入数据
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequiresPermissions("zbu:t_counselor:importExcel")
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 1. 获取上传的Excel文件
			MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
			Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
			if (fileMap.isEmpty()) {
				return Result.error("请选择要导入的Excel文件！");
			}

			// 2. 基础导入参数配置
			ImportParams importParams = new ImportParams();
			importParams.setTitleRows(2); // 标题行数量
			importParams.setHeadRows(1); // 表头行数量
			importParams.setNeedSave(false);

			// 存储有效数据和失败信息
			List<TCounselor> validCounselorList = new ArrayList<>();
			List<String> failMsgList = new ArrayList<>();
			int totalRow = 0;

			// 3. 遍历解析Excel文件
			for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
				MultipartFile file = entry.getValue();
				if (file.isEmpty()) {
					continue;
				}

				// 4. 解析Excel为辅导员列表
				List<TCounselor> tempList = ExcelImportUtil.importExcel(
						file.getInputStream(),
						TCounselor.class,
						importParams
				);

				// 5. 逐行校验+过滤
				for (int i = 0; i < tempList.size(); i++) {
					totalRow = i + 2; // Excel行号（标题+表头后从第2行开始）
					TCounselor counselor = tempList.get(i);

					// 5.1 辅导员工号空值校验
					String counselorId = counselor.getCounselorId();
					if (oConvertUtils.isEmpty(counselorId) || counselorId.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：辅导员工号为空，跳过导入");
						continue;
					}
					counselor.setCounselorId(counselorId.trim());

					// 5.2 辅导员姓名空值校验
					String counselorName = counselor.getCounselorName();
					if (oConvertUtils.isEmpty(counselorName) || counselorName.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：辅导员姓名为空（工号：" + counselorId + "），跳过导入");
						continue;
					}
					counselor.setCounselorName(counselorName.trim());

					// 5.3 学院校验：必须为学院表中已存在的学院
					String collegeId = counselor.getCollegeId();
					if (oConvertUtils.isEmpty(collegeId) || collegeId.trim().isEmpty()) {
						failMsgList.add("第" + totalRow + "行：所属学院为空（工号：" + counselorId + "），跳过导入");
						continue;
					}
					// 校验学院是否存在（支持ID或名称查询）
					TCollege college = null;
					if (collegeId.matches("^\\d{16,}$")) {
						// 按ID查询
						college = tCollegeService.getById(collegeId.trim());
					}
					if (college == null) {
						// 按学院名称查询
						college = tCollegeService.getOne(new LambdaQueryWrapper<TCollege>()
								.eq(TCollege::getCollegeName, collegeId.trim()));
					}
					if (college == null) {
						failMsgList.add("第" + totalRow + "行：学院【" + collegeId + "】不存在于学院表中（工号：" + counselorId + "），跳过导入");
						continue;
					}
					// 使用学院表中的ID
					counselor.setCollegeId(college.getId());

					// 6. 校验工号是否已存在于辅导员表
					QueryWrapper<TCounselor> counselorWrapper = new QueryWrapper<>();
					counselorWrapper.eq("counselor_id", counselor.getCounselorId());
					if (tCounselorService.count(counselorWrapper) > 0) {
						failMsgList.add("第" + totalRow + "行：工号【" + counselorId + "】已存在，跳过导入");
						continue;
					}

					// 7. 校验工号是否已存在于系统用户表（因为工号用作用户名）
					SysUser existUser = sysUserService.getOne(new LambdaQueryWrapper<SysUser>()
							.eq(SysUser::getUsername, counselor.getCounselorId())
							.eq(SysUser::getDelFlag, CommonConstant.DEL_FLAG_0));
					if (existUser != null) {
						failMsgList.add("第" + totalRow + "行：工号【" + counselorId + "】已存在系统账号，跳过导入");
						continue;
					}

					// 7. 预查询辅导员角色
					SysRole counselorRole = sysRoleService.getOne(new LambdaQueryWrapper<SysRole>()
							.eq(SysRole::getRoleCode, COUNSELOR_ROLE_CODE));
					if (counselorRole == null) {
						return Result.error("系统未配置【counselor】角色，请先在角色管理中创建！");
					}

					// 8. 创建系统用户
					SysUser sysUser = new SysUser();
					sysUser.setUsername(counselor.getCounselorId());
					sysUser.setRealname(counselor.getCounselorName());
					// 密码加密
					String salt = oConvertUtils.randomGen(8);
					String plainPwd = counselor.getCounselorId()+"Zbu1";
					String encryptedPwd = PasswordUtil.encrypt(sysUser.getUsername(), plainPwd, salt);
					sysUser.setSalt(salt);
					sysUser.setPassword(encryptedPwd);
					// 补充系统用户字段
					sysUser.setDepartIds("1");
					sysUser.setStatus(1);
					sysUser.setDelFlag(CommonConstant.DEL_FLAG_0);
					sysUser.setCreateTime(new Date());
					sysUser.setLastPwdUpdateTime(new Date());
					sysUser.setOrgCode(null);

					// 9. 保存系统用户
					boolean saveUserOk = sysUserService.save(sysUser);
					if (!saveUserOk) {
						failMsgList.add("第" + totalRow + "行：工号【" + counselorId + "】创建系统账号失败");
						continue;
					}

					// 10. 绑定用户-角色关系
					SysUserRole userRole = new SysUserRole();
					userRole.setUserId(sysUser.getId());
					userRole.setRoleId(counselorRole.getId());
					sysUserRoleService.save(userRole);

					// 11. 关联用户ID到辅导员表
					counselor.setUserId(sysUser.getId());
					counselor.setCreateTime(new Date());
					validCounselorList.add(counselor);
				}
			}

			// 12. 批量保存有效数据
			if (!validCounselorList.isEmpty()) {
				tCounselorService.saveBatch(validCounselorList);
			}

			// 13. 构建返回结果
			String successMsg = "导入完成！成功导入【" + validCounselorList.size() + "】条有效数据";
			return ImportErrorExportUtil.buildErrorResult(failMsgList, successMsg, uploadPath, "辅导员表");

		} catch (Exception e) {
			log.error("Excel导入辅导员数据失败", e);
			return Result.error("导入失败：" + e.getMessage());
		}
	}

	/**
	 * 按工号查询辅导员信息
	 */
	@Operation(summary="按工号查询辅导员信息")
	@GetMapping(value = "/queryByCounselorId")
	public Result<TCounselor> queryByCounselorId(@RequestParam(name="counselorId") String counselorId) {
		QueryWrapper<TCounselor> wrapper = new QueryWrapper<>();
		wrapper.eq("counselor_id", counselorId);
		TCounselor counselor = tCounselorService.getOne(wrapper);
		if(counselor == null) {
			return Result.error("未找到对应辅导员");
		}
		return Result.OK(counselor);
	}

}
