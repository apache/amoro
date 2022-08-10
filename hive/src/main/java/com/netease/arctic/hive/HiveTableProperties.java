package com.netease.arctic.hive;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;

public class HiveTableProperties {

  public static final String ARCTIC_TABLE_FLAG = "arctic.enable";

  public static final String ARCTIC_TABLE_PRIMARY_KEYS = "arctic.table.primary-keys";

}

