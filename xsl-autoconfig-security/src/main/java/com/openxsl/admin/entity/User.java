package com.openxsl.admin.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.openxsl.admin.api.IUser;
import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_user")
@SuppressWarnings("serial")
public class User extends BaseEntity<Integer> implements IUser {
//	@Column
//	private int id;
	@Column
	private String username;          //用户名（登录账号）
	@Column
	private String password;          //密码
	@Column(name="user_type")
	private int userType;             //类型：0普通用户，1-业务管理员，2-系统管理员
	@Column
	private String email;
	@Column
	private boolean disabled;         //true:禁用, false:启用
	@Column
	private String domain;            //sysId 来自哪个系统
	
	private transient List<String> roles;
	
	//========================================== IUser ======================================
	@Override
	public String getUserId() {
		return String.valueOf(this.getId());
	}
	@Override
	public boolean isSysAdmin() {
		return this.hasRole(Role.ADMIN_ID);
	}
	@Override
	public boolean hasRole(String roleId) {
		if (roleId == null) {
			return true;
		}
		return (roles != null) ? roles.contains(roleId) : false;
	}
//	@Override
//	public boolean isAccessible(String resourceId) {
//		return false;
//	}
	
	//=================================Spring security==============================
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		int len = (roles == null) ? 0 : roles.size();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(len);
		if (len > 0) {
			for (String roleId : roles) {
				authorities.add(new SimpleGrantedAuthority(roleId));
			}
		}
		return authorities;
	}
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	@Override
	public boolean isEnabled() {
		return !disabled;
	}
	
	// ==================================== JavaBean =======================================
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public int getUserType() {
		return userType;
	}
	public void setUserType(int userType) {
		this.userType = userType;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

}
