package org.jeecg.modules.cas.controller;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.cas.util.CasServiceUtil;
import org.jeecg.modules.cas.util.XmlUtils;
import org.jeecg.modules.system.entity.SysDepart;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.service.ISysDepartService;
import org.jeecg.modules.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/sys/cas/client")
public class CasClientController {

	@Autowired
	private ISysUserService sysUserService;
	@Autowired
	private ISysDepartService sysDepartService;
	@Autowired
	private RedisUtil redisUtil;

	@Value("${cas.prefixUrl}")
	private String prefixUrl;


	@GetMapping("/validateLogin")
	public Object validateLogin(@RequestParam(name="ticket") String ticket,
								@RequestParam(name="service") String service,
								HttpServletRequest request,
								HttpServletResponse response) throws Exception {
		Result<JSONObject> result = new Result<JSONObject>();
		log.info("CAS validateLogin, ticket={}, service={}", ticket, service);
		try {
			String validateUrl = prefixUrl + "/p3/serviceValidate";
			log.info("Calling CAS: {}", validateUrl);

			String res = CasServiceUtil.getStValidate(validateUrl, ticket, service);
			log.info("CAS raw response: {}", res);

			if (res == null || res.isEmpty()) {
				throw new Exception("CAS server returned empty response. "
						+ "Request URL: " + validateUrl
						+ "?service=" + service + "&ticket=" + ticket);
			}

			final String error = XmlUtils.getTextForElement(res, "authenticationFailure");
			if (StringUtils.isNotEmpty(error)) {
				throw new Exception("CAS auth failure: " + error);
			}

			final String principal = XmlUtils.getTextForElement(res, "user");
			if (StringUtils.isEmpty(principal)) {
				log.error("No 'user' element in CAS response. Raw: {}", res);
				throw new Exception("CAS returned no user element. Raw: "
						+ res.substring(0, Math.min(res.length(), 500)));
			}

			log.info("CAS principal: {}", principal);
			return doLogin(principal, result);
		} catch (Exception e) {
			log.error("CAS validateLogin failed", e);
			result.error500(e.getMessage());
		}
		return new HttpEntity<>(result);
	}


	private HttpEntity<Result<JSONObject>> doLogin(String username, Result<JSONObject> result) {
		SysUser sysUser = sysUserService.getUserByName(username);
		if (sysUser == null) {
			result.error500("User not found: " + username);
			return new HttpEntity<>(result);
		}
		result = sysUserService.checkUserIsEffective(sysUser);
		if (!result.isSuccess()) {
			return new HttpEntity<>(result);
		}

		String token = JwtUtil.sign(sysUser.getUsername(), sysUser.getPassword(), CommonConstant.CLIENT_TYPE_PC);
		redisUtil.set(CommonConstant.PREFIX_USER_TOKEN + token, token);
		redisUtil.expire(CommonConstant.PREFIX_USER_TOKEN + token, JwtUtil.EXPIRE_TIME * 2 / 1000);

		JSONObject obj = new JSONObject();
		List<SysDepart> departs = sysDepartService.queryUserDeparts(sysUser.getId());
		obj.put("departs", departs);
		if (departs == null || departs.size() == 0) {
			obj.put("multi_depart", 0);
		} else if (departs.size() == 1) {
			sysUserService.updateUserDepart(username, departs.get(0).getOrgCode(), null);
			obj.put("multi_depart", 1);
		} else {
			obj.put("multi_depart", 2);
		}
		obj.put("token", token);
		obj.put("userInfo", sysUser);
		result.setResult(obj);
		result.success("Login success");
		return new HttpEntity<>(result);
	}

}
