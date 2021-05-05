package com.openxsl.config.verifycode;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.openxsl.config.util.StringUtils;

@Service
public class VerifyCodeService {
	private final Logger logger = LoggerFactory.getLogger(getClass());
//	private GenericRedisHelper<Long> redisHelper;
//	
//	@Autowired
//	public void setRedisHelper(GenericRedisHelper<Long> redisHelper) {
//		this.redisHelper = redisHelper;
//		this.redisHelper.setEntityClass(Long.class);
//	}
	
	public boolean validate(HttpServletRequest request) {
		String code = request.getParameter("verifycode");
		if (StringUtils.isEmpty(code)) {
			throw new IllegalArgumentException("verify must not be empty");
		}
		long now = System.currentTimeMillis();
//		String hostIp = NetworkUtils.getRequestHost(request);
//		Long realCode = (Long)redisHelper.get(VerifyCodeImage.ATTR_CODE+hostIp); 
//		Long timestamp = (Long)redisHelper.get(VerifyCodeImage.ATTR_GENER_TIME+hostIp);
		HttpSession session = request.getSession();
		Long realCode = (Long)session.getAttribute(VerifyCodeImage.ATTR_CODE);
		Long timestamp = (Long)session.getAttribute(VerifyCodeImage.ATTR_GENER_TIME);
		if (realCode == null || timestamp == null) {
			return false;
		} else {
			if (now - timestamp > 15 * 60000) {
				logger.info("验证码已过期");
				return false;
			}
		}
		
		try {
//			String key = VerifyCodeImage.ATTR_VERIFY_TIME + hostIp;
//			Long verifyTime = (Long)redisHelper.get(key);  
			Long verifyTime = (Long)session.getAttribute(VerifyCodeImage.ATTR_VERIFY_TIME);
			if (verifyTime!=null && now-timestamp < 1000) {
				logger.info("间隔时间太短，疑似暴力攻击");
				return false;
			}
			if (Integer.parseInt(code) == realCode) {
//				redisHelper.delete(key);  
				session.removeAttribute(VerifyCodeImage.ATTR_VERIFY_TIME);
				return true;
			} else {
//				redisHelper.save(key, now);  
				session.setAttribute(VerifyCodeImage.ATTR_VERIFY_TIME, now);
			}
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("verify must not be number");
		}
		return false;
	}
	
	public void saveCode(HttpServletRequest request, Integer validCode) {
//		String hostIp = NetworkUtils.getRequestHost(request);
//		redisHelper.save(VerifyCodeImage.ATTR_CODE+hostIp, validCode);
//		redisHelper.save(VerifyCodeImage.ATTR_GENER_TIME+hostIp, System.currentTimeMillis());
		request.getSession().setAttribute(VerifyCodeImage.ATTR_CODE, validCode);
		request.getSession().setAttribute(VerifyCodeImage.ATTR_GENER_TIME, System.currentTimeMillis());
	}
	
}
