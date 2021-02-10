package com.openxsl.admin.organ.dao;

import java.util.List;

import com.openxsl.admin.organ.entity.joint.PositionRole;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface PositionRoleDao extends BaseMapper<PositionRole>{

    void deleteByPostId(Integer positionId);

    List<String> queryUserRoles(String userId);
    
    List<String> queryPostUsers(String postId);
    
    List<String> queryPostRoles(String postId);
}
