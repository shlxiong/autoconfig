package com.openxsl.admin.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.entity.Role;
import com.openxsl.admin.service.AuthorizeService;
import com.openxsl.admin.service.RoleService;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

/**
 * 角色管理
 * @author shuilin.xiong
 */
@Api(value="vmp-admin", tags="系统管理-角色")
@RestController
@RequestMapping("admin/role")
public class RoleController extends BaseController<RoleService, Role, Integer> {
	@Autowired
	private AuthorizeService authorService;

	//save, get, delete
	
	@ApiOperation("分页查询")
    @PostMapping(value = "list")
    public Response list(String roleName, Pagination page){
		QueryMap<Object> params = new QueryMap<Object>("roleName", roleName);
		return Response.build(()->service.queryForPage(params, page));
	}
	
	@ApiOperation("查询启用状态的角色")
	@GetMapping(value = "available")
    public Response list(){
		return Response.build(()->service.queryAll(false));
	}
	
	/**
     * 禁用/启用
     */
    @ApiOperation("禁用/启用")
    @GetMapping(value = "disable")
    public Response disable(int id, boolean disabled) {
    	if (id < 1) {
    		return Response.SUCCESS;
    	}
    	return Response.build(() ->	service.setDisabled(id, disabled));
    }
    @RequestMapping(path = "delete", method = RequestMethod.POST)
    @Override
    public Response delete(@RequestParam("ids") Integer[] ids) {
    	return Response.build(() -> {
    		if (ids.length < 1) {
    			throw new IllegalArgumentException("没有提交任何数据");
    		}
    		List<Integer> list = Arrays.asList(ids).stream().filter(e -> (e>0))
    				.collect(Collectors.toList());
    		Integer[] newids = new Integer[list.size()];
    		if (service.delete(list.toArray(newids)) < 1) {
    			throw new IllegalArgumentException("操作失败");
    		}
    	});
    }
    
    @ApiOperation(value="角色-资源列表", response=Resource.class, responseContainer="List")
    @GetMapping(value = "rolePerms")
    public Response getResources(String roleId) {
    	return Response.build(() ->	authorService.queryRoleResources(roleId));
    }
    
    @ApiOperation("给角色授权")
    @GetMapping(value = "grantPerms")
    public Response bindResources(@RequestParam String roleId, @RequestParam String[] resourceIds) {
    	return Response.build(() ->	{
    		authorService.unbindResources(roleId);
    		authorService.bindResources(roleId, resourceIds);
    	});
    }

}
