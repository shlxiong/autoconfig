package com.openxsl.admin.organ.control;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.openxsl.admin.context.LocalUserHolder;
import com.openxsl.admin.organ.entity.Corporation;
import com.openxsl.admin.organ.service.CorporationService;
import com.openxsl.config.webmvc.BaseController;
import com.openxsl.config.webmvc.Response;

/**
 * 机构接口
 * @author shuilin.xiong
 */
@Api(value="vmp-admin", tags="系统管理-机构")
@RestController
@RequestMapping("/corp")
public class CorporationController extends BaseController<CorporationService, Corporation, Integer> {
	
	@ApiOperation("获取机构/部门树")
    @RequestMapping(path="/tree", method=RequestMethod.GET)
    public Response corpDepTree() {
		Map<String, Integer> dataMap = service.list(new Corporation()).stream()
				.collect(Collectors.toMap(Corporation::getCode, Corporation::getId));
		Integer parentId = dataMap.get(LocalUserHolder.getCorpCode());
		String strPId = parentId==null ? null : parentId.toString();
        return Response.build(() -> service.queryAsTree(strPId));
    }

	@ApiOperation("获取下级机构/部门")
    @RequestMapping(path="/subOrgs", method=RequestMethod.GET)
    public Response findSubOrgans(String id) {
        return Response.build(()->service.queryDirectOrgans(id));
    }
	
	@Override
	protected void checkExists(Corporation corp) {
		if (service.existsCorpCode(corp.getCode())) {
			throw new IllegalArgumentException("机构编号已经存在");
		}
	}

}
