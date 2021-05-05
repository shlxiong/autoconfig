package com.openxsl.admin.organ.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.context.LocalUserHolder;
import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.admin.organ.service.DepartmentService;
import com.openxsl.admin.organ.service.StaffService;
import com.openxsl.admin.service.AESPasswordEncoder;
import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.StringUtils;
import com.openxsl.config.util.TreeView.UTreeNode;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

/**
 * 员工接口
 * @author shuilin.xiong
 */
@Api(value="vmp-admin", tags="系统管理-人员")
@RestController
@RequestMapping("/staff")
public class StaffController extends BaseController<StaffService, Staff, Integer> {
	@Autowired
	private DepartmentService deptService;
	@Resource(name="AESEncoder")
	private AESPasswordEncoder passwordEncoder;

    /**
     * 部门数据ID list
     */
	@ApiOperation("查询员工所在的部门ID")
    @GetMapping(value = "getDeptIds")
    public Response getDeptIds(Integer staffId){
		 return Response.build(() -> service.getDeptIds(staffId));
    }

	@ApiOperation("按部门/姓名/电话查询员工信息（分页）")
	@PostMapping(path = "/page")
    public Response page(String deptId, String name, String phone, Pagination page) {
    	QueryMap<Object> params = new QueryMap<Object>();
		params.put("name", name);
		params.put("telephone", phone);
		if (!StringUtils.isEmpty(deptId)) {
			List<String> depts = new ArrayList<String>();
			for (UTreeNode node : deptService.getDepartsByNodeId(deptId)) {
				depts.add(node.getId().toString());
			}
			params.put("depts", depts);
		}
        return Response.build(() -> {
        	Page<Staff> pageData = service.queryForPage(params, page);
        	pageData.getData().forEach(e -> {
        		String strUserId = passwordEncoder.encode(e.getUserId());
        		e.setUserId(strUserId);
        	});
        	return pageData;
        });
    }

    /**
     * @Description: 保存员工
     */
	@ApiOperation("保存员工信息")
	@PostMapping(path = "/saveStaff")
    public Response saveOrUpdate(Integer deptId, Staff staff) {
        return Response.build(() -> {
        	service.saveOrUpdate(staff, deptId);
        });
    }
	
	@ApiOperation("查询没有机构的新用户")
	@PostMapping(path = "/listNewUsers")
	public Response listNewUsers(Pagination page) {
		return Response.build(() -> service.listNewUsers(page));
	}
	
	@ApiOperation("查询机构的所有职员")
	@PostMapping(path = "/listCorpStaffs")
	public Response listCorpStaffs(Pagination page) {
		String corpCode = LocalUserHolder.getCorpCode();
		return Response.build(() -> service.listCorpStaffs(corpCode, page));
	}

	@ApiOperation("员工关联多个部门")
	@PostMapping(path = "/setDeptments")
    public Response setDeptments(Integer staffId, Integer[] deptIds) {
        Assert.notNull(staffId, "员工ID不能为空");
        Assert.isTrue(deptIds!=null && deptIds.length>0, "部门ID不能为空");
        return Response.build(() -> {
        	service.setDeptments(staffId, deptIds);
        });
    }
	
	@ApiOperation("员工关联用户")
	@PostMapping(path = "/bindUser")
	public Response bindUser(Integer staffId, String userId) {
		return Response.build(() -> {
        	service.bindUser(staffId, userId);
        });
	}

}
