package com.netease.arctic;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.streaming.api.functions.source.datagen.DataGenerator;

public class Orders {
    int o_w_id;
    int o_d_id;
    int o_id;
    long ts;

    public Orders(int o_w_id, int o_d_id, int o_id, long ts) {
        this.o_w_id = o_w_id;
        this.o_d_id = o_d_id;
        this.o_id = o_id;
        this.ts = ts;
    }

    static class OrderDataGenerator implements DataGenerator<Orders> {

        // 随机数据生成器对象
        RandomDataGenerator generator;

        @Override
        public void open(String name, FunctionInitializationContext context, RuntimeContext runtimeContext) throws Exception {
            // 实例化生成器对象
            generator = new RandomDataGenerator();
        }

        // 是否有下一个
        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public Orders next() {
            // 使用随机生成器生成数据，构造流量对象
            return new Orders(
                generator.nextInt(1, 100),
                generator.nextInt(1, 10),
                generator.nextInt(1, 100),
                System.currentTimeMillis()
            );
        }
    }
}
