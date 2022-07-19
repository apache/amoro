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

package com.netease.arctic.data;

import com.netease.arctic.table.MetadataColumns;
import com.netease.arctic.utils.FileUtil;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.FileFormat;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.types.Conversions;
import org.apache.iceberg.types.Types;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link PrimaryKeyedFile}, wrapping a {@link DataFile} and parsing extra information from
 * file name.
 */
public class DefaultKeyedFile implements PrimaryKeyedFile, Serializable {

  public static final String FILE_NAME_PATTERN_STRING = "(\\d+)-(\\w+)-(\\d+)-(\\d+)-(\\d+)-(\\d+)\\.\\w+";
  private static final Pattern FILE_NAME_PATTERN = Pattern.compile(FILE_NAME_PATTERN_STRING);

  private final DataFile internalFile;

  private transient FileMeta meta;
  private transient ChangedLsn minLsn;
  private transient ChangedLsn maxLsn;

  public DefaultKeyedFile(DataFile internalFile) {
    this.internalFile = internalFile;
  }

  private void parse() {
    meta = parseMetaFromFileName(FileUtil.getFileName(internalFile.path().toString()));
    if (internalFile.lowerBounds() != null) {
      ByteBuffer minOffsetBuffer = internalFile.lowerBounds().get(MetadataColumns.FILE_OFFSET_FILED_ID);
      if (minOffsetBuffer != null) {
        minLsn = ChangedLsn.of(meta.transactionId(), Conversions.fromByteBuffer(Types.LongType.get(), minOffsetBuffer));
      } else {
        minLsn = ChangedLsn.of(meta.transactionId(), Long.MAX_VALUE);
      }
    }
    if (internalFile.upperBounds() != null) {
      ByteBuffer maxOffsetBuffer = internalFile.upperBounds().get(MetadataColumns.FILE_OFFSET_FILED_ID);
      if (maxOffsetBuffer != null) {
        maxLsn = ChangedLsn.of(meta.transactionId(), Conversions.fromByteBuffer(Types.LongType.get(), maxOffsetBuffer));
      } else {
        maxLsn = ChangedLsn.of(meta.transactionId(), Long.MAX_VALUE);
      }
    }
  }

  @Override
  public Long transactionId() {
    if (meta == null) {
      parse();
    }
    return meta.transactionId();
  }

  @Override
  public ChangedLsn minLsn() {
    if (minLsn == null) {
      parse();
    }
    return minLsn;
  }

  @Override
  public ChangedLsn maxLsn() {
    if (maxLsn == null) {
      parse();
    }
    return maxLsn;
  }

  @Override
  public DataFileType type() {
    if (meta == null) {
      parse();
    }
    return meta.type();
  }

  @Override
  public Long pos() {
    return internalFile.pos();
  }

  @Override
  public int specId() {
    return internalFile.specId();
  }

  @Override
  public CharSequence path() {
    return internalFile.path();
  }

  @Override
  public FileFormat format() {
    return internalFile.format();
  }

  @Override
  public long recordCount() {
    return internalFile.recordCount();
  }

  @Override
  public long fileSizeInBytes() {
    return internalFile.fileSizeInBytes();
  }

  @Override
  public Map<Integer, Long> columnSizes() {
    return internalFile.columnSizes();
  }

  @Override
  public Map<Integer, Long> valueCounts() {
    return internalFile.valueCounts();
  }

  @Override
  public Map<Integer, Long> nullValueCounts() {
    return internalFile.nullValueCounts();
  }

  @Override
  public Map<Integer, Long> nanValueCounts() {
    return internalFile.nanValueCounts();
  }

  @Override
  public Map<Integer, ByteBuffer> lowerBounds() {
    return internalFile.lowerBounds();
  }

  @Override
  public Map<Integer, ByteBuffer> upperBounds() {
    return internalFile.upperBounds();
  }

  @Override
  public ByteBuffer keyMetadata() {
    return internalFile.keyMetadata();
  }

  @Override
  public List<Long> splitOffsets() {
    return internalFile.splitOffsets();
  }

  @Override
  public DataFile copy() {
    return new DefaultKeyedFile(internalFile.copy());
  }

  @Override
  public DataFile copyWithoutStats() {
    return new DefaultKeyedFile(internalFile.copyWithoutStats());
  }

  @Override
  public StructLike partition() {
    return internalFile.partition();
  }

  @Override
  public long mask() {
    if (meta == null) {
      parse();
    }
    return meta.node().mask();
  }

  @Override
  public long index() {
    if (meta == null) {
      parse();
    }
    return meta.node().index();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DefaultKeyedFile that = (DefaultKeyedFile) o;
    return Objects.equals(internalFile.path(), that.internalFile.path());
  }

  @Override
  public int hashCode() {
    return Objects.hash(internalFile.path());
  }

  public static FileMeta parseMetaFromFileName(String fileName) {
    fileName = FileUtil.getFileName(fileName);
    Matcher matcher = FILE_NAME_PATTERN.matcher(fileName);
    long nodeId = 1;
    DataFileType type = DataFileType.BASE_FILE;
    long transactionId = 0L;
    if (matcher.matches()) {
      nodeId = Long.parseLong(matcher.group(1));
      type = DataFileType.ofShortName(matcher.group(2));
      transactionId = Long.parseLong(matcher.group(3));
    }
    DataTreeNode node = DataTreeNode.ofId(nodeId);
    return new FileMeta(transactionId, type, node);
  }

  public static class FileMeta {
    private final long transactionId;
    private final DataFileType type;
    private final DataTreeNode node;

    public FileMeta(Long transactionId, DataFileType type, DataTreeNode node) {
      this.transactionId = transactionId;
      this.type = type;
      this.node = node;
    }

    public long transactionId() {
      return transactionId;
    }

    public DataFileType type() {
      return type;
    }

    public DataTreeNode node() {
      return node;
    }
  }
}
