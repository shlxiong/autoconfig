package com.openxsl.admin.context;

import java.util.Date;

import org.springframework.security.core.session.SessionInformation;

import com.openxsl.admin.entity.User;
import com.openxsl.admin.entity.UserDetail;
import com.openxsl.admin.organ.entity.Staff;

@SuppressWarnings("serial")
public class UserSession extends SessionInformation implements java.io.Serializable {
    private Date loginTime;
    private String hostIp;
	private String operSys;
	private String browser;
	private User user;
    private UserDetail userDetail;
    private Staff staff;
    
    public UserSession() {
		super(new Object(), "temp-init", new Date());
	}
    
    public UserSession(Object principal, String sessionId, Date lastRequest) {
		super(principal, sessionId, lastRequest);
		if (principal instanceof User) {
			this.setUser((User)principal);
		}
	}

//    private static String CACHE_KEY = "if.applog-user.userid_{}";
//    public static String getCacheKey(String userId) {
//        if (userId == null) {
//            return CACHE_KEY.substring(0, CACHE_KEY.length() - 3);
//        }
//        return CACHE_KEY.replace("{}", userId);
//    }
    
//    public String getSessionId() {
//        return sessionId;
//    }
//    public void setSessionId(String sessionId) {
//        this.sessionId = sessionId;
//    }
    public Date getLoginTime() {
        return loginTime;
    }
    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getHostIp() {
		return hostIp;
	}

	public void setHostIp(String hostIp) {
		this.hostIp = hostIp;
	}

	public String getOperSys() {
		return operSys;
	}

	public void setOperSys(String operSys) {
		this.operSys = operSys;
	}

	public String getBrowser() {
		return browser;
	}

	public void setBrowser(String browser) {
		this.browser = browser;
	}

	public UserDetail getUserDetail() {
		return userDetail;
	}

	public void setUserDetail(UserDetail userDetail) {
		this.userDetail = userDetail;
	}

	public Staff getStaff() {
		return staff;
	}

	public void setStaff(Staff staff) {
		this.staff = staff;
	}

}
