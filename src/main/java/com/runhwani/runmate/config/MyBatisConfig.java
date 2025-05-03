// src/main/java/com/runhwani/runmate/config/MyBatisConfig.java
package com.runhwani.runmate.config;

import com.runhwani.runmate.mybatis.MybatisLoggingInterceptor;
import com.runhwani.runmate.mybatis.UUIDTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandler;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MyBatisConfig {

    @Bean
    public ConfigurationCustomizer mybatisCustomizer(MybatisLoggingInterceptor interceptor) {
        return configuration -> configuration.addInterceptor(interceptor);
    }

    @Bean
    public MybatisLoggingInterceptor mybatisLoggingInterceptor() {
        return new MybatisLoggingInterceptor();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);

        // Mapper XML 위치 설정
        sessionFactory.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources("classpath:mappers/*.xml")
        );

        // TypeHandler 등록
        sessionFactory.setTypeHandlers(new TypeHandler[]{
                new UUIDTypeHandler()
        });


        // 모델 클래스의 패키지 위치 설정
        sessionFactory.setTypeAliasesPackage("com.runhwani.runmate.model");

        return sessionFactory.getObject();
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

}
