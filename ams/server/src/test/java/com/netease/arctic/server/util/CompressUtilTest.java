package com.netease.arctic.server.util;

import com.netease.arctic.server.utils.CompressUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class CompressUtilTest {

  @Test
  public void zipAndUnzip() {
    byte[] bytes = new byte[1024];
    Arrays.fill(bytes, (byte) 10);
    byte[] gzip = CompressUtil.gzip(bytes);
    Assertions.assertTrue(gzip.length < bytes.length);
    byte[] unGzip = CompressUtil.unGzip(gzip);
    Assertions.assertTrue(Arrays.equals(unGzip, bytes));
  }
}
