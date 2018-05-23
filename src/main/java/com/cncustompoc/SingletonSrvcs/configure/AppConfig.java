package com.cncustompoc.SingletonSrvcs.configure;


import com.cncustompoc.SingletonSrvcs.Common.CommonParams;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = CommonParams.BASEPATH)
public class AppConfig extends WebMvcConfigurerAdapter {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/test/**").addResourceLocations("/test/").setCachePeriod(0);
        registry.addResourceHandler("/css/**").addResourceLocations("/css/").setCachePeriod(0);
        registry.addResourceHandler("/img/**").addResourceLocations("/img/").setCachePeriod(0);
        registry.addResourceHandler("/js/**").addResourceLocations("/js/").setCachePeriod(0);
    }


    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        /*
        converters.add(new MappingJackson2HttpMessageConverter());
        super.configureMessageConverters(converters);
        */
    }
}