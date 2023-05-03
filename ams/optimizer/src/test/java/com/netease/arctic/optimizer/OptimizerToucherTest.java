package com.netease.arctic.optimizer;

import com.netease.arctic.ams.api.MockArcticMetastoreServer;
import org.apache.iceberg.relocated.com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.kohsuke.args4j.CmdLineException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class OptimizerToucherTest {

  @Test
  public void testRegisterOptimizer() throws CmdLineException, InterruptedException {
    MockArcticMetastoreServer mockAMS = MockArcticMetastoreServer.getInstance();
    String[] optimizerArgs = new String[]{"-a", mockAMS.getUrl(), "-p", "1", "-m", "512", "-g", "g1"};
    OptimizerToucher optimizerToucher = new OptimizerToucher(new OptimizerConfig(optimizerArgs));
    final CountDownLatch latch = new CountDownLatch(1);
    List<String> tokenList = Lists.newArrayList();
    optimizerToucher.withTokenChangeListener(newToken -> {
      tokenList.add(newToken);
      latch.countDown();
    });
    new Thread(optimizerToucher::start).start();
    if (latch.await(5, TimeUnit.SECONDS)) {
      Assert.assertEquals(1, tokenList.size());
      Assert.assertEquals(1, mockAMS.optimizerHandler().getRegisteredOptimizers().size());
      Assert.assertEquals(mockAMS.optimizerHandler().getRegisteredOptimizers().keySet().iterator().next(),
          tokenList.get(0));
      Assert.assertEquals(tokenList.get(0), optimizerToucher.getToken());
      optimizerToucher.stop();
    } else {
      throw new RuntimeException("Wait for token timeout");
    }
  }
}
