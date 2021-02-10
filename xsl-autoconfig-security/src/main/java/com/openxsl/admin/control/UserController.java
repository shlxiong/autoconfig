package com.openxsl.admin.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.text.SimpleDateFormat;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.entity.User;
import com.openxsl.admin.entity.UserDetail;
import com.openxsl.admin.service.UserService;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.webmvc.Response;

@Api(value="vmp-admin", tags="系统管理-用户")
@RequestMapping("admin/user")
@RestController
public class UserController { //extends BaseController<UserService, User, Integer> {
	@Value("${security.authen-login-url}")
	private String loginPage = "/login.jsp";
	@Autowired
	private UserService service;
	
	@ApiOperation("查询用户详情")
	@GetMapping(path = "get")
	public Response getDetail(String userId) {
		return Response.build(() -> service.getUser(userId));
	}
	
	@ApiOperation("删除用户")
	@PostMapping(path = "delete")
	public Response delete(String userId) {
		return Response.build(() -> {
			service.deleteUser(userId);
		});
	}
	
	@InitBinder
	public void initBinder(WebDataBinder binder) throws Exception {
//		binder.registerCustomEditor(Long.class, new CustomNumberEditor(Long.class, true));
		binder.registerCustomEditor(java.sql.Date.class, new CustomDateEditor(new SimpleDateFormat("yyyy-MM-dd"), true));
	}
	
	@ApiOperation("禁用/启用用户")
	@GetMapping(path="disable")
	public Response disableUser(String userId, boolean disabled) {
		return Response.build(() -> {
			service.modifyStatus(userId, disabled);
		});
	}
	
	@ApiOperation("保存用户信息")
	@PostMapping(path = "saveDetail")
	public Response saveDetail(@Valid User user, UserDetail detail) {
		return Response.build(() -> {
			if (user.getId() == null) {
				service.register(user, detail);
			} else {
				service.modifyUser(user, detail);
			}
		});
    }
	
	@ApiOperation("按条件分页查询")
	@PostMapping("query")
	public Response query(String username, String mobile, Pagination page) {
		return Response.build(() -> service.queryUser(username, mobile, page));
	}
	
	@ApiOperation("查询用户角色")
	@GetMapping("getRoles")
	public Response getUserRoles(@RequestParam("userId") String userId) {
		return Response.build(() -> service.listRoles(userId));
	}
	
	@ApiOperation("修改用户角色")
	@PostMapping("updateUserRole")
	@ResponseBody
	public Response updateUserRole(String userId, String[] roleIds) {
		return Response.build(() -> {
			service.grantRoles(userId, roleIds);
		});
	}
	
}
