package com.openxsl.admin.dao;

import com.openxsl.admin.entity.User;
import com.openxsl.admin.entity.UserDetail;
import com.openxsl.config.rpcmodel.Page;

public interface UserDao {
	
public User getUser(int userId);
	
	public User findUser(String userName);
	
	public User findUser(String userName, String password);
	
	public Page<User> queryUser(int pageNo, int pageSize);
	
	public int insertUser(User user);
	
	public int updateUser(User user);
	
	public int modifyPassword(String userName, String oldPass, String password);
	
	public UserDetail getUserDetail(int userId);

}
