package com.openxsl.config.filedata.export.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.config.filedata.export.entity.ImportConfig;
import com.openxsl.config.filedata.export.entity.ImportLog;
import com.openxsl.config.filedata.export.entity.ImportMapping;
import com.openxsl.config.filedata.export.service.ImportConfigService;
import com.openxsl.config.filedata.export.service.ImportService;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.StringUtils;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

@Api(value="vmp-base", tags="数据导入")
@RequestMapping("import")
@RestController
public class ImportController extends BaseController<ImportConfigService, ImportConfig, Integer>{
	@Autowired
	private ImportService importService;
	
	@ApiOperation(value="查询导入配置", notes="综合管控,产业监测")
	@GetMapping(value="page")
	public Response page(String name, String tableName, String scenicCode, Pagination page){
		return Response.build(() -> {
			QueryMap<Object> params = new QueryMap<Object>();
			if (!StringUtils.isEmpty(name)) {
				params.put("name", name);
			}
			if (!StringUtils.isEmpty(tableName)) {
				params.put("tableName", tableName);
			}
			if (!StringUtils.isEmpty(scenicCode)) {
				params.put("scenicCode", scenicCode);
			}
			return service.queryForPage(params, page);
		});
	}
	@ApiOperation(value="查询导入配置列表", notes="综合管控,产业监测")
	@GetMapping(value="list")
	public Response list(String scenicCode){
		return Response.build(() -> {
			QueryMap<Object> params = new QueryMap<>("scenicCode",scenicCode);
			return service.getKeyValues(params);
		});
	}
	
	@ApiOperation(value="文件导入", notes="综合管控,产业监测")
	@PostMapping(value="importData")
	public Response importData(HttpServletRequest request, ImportLog importLog) {
		return Response.build(() -> {
			String sourceFile = request.getServletContext().getRealPath(importLog.getSourceFile());
			importLog.setSourceFile(sourceFile);
			importService.importFile(importLog);
		});
	}
	
	@ApiOperation(value="读取表字段", notes="综合管控,产业监测")
	@GetMapping(value="getTable")
	public Response getTable(String tableName) {
		return Response.build(() -> importService.generateMappings(tableName));
	}
	@ApiOperation(value="读取已存在的映射关系", notes="综合管控,产业监测")
	@GetMapping(value="getMappings")
	public Response getMappings(String importName, String scenicCode) {
		return Response.build(() -> importService.getMappings(importName, scenicCode));
	}
	
	@ApiOperation(value="保存映射关系", notes="综合管控,产业监测")
	@PostMapping(value="saveMappings")
	public Response saveMappings(@RequestBody ImportMapping[] importMappings) {
		return Response.build(() -> {
//			importService.saveImportConfig(config);
			if (importMappings.length > 0) {
				String importName = importMappings[0].getConfigName();
				String scenicCode = importMappings[0].getScenicCode();
				importService.saveMappings(importName, importMappings, scenicCode);
			}
		});
	}

}
