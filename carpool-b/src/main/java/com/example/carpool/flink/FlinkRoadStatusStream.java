package com.example.carpool.flink;

import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.RowTypeInfo;
import org.apache.flink.connector.jdbc.JdbcInputFormat;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public class FlinkRoadStatusStream {
    public static void main(String[] args) throws Exception {
        // 读取 application.properties
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("src/main/resources/application.properties")) {
            props.load(input);
        }
        String url = props.getProperty("spring.datasource.url");
        String user = props.getProperty("spring.datasource.username");
        String password = props.getProperty("spring.datasource.password");
        String driver = props.getProperty("spring.datasource.driver-class-name");

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String query = "SELECT road_name, city, evaluation_status FROM road_traffic_overall WHERE create_time >= NOW() - INTERVAL 10 MINUTE";
        JdbcInputFormat jdbcInput = JdbcInputFormat.buildJdbcInputFormat()
                .setDrivername(driver)
                .setDBUrl(url)
                .setUsername(user)
                .setPassword(password)
                .setQuery(query)
                .setRowTypeInfo(new RowTypeInfo(
                        Types.STRING, // road_name
                        Types.STRING, // city
                        Types.INT     // evaluation_status
                ))
                .finish();

        DataStream<Row> roadStatusStream = env.createInput(jdbcInput);
        roadStatusStream.addSink((SinkFunction<Row>) new PrintSinkFunction<>());
        env.execute("Flink Road Status Stream");
    }
}
