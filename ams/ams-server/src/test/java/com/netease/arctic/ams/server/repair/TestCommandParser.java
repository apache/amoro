package com.netease.arctic.ams.server.repair;


import com.netease.arctic.PooledAmsClient;
import com.netease.arctic.ams.api.client.OptimizeManagerClient;
import com.netease.arctic.ams.server.repair.command.AnalyzeCall;
import com.netease.arctic.ams.server.repair.command.CallCommand;
import com.netease.arctic.ams.server.repair.command.CommandParser;
import com.netease.arctic.ams.server.repair.command.DefaultCallFactory;
import com.netease.arctic.ams.server.repair.command.HelpCall;
import com.netease.arctic.ams.server.repair.command.IllegalCommandException;
import com.netease.arctic.ams.server.repair.command.OptimizeCall;
import com.netease.arctic.ams.server.repair.command.RefreshCall;
import com.netease.arctic.ams.server.repair.command.RepairCall;
import com.netease.arctic.ams.server.repair.command.ShowCall;
import com.netease.arctic.ams.server.repair.command.SimpleRegexCommandParser;
import com.netease.arctic.ams.server.repair.command.UseCall;
import com.netease.arctic.catalog.CatalogManager;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestCommandParser {
  private static SimpleRegexCommandParser simpleRegexCommandParser = 
      new SimpleRegexCommandParser(new DefaultCallFactory(
          new RepairConfig("thrift://localhost:1260", "local_catalog", 100, 100),
          new CatalogManager("thrift://localhost:1260"),
          new OptimizeManagerClient("thrift://localhost:1260"),
          new PooledAmsClient("thrift://localhost:1260")
      ));


  @Test
  public void testKeyWords() {
    String[] keywords = simpleRegexCommandParser.keywords();
    Assert.assertArrayEquals(new String[]{
        "ANALYZE", "REPAIR", "THROUGH", "USE", "OPTIMIZE", "REFRESH", "FILE_CACHE", "SHOW", "START", "STOP",
        "FIND_BACK", "SYNC_METADATA", "ROLLBACK", "DROP_TABLE", "CATALOGS", "DATABASES", "TABLES",
        "analyze", "repair", "through", "use", "optimize", "refresh", "file_cache", "show", "start", "stop",
        "find_back", "sync_metadata", "rollback", "drop_table", "catalogs", "databases", "tables"
    }, keywords);
  }

  @Test
  public void testParser() throws Exception {
    //AnalyzeCall test
    Assert.assertEquals(AnalyzeCall.class,
        simpleRegexCommandParser.parse("ANALYZE stock ").getClass());
    Assert.assertEquals(AnalyzeCall.class,
        simpleRegexCommandParser.parse("  analyze order").getClass());
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("ANALYZE stock order"));

    //RepairCall test
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("REPAIR stock ROLLBACK 1234567"));
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("REPAIR stock FIND_BACK 1234567"));
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("REPAIR stock SYNC_METADATA 1234567"));
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("REPAIR stock THROUGH FIND_BACK 1234567"));
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("REPAIR stock THROUGH SYNC_METADATA 1234567"));
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("REPAIR stock SYNC_METADATA"));
    Assert.assertEquals(RepairCall.class,
        simpleRegexCommandParser.parse("REPAIR  stock  THROUGH  ROLLBACK  123456789").getClass());
    Assert.assertEquals(RepairCall.class,
        simpleRegexCommandParser.parse("repair stock through rollback 123456789").getClass());
    Assert.assertEquals(RepairCall.class,
        simpleRegexCommandParser.parse("repair stock through rollback ").getClass());
    Assert.assertEquals(RepairCall.class,
        simpleRegexCommandParser.parse(" REPAIR stock THROUGH FIND_BACK ").getClass());
    Assert.assertEquals(RepairCall.class,
        simpleRegexCommandParser.parse("repair  stock  through  sync_metadata").getClass());
    Assert.assertEquals(RepairCall.class,
        simpleRegexCommandParser.parse(" REPAIR stock THROUGH drop_table ").getClass());

    //UseCall test
    Assert.assertEquals(UseCall.class,
        simpleRegexCommandParser.parse("USE  my_db").getClass());
    Assert.assertEquals(UseCall.class,
        simpleRegexCommandParser.parse("use my_catalog").getClass());
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("USE my_catalog my_db"));

    //OptimizeCall test
    Assert.assertEquals(OptimizeCall.class,
        simpleRegexCommandParser.parse("OPTIMIZE START order_line").getClass());
    Assert.assertEquals(OptimizeCall.class,
        simpleRegexCommandParser.parse("optimize stop  stock").getClass());
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("OPTIMIZE stock"));

    //RefreshCall test
    Assert.assertEquals(RefreshCall.class,
        simpleRegexCommandParser.parse("REFRESH FILE_CACHE order_line").getClass());
    Assert.assertEquals(RefreshCall.class,
        simpleRegexCommandParser.parse("refresh  file_cache order_line").getClass());
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("refresh order_line"));

    //ShowCall test
    Assert.assertEquals(ShowCall.class,
        simpleRegexCommandParser.parse("SHOW DATABASES").getClass());
    Assert.assertEquals(ShowCall.class,
        simpleRegexCommandParser.parse("SHOW TABLES").getClass());
    Assert.assertEquals(ShowCall.class,
        simpleRegexCommandParser.parse("show  databases").getClass());
    Assert.assertEquals(ShowCall.class,
        simpleRegexCommandParser.parse("show catalogs").getClass());
    Assert.assertThrows(IllegalCommandException.class,
        () -> simpleRegexCommandParser.parse("show my_db tables"));

    Assert.assertEquals(HelpCall.class,
        simpleRegexCommandParser.parse("FIND_BACK stock").getClass());
    Assert.assertEquals(HelpCall.class,
        simpleRegexCommandParser.parse("help").getClass());
    Assert.assertEquals(HelpCall.class,
        simpleRegexCommandParser.parse("analyze").getClass());

  }

}
