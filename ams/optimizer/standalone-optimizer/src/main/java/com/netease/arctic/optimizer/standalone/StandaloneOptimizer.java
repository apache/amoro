package com.netease.arctic.optimizer.standalone;

import com.netease.arctic.ams.api.resource.Resource;
import com.netease.arctic.optimizer.common.Optimizer;
import com.netease.arctic.optimizer.common.OptimizerConfig;
import org.kohsuke.args4j.CmdLineException;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

public class StandaloneOptimizer {
  public static void main(String[] args) throws CmdLineException {
    OptimizerConfig optimizerConfig = new OptimizerConfig(args);
    Optimizer optimizer = new Optimizer(optimizerConfig);

    // calculate optimizer memory allocation
    long memorySize = Runtime.getRuntime().maxMemory() / 1024 / 1024;
    optimizerConfig.setMemorySize((int) memorySize);

    RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    String processId = runtimeMXBean.getName().split("@")[0];
    optimizer.getToucher().withRegisterProperty(Resource.PROPERTY_JOB_ID, processId);
    optimizer.startOptimizing();
  }
}
