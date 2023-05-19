package com.netease.arctic.optimizer.local;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.netease.arctic.ams.api.Environments;
import com.netease.arctic.optimizer.OptimizerConfig;

import java.io.File;
import java.io.IOException;

public class OptimizerConfigYamlGenerator {
  public static void main(String[] args) throws IOException {
    OptimizerConfig config = new OptimizerConfig();
    // set config properties here
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.writeValue(new File(Environments.getArcticHome() + "/optimizer.yaml"), config);
  }
}