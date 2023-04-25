package com.netease.arctic.spark.test.suites.sql;

import com.netease.arctic.spark.test.SparkTableTestBase;
import com.netease.arctic.spark.test.extensions.EnableCatalogSelect;

@EnableCatalogSelect
@EnableCatalogSelect.SelectCatalog(byTableFormat = true)
public class TestInsertOverwriteSQL extends SparkTableTestBase {

  // 1. dynamic insert overwrite
  // 2. static insert overwrite
  // 3. duplicate check for insert overwrite
  // 4. optimize write is work for insert overwrite
}
