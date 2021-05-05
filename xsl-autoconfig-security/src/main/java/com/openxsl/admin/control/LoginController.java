package com.openxsl.admin.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.openxsl.admin.api.IPasswordEncoder;
import com.openxsl.admin.context.LocalUserHolder;
import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.entity.User;
import com.openxsl.admin.entity.UserDetail;
import com.openxsl.admin.organ.service.DistrictAreaService;
import com.openxsl.admin.security.csrf.CsrfSecurityRepository;
import com.openxsl.admin.security.exception.PasswordRequiresModifyException;
import com.openxsl.admin.service.AuthenticateService;
import com.openxsl.admin.service.OperationLogService;
import com.openxsl.admin.service.SsoService;
import com.openxsl.admin.service.UserService;
import com.openxsl.admin.service.WebConfigService;
import com.openxsl.config.redis.GenericRedisHelper;
import com.openxsl.config.util.StringUtils;
import com.openxsl.config.webmvc.Response;

@Api(value="vmp-admin", tags="系统管理-登录")
@Controller
public class LoginController {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Value("${security.authen-login-url}")
	private String loginPage = "/login.jsp";
	@Value("${security.authen-success-url}")
	private String successUrl;
	
	@Autowired
	private AuthenticateService authenciate;
	@javax.annotation.Resource(name="AESEncoder")
	private IPasswordEncoder passwordEncoder;
	@Autowired
	private UserService service;
	@Autowired
	private SsoService ssoService;
	
	@Autowired
	private DistrictAreaService areaService;
	@Autowired
	private WebConfigService webConfigService;
	@Autowired
	private OperationLogService operLogService;
	@Autowired
	private CsrfSecurityRepository csrfRepo;
	
	private GenericRedisHelper<Integer> badPswdHelper;
	@Autowired
	public void setRedisHelper(GenericRedisHelper<Integer> redisHelper) {
		this.badPswdHelper = redisHelper;
		this.badPswdHelper.setEntityClass(Integer.class);
	}
	
	@Deprecated
	@RequestMapping(path = "/index1")
	public String index() {
//		Object subject = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return "redirect:"+successUrl;
	}
	/**
	 * SpringSecuriy验证密码失败
	 */
	@Deprecated
	@RequestMapping(path = "/authFailed1")
	public String authFailed(HttpServletRequest request) {
		Throwable e = (Throwable) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		String message = e.getMessage();
		if (e instanceof UsernameNotFoundException) {
			message = "用户名或密码错误";
		} else if (e instanceof BadCredentialsException) {
			message = "用户名或密码错误";
		} else if (e instanceof DisabledException) {
			message = "用户已禁用";
		}else if (e instanceof LockedException){
			message = "用户已被锁住";
		}else if (e instanceof AccountExpiredException){
			message = "用户已过期";
		} 
		csrfRepo.removeToken(request);

		// 错误次数+1
		request.getSession().setAttribute("error", message);
		return "redirect:" + loginPage;
	}
	
	/**
	 * 返回登录用户的信息
	 * 
	 * {
	 *   "success": true
	 *   "user": {},
	 *   "token": "",
	 *   "scenicCode": ""
	 *   "webconfig": {}
	 *   "bizSystem": []
	 * }
	 */
	@CrossOrigin
	@RequestMapping(path = "/index")
	@ResponseBody  //{"user":{}, "token":"", "webconfig":{}}
	public Map<String, Object> loginSucc(HttpServletRequest request) {
		String webType = request.getParameter("webType");
		String userName = LocalUserHolder.getUserName();
		String corpCode = LocalUserHolder.getCorpCode();
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", true);
		result.put("user", LocalUserHolder.getUser().getUser());  //ForwardAuthenticationSuccessHandler
		result.put("token", LocalUserHolder.getUser().getSessionId());
		result.put("scenicCode", corpCode);
		result.put("userLogo", LocalUserHolder.getUserLogo());
		result.put("userName", userName);
		result.put("deptNames", LocalUserHolder.getDeptNames());
		
		this.operateLogin(request, userName);
		
		try {
			result.put("webConfig", webConfigService.getWebConfig(webType, corpCode));
			Map<String,List<Resource>> resourceMap = webConfigService.getResources();
			result.put("resources", resourceMap);
			result.put("bizSystem", webConfigService.getBizSystems(resourceMap.keySet()));
			badPswdHelper.delete("__FORGET__"+userName);
			result.put("areas",	areaService.getDistrictAreas(LocalUserHolder.getAreaCode()));
		} catch (Exception e) {
			logger.error("", e);
		}
		return result;
	}
	
	@CrossOrigin
	@RequestMapping(path = "/authFailed")
	@ResponseBody
	public Map<String, Object> authFailedJson(HttpServletRequest request) {
		Throwable e = (Throwable) request.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
		String message = e.getMessage();
		if (e instanceof BadCredentialsException) {
			message = "用户名或密码错误";
			String username = request.getParameter("username");
			Long times = (Long)badPswdHelper.increaseOrDecr("__FORGET__"+username, 1L, 1200L);
			if (times > 4) {
				service.lockUser(username);
				message = "用户已被锁住";
			} else if (times == 3) {
				message = "您还有两次机会";
			}
		} else if (e instanceof UsernameNotFoundException) {
			message = "用户名或密码错误";
		} else if (e instanceof DisabledException) {
			message = "用户已禁用";
		}else if (e instanceof LockedException){
			message = "用户已被锁住";
		}else if (e instanceof AccountExpiredException){
			message = "用户已过期";
		} 
		csrfRepo.removeToken(request);
		this.operateLogin(request, "login-failed");

		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", false);
		result.put("message", message);
		if (e instanceof PasswordRequiresModifyException) {
			result.put("code", "402");
		}
		return result;
	}
	
