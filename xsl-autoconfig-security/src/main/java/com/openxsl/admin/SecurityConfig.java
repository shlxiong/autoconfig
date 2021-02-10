package com.openxsl.admin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity.IgnoredRequestConfigurer;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.openxsl.admin.api.IAuthenticate;
import com.openxsl.admin.security.AccessDecisionFilter;
import com.openxsl.admin.security.config.ExSecurityProperties;
import com.openxsl.admin.security.session.SessionControlStrategy;

@Configuration
//@EnableWebSecurity
@ImportResource("classpath:spring/spring-security.xml")
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	private static List<String> DEFAULT_IGNORED = Arrays.asList(
				"/css/**", "/js/**", "/images/**", "/webjars/**", "/**/favicon.ico");
	@Autowired
	private ExSecurityProperties properties;   // security.*
	@Autowired
	private IAuthenticate authenticate;
	@Autowired
	private SessionControlStrategy sessionStrategy;
	@Autowired
	private AuthenticationSuccessHandler loginSuccessHandler;
	@Autowired
	private AccessDecisionFilter accessFilter = new AccessDecisionFilter();
	
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","OPTIONS"));
		
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
	
	/**
	 * AbstractConfiguredSecurityBuilder.build() -> init()
	 * getHttp();
	 *    ->authenticationManager()
	 *    ->config(http)
	 */
	@Override
	public void init(WebSecurity builder) throws Exception {
		super.init(builder);
		
		IgnoredRequestConfigurer configurer = builder.ignoring();
		List<RequestMatcher> matchers = new ArrayList<RequestMatcher>();
		for (String pattern : DEFAULT_IGNORED) {
			matchers.add(new AntPathRequestMatcher(pattern, null));
		}
		for (String pattern : properties.getIgnored()) {
			matchers.add(new AntPathRequestMatcher(pattern, null));
		}
		matchers.add(new AntPathRequestMatcher(properties.getAuthenLoginUrl(), null));
		
		configurer.requestMatchers(new OrRequestMatcher(matchers));
		//builder.httpFirewall(httpFirewall)
	}
	
	//<authentication-manager>
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	    auth.userDetailsService(authenticate).passwordEncoder(authenticate.getPasswordEncoder())
	    	.and().eraseCredentials(false);
	}
	
	// <http ...>
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String loginUrl = properties.getAuthenLoginUrl();
		http.authorizeRequests()
			.antMatchers("/user/**").hasAnyRole("admin")
			.antMatchers("/**").authenticated();
		http.logout().logoutUrl("/logout").logoutSuccessUrl(loginUrl)
			.and().formLogin()
				.loginPage(loginUrl).loginProcessingUrl("/j_security_check")
				.defaultSuccessUrl(properties.getAuthenSuccessUrl())
				.successHandler(loginSuccessHandler)
//			.failureHandler(failureHandler)
//			.and().rememberMe().rememberMeCookieName("sky9c_security_me").tokenValiditySeconds(7*24*3600)
			.and().csrf().disable().cors()
			.and().sessionManagement().sessionAuthenticationStrategy(sessionStrategy);
//		http.exceptionHandling().accessDeniedPage(accessDeniedUrl)
		if (!properties.isEnableCsrf()) {
			http.csrf().disable();
		}
		http.headers().disable();
		http.httpBasic().disable();
		http.addFilterBefore(accessFilter, FilterSecurityInterceptor.class);
	}
	
}
