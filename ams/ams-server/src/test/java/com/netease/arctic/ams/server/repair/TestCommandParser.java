package com.netease.arctic.ams.server.repair;


import com.netease.arctic.ams.server.repair.command.CallCommand;
import com.netease.arctic.ams.server.repair.command.CommandParser;
import com.netease.arctic.ams.server.repair.command.IllegalCommandException;
import com.netease.arctic.ams.server.repair.command.OptimizeCall;
import com.netease.arctic.ams.server.repair.command.ShowCall;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TestCommandParser {

  private static final MockSimpleRegexCommandParser mockSimpleRegexCommandParser = new MockSimpleRegexCommandParser();

  //Temporary, will be deleted after the generator completes
  private static class MockSimpleRegexCommandParser implements CommandParser {
    private static final String ANALYZE = "ANALYZE";
    private static final String REPAIR = "REPAIR";
    private static final String THROUGH = "THROUGH";
    private static final String USE = "USE";
    private static final String OPTIMIZE = "OPTIMIZE";
    private static final String REFRESH = "REFRESH";
    private static final String FILE_CACHE = "FILE_CACHE";
    private static final String SHOW = "SHOW";

    private static final String ANALYZE_EXCEPTION_MESSAGE =
        "Please check if your command is correct! Pattern: ANALYZE ${table_name}";
    private static final String REPAIR_EXCEPTION_MESSAGE =
        "Please check if your command is correct! " +
            "Pattern: REPAIR ${table_name} THROUGH " +
            "[ FIND_BACK | SYNC_METADATA | ROLLBACK ${snapshot_id} | DROP_TABLE ]";
    private static final String USE_EXCEPTION_MESSAGE =
        "Please check if your command is correct! " +
            "Pattern: USE [ ${catalog_name} | ${database_name}  ]";
    private static final String OPTIMIZE_EXCEPTION_MESSAGE =
        "Please check if your command is correct! " +
            "Pattern: OPTIMIZE [ STOP | START ] ${table_name}";
    private static final String REFRESH_EXCEPTION_MESSAGE =
        "Please check if your command is correct! " +
            "Pattern: REFRESH FILE_CACHE ${table_name}";
    private static final String SHOW_EXCEPTION_MESSAGE =
        "Please check if your command is correct! " +
            "Pattern: SHOW [ CATALOGS | DATABASES | TABLES ]";

    @Override
    public CallCommand parse(String line) throws IllegalCommandException {
      String[] commandSplit = line.trim().split("\\s+");
      if (commandSplit.length < 2) {
        return call -> "HelpCall";
      }

      switch (commandSplit[0].toUpperCase()) {
        case ANALYZE:
          if (commandSplit.length != 2) {
            throw new IllegalCommandException(ANALYZE_EXCEPTION_MESSAGE);
          }
          return call -> "AnalyzeCall " + commandSplit[1];
        case REPAIR:
          if (commandSplit.length > 5) {
            throw new IllegalCommandException(REPAIR_EXCEPTION_MESSAGE);
          }
          if (commandSplit.length < 4 || !StringUtils.equalsIgnoreCase(commandSplit[2], THROUGH)) {
            throw new IllegalCommandException(REPAIR_EXCEPTION_MESSAGE);
          }
          if (StringUtils.equalsIgnoreCase(commandSplit[3], RepairWay.ROLLBACK.name())) {
            if (commandSplit.length != 5) {
              throw new IllegalCommandException("Please check if you enter the snapshot id!");
            } else {
              return call -> "RepairCall " + commandSplit[1] + " " +
                  RepairWay.ROLLBACK + " " + commandSplit[4];
            }
          } else {
            if (commandSplit.length != 4) {
              throw new IllegalCommandException(REPAIR_EXCEPTION_MESSAGE);
            }
            RepairWay repairWay;
            try {
              repairWay = RepairWay.valueOf(commandSplit[3].toUpperCase());
            } catch (IllegalArgumentException e) {
              throw new IllegalCommandException(REPAIR_EXCEPTION_MESSAGE);
            }
            return call -> "RepairCall " + commandSplit[1] + " " + repairWay;
          }
        case USE:
          if (commandSplit.length != 2 || commandSplit[1].split("\\.").length > 2) {
            throw new IllegalCommandException(USE_EXCEPTION_MESSAGE);
          }
          return call -> "UseCall " + commandSplit[1];
        case OPTIMIZE:
          if (commandSplit.length != 3) {
            throw new IllegalCommandException(OPTIMIZE_EXCEPTION_MESSAGE);
          }
          OptimizeCall.Action optimizeAction;
          try {
            optimizeAction = OptimizeCall.Action.valueOf(commandSplit[1].toUpperCase());
          } catch (IllegalArgumentException e) {
            throw new IllegalCommandException(OPTIMIZE_EXCEPTION_MESSAGE);
          }
          return call -> "OptimizeCall "
              + optimizeAction + " " + commandSplit[2];
        case REFRESH:
          if (commandSplit.length == 3 && StringUtils.equalsIgnoreCase(commandSplit[1], FILE_CACHE)) {
            return call -> "RefreshCall " + commandSplit[2];
          } else {
            throw new IllegalCommandException(REFRESH_EXCEPTION_MESSAGE);
          }
        case SHOW:
          if (commandSplit.length != 2) {
            throw new IllegalCommandException(SHOW_EXCEPTION_MESSAGE);
          }
          ShowCall.Namespaces namespaces;
          try {
            namespaces = ShowCall.Namespaces.valueOf(commandSplit[1].toUpperCase());
          } catch (IllegalArgumentException e) {
            throw new IllegalCommandException(SHOW_EXCEPTION_MESSAGE);
          }
          return call -> "ShowCall " + namespaces;
      }
      return call -> "HelpCall";
    }

    @Override
    public String[] keywords() {
      String[] keywordsUpper = {
          ANALYZE,
          REPAIR,
          THROUGH,
          USE,
          OPTIMIZE,
          REFRESH,
          FILE_CACHE,
          SHOW,
          OptimizeCall.Action.START.name(),
          OptimizeCall.Action.STOP.name(),
          RepairWay.FIND_BACK.name(),
          RepairWay.SYNC_METADATA.name(),
          RepairWay.ROLLBACK.name(),
          RepairWay.DROP_TABLE.name(),
          ShowCall.Namespaces.CATALOGS.name(),
          ShowCall.Namespaces.DATABASES.name(),
          ShowCall.Namespaces.TABLES.name()
      };
      Object[] keywordsLower = Arrays.stream(keywordsUpper).map(
          keyword -> keyword.toLowerCase()).collect(Collectors.toList()).toArray();

      return (String[]) ArrayUtils.addAll(keywordsUpper, keywordsLower);
    }
  }


  @Test
  public void testKeyWords() {
    String[] keywords = mockSimpleRegexCommandParser.keywords();
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
    Assert.assertEquals("AnalyzeCall stock",
        mockSimpleRegexCommandParser.parse("ANALYZE stock ").call(null));
    Assert.assertEquals("AnalyzeCall order",
        mockSimpleRegexCommandParser.parse("  analyze order").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("ANALYZE stock order").call(null));

    //RepairCall test
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR  stock THROUGH ROLLBACK").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR stock ROLLBACK 1234567").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR stock FIND_BACK 1234567").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR stock SYNC_METADATA 1234567").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR stock THROUGH FIND_BACK 1234567").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR stock THROUGH SYNC_METADATA 1234567").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("REPAIR stock SYNC_METADATA").call(null));
    Assert.assertEquals("RepairCall stock ROLLBACK 123456789",
        mockSimpleRegexCommandParser.parse("REPAIR  stock  THROUGH  ROLLBACK  123456789").call(null));
    Assert.assertEquals("RepairCall stock ROLLBACK 123456789",
        mockSimpleRegexCommandParser.parse("repair stock through rollback 123456789").call(null));
    Assert.assertEquals("RepairCall stock FIND_BACK",
        mockSimpleRegexCommandParser.parse(" REPAIR stock THROUGH FIND_BACK ").call(null));
    Assert.assertEquals("RepairCall stock SYNC_METADATA",
        mockSimpleRegexCommandParser.parse("repair  stock  through  sync_metadata").call(null));
    Assert.assertEquals("RepairCall stock DROP_TABLE",
        mockSimpleRegexCommandParser.parse(" REPAIR stock THROUGH drop_table ").call(null));

    //UseCall test
    Assert.assertEquals("UseCall my_db",
        mockSimpleRegexCommandParser.parse("USE  my_db").call(null));
    Assert.assertEquals("UseCall my_catalog",
        mockSimpleRegexCommandParser.parse("use my_catalog").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("USE my_catalog my_db").call(null));

    //OptimizeCall test
    Assert.assertEquals("OptimizeCall START order_line",
        mockSimpleRegexCommandParser.parse("OPTIMIZE START order_line").call(null));
    Assert.assertEquals("OptimizeCall STOP stock",
        mockSimpleRegexCommandParser.parse("optimize stop  stock").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("OPTIMIZE stock").call(null));

    //RefreshCall test
    Assert.assertEquals("RefreshCall order_line",
        mockSimpleRegexCommandParser.parse("REFRESH FILE_CACHE order_line").call(null));
    Assert.assertEquals("RefreshCall order_line",
        mockSimpleRegexCommandParser.parse("refresh  file_cache order_line").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("refresh order_line").call(null));

    //ShowCall test
    Assert.assertEquals("ShowCall DATABASES",
        mockSimpleRegexCommandParser.parse("SHOW DATABASES").call(null));
    Assert.assertEquals("ShowCall TABLES",
        mockSimpleRegexCommandParser.parse("SHOW TABLES").call(null));
    Assert.assertEquals("ShowCall DATABASES",
        mockSimpleRegexCommandParser.parse("show  databases").call(null));
    Assert.assertEquals("ShowCall CATALOGS",
        mockSimpleRegexCommandParser.parse("show catalogs").call(null));
    Assert.assertThrows(IllegalCommandException.class,
        () -> mockSimpleRegexCommandParser.parse("show my_db tables").call(null));

    Assert.assertEquals("HelpCall",
        mockSimpleRegexCommandParser.parse("FIND_BACK stock").call(null));
    Assert.assertEquals("HelpCall",
        mockSimpleRegexCommandParser.parse("help").call(null));
    Assert.assertEquals("HelpCall",
        mockSimpleRegexCommandParser.parse("analyze").call(null));

  }

}
