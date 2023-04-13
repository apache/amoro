package com.netease.arctic.trino.keyed;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.netease.arctic.scan.CombinedScanTask;
import com.netease.arctic.scan.KeyedTableScan;
import com.netease.arctic.table.ArcticTable;
import io.airlift.log.Logger;
import io.trino.plugin.iceberg.IcebergColumnHandle;
import io.trino.plugin.iceberg.IcebergTableHandle;
import io.trino.spi.connector.ColumnHandle;
import io.trino.spi.connector.ConnectorSession;
import io.trino.spi.predicate.TupleDomain;
import io.trino.spi.statistics.ColumnStatistics;
import io.trino.spi.statistics.DoubleRange;
import io.trino.spi.statistics.Estimate;
import io.trino.spi.statistics.TableStatistics;
import io.trino.spi.type.TypeManager;
import org.apache.iceberg.Schema;
import org.apache.iceberg.io.CloseableIterable;
import org.apache.iceberg.types.Types;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.trino.plugin.iceberg.ExpressionConverter.toIcebergExpression;
import static io.trino.plugin.iceberg.IcebergSessionProperties.isExtendedStatisticsEnabled;
import static io.trino.plugin.iceberg.IcebergUtil.getColumns;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toUnmodifiableMap;

public class KeyedTableStatisticsMaker {
  private static final Logger log = Logger.get(KeyedTableStatisticsMaker.class);

  public static final String TRINO_STATS_PREFIX = "trino.stats.ndv.";
  public static final String TRINO_STATS_NDV_FORMAT = TRINO_STATS_PREFIX + "%d.ndv";
  public static final Pattern TRINO_STATS_COLUMN_ID_PATTERN =
      Pattern.compile(Pattern.quote(TRINO_STATS_PREFIX) + "(?<columnId>\\d+)\\..*");
  public static final Pattern TRINO_STATS_NDV_PATTERN =
      Pattern.compile(Pattern.quote(TRINO_STATS_PREFIX) + "(?<columnId>\\d+)\\.ndv");

  public static final String APACHE_DATASKETCHES_THETA_V1_NDV_PROPERTY = "ndv";

  private final TypeManager typeManager;
  private final ConnectorSession session;
  private final ArcticTable arcticTable;

  private KeyedTableStatisticsMaker(TypeManager typeManager, ConnectorSession session, ArcticTable arcticTable) {
    this.typeManager = typeManager;
    this.session = session;
    this.arcticTable = arcticTable;
  }

  public static TableStatistics getTableStatistics(
      TypeManager typeManager,
      ConnectorSession session,
      IcebergTableHandle tableHandle,
      ArcticTable arcticTable) {
    return new KeyedTableStatisticsMaker(typeManager, session, arcticTable).makeTableStatistics(tableHandle);
  }

  private TableStatistics makeTableStatistics(IcebergTableHandle tableHandle) {
    TupleDomain<IcebergColumnHandle> enforcedPredicate = tableHandle.getEnforcedPredicate();

    if (enforcedPredicate.isNone()) {
      return TableStatistics.builder()
          .setRowCount(Estimate.of(0))
          .build();
    }

    Schema arcticTableSchema = arcticTable.schema();
    List<Types.NestedField> columns = arcticTableSchema.columns();

    List<IcebergColumnHandle> columnHandles = getColumns(arcticTableSchema, typeManager);
    Map<Integer, IcebergColumnHandle> idToColumnHandle = columnHandles.stream()
        .collect(toUnmodifiableMap(IcebergColumnHandle::getId, identity()));

    KeyedTableScan tableScan = arcticTable.asKeyedTable().newScan()
        .filter(toIcebergExpression(enforcedPredicate)).includeColumnStats();

    KeyedTableStatistics.Builder icebergStatisticsBuilder = new KeyedTableStatistics.Builder(columns, typeManager);
    try (CloseableIterable<CombinedScanTask> combinedScanTasks = tableScan.planTasks()) {
      combinedScanTasks.forEach(combinedScanTask -> combinedScanTask.tasks().forEach(
          keyedTableScanTask -> Streams.concat(
                  keyedTableScanTask.dataTasks().stream(), keyedTableScanTask.arcticEquityDeletes().stream())
              .forEach(arcticFileScanTask -> icebergStatisticsBuilder.acceptDataFile(
                      arcticFileScanTask.file(), arcticFileScanTask.spec(), arcticFileScanTask.fileType()
                  )
              )
      ));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    KeyedTableStatistics summary = icebergStatisticsBuilder.build();

    if (summary.getFileCount() == 0) {
      return TableStatistics.builder()
          .setRowCount(Estimate.of(0))
          .build();
    }

    Map<Integer, Long> ndvs = readNdvs(arcticTable);

    ImmutableMap.Builder<ColumnHandle, ColumnStatistics> columnHandleBuilder = ImmutableMap.builder();
    double recordCount = summary.getRecordCount();
    for (IcebergColumnHandle columnHandle : idToColumnHandle.values()) {
      int fieldId = columnHandle.getId();
      ColumnStatistics.Builder columnBuilder = new ColumnStatistics.Builder();
      Long nullCount = summary.getNullCounts().get(fieldId);
      if (nullCount != null) {
        columnBuilder.setNullsFraction(Estimate.of(nullCount / recordCount));
      }
      if (summary.getColumnSizes() != null) {
        Long columnSize = summary.getColumnSizes().get(fieldId);
        if (columnSize != null) {
          columnBuilder.setDataSize(Estimate.of(columnSize));
        }
      }
      Object min = summary.getMinValues().get(fieldId);
      Object max = summary.getMaxValues().get(fieldId);
      if (min != null && max != null) {
        columnBuilder.setRange(DoubleRange.from(columnHandle.getType(), min, max));
      }
      columnBuilder.setDistinctValuesCount(
          Optional.ofNullable(ndvs.get(fieldId))
              .map(Estimate::of)
              .orElseGet(Estimate::unknown));
      columnHandleBuilder.put(columnHandle, columnBuilder.build());
    }
    return new TableStatistics(Estimate.of(recordCount), columnHandleBuilder.buildOrThrow());
  }

  private Map<Integer, Long> readNdvs(ArcticTable arcticTable) {
    if (!isExtendedStatisticsEnabled(session)) {
      return ImmutableMap.of();
    }

    ImmutableMap.Builder<Integer, Long> ndvByColumnId = ImmutableMap.builder();
    arcticTable.properties().forEach((key, value) -> {
      if (key.startsWith(TRINO_STATS_PREFIX)) {
        Matcher matcher = TRINO_STATS_NDV_PATTERN.matcher(key);
        if (matcher.matches()) {
          int columnId = Integer.parseInt(matcher.group("columnId"));
          long ndv = Long.parseLong(value);
          ndvByColumnId.put(columnId, ndv);
        }
      }
    });
    return ndvByColumnId.buildOrThrow();
  }
}
