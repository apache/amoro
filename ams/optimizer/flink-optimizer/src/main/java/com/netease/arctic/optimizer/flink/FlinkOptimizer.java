package com.netease.arctic.optimizer.flink;

import com.netease.arctic.optimizer.common.Optimizer;
import com.netease.arctic.optimizer.common.OptimizerConfig;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.MemorySize;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.configuration.TaskManagerOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.DiscardingSink;
import org.kohsuke.args4j.CmdLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlinkOptimizer {
  private static final Logger LOG = LoggerFactory.getLogger(FlinkOptimizer.class);

  private static final String JOB_NAME = "arctic-flink-optimizer";

  public static void main(String[] args) throws CmdLineException {
    StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment(new Configuration());
    OptimizerConfig optimizerConfig = new OptimizerConfig(args);

    // calculate optimizer memory allocation
    calcOptimizerMemory(optimizerConfig, env);

    Optimizer optimizer = new Optimizer(optimizerConfig);
    env.addSource(new FlinkToucher(optimizer.getToucher()))
        .setParallelism(1)
        .broadcast()
        .transform(FlinkExecutor.class.getName(), Types.VOID, new FlinkExecutor(optimizer.getExecutors()))
        .setParallelism(optimizerConfig.getExecutionParallel())
        .addSink(new DiscardingSink<>())
        .name("Optimizer empty sink")
        .setParallelism(1);

    try {
      env.execute(JOB_NAME);
    } catch (Exception e) {
      LOG.error("Execute flink optimizer failed", e);
    }
  }

  private static void calcOptimizerMemory(OptimizerConfig config, StreamExecutionEnvironment env) {
    ReadableConfig configuration = env.getConfiguration();
    MemorySize jobMemorySize = configuration.get(JobManagerOptions.TOTAL_PROCESS_MEMORY);
    MemorySize taskMemorySize = configuration.get(TaskManagerOptions.TOTAL_PROCESS_MEMORY);
    if (jobMemorySize == null || taskMemorySize == null) {
      // Running locally will not get the configuration.
      return;
    }
    int parallelism = config.getExecutionParallel();
    int numberOfTaskSlots = configuration.get(TaskManagerOptions.NUM_TASK_SLOTS);
    int memorySize = jobMemorySize.getMebiBytes() + (parallelism / numberOfTaskSlots) * taskMemorySize.getMebiBytes();
    if (parallelism % numberOfTaskSlots != 0) {
      memorySize += taskMemorySize.getMebiBytes();
    }
    if (memorySize != config.getMemorySize()) {
      LOG.info("Reset the memory allocation of the optimizer to {}, before set is {}", memorySize,
          config.getMemorySize());
      config.setMemorySize(memorySize);
    }
  }
}
