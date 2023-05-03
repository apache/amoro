package com.netease.arctic.optimizer.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.netease.arctic.ams.api.Environments;
import com.netease.arctic.optimizer.Optimizer;
import com.netease.arctic.optimizer.OptimizerConfig;
import org.kohsuke.args4j.CmdLineException;

import java.io.File;
import java.io.IOException;

public class ExternalOptimizer {

  public static void main(String[] args) throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    File file = new File(Environments.getArcticHome() + "/optimizer.yaml");
    OptimizerConfig config = mapper.readValue(file, OptimizerConfig.class);
    Optimizer optimizer = new Optimizer(config);
    optimizer.startOptimizing();
  }
}
