package com.netease.arctic;

import com.netease.arctic.flink.InternalCatalogBuilder;
import com.netease.arctic.flink.table.ArcticTableLoader;
import com.netease.arctic.flink.table.FlinkSource;
import com.netease.arctic.table.TableIdentifier;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.datagen.DataGeneratorSource;
import org.apache.flink.table.api.Schema;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.data.RowData;

import java.util.HashMap;
import java.util.Map;

public class MainTest {
    public static void main(String[] args) throws Exception {
        // 创建env对象
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tEnv = StreamTableEnvironment.create(env);
        // 创建DataGeneratorSource。传入上面自定义的数据生成器
        DataGeneratorSource<Orders> orderDataDataGeneratorSource
            = new DataGeneratorSource<>(new Orders.OrderDataGenerator());

        InternalCatalogBuilder catalogBuilder =
            InternalCatalogBuilder
                .builder()
                .metastoreUrl("thrift://10.196.98.23:18112/trino_online_env");

        TableIdentifier tableId = TableIdentifier.of("trino_online_env", "upsertSpeedTest", "hugeTestTable");
        ArcticTableLoader tableLoader = ArcticTableLoader.of(tableId, catalogBuilder);

        Map<String, String> properties = new HashMap<>();
        // 默认为 true
        properties.put("streaming", "true");

        DataStream<RowData> dimStream = FlinkSource.forRowData()
            .env(env)
            .tableLoader(tableLoader)
            // 主键表暂时只支持读当前全量及之后的 CDC 数据，可无需 properties 参数
            .properties(properties)
            .build();

        // 提交并执行任务
        env.execute("Test Arctic Stream Read");


        Table rateTable = tEnv.fromDataStream(dimStream, Schema.newBuilder().build());
        tEnv.registerTable("RatesHistory", rateTable);
        rateTable.printSchema();
        tEnv.from("RatesHistory").execute().print();


        // 添加source
        env.addSource(orderDataDataGeneratorSource)
            // 指定返回类型
            .returns(new TypeHint<Orders>() {
            })
            // 输出
            .print();
        env.execute();
    }
}
