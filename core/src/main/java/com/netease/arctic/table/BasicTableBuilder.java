package com.netease.arctic.table;

import com.netease.arctic.ams.api.TableFormat;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.Schema;
import org.apache.iceberg.SortOrder;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.collect.Maps;

import java.util.Map;

public abstract class BasicTableBuilder<SELF extends TableBuilder> implements TableBuilder {
  protected PartitionSpec spec = PartitionSpec.unpartitioned();
  protected SortOrder sortOrder = SortOrder.unsorted();
  protected Map<String, String> properties = Maps.newHashMap();
  protected PrimaryKeySpec keySpec = PrimaryKeySpec.noPrimaryKey();

  protected final Schema schema;
  protected final TableIdentifier identifier;
  protected final TableFormat format;


  public BasicTableBuilder(Schema schema, TableFormat format, TableIdentifier identifier) {
    this.schema = schema;
    this.format = format;
    this.identifier = identifier;
  }


  @Override
  public TableBuilder withPartitionSpec(PartitionSpec partitionSpec) {
    this.spec = partitionSpec;
    return self();
  }

  @Override
  public TableBuilder withSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
    return self();
  }

  @Override
  public TableBuilder withProperties(Map<String, String> properties) {
    Preconditions.checkNotNull(properties, "table properties must not be null");
    this.properties = properties;
    return self();
  }

  @Override
  public TableBuilder withProperty(String key, String value) {
    this.properties.put(key, value);
    return self();
  }

  @Override
  public TableBuilder withPrimaryKeySpec(PrimaryKeySpec primaryKeySpec) {
    this.keySpec = primaryKeySpec;
    return self();
  }

  @Override
  public Transaction newCreateTableTransaction() {
    throw new UnsupportedOperationException("do not support create table transactional.");
  }

  protected abstract SELF self() ;
}
