package com.netease.arctic;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class readTest {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(8);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.enableUnalignedCheckpoints();
        checkpointConfig.setCheckpointInterval(60000L);
        checkpointConfig.setCheckpointTimeout(1800000L);
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Configuration configuration = tableEnv.getConfig().getConfiguration();
        configuration.setString("table.dynamic-table-options.enabled", "true");
        configuration.setString("execution.checkpointing.unaligned.forced", "true");
        tableEnv.executeSql("Create catalog c WITH ('type'='arctic', " +
            "'metastore.url'='thrift://10.196.98.23:18112/trino_online_env')");
        String s1 = "create table if not exists user_dim  (\n" +
            "optime TIMESTAMP(3),\n" +
            "WATERMARK FOR optime AS optime \n" +
            ") LIKE c.upsertSpeedTest.hugeTestTable\n";
        tableEnv.executeSql(s1);
        String s2 = "SELECT c_w_id, c_id, c_credit \n" +
            "FROM user_dim";
        tableEnv.sqlQuery(s2).execute().print();
    }
}
