package com.openxsl.admin.organ.dao;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.organ.entity.Position;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface PositionDao extends BaseMapper<Position> {
	
	List<Position> queryByBizType(@Param("bizType")String bizType, 
							@Param("corpCode")String corpCode);
	
	int setLeader(@Param("id")Integer positionId, @Param("leader")String leader);

}
