package com.openxsl.admin.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.service.OperationLogService;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.webmvc.Response;

/**
 * 操作日志
 * @author shuilin.xiong
 */
@Api(value="vmp-admin", tags="系统管理-操作日志")
@RestController
@RequestMapping("admin/userLog")
public class OperationLogController {
	@Autowired
	private OperationLogService service;

	@ApiOperation(value="分页查询")
	@PostMapping(value = "/page")
	public Response page(@RequestParam String beginDate, @RequestParam String endDate,
						String userName, Pagination page){
		QueryMap<Object> params = new QueryMap<Object>();
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
		params.put("userName", userName);
		return Response.build(() -> service.queryForPage(params, page));
	}


}
