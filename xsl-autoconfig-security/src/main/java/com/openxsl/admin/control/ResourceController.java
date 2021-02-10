package com.openxsl.admin.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.service.AuthorizeService;
import com.openxsl.admin.service.ResourceService;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

/**
 * 菜单和资源
 * @author shuilin.xiong
 */
@Api(value="vmp-admin", tags="系统管理-菜单")
@RestController
//@RequestMapping("admin/resource")
public class ResourceController extends BaseController<ResourceService, Resource, Integer>{
	private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private AuthorizeService authorizeService;
    
    //save, get, delete

    @ApiOperation("列举顶级菜单（下拉）")
    @GetMapping(value = "admin/resource/topMenu")
    public Response getTopMenu(String bizSys) {
    	return Response.build(() -> service.getTopMenu(bizSys));
    }
    
    @ApiOperation("列举下级菜单")
    @PostMapping(value = "admin/resource/list")
    public Response list(Integer parentId) {
    	return Response.build(() -> service.getSubTree(parentId));
    }

    /**
     * 禁用/启用
     */
    @ApiOperation("禁用/启用")
    @GetMapping(value = "admin/resource/disable")
    public Response disable(int id, boolean disabled) {
    	try {
	    	if (service.setDisable(id, disabled) > 0) {
	    		return Response.SUCCESS;
	    	}
    	} catch (Exception e) {
    		logger.error("", e);
    	}
    	return Response.FAILURE;
    }
    
    @ApiOperation("展示菜单树（授权）")
    @GetMapping(value = "admin/resource/tree")
    public Response getTree(String bizSys) {
        return Response.build(() -> service.queryAsTree(bizSys));
    }

    /**
     * 登录后获取用户菜单，这个需要放开权限
     */
    @ApiOperation("查询当前用户的菜单")
    @GetMapping(value = "resource/getUserMenu")
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Response getUserMenu(String bizSys) {
    	return Response.build(() -> {
			List<Resource> resources = (List)authorizeService.queryUserResources();
        	resources = resources.stream().filter(e->bizSys.equals(e.getDomain()))
        						.collect(Collectors.toList());
        	return resources;
    	});
    }
    
}
