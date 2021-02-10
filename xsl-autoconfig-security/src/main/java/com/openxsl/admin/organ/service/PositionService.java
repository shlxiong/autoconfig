package com.openxsl.admin.organ.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.openxsl.admin.context.LocalUserHolder;
import com.openxsl.admin.organ.dao.PositionDao;
import com.openxsl.admin.organ.dao.PositionRoleDao;
import com.openxsl.admin.organ.dao.PositionStaffDao;
import com.openxsl.admin.organ.entity.Position;
import com.openxsl.admin.organ.entity.Staff;
import com.openxsl.admin.organ.entity.joint.PositionRole;
import com.openxsl.admin.organ.entity.joint.PositionStaff;
import com.openxsl.config.dal.jdbc.BaseService;

/**
 * @author shuilin.xiong
 */
@Service
public class PositionService extends BaseService<PositionDao, Position, Integer> {
	@Autowired
	private PositionRoleDao roleDao;
	@Autowired
	private PositionStaffDao staffDao;
	
	@Transactional
	public void updateRoles(Integer positionId, Integer[] roleIds) {
		roleDao.deleteByPostId(positionId);
		for (Integer roleId : roleIds) {
			roleDao.insert(new PositionRole(positionId, roleId));
		}
	}
	
	public List<String> getRoles(String groupId) {
		return roleDao.queryPostRoles(groupId);
	}
	
	/**
	 * 按照业务类型查询
	 * @param bizType 'remark'字段
	 * @return
	 */
	public List<Position> queryByBizType(String bizType) {
		String corpCode = LocalUserHolder.getCorpCode();
		return mapper.queryByBizType(bizType, corpCode);
	}
	
	/**
	 * 查询所有人的联系信息
	 * @param positionId
	 */
	public List<Staff> queryStaffs(int positionId){
		return staffDao.queryStaffsByPosit(positionId);
	}
	
	@Transactional
	public int updatePositionStaff(Integer positionId, Integer[] staffIds) {
		staffDao.deleteByStaffId(positionId);
		int cnt = 0;
		for (Integer staffId : staffIds) {
			cnt += staffDao.insert(new PositionStaff(positionId, staffId));
		}
		return cnt;
	}
	
	public int updateLeader(Integer positionId, String leader) {
		return mapper.setLeader(positionId, leader);
	}
	
}
