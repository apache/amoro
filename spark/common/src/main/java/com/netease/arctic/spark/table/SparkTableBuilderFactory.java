package com.netease.arctic.spark.table;

import com.netease.arctic.catalog.ArcticCatalog;
import org.apache.spark.sql.util.CaseInsensitiveStringMap;

public interface SparkTableBuilderFactory {

  SparkTableBuilder create(ArcticCatalog catalog, CaseInsensitiveStringMap options);
}
