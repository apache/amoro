/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.amoro.server.optimizing.maintainer;

import static org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer.EXPIRE_TIMESTAMP_MS;
import static org.apache.amoro.server.optimizing.maintainer.IcebergTableMaintainer.EXPIRE_TIMESTAMP_S;

import org.apache.amoro.config.DataExpirationConfig;
import org.apache.amoro.shade.guava32.com.google.common.collect.ArrayListMultimap;
import org.apache.amoro.shade.guava32.com.google.common.collect.Iterators;
import org.apache.amoro.shade.guava32.com.google.common.collect.Lists;
import org.apache.amoro.shade.guava32.com.google.common.collect.Maps;
import org.apache.amoro.shade.guava32.com.google.common.collect.Sets;
import org.apache.iceberg.ContentFile;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.ManifestContent;
import org.apache.iceberg.ManifestFile;
import org.apache.iceberg.ManifestFiles;
import org.apache.iceberg.ManifestReader;
import org.apache.iceberg.PartitionField;
import org.apache.iceberg.PartitionSpec;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Table;
import org.apache.iceberg.expressions.Expression;
import org.apache.iceberg.expressions.Expressions;
import org.apache.iceberg.io.CloseableIterator;
import org.apache.iceberg.transforms.Transform;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.DateTimeUtil;
import org.apache.iceberg.util.SerializableFunction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class DataExpirationProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(DataExpirationProcessor.class);

  protected final DataExpirationConfig config;
  protected final Function<DataFile, ?> dataFileDeleteFunc;

  private static final int DEFAULT_MAX_EXPIRING_FILE_COUNT = 10000;
  protected static final OffsetDateTime EPOCH = Instant.ofEpochSecond(0).atOffset(ZoneOffset.UTC);

  public DataExpirationProcessor(
      DataExpirationConfig config, Function<DataFile, ?> dataFileDeleteFunc) {
    this.config = config;
    this.dataFileDeleteFunc = dataFileDeleteFunc;
  }

  /**
   * Get the maximum number of files that can be expired in one operation. This reads the system
   * property dynamically to support testing.
   */
  static int maxExpiringFileCount() {
    String maxExpiringFileCount =
        System.getProperty(
            "MAX_EXPIRING_FILE_COUNT", String.valueOf(DEFAULT_MAX_EXPIRING_FILE_COUNT));
    try {
      return Integer.parseInt(maxExpiringFileCount);
    } catch (NumberFormatException e) {
      LOG.warn(
          "Invalid MAX_EXPIRING_FILE_COUNT value: {}, using default {}",
          maxExpiringFileCount,
          DEFAULT_MAX_EXPIRING_FILE_COUNT);
      return DEFAULT_MAX_EXPIRING_FILE_COUNT;
    }
  }

  abstract void process(Instant expireInstant);

  abstract Types.NestedField expirationField();

  abstract Optional<PartitionFieldInfo> findFieldInSpec(PartitionSpec spec, int sourceId);

  abstract Types.NestedField findOriginalField(PartitionSpec spec, int sourceId);

  void collectExpiredFiles(
      Table table,
      ManifestsCollection manifestsCollection,
      Map<StructLike, List<Object>> dataFilesToExpire,
      Map<StructLike, List<DeleteFile>> deleteFilesToExpire,
      long expireTimestamp,
      long expiredFileCount)
      throws IOException {
    List<ManifestFileWrapper> pureExpiredManifests = manifestsCollection.pureExpiredManifests();
    // process fully expired partitions
    processPureExpiredManifests(
        table, pureExpiredManifests, dataFilesToExpire, deleteFilesToExpire, expiredFileCount);

    List<ManifestFileWrapper> remainingManifests =
        Lists.newArrayList(manifestsCollection.fullyExpired);
    remainingManifests.removeAll(pureExpiredManifests);
    // process remaining and partially expired manifests
    processPartiallyExpiredManifests(
        table,
        remainingManifests,
        manifestsCollection.partiallyExpired,
        dataFilesToExpire,
        deleteFilesToExpire,
        expiredFileCount,
        expireTimestamp);
  }

  protected void processPureExpiredManifests(
      Table table,
      List<ManifestFileWrapper> expiredManifests,
      Map<StructLike, List<Object>> dataFilesToExpire,
      Map<StructLike, List<DeleteFile>> deleteFilesToExpire,
      long expiredFileCount)
      throws IOException {
    if (expiredManifests.isEmpty()) {
      return;
    }
    // sort by partition range upper bound ascendingly
    Collections.sort(expiredManifests);

    for (ManifestFileWrapper wrapper : expiredManifests) {
      ManifestContent content = wrapper.manifestFile.content();
      ManifestFile file = wrapper.manifestFile;
      if (content == ManifestContent.DATA) {
        ManifestReader<DataFile> reader = ManifestFiles.read(file, table.io(), table.specs());
        expiredFileCount += readDataManifest(reader, dataFilesToExpire, Collections.emptySet());
      } else if (content == ManifestContent.DELETES) {
        ManifestReader<DeleteFile> reader =
            ManifestFiles.readDeleteManifest(file, table.io(), table.specs());
        expiredFileCount += readDeleteManifest(reader, deleteFilesToExpire, Collections.emptySet());
      }

      if (exceedMaxSize(expiredFileCount)) {
        break;
      }
    }
  }

  long readDataManifest(
      ManifestReader<DataFile> reader,
      Map<StructLike, List<Object>> dataFilesToExpire,
      Set<StructLike> excludedPartitions)
      throws IOException {
    long readFileCount = 0L;

    try (CloseableIterator<DataFile> iterator = reader.iterator()) {
      while (iterator.hasNext()) {
        DataFile dataFile = iterator.next();
        if (excludedPartitions.contains(dataFile.partition())) {
          continue;
        }

        dataFilesToExpire
            .computeIfAbsent(dataFile.partition(), k -> new ArrayList<>())
            .add(dataFileDeleteFunc.apply(dataFile));
        readFileCount++;
      }
    }

    return readFileCount;
  }

  long readDeleteManifest(
      ManifestReader<DeleteFile> reader,
      Map<StructLike, List<DeleteFile>> deleteFilesToExpire,
      Set<StructLike> excludedPartitions)
      throws IOException {
    long readFileCount = 0L;

    try (CloseableIterator<DeleteFile> iterator = reader.iterator()) {
      while (iterator.hasNext()) {
        DeleteFile deleteFile = iterator.next();
        if (excludedPartitions.contains(deleteFile.partition())) {
          continue;
        }

        deleteFilesToExpire
            .computeIfAbsent(deleteFile.partition(), k -> new ArrayList<>())
            .add(deleteFile.copyWithoutStats());
        readFileCount++;
      }
    }

    return readFileCount;
  }

  void processPartiallyExpiredManifests(
      Table table,
      List<ManifestFileWrapper> remainingManifests,
      List<ManifestFileWrapper> partiallyExpiredManifests,
      Map<StructLike, List<Object>> dataFilesToExpire,
      Map<StructLike, List<DeleteFile>> deleteFilesToExpire,
      long expiredFileCount,
      long expireTimestamp)
      throws IOException {
    if (partiallyExpiredManifests.isEmpty() && remainingManifests.isEmpty()) {
      return;
    }

    if (expiredFileCount >= maxExpiringFileCount()) {
      return;
    }

    Map<StructLike, List<Object>> partitionToData = Maps.newHashMap();
    Map<StructLike, List<DeleteFile>> partitionToDeletes = Maps.newHashMap();
    Set<StructLike> excludedPartitions = Sets.newHashSet();
    Iterator<ManifestFileWrapper> remainIter =
        Iterators.concat(remainingManifests.iterator(), partiallyExpiredManifests.iterator());
    ArrayListMultimap<Long, ManifestFileWrapper> boundToManifests =
        ManifestsCollection.groupByLowerBound(remainIter);
    long processedFileCount = 0L;

    for (Long partitionBound : boundToManifests.keySet()) {
      List<ManifestFileWrapper> wrappers = boundToManifests.get(partitionBound);
      for (ManifestFileWrapper wrapper : wrappers) {
        ManifestContent content = wrapper.manifestFile.content();
        ManifestFile file = wrapper.manifestFile;
        // remaining fully expired manifests
        if (wrapper.range.upperBoundLt(expireTimestamp)) {
          if (content == ManifestContent.DATA) {
            ManifestReader<DataFile> reader = ManifestFiles.read(file, table.io(), table.specs());
            processedFileCount += readDataManifest(reader, partitionToData, excludedPartitions);
          } else if (content == ManifestContent.DELETES) {
            ManifestReader<DeleteFile> reader =
                ManifestFiles.readDeleteManifest(file, table.io(), table.specs());
            processedFileCount +=
                readDeleteManifest(reader, partitionToDeletes, excludedPartitions);
          }
        } else {
          // partially expired manifests
          if (content == ManifestContent.DATA) {
            processedFileCount +=
                readPartialDataManifest(
                    table, file, partitionToData, excludedPartitions, expireTimestamp);
          } else if (content == ManifestContent.DELETES) {
            processedFileCount +=
                readPartialDeleteManifest(
                    table, file, partitionToDeletes, excludedPartitions, expireTimestamp);
          }
        }
      }

      if (processedFileCount >= maxExpiringFileCount()) {
        break;
      }
    }

    // remove excluded partitions
    excludedPartitions.forEach(partitionToData::remove);
    excludedPartitions.forEach(partitionToDeletes::remove);
    // merge to main maps
    dataFilesToExpire.putAll(partitionToData);
    deleteFilesToExpire.putAll(partitionToDeletes);
  }

  private long readPartialDataManifest(
      Table table,
      ManifestFile file,
      Map<StructLike, List<Object>> dataFilesToExpire,
      Set<StructLike> excludedPartitions,
      long expireTimestamp)
      throws IOException {
    long readFileCount = 0L;

    ManifestReader<DataFile> reader = ManifestFiles.read(file, table.io(), table.specs());
    try (CloseableIterator<DataFile> iterator = reader.iterator()) {
      while (iterator.hasNext()) {
        DataFile dataFile = iterator.next();
        StructLike partition = dataFile.partition();
        if (excludedPartitions.contains(partition)) {
          continue;
        }

        if (partition != null) {
          if (canBeExpiredByPartitionValue(
              dataFile, expireTimestamp, expirationField(), table.specs())) {
            dataFilesToExpire
                .computeIfAbsent(dataFile.partition(), k -> new ArrayList<>())
                .add(dataFileDeleteFunc.apply(dataFile));
            readFileCount++;
          } else {
            excludedPartitions.add(partition);
          }
        } else {
          ByteBuffer upperBound = dataFile.upperBounds().get(expirationField().fieldId());
          DatafileRange range =
              new DatafileRange(null, upperBound, expirationField().type(), config);
          if (range.upperBoundLtEq(expireTimestamp)) {
            dataFilesToExpire
                .computeIfAbsent(null, k -> new ArrayList<>())
                .add(dataFileDeleteFunc.apply(dataFile));
            readFileCount++;
          } else {
            excludedPartitions.add(null);
          }
        }
      }

      return readFileCount;
    }
  }

  private long readPartialDeleteManifest(
      Table table,
      ManifestFile file,
      Map<StructLike, List<DeleteFile>> deleteFilesToExpire,
      Set<StructLike> excludedPartitions,
      long expireTimestamp)
      throws IOException {
    long readFileCount = 0L;

    ManifestReader<DeleteFile> reader =
        ManifestFiles.readDeleteManifest(file, table.io(), table.specs());
    Types.NestedField expirationField = table.schema().findField(config.getExpirationField());
    try (CloseableIterator<DeleteFile> iterator = reader.iterator()) {
      while (iterator.hasNext()) {
        DeleteFile deleteFile = iterator.next();
        StructLike partition = deleteFile.partition();
        if (excludedPartitions.contains(partition)) {
          continue;
        }

        if (partition != null) {
          if (canBeExpiredByPartitionValue(
              deleteFile, expireTimestamp, expirationField, table.specs())) {
            deleteFilesToExpire
                .computeIfAbsent(deleteFile.partition(), k -> new ArrayList<>())
                .add(deleteFile.copyWithoutStats());
            readFileCount++;
          } else {
            excludedPartitions.add(partition);
          }
        } else {
          ByteBuffer upperBound = deleteFile.upperBounds().get(expirationField.fieldId());
          DatafileRange range = new DatafileRange(null, upperBound, expirationField.type(), config);
          if (range.upperBoundLtEq(expireTimestamp)) {
            deleteFilesToExpire
                .computeIfAbsent(deleteFile.partition(), k -> new ArrayList<>())
                .add(deleteFile.copyWithoutStats());
            readFileCount++;
          } else {
            // this partition is not expired, exclude it for later files
            excludedPartitions.add(deleteFile.partition());
          }
        }
      }

      return readFileCount;
    }
  }

  @SuppressWarnings("unchecked")
  private boolean canBeExpiredByPartitionValue(
      ContentFile<?> contentFile,
      Long expireTimestamp,
      Types.NestedField expirationField,
      Map<Integer, PartitionSpec> specs) {
    int pos = 0;
    List<Boolean> compareResults = new ArrayList<>();
    PartitionSpec partitionSpec = specs.get(contentFile.specId());
    Types.NestedField originalField = findOriginalField(partitionSpec, expirationField.fieldId());
    Type type = originalField.type();
    Long sanitizedTs = sanitizeExpireTimestamp(expireTimestamp, type, config);
    Comparable<?> sanitizedValue;
    if (type.asPrimitiveType() == Types.StringType.get()) {
      DateTimeFormatter formatter =
          DateTimeFormatter.ofPattern(config.getDateTimePattern(), Locale.getDefault());
      sanitizedValue =
          LocalDateTime.ofInstant(
                  Instant.ofEpochMilli(expireTimestamp), defaultZoneId(expirationField.type()))
              .format(formatter);
    } else {
      sanitizedValue = sanitizedTs;
    }

    for (PartitionField partitionField : partitionSpec.fields()) {
      if (partitionField.sourceId() == originalField.fieldId()) {
        if (partitionField.transform().isVoid()) {
          return false;
        }

        Comparable<?> partitionUpperBound =
            ((SerializableFunction<Comparable<?>, Comparable<?>>)
                    partitionField.transform().bind(expirationField.type()))
                .apply(sanitizedValue);
        Comparable<Object> filePartitionValue =
            contentFile.partition().get(pos, partitionUpperBound.getClass());
        int compared = filePartitionValue.compareTo(partitionUpperBound);
        Boolean compareResult =
            expirationField.type() == Types.StringType.get() ? compared <= 0 : compared < 0;
        compareResults.add(compareResult);
      }

      pos++;
    }

    return !compareResults.isEmpty() && compareResults.stream().allMatch(Boolean::booleanValue);
  }

  static class ManifestsCollection {
    final List<ManifestFileWrapper> fullyExpired;
    Long fMaxBound = Long.MIN_VALUE;
    final List<ManifestFileWrapper> partiallyExpired;
    Long pMinBound = Long.MAX_VALUE;
    Long pMaxBound = Long.MIN_VALUE;
    final DataExpirationConfig config;

    ManifestsCollection(int max, DataExpirationConfig config) {
      this.config = config;
      this.fullyExpired = Lists.newArrayListWithCapacity(max);
      this.partiallyExpired = Lists.newArrayListWithCapacity(max);
    }

    void addFullyExpired(ManifestFileWrapper wrapper) {
      this.fMaxBound = Math.max(fMaxBound, wrapper.range.upper());

      fullyExpired.add(wrapper);
    }

    void addPartiallyExpired(ManifestFileWrapper wrapper) {
      this.pMinBound = Math.min(pMinBound, wrapper.range.lower());
      this.pMaxBound = Math.max(pMaxBound, wrapper.range.upper());

      partiallyExpired.add(wrapper);
    }

    void add(ManifestFileWrapper wrapper, long expireTimestamp) {
      if (wrapper.fullyExpired(expireTimestamp)) {
        addFullyExpired(wrapper);
      } else if (wrapper.partiallyExpired(expireTimestamp)) {
        addPartiallyExpired(wrapper);
      }
    }

    /**
     * Get the fully expired manifests whose partition upper bound is less than or equal to the
     * minimum lower bound of partially expired manifests.
     *
     * @return list of manifests that in the expired partitions
     */
    List<ManifestFileWrapper> pureExpiredManifests() {
      if (fullyExpired.isEmpty()) {
        return Collections.emptyList();
      }

      return fullyExpired.stream()
          .filter(m -> m.range.upper() <= pMinBound)
          .collect(Collectors.toList());
    }

    int totalSize() {
      return fullyExpired.size() + partiallyExpired.size();
    }

    static ArrayListMultimap<Long, ManifestFileWrapper> groupByLowerBound(
        Iterator<ManifestFileWrapper> iter) {
      ArrayListMultimap<Long, ManifestFileWrapper> boundToManifests = ArrayListMultimap.create();
      while (iter.hasNext()) {
        ManifestFileWrapper wrapper = iter.next();
        Long lowerBound = wrapper.range.lower();
        boundToManifests.put(lowerBound, wrapper);
      }

      return boundToManifests;
    }
  }

  protected static class ManifestFileWrapper implements Comparable<ManifestFileWrapper> {
    ManifestFile manifestFile;
    PartitionRange range;

    ManifestFileWrapper(ManifestFile manifestFile, PartitionRange range) {
      this.manifestFile = manifestFile;
      this.range = range;
    }

    boolean fullyExpired(long expireTimestamp) {
      return range.upperBoundLt(expireTimestamp);
    }

    boolean partiallyExpired(long expireTimestamp) {
      return range.in(expireTimestamp);
    }

    @Override
    public int compareTo(@NotNull ManifestFileWrapper o) {
      return this.range.compareTo(o.range);
    }

    @Override
    public boolean equals(Object o) {
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ManifestFileWrapper wrapper = (ManifestFileWrapper) o;
      return com.google.common.base.Objects.equal(manifestFile, wrapper.manifestFile)
          && com.google.common.base.Objects.equal(range, wrapper.range);
    }

    @Override
    public int hashCode() {
      return com.google.common.base.Objects.hashCode(manifestFile, range);
    }
  }

  interface Range extends Comparable<Range> {
    boolean lowerBoundGt(long ts);

    boolean lowerBoundGtEq(long ts);

    boolean upperBoundLt(long ts);

    boolean upperBoundLtEq(long ts);

    default boolean in(long ts) {
      return !lowerBoundGt(ts) && !upperBoundLt(ts);
    }

    // return the decoded bound as milliseconds timestamp
    Long decodeBound(ByteBuffer bound);

    Long lower();

    Long upper();

    @Override
    default int compareTo(@NotNull IcebergExpirationProcessor.Range o) {
      return this.upper().compareTo(o.upper());
    }
  }

  static class PartitionRange implements Range {
    private final PartitionField partitionField;
    private final Type sourceType;
    private final DataExpirationConfig config;
    final Long lower;
    final Long upper;

    PartitionRange(
        @Nonnull ManifestFile.PartitionFieldSummary summary,
        PartitionField field,
        Type sourceType,
        DataExpirationConfig config) {
      this.partitionField = field;
      this.sourceType = sourceType;
      this.config = config;
      ByteBuffer lowerBound = summary.lowerBound();
      ByteBuffer upperBound = summary.upperBound();
      this.lower = decodeBound(lowerBound);
      this.upper = decodeBound(upperBound);
    }

    @SuppressWarnings("unchecked")
    private Long sanitize(long expireTimestamp) {
      Long sanitized = sanitizeExpireTimestamp(expireTimestamp, sourceType, config);
      String transformString = partitionField.transform().toString().toLowerCase();
      switch (transformString) {
        case "day":
          {
            Integer applied =
                ((SerializableFunction<Long, Integer>) partitionField.transform().bind(sourceType))
                    .apply(sanitized);
            return EPOCH.plusDays(applied).toInstant().toEpochMilli();
          }
        case "month":
          {
            Integer applied =
                ((SerializableFunction<Long, Integer>) partitionField.transform().bind(sourceType))
                    .apply(sanitized);
            return EPOCH.plusMonths(applied).toInstant().toEpochMilli();
          }
        case "year":
          {
            Integer applied =
                ((SerializableFunction<Long, Integer>) partitionField.transform().bind(sourceType))
                    .apply(sanitized);
            return EPOCH.plusYears(applied).toInstant().toEpochMilli();
          }
        case "hour":
          {
            Integer applied =
                ((SerializableFunction<Long, Integer>) partitionField.transform().bind(sourceType))
                    .apply(sanitized);
            return EPOCH.plusHours(applied).toInstant().toEpochMilli();
          }
        case "identity":
          if (sourceType == Types.StringType.get()) {
            Instant instant = Instant.ofEpochMilli(sanitized);
            int days = DateTimeUtil.daysFromInstant(instant);
            return EPOCH.plusDays(days).toInstant().toEpochMilli();
          } else {
            return sanitized;
          }
        default:
          throw new UnsupportedOperationException(
              "Cannot convert expire timestamp for transform: " + transformString);
      }
    }

    @Override
    public boolean lowerBoundGt(long ts) {
      Long sanitized = sanitize(ts);
      return lower.compareTo(sanitized) > 0;
    }

    @Override
    public boolean lowerBoundGtEq(long ts) {
      Long sanitized = sanitize(ts);
      return lower.compareTo(sanitized) >= 0;
    }

    @Override
    public boolean upperBoundLt(long ts) {
      return upper.compareTo(sanitize(ts)) < 0;
    }

    @Override
    public boolean upperBoundLtEq(long ts) {
      return upper.compareTo(sanitize(ts)) <= 0;
    }

    @Override
    public Long decodeBound(ByteBuffer bound) {
      Transform<?, ?> transform = partitionField.transform();
      if (transform.isVoid()) {
        throw new UnsupportedOperationException(
            "Cannot extract partition bound from void transform");
      }

      Object boundValue = Conversions.fromByteBuffer(transform.getResultType(sourceType), bound);
      String transformName = transform.toString();
      if (transformName.equalsIgnoreCase("identity")) {

        switch (sourceType.typeId()) {
          case STRING:
            DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern(config.getDateTimePattern(), Locale.getDefault());
            LocalDate localDate = LocalDate.parse((CharBuffer) boundValue, formatter);
            return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
          case TIMESTAMP:
            // timestamp is stored as nanoseconds in iceberg
            return (Long) boundValue / 1000;
          case LONG:
            String tsUnit = config.getNumberDateFormat();
            if (tsUnit.equals(EXPIRE_TIMESTAMP_MS)) {
              return (Long) boundValue;
            } else if (tsUnit.equals(EXPIRE_TIMESTAMP_S)) {
              return (Long) boundValue * 1000;
            }
            break;
        }
      } else if (transformName.equalsIgnoreCase("day")) {
        Integer days = (Integer) boundValue;
        return EPOCH.plusDays(days).toInstant().toEpochMilli();
      } else if (transformName.equalsIgnoreCase("month")) {
        Integer months = (Integer) boundValue;
        return EPOCH.plusMonths(months).toInstant().toEpochMilli();
      } else if (transformName.equalsIgnoreCase("year")) {
        Integer years = (Integer) boundValue;
        return EPOCH.plusYears(years).toInstant().toEpochMilli();
      } else if (transformName.equalsIgnoreCase("hour")) {
        Integer hours = (Integer) boundValue;
        return EPOCH.plusHours(hours).toInstant().toEpochMilli();
      }

      throw new UnsupportedOperationException(
          "Cannot extract partition bound from transform: " + transformName);
    }

    @Override
    public Long lower() {
      return lower;
    }

    @Override
    public Long upper() {
      return upper;
    }
  }

  static class DatafileRange implements Range {
    final Long lowerTimestamp;
    final Long upperTimestamp;
    final Type partitionType;
    final DataExpirationConfig config;

    public DatafileRange(
        ByteBuffer lowerBound,
        ByteBuffer upperBound,
        Type partitionType,
        DataExpirationConfig config) {
      this.config = config;
      this.partitionType = partitionType;
      this.lowerTimestamp =
          lowerBound == null ? Long.valueOf(Long.MAX_VALUE) : decodeBound(lowerBound);
      this.upperTimestamp =
          upperBound == null ? Long.valueOf(Long.MIN_VALUE) : decodeBound(upperBound);
    }

    @Override
    public boolean lowerBoundGt(long ts) {
      return lowerTimestamp > ts;
    }

    @Override
    public boolean lowerBoundGtEq(long ts) {
      return lowerTimestamp >= ts;
    }

    @Override
    public boolean upperBoundLt(long ts) {
      return upperTimestamp < ts;
    }

    @Override
    public boolean upperBoundLtEq(long ts) {
      return upperTimestamp <= ts;
    }

    @Override
    public Long decodeBound(ByteBuffer bound) {
      Object boundValue = Conversions.fromByteBuffer(partitionType, bound);

      if (boundValue instanceof Long) {
        if (partitionType.typeId() == Type.TypeID.TIMESTAMP) {
          // timestamp is stored as nanoseconds in iceberg
          return (Long) boundValue / 1000;
        } else {
          String tsUnit = config.getNumberDateFormat();
          if (tsUnit.equals(EXPIRE_TIMESTAMP_MS)) {
            return (Long) boundValue;
          } else if (tsUnit.equals(EXPIRE_TIMESTAMP_S)) {
            return (Long) boundValue * 1000;
          }
        }
      } else if (boundValue instanceof String) {
        DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(config.getDateTimePattern(), Locale.getDefault());
        LocalDateTime dateTime = LocalDateTime.parse((String) boundValue, formatter);
        return dateTime.atZone(defaultZoneId(partitionType)).toInstant().toEpochMilli();
      }

      return null;
    }

    @Override
    public Long lower() {
      return lowerTimestamp;
    }

    @Override
    public Long upper() {
      return upperTimestamp;
    }
  }

  /**
   * Sanitize the expire timestamp to match the expiration field type
   *
   * @param expireTimestamp expire timestamp in milliseconds
   * @param expirationType expiration field type
   * @param config expiration configuration
   * @return sanitized expire timestamp, null if cannot be sanitized
   */
  static Long sanitizeExpireTimestamp(
      long expireTimestamp, Type expirationType, DataExpirationConfig config) {
    switch (expirationType.typeId()) {
        // expireTimestamp is in milliseconds, TIMESTAMP type is in microseconds
      case TIMESTAMP:
        return expireTimestamp * DateTimeUtil.MICROS_PER_MILLIS;
      case LONG:
        String dateFormat = config.getNumberDateFormat();
        if (EXPIRE_TIMESTAMP_MS.equalsIgnoreCase(dateFormat)) {
          return expireTimestamp;
        } else if (EXPIRE_TIMESTAMP_S.equalsIgnoreCase(dateFormat)) {
          return expireTimestamp / 1000L;
        }
        break;
      case STRING:
        return expireTimestamp;
      default:
        throw new UnsupportedOperationException(
            "Cannot sanitize expire timestamp: "
                + expireTimestamp
                + " for type: "
                + expirationType);
    }

    throw new IllegalStateException("Cannot sanitize expire timestamp: " + expireTimestamp);
  }

  static class PartitionFieldInfo {
    final PartitionField field;
    final int index;
    final int specId;

    PartitionFieldInfo(PartitionField field, int index, int specId) {
      this.field = field;
      this.index = index;
      this.specId = specId;
    }
  }

  /**
   * Create a filter expression for expired files for the `FILE` level
   *
   * @param expireTimestamp expired timestamp
   * @return filter expression, alwaysTrue if field type is not supported
   */
  Expression buildExpirationFilter(long expireTimestamp) {
    if (expirationField().type().typeId() == Type.TypeID.TIMESTAMP) {
      return Expressions.lessThanOrEqual(
          expirationField().name(), expireTimestamp * 1000); // Convert to microseconds
    } else if (expirationField().type().typeId() == Type.TypeID.LONG) {
      if (config.getNumberDateFormat().equals(EXPIRE_TIMESTAMP_MS)) {
        return Expressions.lessThanOrEqual(expirationField().name(), expireTimestamp);
      } else if (config.getNumberDateFormat().equals(EXPIRE_TIMESTAMP_S)) {
        return Expressions.lessThanOrEqual(expirationField().name(), expireTimestamp / 1000);
      } else {
        return Expressions.alwaysTrue();
      }
    } else if (expirationField().type().typeId() == Type.TypeID.STRING) {
      String expireDateTime =
          LocalDateTime.ofInstant(
                  Instant.ofEpochMilli(expireTimestamp), defaultZoneId(expirationField().type()))
              .format(
                  DateTimeFormatter.ofPattern(config.getDateTimePattern(), Locale.getDefault()));
      return Expressions.lessThanOrEqual(expirationField().name(), expireDateTime);
    } else {
      return Expressions.alwaysTrue();
    }
  }

  // To avoid too many files to be expired in one transaction and may cause OutOfMemory issue,
  // we limit the max expiring file count in one run.
  boolean exceedMaxSize(long cachedFileCnt) {
    boolean result = cachedFileCnt >= maxExpiringFileCount();
    if (result) {
      LOG.warn(
          "Reached max expiring file count {}/{}, stop scanning more files",
          cachedFileCnt,
          maxExpiringFileCount());
    }

    return result;
  }

  static ZoneId defaultZoneId(Type type) {
    return type.typeId() == Type.TypeID.STRING ? ZoneId.systemDefault() : ZoneOffset.UTC;
  }
}
