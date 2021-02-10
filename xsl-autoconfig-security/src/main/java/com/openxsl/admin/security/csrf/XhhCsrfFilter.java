package com.openxsl.admin.security.csrf;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 由于spring.CsrfFilter标签disabled不能使用properties属性，故自定义一个CsrfFilter增加disabled属性
 * 
 * @author 001327
 */
public class XhhCsrfFilter extends OncePerRequestFilter {
	private final AccessDeniedHandler accessDeniedHandler = new AccessDeniedHandlerImpl();
	@Autowired
	private CsrfTokenRepository tokenRepository;
	@Autowired
	private RequestMatcher csrfMatcher;
	private boolean enabled = false;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
						HttpServletResponse response, FilterChain filterChain)
				throws ServletException, IOException {
		if (enabled){
			CsrfToken csrfToken = tokenRepository.loadToken(request);
			boolean missingToken = (csrfToken == null);
			if (missingToken) {
				CsrfToken generatedToken = tokenRepository.generateToken(request);
				csrfToken = new SaveOnAccessCsrfToken(tokenRepository, request, response,
						generatedToken);
			}
//			request.setAttribute(CsrfToken.class.getName(), csrfToken);
			request.setAttribute(csrfToken.getParameterName(), csrfToken);
	
			if (!csrfMatcher.matches(request)) {
				filterChain.doFilter(request, response);
				return;
			}
	
			String actualToken = request.getHeader(csrfToken.getHeaderName());
			if (actualToken == null) {
				actualToken = request.getParameter(csrfToken.getParameterName());
			}
			if (!csrfToken.getToken().equals(actualToken)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Invalid CSRF token found for "
							+ UrlUtils.buildFullRequestUrl(request));
				}
				if (missingToken) {
					logger.error("missing csrf-token!!");
					accessDeniedHandler.handle(request, response,
							new MissingCsrfTokenException(actualToken));
				} else {
					logger.error("csrf-token not matches!!");
					accessDeniedHandler.handle(request, response,
							new InvalidCsrfTokenException(csrfToken, actualToken));
				}
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
	
	public void setRequestMatcher(RequestMatcher csrfMatcher){
		this.csrfMatcher = csrfMatcher;
	}
	public void setTokenRepository(CsrfTokenRepository tokenRepository){
		this.tokenRepository = tokenRepository;
	}
	public void setEnabled(boolean flag){
		this.enabled = flag;
	}

	@SuppressWarnings("serial")
	private final class SaveOnAccessCsrfToken implements CsrfToken {
		private transient CsrfTokenRepository tokenRepository;
		private transient HttpServletRequest request;
		private transient HttpServletResponse response;

		private final CsrfToken delegate;

		public SaveOnAccessCsrfToken(CsrfTokenRepository tokenRepository,
						HttpServletRequest request, HttpServletResponse response,
						CsrfToken delegate) {
			this.tokenRepository = tokenRepository;
			this.request = request;
			this.response = response;
			this.delegate = delegate;
		}

		public String getHeaderName() {
			return delegate.getHeaderName();
		}

		public String getParameterName() {
			return delegate.getParameterName();
		}

		public String getToken() {
			saveTokenIfNecessary();
			return delegate.getToken();
		}

		@Override
		public String toString() {
			return "SaveOnAccessCsrfToken [delegate=" + delegate + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SaveOnAccessCsrfToken other = (SaveOnAccessCsrfToken) obj;
			if (delegate == null) {
				if (other.delegate != null)
					return false;
			}
			else if (!delegate.equals(other.delegate))
				return false;
			return true;
		}

		private void saveTokenIfNecessary() {
			if (tokenRepository == null) {
				return;
			}

			synchronized (this) {
				if (tokenRepository != null) {
					tokenRepository.saveToken(delegate, request, response);
					tokenRepository = null;
					this.request = null;
					this.response = null;
				}
			}
		}

	}
}
