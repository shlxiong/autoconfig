package com.openxsl.admin.service;

import java.util.Date;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.openxsl.admin.api.IAuthenticate;
import com.openxsl.admin.api.IAuthorize;
import com.openxsl.admin.api.IPasswordEncoder;
import com.openxsl.admin.api.IUser;
import com.openxsl.admin.dao.UserDao;
import com.openxsl.admin.entity.User;
import com.openxsl.admin.security.exception.PasswordRequiresModifyException;
import com.openxsl.config.util.HexEncoder;

/**
 * 登录认证服务
 * @author shuilin.xiong
 */
@Service
public class AuthenticateService implements IAuthenticate {
	@Value("${security.password-init}")
	private String initPswd;
	@Resource(name="userDao")
	private UserDao dao;
	@Resource(name="MD5Encoder")
	private IPasswordEncoder passwordEncoder;
	@Autowired
	private IAuthorize authorize;
	
	@Override
	public IUser passpord(String account, String password) {
		password = passwordEncoder.encode(password);
		return dao.validatePassword(account, password);
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
		}
		return user;
	}

	@Override
	public IUser loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = (User)this.getUserByName(username);
		String userId = user.getUserId();
		user.setRoles(authorize.queryUserRoles(userId));
		return user;
	}
	
	@Override
	public IPasswordEncoder getPasswordEncoder() {
		return this.passwordEncoder;
	}

	public boolean existsUser(String username) {
		try{
			return dao.findUser(username) != null;
		}catch(Exception e){
			return true;
		}
	}

	/**
	 * 修改密码
	 * @param userName
	 * @param oldPass
	 * @param password
	 */
	public void modifyPassword(String userName, String oldPass, String password) {
		this.isSimplePassword(password);
		
		oldPass = passwordEncoder.encode(oldPass);
		password = passwordEncoder.encode(password);
		if (dao.modifyPassword(userName, oldPass, password) < 1) {
			throw new IllegalArgumentException("用户名或密码错");
		}
	}

	/**
	 * 管理员重置密码
	 */
	public void resetPassword(String username) {
		this.forgetPwdPassword(username, HexEncoder.decode(initPswd));
	}
	
	/**
	 * 忘记密码，通过邮箱验证后过来的，由用户提供新密码
	 * @param username 账号
	 * @param password 新密码
	 */
	public void forgetPwdPassword(String username, String password) {
		this.isSimplePassword(password);
		
		IUser user = this.getUserByName(username);
		password = passwordEncoder.encode(password);
		if (dao.modifyPassword(username, user.getPassword(), password) < 1) {
			throw new IllegalArgumentException("用户名或密码错");
		}
	}
	
	public void checkPassword(Authentication authentication) {
		this.isSimplePassword(authentication);
		
		if (this.requiresModifyPswd(authentication)) {
			throw new PasswordRequiresModifyException("密码已过期");
		}
	}
	
	private void isSimplePassword(Authentication authentication) {
		String loginPswd = (String)authentication.getCredentials();
		this.isSimplePassword(loginPswd);
	}
	private void isSimplePassword(String loginPswd) {
		String message = "密码不符合安全要求";
		if (loginPswd.length() < 8) {
			throw new PasswordRequiresModifyException(message);
		}
		
		boolean[] flag = new boolean[4];
		for (char ch : loginPswd.toCharArray()) {
			if (Character.isUpperCase(ch)) {
				flag[0] = true;
			} else if (Character.isLowerCase(ch)) {
				flag[1] = true;
			} else if (Character.isDigit(ch)) {
				flag[2] = true;
			} else {
				flag[3] = true;
			}
		}
		if (!(flag[0] && flag[1] && flag[2] && flag[3])) {
			throw new PasswordRequiresModifyException(message);
		}
	}
	
	private boolean requiresModifyPswd(Authentication authentication) {
		String initPswd = passwordEncoder.getInitialPswd();
		String loginPswd = (String)authentication.getCredentials();
		if (passwordEncoder.matches(loginPswd, initPswd)) {
			return true;
		}
		long expireTime = System.currentTimeMillis() - 30 * 24 * 3600000L;
		User user = null;
		if (authentication instanceof User) {
			user = (User)authentication;
		} else {
			user = (User)authentication.getPrincipal();
		}
		Date passwordDate = user.getPswdDate();
		if (passwordDate!=null && passwordDate.before(new Date(expireTime))) {
			return true;
		}
		return false;
	}

}
