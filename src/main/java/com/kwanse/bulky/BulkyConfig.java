package com.kwanse.bulky;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@AutoConfiguration
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
