package com.example.aiquiz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@Service
public class DatabaseInitService {
    
    private static final Logger log = LoggerFactory.getLogger(DatabaseInitService.class);
    
    @Autowired
    private DataSource dataSource;
    
    @EventListener(ApplicationReadyEvent.class)
    public void initDatabase() {
        try {
            log.info("开始初始化数据库...");
            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(new ClassPathResource("schema.sql"));
            populator.execute(dataSource);
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
        }
    }
} 