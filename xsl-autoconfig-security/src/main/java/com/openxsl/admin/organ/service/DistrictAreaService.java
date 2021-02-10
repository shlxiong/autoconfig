package com.openxsl.admin.organ.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.openxsl.admin.organ.dao.DistrictAreaDao;
import com.openxsl.admin.organ.entity.DistrictArea;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.service.Refreshable;

@Service
public class DistrictAreaService extends BaseService<DistrictAreaDao, DistrictArea, Integer>
				implements Refreshable {
	private final Map<String, String> provinces = new HashMap<String, String>(40);
	private final Map<String, List<DistrictArea>> provinceMap = 
						new HashMap<String, List<DistrictArea>>(40);
	
	@PostConstruct
	@Override
	public boolean refresh() {
		provinceMap.clear();
		List<DistrictArea> data = this.queryProvinces();
		provinces.putAll(data.stream().collect(
					Collectors.toMap(DistrictArea::getAreaCode, DistrictArea::getShortName)));
		provinceMap.putAll(data.stream().collect( 
					Collectors.groupingBy(DistrictArea::getShortName)) );
		data.clear();
		return true;
	}
	
	public Map<String,String> getProvinces(){
		return provinces;
	}
	public List<DistrictArea> getCities(String provCode){
		String province = provinces.get(provCode);
		List<DistrictArea> cities = new ArrayList<DistrictArea>();
		if (province == null) {
			for (List<DistrictArea> each : provinceMap.values()) {
				cities.addAll(each);
			}
		} else {
			cities.addAll(provinceMap.get(province));
			if (cities.isEmpty() && province.length() > 1) {
				cities.addAll(provinceMap.get(province.substring(0, 2)));  //多数为2位
			}
			if (cities.isEmpty() && province.length() > 2) {
				cities.addAll(provinceMap.get(province.substring(0, 3)));  //内蒙古、黑龙江
			}
		}
		return cities;
	}
	public Map<String, Object> getDistrictAreas(String areaCode) {
		Map<String, Object> result = new HashMap<String, Object>();
		String[] names = {"province", "city", "county"};
		int level = 3;
		while (level > 1) {  //1省/直辖市,2地级市,3区县,4镇/街道
			DistrictArea area = this.getByCode(areaCode);
			if (area == null) {
				break;
			}
			level = area.getLevel();
			if (level > 3) {
				continue;
			}
			Map<String,String> map = new HashMap<String,String>();
			map.put("code", areaCode);
			map.put("name", area.getShortName());
			result.put(names[level-1], map);
			areaCode = area.getParentCode();
		}
		return result;
	}
	
	public List<DistrictArea> queryProvinces() {
		return mapper.queryProvinces();
	}
	
	public List<DistrictArea> querySubAreas(String parentCode) {
		return mapper.querySubAreas(parentCode);
	}
	
	public DistrictArea getByCode(String code) {
		try {
			return mapper.getByCode(code);
		} catch (Exception e) {
			return null;
		}
	}
	
	public DistrictArea findByShortName(String shortName, int level) {
		try {
			return mapper.findByShortName(shortName, level);
		} catch (Exception e) {
			return null;
		}
	}
	
	public String[] getCertCity(String certNo) {
		Assert.isTrue(certNo!=null && certNo.length()>=4, "'certNO' must have more than 4 chars");
		String provinceCode = certNo.substring(0, 2) + "0000";
		String provinceName = provinces.get(provinceCode);
		String cityCode = certNo.substring(0, 4) + "00";
		StringBuilder cityName = new StringBuilder();
		provinceMap.get(provinceName).forEach(e -> {
			if (e.getAreaCode().equals(cityCode)) {
				cityName.append(e.getShortName());
			}
		});
		return new String[] {provinceName, cityName.toString()};
	}

}
