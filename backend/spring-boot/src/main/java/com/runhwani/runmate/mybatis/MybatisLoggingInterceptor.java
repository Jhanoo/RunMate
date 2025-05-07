// src/main/java/com/runhwani/runmate/mybatis/MybatisLoggingInterceptor.java
package com.runhwani.runmate.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;

import java.util.Properties;

/**
 * MyBatis Interceptor로 insert/update/delete 수행 시 파라미터와 결과 건수를 로깅
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class MybatisLoggingInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs()[1];

        // SQL ID: 네임스페이스.메서드명
        String sqlId = ms.getId();
        log.info(">> MyBatis SQL 실행: {}", sqlId);
        log.info("   파라미터: {}", parameter);

        Object result = invocation.proceed();

        // result는 변경된 row 수(Integer) 또는 select 결과 List 등
        log.info("<< MyBatis 실행 결과: {}", result);
        return result;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 필요 시 설정값 읽어 처리
    }
}
