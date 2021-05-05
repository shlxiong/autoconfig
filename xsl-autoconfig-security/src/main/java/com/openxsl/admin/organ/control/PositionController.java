package com.openxsl.admin.organ.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.organ.entity.Position;
import com.openxsl.admin.organ.service.PositionService;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

@Api(value="vmp-admin", tags="系统管理-用户组")
@RequestMapping("/position")
@RestController
public class PositionController extends BaseController<PositionService, Position, Integer> {

    @ApiOperation("查询用户组列表（分页）")
    @PostMapping(path = "/page")
    public Response page(String name, Pagination page) {
    	QueryMap<Object> params = new QueryMap<Object>(2);
    	params.put("name", name);
        return Response.build(() -> service.queryForPage(params,page));
    }
    
    @ApiOperation("更新用户组角色")
    @PostMapping(value = "updateRoles")
    public Response updateRoles(Integer positionId, Integer[] roleIds) {
    	Assert.notNull(positionId, "职位ID不能为空");
        Assert.isTrue(roleIds!=null && roleIds.length>0, "角色ID不能为空");
        return Response.build(()-> {
        		service.updateRoles(positionId, roleIds);
        });
    }
    
    @ApiOperation("获取用户组的角色")
    @GetMapping(value = "getRoles")
    public Response getRoles(String positionId) {
    	Assert.notNull(positionId, "职务ID不能为空");
        return Response.build(() -> service.getRoles(positionId));
    }
    
    @ApiOperation("获取用户组的人员")
    @GetMapping(value = "getStaffs")
    public Response getStaffs(Integer positionId) {
    	Assert.notNull(positionId, "职务ID不能为空");
    	return Response.build(() -> service.queryStaffs(positionId));
    }

    @ApiOperation("添加人员")
    @PostMapping(path = "/addStaffs")
    public Response addStaffs(Integer positionId, Integer[] staffIds) {
        Assert.notNull(positionId, "职务ID不能为空");
        return Response.build(()-> {
        		service.updatePositionStaff(positionId, staffIds);
        });
    }
    
    @ApiOperation("设置业务小组的组长")
    @PostMapping(path = "/setLeader")
    public Response setLeader(Integer positionId, String staffId) {
    	return Response.build(()-> {
    			service.updateLeader(positionId, staffId);
    	});
    }

}
