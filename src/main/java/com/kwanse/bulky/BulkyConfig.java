package com.kwanse.bulky_dummy;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@AutoConfiguration
@ConditionalOnProperty(name = "bulky", havingValue = "on")
public class BulkyConfig {

    @Bean
    public BulkyTemplate bulkyTemplate(JdbcTemplate jdbcTemplate) {
        return new BulkyTemplate(jdbcTemplate);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
