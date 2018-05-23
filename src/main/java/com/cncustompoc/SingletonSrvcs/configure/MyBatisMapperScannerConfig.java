package com.cncustompoc.SingletonSrvcs.configure;


import com.cncustompoc.SingletonSrvcs.Common.CommonParams;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

import org.mybatis.spring.mapper.MapperScannerConfigurer;

@Configuration
@AutoConfigureAfter(MyBatisConfiguration.class)
public class MyBatisMapperScannerConfig {
    private Logger logger = Logger.getLogger(MyBatisMapperScannerConfig.class.getName());
    @Bean
    public MapperScannerConfigurer mapperScannerConfigurer() {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperScannerConfigurer.setBasePackage(CommonParams.DAOPACKAGEPATH);
        logger.info("mapperScannerConfigurer created");
        return mapperScannerConfigurer;
    }
}