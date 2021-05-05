package com.openxsl.admin.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.openxsl.admin.dao.RoleDao;
import com.openxsl.admin.dao.UserDao;
import com.openxsl.admin.entity.Role;
import com.openxsl.admin.entity.User;
import com.openxsl.admin.entity.UserDetail;
import com.openxsl.config.dal.jdbc.BaseService;
import com.openxsl.config.rpcmodel.Page;
import com.openxsl.config.rpcmodel.Pagination;
import com.openxsl.config.rpcmodel.QueryMap;
import com.openxsl.config.util.StringUtils;

@Service
public class UserService extends BaseService<UserDao, User, Integer>{
	@Autowired
	private AuthenticateService authen;
	@Resource(name="AESEncoder")
	private AESPasswordEncoder passwordEncoder;
	@Autowired
	private AuthorizeService author;
	@Autowired
	private RoleDao roleDao;
	
	/**
	 * 用户列表查询
	 */
	public Page<UserDetail> queryUser(String name, String mobile, Pagination page) {
		QueryMap<Object> wheres = new QueryMap<Object>();
		wheres.put("name", name);
		wheres.put("mobile", mobile);
		wheres.put("page", page);
		List<UserDetail> lstUser = mapper.queryUsers(wheres);
		lstUser.forEach(e->{
			e.setUserId(passwordEncoder.encode(e.getUserId()));
			e.setEncodeName(passwordEncoder.encode(e.getUsername()));
		});
		return new Page<UserDetail>(page, lstUser);
	}
	
	/**
	 * 读取用户明细信息
	 */
	public UserDetail getUser(String strUserId) {
		int userId = Integer.parseInt(passwordEncoder.decode(strUserId));
		User user = mapper.getUser(userId);
		UserDetail detail = mapper.getUserDetail(userId);
		if (detail == null) {
			detail = new UserDetail();
		}
		detail.setUserId(strUserId);
		detail.setUsername(user.getUsername());
		detail.setEmail(user.getEmail());
		detail.setDomain(user.getDomain());
		detail.setUserType(user.getUserType());
		return detail;
	}
	
	/**
	 * 注册用户
	 * @param user
	 */
	public boolean register(User user, UserDetail detail) {
		if (authen.existsUser(user.getUsername())) {
			throw new IllegalArgumentException(user.getUsername()+" already exists");
		}
		String password = user.getPassword();
		if (StringUtils.isEmpty(password)) {
			password = authen.getPasswordEncoder().getInitialPswd();
		} else {
			password = authen.getPasswordEncoder().encode(password);
		}
		user.setPassword(password);
		boolean flag = false;
		try {
			flag = mapper.insertUser(user) > 0;
		} catch (Exception e) {
			return false;
		}
		if (flag && detail != null) {
			detail.setUserId(user.getUserId());
			mapper.insertUserDetail(detail);
		}
		return flag;
	}

	/**
	 * 修改用户
	 * @param user
	 */
	public int modifyUser(User user, UserDetail detail) {
		String userId = passwordEncoder.decode(detail.getUserId());
		user.setId(Integer.parseInt(userId));
		detail.setUserId(userId);
		mapper.updateUserDetail(detail);
		return mapper.updateUser(user);
	}
	
	/**
	 * 删除一个用户
	 * @param userId
	 * @return
	 */
	public int deleteUser(String strUserId) {
		int userId = Integer.parseInt(passwordEncoder.decode(strUserId));
		mapper.deleteUserDetail(userId);
		return mapper.deleteUser(userId);
	}
	
	/**
	 * 修改用户状态
	 * @param userId  用户ID
	 * @param disabled
	 */
	public int modifyStatus(String strUserId, boolean disabled) {
		int userId = Integer.parseInt(passwordEncoder.decode(strUserId));
		return mapper.disableUser(userId, disabled);
	}
	
	/**
	 * 锁定用户一小时
	 * @param userName
	 */
	public int lockUser(String userName) {
		return mapper.lockUser(userName);
	}
	
	/**
	 * 列举用户拥有的角色
	 * @param userId
	 */
	public List<Role> listRoles(String strUserId){
		int userId = Integer.parseInt(passwordEncoder.decode(strUserId));
		return roleDao.queryUserRoles(userId);
	}
	
	/**
	 * 给用户重新赋角色
	 * @param userId  用户ID
	 * @param roleIds 角色ID
	 */
	public boolean grantRoles(String strUserId, String[] roleIds) {
		String userId = passwordEncoder.decode(strUserId);
		author.revokeRolesOfUser(userId);
		author.grantUserRole(userId, roleIds);
		return true;
	}
	
}
