package com.openxsl.admin.dao;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.openxsl.admin.entity.User;
import com.openxsl.admin.entity.UserDetail;
import com.openxsl.config.dal.jdbc.BaseMapper;

public interface UserDao extends BaseMapper<User> {
	
	public User getUser(int userId);
	
	public User findUser(String userName);
	
	public User validatePassword(String userName, String password);
	
	public int insertUser(User user);
	
	public int insertUserDetail(UserDetail user);
	
	public int updateUser(User user);
	
	public int updateUserDetail(UserDetail user);
	
	public int deleteUser(int userId);
	
	public int deleteUserDetail(int userId);
	
	public int modifyPassword(String userName, String oldPass, String password);
	
	public int disableUser(@Param("userId")int userId, @Param("disabled")boolean disabled);
	
	public int lockUser(String userName);
	
	public UserDetail getUserDetail(int userId);
	
	public List<UserDetail> queryUsers(Map<String,?> wheres);

}
