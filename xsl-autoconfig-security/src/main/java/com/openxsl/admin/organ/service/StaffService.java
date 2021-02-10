package com.openxsl.admin.organ.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openxsl.admin.entity.User;
import com.openxsl.admin.organ.dao.DeptStaffDao;
import com.openxsl.admin.organ.dao.StaffDao;
import com.openxsl.admin.organ.entity.Corporation;
import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.admin.organ.entity.joint.DeptStaff;
import com.openxsl.admin.service.AESPasswordEncoder;
import com.openxsl.admin.service.AuthenticateService;
import com.openxsl.config.dal.jdbc.BaseService;

@Service
public class StaffService extends BaseService<StaffDao,Staff,Integer> {
	@Autowired
	private DeptStaffDao deptStaffDao;
	@Autowired
	private CorporationService corpService;
	@Autowired
	private AuthenticateService authen;
	@Resource(name="AESEncoder")
	private AESPasswordEncoder passwordEncoder;
	
	public Staff getStaff(int staffId) {
		return mapper.getStaff(staffId);
	}
	public Staff getByUserId(int userId) {
		return mapper.getByUserId(userId);
	}
	
	public List<Integer> getDeptIds(Integer staffId){
		return deptStaffDao.selectByStaffId(staffId).stream().map(DeptStaff::getDeptId)
					.collect(Collectors.toList());
	}
	
	@Transactional
	public int setDeptments(Integer staffId, Integer[] deptIds) {
		Integer userType = mapper.getUserType(staffId);
		List<Integer> list = Arrays.asList(deptIds);
		if (userType != null) {
			list = this.filterDeptIds(staffId, deptIds);
			if (list.size() < 1) {
				throw new IllegalArgumentException("机构类型不正确");
			}
		}
		
		deptStaffDao.deleteByStaffId(staffId);
		int cnt = 0;
		for (Integer deptId : list) {
			cnt += deptStaffDao.insert(new DeptStaff(deptId, staffId));
		}
		return cnt;
	}
	
	public int bindUser(Integer staffId, String strUserId) {
		String userId = passwordEncoder.decode(strUserId);
		List<Integer> deptIds = this.getDeptIds(staffId);
		int size = deptIds.size();
		if (size > 0) {
			int userType = ((User)authen.getUser(Integer.parseInt(userId))).getUserType();
			Integer[] array = new Integer[deptIds.size()];
			if (this.filterDeptIds(userType, deptIds.toArray(array)).size() < size) {
				throw new IllegalArgumentException("用户类型不匹配");
			}
		}
		
		Staff staff = new Staff();
		staff.setId(staffId);
		staff.setUserId(userId);
		return this.update(staff);
	}
	
	//新增或修改时，只有一个部门
	public int saveOrUpdate(Staff staff, Integer deptId) {
		if (null == staff.getId()){
			String corpId = corpService.getCorporationId(deptId)[0];
			if (corpId == null) {
				throw new IllegalArgumentException("部门ID错误");
			}
			Corporation organ = corpService.get(corpId);
			staff.setCorpCode(organ.getCode());
			staff.setAreaCode(organ.getAreaCode());
			this.insert(staff);
			return deptStaffDao.insert(new DeptStaff(deptId,staff.getId()));
		} else {
			return this.update(staff);
		}
	}
	
	public List<String> getSubCorps(Staff staff) {
		List<Integer> deptIds = this.getDeptIds(staff.getId());
		if (deptIds == null) {
			return Collections.emptyList();
		} else {
			return corpService.getSubCorpCodes(deptIds.toArray(new Integer[0]));
		}
	}
	
	private List<Integer> filterDeptIds(int userType, Integer... deptIds) {
		List<Integer> results = new ArrayList<Integer>();
		int i = 0;
		for (String corpId : corpService.getCorporationId(deptIds)) {
			if (corpId == null) {
				continue;
			}
			int corpType = corpService.get(corpId).getType();
			if (userType == corpType) {
				results.add(deptIds[i]);
			}
			i++;
		}
		return results;
	}
	
}
