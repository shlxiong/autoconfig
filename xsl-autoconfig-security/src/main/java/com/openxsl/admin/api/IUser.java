package com.openxsl.admin.api;

import org.springframework.security.core.userdetails.UserDetails;

public interface IUser extends UserDetails{
	
	public String getUserId();

    public String getUsername();   //UserDetails

    public String getPassword();   //UserDetails

    public String getDomain();

    public boolean isEnabled();    //UserDetails

    public boolean isSysAdmin();

    public boolean hasRole(String roleId);

//    public boolean hasPermiss(String permissId);
//
//    public boolean isAccessible(String resourceId);

}
