package com.openxsl.admin.context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openxsl.config.util.StringUtils;

/**
 * 本地（线程）UserHolder
 * 
 * @author shuilin.xiong
 */
public class LocalUserHolder {
	/** User对象标识 */
	public static final String CONTEXT_ONLINE_USER = "UserSession";
	public static final String CONTEXT_REQUEST_IGNORE_AUTH = "request_ignore_auth";
	
	private static ThreadLocal<Map<String, Object>> context = new ThreadLocal<Map<String, Object>>();
	private static final LocalUserHolder instance = new LocalUserHolder();
	
	LocalUserHolder() {  //private
	}
	
	/**
	 * 将在线用户对象设置到当前线程中
	 * @param user
	 */
	static void setUser_(UserSession user) {
		setAttr(CONTEXT_ONLINE_USER, user);
	}

	/**
	 * 清除线程数据
	 */
	public static void clear() {
		Map<String, Object> map = context.get();
		if (map != null) {
			map.clear();
		}
		context.remove();
	}
	
	/**
	 * 获取当前线程中的用户对象
	 * @return
	 */
	public static UserSession getUser() {
		return instance.getUser_();
	}
	protected UserSession getUser_() {
		return getAttr(CONTEXT_ONLINE_USER);
	}

	/**
	 * 获取当前用户标识
	 * @return 用户标识
	 */
	public static String getUserId() {
		try {
			return instance.getUser_().getUser().getUserId();
		} catch (NullPointerException npe) {
			return null;
		}
	}

	/**
	 * 获取当前用户登录名
	 * @return 用户登录名
	 */
	public static String getUserName() {
		try {
			return instance.getUser_().getStaff().getName();
		} catch (NullPointerException npe) {
			try {
				return instance.getUser_().getUserDetail().getRealName();
			} catch (NullPointerException npe1) {
				try {
					return instance.getUser_().getUser().getUsername();
				} catch (NullPointerException e) {
					return null;
				}
			}
		}
	}
	
	public static String getUserLogo() {
		String logo = null;
		try {
			logo = instance.getUser_().getStaff().getLogo();
		} catch (NullPointerException npe) {
		} finally {
			if (StringUtils.isEmpty(logo)) {
				logo = instance.getUser_().getUserDetail().getLogo();
			}
		}
		return logo;
	}

	/**
	 * 获取企业编码
	 * @return 企业编码
	 */
	public static String getCorpCode() {
		try {
			return instance.getUser_().getStaff().getCorpCode();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	public static String getAreaCode() {
		try {
			return instance.getUser_().getStaff().getAreaCode();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	public static List<String> getSubCorpCodes() {
		try {
			return instance.getUser_().getStaff().getSubCorpCodes();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	public static List<String> getDeptNames() {
		try {
			return instance.getUser_().getStaff().getDeptNames();
		} catch (NullPointerException npe) {
			return null;
		}
	}
	
	/**
	 * 设置当前请求是否可以忽略权限认证
	 */
	public static void setRequestIgnoreAuth() {
		setAttr(CONTEXT_REQUEST_IGNORE_AUTH, Boolean.TRUE);
	}

	/**
	 * 返回当前请求是否可以忽略权限认证
	 */
	public static boolean isRequestIgnoreAuth() {
		Boolean bool = getAttr(CONTEXT_REQUEST_IGNORE_AUTH);
		return bool == null ? false : bool.booleanValue();
	}

	/**
	 * 设置数据
	 * @param key 属性键
	 * @param value 属性值
	 */
	private static <T> void setAttr(String key, T value) {
		Map<String, Object> map = context.get();
		if (map == null) {
			map = new HashMap<String, Object>();
			context.set(map);
		}
		map.put(key, value);
	}

	/**
	 * 根据KEY返回数据
	 * @param key
	 */
	@SuppressWarnings("unchecked")
	private static <T> T getAttr(String key) {
		Map<String, Object> map = context.get();
		if (map != null) {
			return (T) map.get(key);
		}
		return null;
	}
}
