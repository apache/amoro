package com.netease.arctic;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class RightTableStreamer {
    public static void main(String[] args) {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        CheckpointConfig checkpointConfig = env.getCheckpointConfig();
        checkpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        checkpointConfig.setCheckpointInterval(60000L);
        checkpointConfig.setCheckpointTimeout(1800000L);

        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);
        Configuration configuration = tableEnv.getConfig().getConfiguration();
        configuration.setString("table.dynamic-table-options.enabled", "true");

        tableEnv.executeSql("Create catalog c WITH ('type'='arctic', " +
            "'metastore.url'='thrift://10.196.98.23:18112/trino_online_env')");

        tableEnv.executeSql("CREATE TABLE skp1 (\n" +
            "    c_w_id         int            NOT NULL,\n" +
            "    c_d_id         int            NOT NULL,\n" +
            "    c_id           int            NOT NULL,\n" +
            "    c_discount     decimal(4, 4)  NOT NULL,\n" +
            "    c_credit       varchar(2)        NOT NULL,\n" +
            "    c_last         varchar(16)    NOT NULL,\n" +
            "    c_first        varchar(16)    NOT NULL,\n" +
            "    c_credit_lim   decimal(12, 2) NOT NULL,\n" +
            "    c_balance      decimal(12, 2) NOT NULL,\n" +
            "    c_ytd_payment  int          NOT NULL,\n" +
            "    c_payment_cnt  int            NOT NULL,\n" +
            "    c_delivery_cnt int            NOT NULL,\n" +
            "    c_street_1     varchar(20)    NOT NULL,\n" +
            "    c_street_2     varchar(20)    NOT NULL,\n" +
            "    c_city         varchar(20)    NOT NULL,\n" +
            "    c_state        varchar(2)        NOT NULL,\n" +
            "    c_zip          varchar(9)        NOT NULL,\n" +
            "    c_phone        varchar(16)       NOT NULL,\n" +
            "    c_since         timestamp        NOT NULL,\n" +
            "    c_middle       varchar(2)        NOT NULL,\n" +
            "    c_data         varchar(500)   NOT NULL)\n" +
            "with \n" +
            "('connector'='datagen'," +
            " 'rows-per-second'='100'," +
            " 'fields.c_w_id.kind'='sequence'," +
            " 'fields.c_w_id.start'='22000000'," +
            " 'fields.c_w_id.end'='2210000')");

        tableEnv.executeSql("insert into c.upsertSpeedTest.hugeTestTable " +
            "select * from skp1");
    }
}
