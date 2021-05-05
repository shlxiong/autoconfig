package com.openxsl.admin.organ.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.organ.entity.Department;
import com.openxsl.admin.organ.service.DepartmentService;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

/**
 * 部门接口
 * @author shuilin.xiong
 */
@Api(value="vmp-admin", tags="部门管理")
@RestController
@RequestMapping("/dept")
public class DepartmentController extends BaseController<DepartmentService, Department, Integer> {
	
	@ApiOperation("查询机构的一级部门(下拉列表)")
    @GetMapping(value = "listNames")
	public Response listNames(String corpCode) {
		return Response.build(() -> {
			Assert.notNull(corpCode, "机构编号不能为空");
        	return service.listNames(corpCode);
        });
	}

}