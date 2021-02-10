package com.openxsl.admin.organ.dao;

import java.util.List;

import com.openxsl.admin.organ.entity.joint.DeptStaff;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface DeptStaffDao extends BaseMapper<DeptStaff> {

	List<Integer> getUserIdsByDepid(Integer id);

    List<DeptStaff> selectByStaffId(Integer id);

    void deleteByStaffId(Integer staffId);

}

