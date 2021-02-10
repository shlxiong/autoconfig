package com.openxsl.admin.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

public class LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
//	private String targetUrlParameter = null;
//	private String defaultTargetUrl = "/";
//	private boolean alwaysUseDefaultTargetUrl = false;
//	private boolean useReferer = false;
	
	private boolean redirect = true;
//	private RequestCache requestCache = new HttpSessionRequestCache();
//	
//	@Override   //SavedRequestAwareAuthenticationSuccessHandler
//	public void onAuthenticationSuccess(HttpServletRequest request,
//						HttpServletResponse response, Authentication authentication)
//				throws IOException, ServletException {
//		SavedRequest savedRequest = requestCache.getRequest(request, response);
//		if (savedRequest == null) {
//			super.onAuthenticationSuccess(request, response, authentication);
//			return;
//		}
//		String targetUrlParameter = getTargetUrlParameter();
//		if (isAlwaysUseDefaultTargetUrl()
//				|| (targetUrlParameter != null && StringUtils.hasText(request
//						.getParameter(targetUrlParameter)))) {
//			requestCache.removeRequest(request, response);
//			super.onAuthenticationSuccess(request, response, authentication);
//			return;
//		}
//
//		clearAuthenticationAttributes(request);  //session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
//
//		// Use the DefaultSavedRequest URL
//		String targetUrl = savedRequest.getRedirectUrl();
//		logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
//		if (redirect) {
//			response.sendRedirect(targetUrl);
//		} else {
//			request.getRequestDispatcher(targetUrl).forward(request, response);
//		}
//	}
	
	protected void handle(HttpServletRequest request, HttpServletResponse response,
						Authentication authentication) throws IOException, ServletException {
		String targetUrl = determineTargetUrl(request, response);

		if (response.isCommitted()) {
			logger.info("Response has already been committed. Unable to redirect to " + targetUrl);
			return;
		}
		
		if (redirect) {
			response.sendRedirect(targetUrl);
		} else {
			request.getRequestDispatcher(targetUrl).forward(request, response);
		}
	}
	
	public void setRedirect(boolean redirect) {
		this.redirect = redirect;
	}

}
