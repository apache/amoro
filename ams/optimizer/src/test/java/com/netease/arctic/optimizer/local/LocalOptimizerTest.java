package com.netease.arctic.optimizer.local;

import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

public class LocalOptimizerTest {

  @Test
  public void testStartLocalOptimizer() throws CmdLineException {
    MockArcticMetastoreServer mockAMS = MockArcticMetastoreServer.getInstance();
    String[] optimizerArgs = new String[]{"-a", mockAMS.getUrl(), "-p", "1", "-m", "512", "-g", "g1"};
    LocalOptimizer.main(optimizerArgs);
  }
}
