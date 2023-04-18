package com.netease.arctic.io;

import com.netease.arctic.table.TableMetaStore;
import com.netease.arctic.utils.TableFileUtils;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.iceberg.io.InputFile;
import org.apache.iceberg.io.OutputFile;
import org.apache.iceberg.relocated.com.google.common.annotations.VisibleForTesting;
import org.apache.iceberg.relocated.com.google.common.base.Preconditions;
import org.apache.iceberg.relocated.com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Implementation of {@link ArcticFileIO} with deleted files recovery support.
 */
public class RecoverableArcticFileIO extends ArcticHadoopFileIO implements SupportFileRecycleOperations {
  private static final Logger LOG = LoggerFactory.getLogger(RecoverableArcticFileIO.class);
  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

  private final TableTrashManager trashManager;
  private final String trashFilePattern;
  private final Pattern pattern;

  RecoverableArcticFileIO(
      TableMetaStore tableMetaStore,
      TableTrashManager trashManager,
      String trashFilePattern) {
    super(tableMetaStore);
    this.trashManager = trashManager;
    this.trashFilePattern = trashFilePattern;
    this.pattern = Strings.isNullOrEmpty(this.trashFilePattern) ? null : Pattern.compile(this.trashFilePattern);
  }

  @Override
  public void deleteFile(String path) {
    if (matchTrashFilePattern(path)) {
      moveToTrash(path);
    } else {
      super.deleteFile(path);
    }
  }

  @Override
  public void deleteFile(InputFile file) {
    if (matchTrashFilePattern(file.location())) {
      moveToTrash(file.location());
    } else {
      super.deleteFile(file);
    }
  }

  @Override
  public void deleteFile(OutputFile file) {
    if (matchTrashFilePattern(file.location())) {
      moveToTrash(file.location());
    } else {
      super.deleteFile(file);
    }
  }

  @VisibleForTesting
  protected boolean matchTrashFilePattern(String path) {
    return pattern.matcher(path).matches();
  }

  @VisibleForTesting
  public TableTrashManager getTrashManager() {
    return trashManager;
  }

  public String getTrashFilePattern() {
    return trashFilePattern;
  }

  private void moveToTrash(String filePath) {
    trashManager.moveFileToTrash(filePath);
    LOG.debug("Move file:{} to table trash", filePath);
  }

  @Override
  public boolean fileRecoverable(String path) {
    return this.trashManager.fileExistInTrash(path);
  }

  @Override
  public boolean recover(String path) {
    return this.trashManager.restoreFileFromTrash(path);
  }

  @Override
  public void expireRecycle(LocalDate expirationDate) {
    this.trashManager.cleanFiles(expirationDate);
  }
}
