package com.cncustompoc.SingletonSrvcs.configure;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.cncustompoc.SingletonSrvcs.Common.CommonParams;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;

import javax.annotation.PostConstruct;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@EnableTransactionManagement
public class MyBatisConfiguration implements TransactionManagementConfigurer {
	protected static Logger logger = Logger.getLogger(MyBatisConfiguration.class.getName());

	@Autowired
	DBProps dBprops;
	@Autowired
	DruidDataSource dataSource;
	
	@Autowired
	StatFilter sf;

	@Bean(name = "dataSource",initMethod = "init", destroyMethod = "close")
	public DruidDataSource dataSource() {
		if (dataSource == null) {
			logger.info("dataSource() invoked");
			try {
				dataSource = new DruidDataSource();
				dataSource.setFilters(dBprops.getFilters());
				dataSource.setInitialSize(dBprops.getInitialSize());
				dataSource.setMaxActive(dBprops.getMaxActive());
				dataSource.setMinIdle(dBprops.getMinIdle());
				dataSource.setMaxWait(dBprops.getMaxWait());
				dataSource.setTimeBetweenEvictionRunsMillis(dBprops.getTimeBetweenEvictionRunsMillis());
				dataSource.setMinEvictableIdleTimeMillis(dBprops.getMinEvictableIdleTimeMillis());
				dataSource.setValidationQuery(dBprops.getValidationQuery());
				dataSource.setTestWhileIdle(dBprops.isTestWhileIdle());
				dataSource.setTestOnBorrow(dBprops.isTestOnBorrow());
				dataSource.setTestOnReturn(dBprops.isTestOnReturn());
				dataSource.setPoolPreparedStatements(dBprops.isPoolPreparedStatements());
				dataSource.setMaxPoolPreparedStatementPerConnectionSize(dBprops.getMaxPoolPreparedStatementPerConnectionSize());
				dataSource.setRemoveAbandoned(dBprops.isRemoveAbandoned());
				dataSource.setRemoveAbandonedTimeout(dBprops.getRemoveAbandonedTimeout());
				dataSource.setLogAbandoned(dBprops.isLogAbandoned());
				dataSource.setUrl(dBprops.getUrl());
				dataSource.setUsername(dBprops.getUsername());
				dataSource.setPassword(dBprops.getPassword());
				dataSource.setDriverClassName(dBprops.getDriverClassName());
						
				dataSource.init();
			} catch (Exception e) {
				logger.log(Level.WARNING, e.getMessage(), e);
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			logger.info("dataSource() initaled");
		}
		return dataSource;
	}
	
	@Bean(name="statfilter")
	public StatFilter statFilter(){
		sf=new StatFilter();
		sf.setLogSlowSql(dBprops.isLogSlowSql());
		sf.setMergeSql(dBprops.isMergeSql());
		sf.setSlowSqlMillis(dBprops.getSlowSqlMillis());
		logger.info("StatFilter() initaled");
		return sf;
	}

	@PostConstruct
	public void checkConfigFileExists() {
		if (dBprops == null) {
			throw new RuntimeException(
					"Cannot find config (please add config file or check your Mybatis configuration)");
		}
	}

	@Bean(name = "sqlSessionFactory")
	public SqlSessionFactory sqlSessionFactoryBean() {
		logger.info("inital SqlSessionFactory");
		SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
		bean.setDataSource(dataSource);
		bean.setTypeAliasesPackage(CommonParams.MODELPACKAGEPATH);

		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		try {
			//bean.setMapperLocations(resolver.getResources("classpath:" + CommonParams.BASEPACKAGEPATH + File.separator + "*.xml"));
			logger.info("SqlSessionFactory Created");
			return bean.getObject();
		} catch (Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			throw new RuntimeException(e);
		}

	}

	@Bean
	public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
		return new SqlSessionTemplate(sqlSessionFactory);
	}

	@Bean
	@Override
	public PlatformTransactionManager annotationDrivenTransactionManager() {
		return new DataSourceTransactionManager(dataSource);
	}
//	@Bean
//    public DataSourceTransactionManager transactionManager() {
//        return new DataSourceTransactionManager(dataSource);
//	}	
}