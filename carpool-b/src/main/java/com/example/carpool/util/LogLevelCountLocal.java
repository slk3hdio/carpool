package com.example.carpool.util;

import java.io.*;
import java.util.*;
import java.util.regex.*;
// LogLevelCountLocal.java - 日志等级与统计工具类

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogLevelCountLocal {
    private static final Logger logger = LoggerFactory.getLogger(LogLevelCountLocal.class);

    public static void main(String[] args) throws IOException {
                        // 2.7 数据库连接池状态统计
                        int hikariConnectionCreated = 0;
                        int hikariConnectionClosed = 0;
                        int hikariConnectionError = 0;
                        int hikariPoolStart = 0;
                        int hikariPoolShutdown = 0;
                    // 2.6 启动耗时与关键阶段耗时统计
                    double totalStartupSeconds = -1;
                    Map<String, String> phaseCost = new LinkedHashMap<>();
                // 2.5 预设常见异常类型
                List<String> presetExceptionTypes = Arrays.asList(
                    "NullPointerException",
                    "IllegalArgumentException",
                    "IllegalStateException",
                    "IOException",
                    "SQLException",
                    "RuntimeException",
                    "ClassNotFoundException",
                    "NoSuchMethodException",
                    "IndexOutOfBoundsException",
                    "ArrayIndexOutOfBoundsException",
                    "NumberFormatException",
                    "FileNotFoundException",
                    "TimeoutException",
                    "ConnectException",
                    "SocketException",
                    "BindException",
                    "ArithmeticException",
                    "StackOverflowError",
                    "OutOfMemoryError"
                );
        // 1. 日志文件路径
        String logFile = "app.log";
        String outFile = "log_level_count.txt";
        String qpsStateFile = "log_level_count_state.dat";


        // 2. 日志等级统计Map
        Map<String, Integer> levelCount = new HashMap<>();
        List<String> levels = Arrays.asList("INFO", "WARN", "ERROR", "DEBUG", "TRACE");
        for (String level : levels) levelCount.put(level, 0);

        // 2.1 Source统计Map（以日志等级为Source）
        Map<String, Integer> sourceCount = new HashMap<>();
        for (String level : levels) sourceCount.put(level, 0);



        // 2.3 统计包名（模块）日志数量
        Map<String, Integer> packageCount = new HashMap<>();

        // 2.4 统计异常类型及堆栈摘要
        Map<String, Integer> exceptionTypeCount = new HashMap<>();
        Map<String, String> exceptionStackSummary = new HashMap<>();

        // 2.2 统计本次处理的日志总字节数
        long totalBytes = 0;


        // 3. 日志等级正则
        Pattern pattern = Pattern.compile("\\s(INFO|WARN|ERROR|DEBUG|TRACE)\\s");
        // 3.1 不再需要Source正则，直接用level


        // 4. 读取日志并统计
        // 包名正则，匹配冒号前的包名字段
        Pattern pkgPattern = Pattern.compile("([a-zA-Z0-9_.]+)\\s+:");
        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            String lastExceptionType = null;
            StringBuilder lastStack = new StringBuilder();
            // 启动阶段正则
            Pattern startupPattern = Pattern.compile("Started .* in ([0-9.]+) seconds");
            Pattern phasePattern = Pattern.compile("initialization completed in ([0-9]+) ms");
            while ((line = br.readLine()) != null) {
                // 统计HikariCP连接池相关日志
                if (line.contains("HikariPool") || line.contains("HikariDataSource")) {
                    if (line.contains("Added connection")) hikariConnectionCreated++;
                    if (line.contains("Closed connection")) hikariConnectionClosed++;
                    if (line.toLowerCase().contains("error") || line.toLowerCase().contains("fail")) hikariConnectionError++;
                    if (line.contains("Start completed")) hikariPoolStart++;
                    if (line.contains("Shutdown initiated")) hikariPoolShutdown++;
                }
                                // 统计启动总耗时
                                Matcher mStartup = startupPattern.matcher(line);
                                if (mStartup.find()) {
                                    totalStartupSeconds = Double.parseDouble(mStartup.group(1));
                                }
                                // 统计关键阶段耗时
                                Matcher mPhase = phasePattern.matcher(line);
                                if (mPhase.find()) {
                                    phaseCost.put("Root WebApplicationContext initialization", mPhase.group(1) + " ms");
                                }
                totalBytes += line.getBytes().length + System.lineSeparator().getBytes().length;
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String level = matcher.group(1);
                    levelCount.put(level, levelCount.get(level) + 1);
                    // 统计QPS时以level为source
                    sourceCount.put(level, sourceCount.get(level) + 1);
                }
                // 统计包名
                Matcher pkgMatcher = pkgPattern.matcher(line);
                if (pkgMatcher.find()) {
                    String pkg = pkgMatcher.group(1);
                    packageCount.put(pkg, packageCount.getOrDefault(pkg, 0) + 1);
                }
                // 统计异常类型和堆栈摘要
                // 匹配异常行，如 java.lang.NullPointerException: ...
                if (line.matches(".*([a-zA-Z0-9_.]+Exception|[a-zA-Z0-9_.]+Error)(:|$).*")) {
                    String type = line.replaceAll(".*?([a-zA-Z0-9_.]+Exception|[a-zA-Z0-9_.]+Error)(:|$).*", "$1");
                    exceptionTypeCount.put(type, exceptionTypeCount.getOrDefault(type, 0) + 1);
                    // 记录堆栈摘要（只保留首次出现的前5行）
                    if (!exceptionStackSummary.containsKey(type)) {
                        lastExceptionType = type;
                        lastStack.setLength(0);
                        lastStack.append(line).append("\n");
                    } else {
                        lastExceptionType = null;
                    }
                } else if (lastExceptionType != null && (line.startsWith("\tat ") || line.trim().isEmpty())) {
                    // 堆栈跟踪行
                    if (lastStack.toString().split("\\n").length < 5) {
                        lastStack.append(line).append("\n");
                    }
                } else if (lastExceptionType != null) {
                    // 堆栈结束
                    exceptionStackSummary.put(lastExceptionType, lastStack.toString());
                    lastExceptionType = null;
                }
            }
            // 文件结尾时补充最后一个异常
            if (lastExceptionType != null && !exceptionStackSummary.containsKey(lastExceptionType)) {
                exceptionStackSummary.put(lastExceptionType, lastStack.toString());
            }
        }

        // 5. 输出到文件（覆盖原有内容）
        try (PrintWriter pw = new PrintWriter(new FileWriter(outFile))) {
            pw.println("--- 日志等级统计Map ---");
            for (String level : levels) {
                pw.println(level + ": " + levelCount.get(level));
            }
        }

        // 6. 计算QPS和数据流量并追加到文件
        // 读取上一次Source计数和字节数
        Map<String, Integer> lastSourceCount = new HashMap<>();
        long lastTotalBytes = 0;
        if (new File(qpsStateFile).exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(qpsStateFile))) {
                Object obj = ois.readObject();
                if (obj instanceof Map) {
                    lastSourceCount = (Map<String, Integer>) obj;
                }
                // 兼容老文件，读取字节数
                if (ois.available() > 0) {
                    lastTotalBytes = ois.readLong();
                }
            } catch (Exception e) {
                logger.warn("读取QPS状态文件失败: {}", e.getMessage());
            }
        }

        // 计算QPS
        Map<String, Double> qpsMap = new HashMap<>();
        for (String level : levels) {
            int now = sourceCount.get(level);
            int last = lastSourceCount.getOrDefault(level, 0);
                double qps = (now - last) / 1.0;
            qpsMap.put(level, qps);
        }

        // 计算数据流量（B/s）
        long bytesDelta = totalBytes - lastTotalBytes;
            double bytesPerSecond = bytesDelta / 1.0;

        // 保存本次Source计数和字节数
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(qpsStateFile))) {
            oos.writeObject(sourceCount);
            oos.writeLong(totalBytes);
        } catch (Exception e) {
            logger.warn("写入QPS状态文件失败: {}", e.getMessage());
        }

        // 追加QPS、数据流量、包名和异常统计到统计文件
        try (PrintWriter pw = new PrintWriter(new FileWriter(outFile, true))) {
            pw.println("\n--- 启动耗时与关键阶段耗时 ---");
            if (totalStartupSeconds >= 0) {
                pw.printf("服务启动总耗时: %.3f 秒\n", totalStartupSeconds);
            } else {
                pw.println("服务启动总耗时: 未检测到");
            }
            for (Map.Entry<String, String> entry : phaseCost.entrySet()) {
                pw.printf("%s: %s\n", entry.getKey(), entry.getValue());
            }

            pw.println("\n--- 数据库连接池（HikariCP）状态统计 ---");
            pw.printf("连接创建次数: %d\n", hikariConnectionCreated);
            pw.printf("连接关闭次数: %d\n", hikariConnectionClosed);
            pw.printf("连接池启动次数: %d\n", hikariPoolStart);
            pw.printf("连接池关闭次数: %d\n", hikariPoolShutdown);
            pw.printf("连接相关异常/错误: %d\n", hikariConnectionError);

            pw.println("\n--- QPS (每个Source) ---");
            for (String level : levels) {
                pw.printf("%s: %.2f\n", level, qpsMap.get(level));
            }
            pw.printf("数据流量（B/s）：%.2f\n", bytesPerSecond);
            pw.println("\n--- 各包名日志数量 ---");
            for (Map.Entry<String, Integer> entry : packageCount.entrySet()) {
                pw.printf("%s: %d\n", entry.getKey(), entry.getValue());
            }
            pw.println("\n--- 异常类型统计 ---");
            // 先输出预设类型
            for (String type : presetExceptionTypes) {
                int count = exceptionTypeCount.getOrDefault(type, 0);
                pw.printf("%s: %d\n", type, count);
            }
            // 再输出日志中出现但不在预设列表的类型
            for (String type : exceptionTypeCount.keySet()) {
                if (!presetExceptionTypes.contains(type)) {
                    pw.printf("%s: %d\n", type, exceptionTypeCount.get(type));
                }
            }
            pw.println("\n--- 异常堆栈摘要（每种类型首例） ---");
            for (Map.Entry<String, String> entry : exceptionStackSummary.entrySet()) {
                pw.printf("%s:\n%s\n", entry.getKey(), entry.getValue());
            }
        }
    }
}
