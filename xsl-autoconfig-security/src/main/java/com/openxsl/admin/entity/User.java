package com.openxsl.admin.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.openxsl.admin.api.IUser;
import com.openxsl.config.dal.jdbc.BaseEntity;

@Entity
@Table(name = "admin_user")
@SuppressWarnings("serial")
public class User extends BaseEntity<Integer> implements IUser {
	public static final int USER_SCENIC = 0;
	public static final int USER_GOV = 1;
	public static final int USER_ADMIN = 2;
	public static final int USER_GUEST = 3;
	
//	@Column
//	private int id;
	@Column
	private String username;          //用户名（登录账号）
	@Column
	private String password;          //密码
	@Column(name="user_type")
	private int userType;             //类型：0-景区用户，1-政府人员，2-系统管理员，3-其他
	@Column
	private String email;
	@Column
	private boolean disabled;         //true:禁用, false:启用
	@Column
	private String domain;            //sysId 来自哪个系统
	@Column
	private Date pswdDate;
	@Column
	private Date lockedDate;
	
	private transient List<String> roles;
	
	//========================================== IUser ======================================
	@JsonIgnore
	@Override
	public String getUserId() {
		return String.valueOf(this.getId());
	}
	@JsonIgnore
	@Override
	public boolean isSysAdmin() {
		return userType == 2;
	}
	@Override
	public boolean hasRole(String roleId) {
		if (roleId == null) {
			return true;
		}
		return this.getRoles().contains(roleId);
	}
//	@Override
//	public boolean isAccessible(String resourceId) {
//		return false;
//	}
	
	//=================================Spring security==============================
	@JsonIgnore
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		int len = (roles == null) ? 0 : roles.size();
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>(len);
		String prefix = "ROLE_";
		if (len > 0) {
			for (String roleId : roles) {
				authorities.add(new SimpleGrantedAuthority(prefix+roleId));
			}
		}
		if (isSysAdmin()) {
			authorities.add(new SimpleGrantedAuthority(prefix+Role.ADMIN_ID));
			authorities.add(new SimpleGrantedAuthority(prefix+Role.ADMIN));
		}
		return authorities;
	}
	@JsonIgnore
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}
	@JsonIgnore
	@Override
	public boolean isAccountNonLocked() {
		return lockedDate==null || lockedDate.before(new Date());
	}
	@JsonIgnore
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}
	@JsonIgnore
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
		return this.getAuthorities().stream().map(e->{
					String roleId = e.getAuthority();
					if (e.getAuthority().startsWith("ROLE_")) {
						roleId = roleId.substring("ROLE_".length());
					}
					return roleId;
			}).collect(Collectors.toList());
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	public Date getPswdDate() {
		return pswdDate;
	}
	public void setPswdDate(Date pswdDate) {
		this.pswdDate = pswdDate;
	}
	public Date getLockedDate() {
		return lockedDate;
	}
	public void setLockedDate(Date lockedDate) {
		this.lockedDate = lockedDate;
	}
}
