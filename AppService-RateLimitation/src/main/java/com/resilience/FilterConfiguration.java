package com.resilience;

import org.eclipse.jetty.servlets.DoSFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfiguration {

	@Bean
	public FilterRegistrationBean DoSFilterBean() {
		final FilterRegistrationBean filterRegBean = new FilterRegistrationBean();
		DoSFilter filter = new DoSFilter();
		filterRegBean.setFilter(filter);
		filterRegBean.addUrlPatterns("/*");
		filterRegBean.setEnabled(true);
		filterRegBean.setAsyncSupported(Boolean.TRUE);
		filterRegBean.addInitParameter("maxRequestsPerSec", "10");
		return filterRegBean;
	}
}
