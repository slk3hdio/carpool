package com.example.carpool;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Component
public class LogLevelCountRunner implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(LogLevelCountRunner.class);
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
         new Thread(() -> {
            // 每隔5秒钟执行一次日志统计
            try {
                while (true) {
                    Thread.sleep(5000);
                    // System.out.println("===================================");
                    // System.out.println("开始统计日志等级...");
                    // logger.info("开始统计日志等级...");
                    // System.out.println("===================================");
                    try{
                        com.example.carpool.util.LogLevelCountLocal.main(null);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    
        System.out.println("hello world.");
    }
}