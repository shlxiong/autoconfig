package com.openxsl.admin.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.openxsl.admin.dao.RoleDao;
import com.openxsl.admin.entity.Role;
import com.openxsl.config.dal.jdbc.BaseService;

@Service
public class RoleService extends BaseService<RoleDao, Role, Integer> {
	
	public int setDisabled(int roleId, boolean disabled) {
		return mapper.setDisabled(roleId, disabled);
	}
	
	public List<Role> queryAll(boolean disabled){
		Role example = new Role();
		example.setDisabled(disabled);
		return super.list(example);
	}

}
