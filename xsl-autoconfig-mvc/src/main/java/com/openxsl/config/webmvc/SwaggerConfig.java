package com.openxsl.config.webmvc;

import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.openxsl.config.condition.ConditionalOnMissingBean;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Value("${swagger.basepackage:com.openxsl}")
	private String basePkg;
	
	@ConditionalOnMissingBean(Docket.class)
	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage(basePkg))
                 //加了ApiOperation注解的类，才生成接口文档
                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                .paths(PathSelectors.any())
                .build();
     }
 
     private ApiInfo apiInfo() {
    	 return new ApiInfoBuilder()
    			 .title("深大景区大屏与综合管控API").version("4.0")
    			 .description("API 描述")
    			 .contact(new Contact("openxsl", "http://haiyang.site", ""))
    			 .build();
     }
}
