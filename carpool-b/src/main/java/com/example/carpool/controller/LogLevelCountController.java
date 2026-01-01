
package com.example.carpool.controller;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@RestController
@RequestMapping("/log")
// @CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://localhost:5176", "http://localhost:8080"})
public class LogLevelCountController {

    private static final Logger logger = LoggerFactory.getLogger(LogLevelCountController.class);

    @GetMapping("/level-count")
    public ResponseEntity<?> getLogLevelCount() {
        // 尝试多个可能的路径
        logger.info("尝试读取日志统计文件...");
        String[] possiblePaths = {
            "log_level_count.txt"
        };
        
        Path foundPath = null;
        for (String pathStr : possiblePaths) {
            Path path = Paths.get(pathStr);
            if (Files.exists(path)) {
                foundPath = path;
                logger.info("找到日志统计文件: {}", path.toAbsolutePath());
                break;
            }
        }
        
        if (foundPath == null) {
            String currentDir = new File(".").getAbsolutePath();
            logger.error("未找到log_level_count.txt，当前工作目录: {}", currentDir);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("log_level_count.txt not found. Current dir: " + currentDir));
        }
        
        try {
            String content = Files.readString(foundPath);
            logger.info("成功读取日志统计文件，内容长度: {}", content.length());
            return ResponseEntity.ok(new LogLevelCountResponse(content));
        } catch (IOException e) {
            logger.error("读取日志统计文件失败", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("读取日志统计文件失败: " + e.getMessage()));
        }
    }

    static class LogLevelCountResponse {
        private String content;
        public LogLevelCountResponse(String content) {
            this.content = content;
        }
        public String getContent() {
            return content;
        }
        public void setContent(String content) {
            this.content = content;
        }
    }

    static class ErrorResponse {
        private String message;
        public ErrorResponse(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
