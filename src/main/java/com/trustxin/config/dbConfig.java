package com.trustxin.config;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.trustxin.dao.BaseDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class dbConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.druid")
    public DataSource dataSource(){
        return DruidDataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.druid.second")
    @Qualifier("dataSourceSecond")
    public DataSource dataSourceSecond() {
        return DruidDataSourceBuilder.create().build();
    }

    @Primary
    @Bean
    public JdbcTemplate jdbcTemplate(
            DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("jdbcTemplateSecond")
    public JdbcTemplate jdbcTemplateSecond(
            @Qualifier("dataSourceSecond") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Primary
    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(
            DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @Qualifier("namedParameterJdbcTemplateSecond")
    public NamedParameterJdbcTemplate namedParameterJdbcTemplateSecond(
            @Qualifier("dataSourceSecond") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    @Primary
    public BaseDao baseDao(JdbcTemplate jdbcTemplate,
                                NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        return new BaseDao(jdbcTemplate, namedParameterJdbcTemplate);
    }

    @Bean
    @Qualifier("baseDaoSecond")
    public BaseDao baseDaoSecond(@Qualifier("jdbcTemplateSecond") JdbcTemplate jdbcTemplate,
                                 @Qualifier("namedParameterJdbcTemplateSecond") NamedParameterJdbcTemplate namedParameterJdbcTemplateSecond) {
        return new BaseDao(jdbcTemplate, namedParameterJdbcTemplateSecond);
    }


}
