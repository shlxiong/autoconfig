package com.openxsl.admin.service;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IAuthenticate;
import com.openxsl.admin.api.IAuthorize;
import com.openxsl.admin.api.IPasswordEncoder;
import com.openxsl.admin.api.IUser;
import com.openxsl.admin.dao.UserDao;
import com.openxsl.admin.entity.User;
import com.openxsl.config.rpcmodel.Page;

/**
 * 认证服务类
 * 
 * @author xiongsl
 */
@Service("userService")
public class AuthenticateService implements IAuthenticate {
	@Resource(name="userDao")
	private UserDao dao;
	@Resource
	private IPasswordEncoder passwordEncoder;
	@Autowired
	private IAuthorize authorize;
	
	@Override
	public IUser passpord(String account, String password) {
		password = passwordEncoder.encode(password);
		return dao.findUser(account, password);
	}

	@Override
	public IUser getUser(int id) {
		return dao.getUser(id);
	}

	@Override
	public IUser getUserByName(String account) {
		User user = dao.findUser(account);
		if (user == null) {
			throw new UsernameNotFoundException("username not found");
		} else {
			return user;
		}
	}

	@Override
	public IUser loadUserByUsername(String username)
				throws UsernameNotFoundException {
		User user = (User)this.getUserByName(username);
		String userId = user.getUserId();
		user.setRoles(authorize.queryUserRoles(userId));
		return user;
	}

	public boolean existsUser(String username) {
		try{
			return dao.findUser(username) != null;
		}catch(Exception e){
			return true;
		}
	}

	/**
	 * 注册用户
	 * 
	 * @param user
	 * @return
	 */
	public boolean register(User user) {
		user.setPassword(passwordEncoder.encode(user.getPassword()));
		try {
			return dao.insertUser(user) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 修改密码
	 * 
	 * @param userName
	 * @param oldPass
	 * @param password
	 * @return
	 */
	public int modifyPassword(String userName, String oldPass, String password) {
		oldPass = passwordEncoder.encode(oldPass);
		password = passwordEncoder.encode(password);
		return dao.modifyPassword(userName, oldPass, password);
	}

	/**
	 * 修改用户
	 * 
	 * @param user
	 * @return
	 */
	public int modifyUser(User user) {
		return dao.updateUser(user);
	}
	
	/**
	 * 修改用户状态
	 * @param userId
	 * @param disabled
	 * @return
	 */
	public int modifyStatus(String userId, int disabled) {
		return 0; //dao.updateUser(Long.parseLong(userId), new KvPair("disabled", disabled));
	}

	/**
	 * 修改用户权限
	 * @param id
	 * @param role
	 * @return
	 */
	public int modifyRole(long id, String role){
//		if(!StringUtils.isEmpty(role)){
//			role = role.replace(Role.PREFIX, "");
//		}
//		return dao.updateUser(id, new KvPair("role", role));
		return 0;
	}

	/**
	 * 用户列表查询
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @return
	 */
	public Page<IUser> queryUser(Integer pageNo, Integer pageSize) {
		return null; //dao.queryUser(pageNo, pageSize);
	}

	/**
	 * 管理员重置密码
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public int resetPassword(String username) {
//		IUser user = this.getUserByName(username);
//		String password = passwordEncoder.getSalt() + "-" + username;
//		password = passwordEncoder.encode(password);
//		return dao.updateUser(user.getId(), new KvPair("password", password));
		return 0;
	}
	
	/**
	 * 忘记密码
	 * 
	 * @param username
	 * @param password
	 * @return
	 */
	public int forgetPwdPassword(String username, String password) {
//		IUser user = this.getUserByName(username);
//		password = passwordEncoder.encode(password);
//		return dao.updateUser(user.getId(), new KvPair("password", password));
		return 0;
	}

}
