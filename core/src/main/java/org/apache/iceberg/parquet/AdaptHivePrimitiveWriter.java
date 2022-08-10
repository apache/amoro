package org.apache.iceberg.parquet;

import org.apache.iceberg.relocated.com.google.common.collect.ImmutableList;
import org.apache.parquet.column.ColumnDescriptor;
import org.apache.parquet.column.ColumnWriteStore;

import java.util.List;

public class AdaptHivePrimitiveWriter<T> implements ParquetValueWriter<T> {

  protected final AdaptHiveColumnWriter<T> column;

  private final List<TripleWriter<?>> children;

  protected AdaptHivePrimitiveWriter(ColumnDescriptor desc) {
    this.column = AdaptHiveColumnWriter.newWriter(desc);
    this.children = ImmutableList.of(column);
  }

  @Override
  public void write(int repetitionLevel, T value) {
    column.write(repetitionLevel, value);
  }

  @Override
  public List<TripleWriter<?>> columns() {
    return children;
  }

  @Override
  public void setColumnStore(ColumnWriteStore columnStore) {
    this.column.setColumnStore(columnStore);
  }
}