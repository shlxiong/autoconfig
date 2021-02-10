package com.openxsl.admin.control;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.context.LocalUserHolder;
import com.openxsl.admin.entity.WebConfig;
import com.openxsl.admin.service.WebConfigService;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

/**
 * 系统配置
 */
@RestController
@RequestMapping("admin/webconfig")
public class WebConfigController extends BaseController<WebConfigService, WebConfig, Integer> {
	
	//get, delete, save
	
	@PostMapping(value = "list")
	public Response list(String name){
		String corpCode = LocalUserHolder.getCorpCode();
		return Response.build(() -> service.listAllConfigs(corpCode));
	}

}