	@ApiOperation("新增(注册)用户")
	@PostMapping(path="register")
	public Response register(HttpServletRequest request, @Valid User user, UserDetail detail) {
		return Response.build(() -> {
			service.register(user, detail);
			this.loginSecurity(request, user);
		});
	}
	
	@ApiOperation("忘记密码")
	@PostMapping(path = "/forgetPswd")
	@ResponseBody
	public Response forgetPswd(@RequestParam String username, @RequestParam String password) {
		return Response.build(() -> {
				authenciate.forgetPwdPassword(username, password);
		});
	}
	
	@ApiOperation("管理员重置密码")
	@PostMapping(path = "/admin/user/resetPswd")
	@ResponseBody
	public Response resetPswd(@RequestParam String username) {
		return Response.build(() -> {
//			User user = (User)SecurityContextHolder.getContext().getAuthentication()
//						.getPrincipal();
//			if (!user.isSysAdmin()) {
//				throw new AccessDeniedException("没有操作权限");
//			}
			String userName = passwordEncoder.decode(username);
			authenciate.resetPassword(userName);
		});
	}
	
	@ApiOperation("修改密码")
	@PostMapping(path = "/changePswd")
	@ResponseBody
	public Response changePswd(@RequestParam String username, @RequestParam String oldPass,
					@RequestParam String password) {
		return Response.build(() -> {
				authenciate.modifyPassword(username, oldPass, password);
		});
	}
	
	/**
	 * 一次性的校验码
	 */
	@GetMapping("ssocode")
	public @ResponseBody String getSecurityCode(String username) {
		return ssoService.getSecurityCode(username);
	}
	
	/**
	 * 忘记密码, 发送邮件到用户邮箱
	 */
	@ApiOperation("发送密码验证邮件")
	@RequestMapping(path="validationMail", method=RequestMethod.POST, produces={"text/html;charset=UTF-8"})
	public @ResponseBody Response validationMail(HttpServletRequest request, 
							@RequestParam("username") String username) {
		return Response.build(() -> {
			User user = (User)authenciate.getUserByName(username);
			if (StringUtils.isEmpty(user.getEmail())){
				throw new IllegalStateException("注册邮箱为空");
			}
			long expirytime = System.currentTimeMillis() + 30*60*1000l;//30分钟后过期
			String validParams = String.format("uid=%d&name=%s&expires=%d",
						user.getId(), user.getUsername(), expirytime);
			String tempUrl = request.getRequestURL().toString();
			StringBuilder validUrl = new StringBuilder(tempUrl.substring(0, tempUrl.lastIndexOf("/")));
			validUrl.append("/validateUrl.htm?").append(passwordEncoder.encode(validParams));
			String emailContent = "您请求了重置密码功能，请您在30分钟内点击:<a href='%s'>重置密码</a>。<br>如果该操作不是您发出，请忽略，很抱歉打扰到您！";
			emailContent = String.format(emailContent, validUrl.toString()) ;
			//mailService.sendPasswordMail(user.getEmail(), emailContent);
//			return "请在您的注册邮箱内点击【重置密码】链接";
		});
	}
	/**
	 * 忘记密码, 验证邮箱url
	 */
	@ApiOperation("验证密码链接")
	@RequestMapping(path = "validateUrl", method = RequestMethod.GET)
	public String validateUrl(HttpServletRequest request, Model model) {
		String validParams = request.getQueryString();
		try {
			validParams = passwordEncoder.decode(validParams);
			StringTokenizer tokens = new StringTokenizer(validParams,"=&");
			tokens.nextToken();
			int uid = Integer.valueOf(tokens.nextToken());
			tokens.nextToken();
			String username = tokens.nextToken();
			tokens.nextToken();
			long expires = Long.valueOf(tokens.nextToken());
			
			if (System.currentTimeMillis() > expires){
				throw new Exception("重置密码的链接已过期");
			}
			User user = (User)authenciate.getUser(uid);
			if (user == null){
				throw new Exception("重置密码的链接失效（用户不存在）");
			}
			if (!user.getUsername().equals(username)){
				throw new Exception("重置密码的链接失效（用户名不匹配）");
			}
//			model.addAttribute("username", passwordEncoder.encode(username));
			return "/forgetPswd.html?username="+passwordEncoder.encode(username);
		} catch (Exception e) {
			logger.error("", e);
			request.getSession().setAttribute("ERROR_MSG", e.getMessage());
			return accessDenied(request);
		}
	}
	
	private String accessDenied(HttpServletRequest request) {
		AccessDeniedException exception = (AccessDeniedException) request
				.getAttribute(WebAttributes.ACCESS_DENIED_403);
		if (exception instanceof CsrfException) {
			request.getSession().setAttribute("error", "由于网络原因或服务器重启，需要重新登录");
			return "redirect:" + loginPage;
		} else {
			return "redirect:/errorPage";
		}
	}
	
	private void operateLogin(HttpServletRequest request, String userName) {
		operLogService.saveLog(request, "/j_security_check", userName);
	}
	
	private final void loginSecurity(HttpServletRequest request, User user){
		UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
						user, user.getPassword(), user.getAuthorities());
		authentication.setDetails(new WebAuthenticationDetails(request));
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}
	
	
}
