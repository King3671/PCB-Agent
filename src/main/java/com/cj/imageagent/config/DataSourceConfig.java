package com.cj.imageagent.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;


@Configuration
public class DataSourceConfig {

    // ==================== MySQL 数据源 Bean 名称：mysqlDataSource ====================
    @Bean("mysqlDataSource")
    @Primary
    public DataSource mysqlDataSource(
            @Value("${mysql.datasource.url}") String url,
            @Value("${mysql.datasource.username}") String username,
            @Value("${mysql.datasource.password}") String password,
            @Value("${mysql.datasource.driver-class-name}") String driver
    ) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driver);
        return ds;
    }

    // ==================== PostgreSQL 数据源 Bean 名称：pgDataSource ====================
    @Bean("pgDataSource")
    public DataSource pgDataSource(
            @Value("${pg.datasource.url}") String url,
            @Value("${pg.datasource.username}") String username,
            @Value("${pg.datasource.password}") String password,
            @Value("${pg.datasource.driver-class-name}") String driverClassName
    ) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(url);
        ds.setUsername(username);
        ds.setPassword(password);
        ds.setDriverClassName(driverClassName);
        return ds;
    }

    // MySQL JdbcTemplate
    @Bean("mysqlJdbcTemplate")
    @Primary
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // PostgreSQL JdbcTemplate
    @Bean("pgJdbcTemplate")
    public JdbcTemplate pgJdbcTemplate(@Qualifier("pgDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
