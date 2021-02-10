package com.openxsl.admin.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.dao.BizSystemDao;
import com.openxsl.admin.dao.WebConfigDao;
import com.openxsl.admin.entity.BizSystem;
import com.openxsl.admin.entity.Resource;
import com.openxsl.admin.entity.WebConfig;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.rpcmodel.QueryMap;

@Service
public class WebConfigService extends BaseService<WebConfigDao, WebConfig, Integer> {
	@Autowired
	private WebConfigDao webConfigDao;
	@Autowired
	private BizSystemDao bizSystemDao;
	@Autowired
	private AuthorizeService author;
	
	public WebConfig getWebConfig(String webType, String corpCode) {
		WebConfig webConfig;
		if (webType != null) {
			webConfig = mapper.get(webType, corpCode);
		} else {
			webConfig = webConfigDao.getAll(corpCode).get(0);
		}
		return webConfig;
	}
	
	public List<WebConfig> listAllConfigs(String corpCode){
		return webConfigDao.getAll(corpCode);
	}
	
	@SuppressWarnings("unchecked")
	public Map<String, List<Resource>> getResources() {
		return ((List<Resource>)author.queryUserResources()).stream()
					.collect(Collectors.groupingBy(Resource::getDomain));
	}
	
	public List<BizSystem> getBizSystems(Set<String> codes) {
		if (codes == null) {
			codes = this.getResources().keySet();
		}
		List<BizSystem> lstBizSys = bizSystemDao.queryAll(
							new QueryMap<Object>("codes", codes));
		Collections.sort(lstBizSys, (s1,s2) -> {
			return s1.getSeqNo() - s2.getSeqNo();
		});
		return lstBizSys;
	}

}
