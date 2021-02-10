package com.openxsl.admin.api;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface IAuthenticate extends UserDetailsService {  

    /**
     * 认证账号和密码
     */
    public IUser passpord(String account, String password);

    public IUser getUser(int userId);

    public IUser getUserByName(String account);   //loadUserByUsername(String username)  
    
    public IPasswordEncoder getPasswordEncoder();
    
}
