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

package com.netease.arctic.ams.server.service;

import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.handler.impl.ArcticTableMetastoreHandler;
import com.netease.arctic.ams.server.handler.impl.OptimizeManagerHandler;
import com.netease.arctic.ams.server.optimize.IOptimizeService;
import com.netease.arctic.ams.server.optimize.OptimizeService;
import com.netease.arctic.ams.server.service.impl.AdaptHiveService;
import com.netease.arctic.ams.server.service.impl.ArcticTransactionService;
import com.netease.arctic.ams.server.service.impl.CatalogMetadataService;
import com.netease.arctic.ams.server.service.impl.ContainerMetaService;
import com.netease.arctic.ams.server.service.impl.DDLTracerService;
import com.netease.arctic.ams.server.service.impl.FileInfoCacheService;
import com.netease.arctic.ams.server.service.impl.JDBCMetaService;
import com.netease.arctic.ams.server.service.impl.OptimizeExecuteService;
import com.netease.arctic.ams.server.service.impl.OptimizeQueueService;
import com.netease.arctic.ams.server.service.impl.OptimizerService;
import com.netease.arctic.ams.server.service.impl.OrphanFilesCleanService;
import com.netease.arctic.ams.server.service.impl.PlatformFileInfoService;
import com.netease.arctic.ams.server.service.impl.QuotaService;
import com.netease.arctic.ams.server.service.impl.RuntimeDataExpireService;
import com.netease.arctic.ams.server.service.impl.SupportHiveSyncService;
import com.netease.arctic.ams.server.service.impl.TableBaseInfoService;
import com.netease.arctic.ams.server.service.impl.TableBlockerService;
import com.netease.arctic.ams.server.service.impl.TableExpireService;
import com.netease.arctic.ams.server.service.impl.TableTaskHistoryService;
import com.netease.arctic.ams.server.service.impl.TrashCleanService;
import com.netease.arctic.ams.server.terminal.TerminalManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

public class ServiceContainer {
  private static final Logger LOG = LoggerFactory.getLogger(ServiceContainer.class);

  private static volatile IOptimizeService optimizeService;

  private static volatile ITableExpireService tableExpireService;

  private static volatile IOrphanFilesCleanService orphanFilesCleanService;

  private static volatile TrashCleanService trashCleanService;

  private static volatile OptimizeQueueService optimizeQueueService;

  private static volatile IMetaService metaService;

  private static volatile IQuotaService quotaService;
  private static volatile OptimizeExecuteService optimizeExecuteService;

  private static volatile OptimizeManagerHandler optimizeManagerHandler;

  private static volatile OptimizerService optimizerService;

  private static volatile ContainerMetaService containerMetaService;

  private static volatile CatalogMetadataService catalogMetadataService;

  private static volatile FileInfoCacheService fileInfoCacheService;

  private static volatile ITableTaskHistoryService tableTaskHistoryService;

  private static volatile ArcticTransactionService arcticTransactionService;

  private static volatile ITableInfoService tableInfoService;

  private static volatile ArcticTableMetastoreHandler tableMetastoreHandler;

  private static volatile DDLTracerService ddlTracerService;

  private static volatile RuntimeDataExpireService runtimeDataExpireService;

  private static volatile AdaptHiveService adaptHiveService;

  private static volatile ISupportHiveSyncService supportHiveSyncService;

  private static volatile TerminalManager terminalManager;

  private static volatile PlatformFileInfoService platformFileInfoService;

  private static volatile TableBlockerService tableBlockerService;
  
  private static volatile boolean closed = false;

  public static IOptimizeService getOptimizeService() {
    checkNotClosed();
    if (optimizeService == null) {
      synchronized (ServiceContainer.class) {
        if (optimizeService == null) {
          optimizeService = new OptimizeService();
        }
      }
    }

    return optimizeService;
  }

  public static ITableExpireService getTableExpireService() {
    checkNotClosed();
    if (tableExpireService == null) {
      synchronized (ServiceContainer.class) {
        if (tableExpireService == null) {
          tableExpireService = new TableExpireService();
        }
      }
    }

    return tableExpireService;
  }

  public static IOrphanFilesCleanService getOrphanFilesCleanService() {
    checkNotClosed();
    if (orphanFilesCleanService == null) {
      synchronized (ServiceContainer.class) {
        if (orphanFilesCleanService == null) {
          orphanFilesCleanService = new OrphanFilesCleanService();
        }
      }
    }

    return orphanFilesCleanService;
  }

  public static TrashCleanService getTrashCleanService() {
    checkNotClosed();
    if (trashCleanService == null) {
      synchronized (ServiceContainer.class) {
        if (trashCleanService == null) {
          trashCleanService = new TrashCleanService();
        }
      }
    }

    return trashCleanService;
  }

  public static OptimizerService getOptimizerService() {
    checkNotClosed();
    if (optimizerService == null) {
      synchronized (ServiceContainer.class) {
        if (optimizerService == null) {
          optimizerService = new OptimizerService();
        }
      }
    }
    return optimizerService;
  }

  public static OptimizeManagerHandler getOptimizeManagerHandler() {
    checkNotClosed();
    if (optimizeManagerHandler == null) {
      synchronized (ServiceContainer.class) {
        if (optimizeManagerHandler == null) {
          optimizeManagerHandler = new OptimizeManagerHandler();
        }
      }
    }
    return optimizeManagerHandler;
  }

  public static OptimizeQueueService getOptimizeQueueService() {
    checkNotClosed();
    if (optimizeQueueService == null) {
      synchronized (ServiceContainer.class) {
        if (optimizeQueueService == null) {
          optimizeQueueService = new OptimizeQueueService();
        }
      }
    }

    return optimizeQueueService;
  }

  public static IMetaService getMetaService() {
    checkNotClosed();
    if (metaService == null) {
      synchronized (ServiceContainer.class) {
        if (metaService == null) {
          metaService = new JDBCMetaService();
        }
      }
    }

    return metaService;
  }

  public static IQuotaService getQuotaService() {
    checkNotClosed();
    if (quotaService == null) {
      synchronized (ServiceContainer.class) {
        if (quotaService == null) {
          quotaService = new QuotaService(getTableTaskHistoryService(), getMetaService());
        }
      }
    }

    return quotaService;
  }

  public static CatalogMetadataService getCatalogMetadataService() {
    checkNotClosed();
    if (catalogMetadataService == null) {
      synchronized (ServiceContainer.class) {
        if (catalogMetadataService == null) {
          catalogMetadataService = new CatalogMetadataService();
        }
      }
    }

    return catalogMetadataService;
  }

  public static FileInfoCacheService getFileInfoCacheService() {
    checkNotClosed();
    if (fileInfoCacheService == null) {
      synchronized (ServiceContainer.class) {
        if (fileInfoCacheService == null) {
          fileInfoCacheService = new FileInfoCacheService();
        }
      }
    }

    return fileInfoCacheService;
  }

  public static ArcticTransactionService getArcticTransactionService() {
    checkNotClosed();
    if (arcticTransactionService == null) {
      synchronized (ServiceContainer.class) {
        if (arcticTransactionService == null) {
          arcticTransactionService = new ArcticTransactionService();
        }
      }
    }

    return arcticTransactionService;
  }

  public static ITableTaskHistoryService getTableTaskHistoryService() {
    checkNotClosed();
    if (tableTaskHistoryService == null) {
      synchronized (ServiceContainer.class) {
        if (tableTaskHistoryService == null) {
          tableTaskHistoryService = new TableTaskHistoryService();
        }
      }
    }

    return tableTaskHistoryService;
  }

  public static ITableInfoService getTableInfoService() {
    checkNotClosed();
    if (tableInfoService == null) {
      synchronized (ServiceContainer.class) {
        if (tableInfoService == null) {
          tableInfoService = new TableBaseInfoService();
        }
      }
    }
    return tableInfoService;
  }

  public static ArcticTableMetastoreHandler getTableMetastoreHandler() {
    checkNotClosed();
    if (tableMetastoreHandler == null) {
      synchronized (ServiceContainer.class) {
        if (tableMetastoreHandler == null) {
          tableMetastoreHandler = new ArcticTableMetastoreHandler(getMetaService());
        }
      }
    }
    return tableMetastoreHandler;
  }

  public static RuntimeDataExpireService getRuntimeDataExpireService() {
    checkNotClosed();
    if (runtimeDataExpireService == null) {
      synchronized (ServiceContainer.class) {
        if (runtimeDataExpireService == null) {
          runtimeDataExpireService = new RuntimeDataExpireService();
        }
      }
    }
    return runtimeDataExpireService;
  }

  public static AdaptHiveService getAdaptHiveService() {
    checkNotClosed();
    if (adaptHiveService == null) {
      synchronized (AdaptHiveService.class) {
        if (adaptHiveService == null) {
          adaptHiveService = new AdaptHiveService();
        }
      }
    }
    return adaptHiveService;
  }

  public static ISupportHiveSyncService getSupportHiveSyncService() {
    checkNotClosed();
    if (supportHiveSyncService == null) {
      synchronized (ServiceContainer.class) {
        if (supportHiveSyncService == null) {
          supportHiveSyncService = new SupportHiveSyncService();
        }
      }
    }

    return supportHiveSyncService;
  }

  public static DDLTracerService getDdlTracerService() {
    checkNotClosed();
    if (ddlTracerService == null) {
      synchronized (ServiceContainer.class) {
        if (ddlTracerService == null) {
          ddlTracerService = new DDLTracerService();
        }
      }
    }

    return ddlTracerService;
  }

  public static TerminalManager getTerminalManager() {
    checkNotClosed();
    if (terminalManager == null) {
      synchronized (ServiceContainer.class) {
        if (terminalManager == null) {
          terminalManager = new TerminalManager(ArcticMetaStore.conf);
        }
      }
    }
    return terminalManager;
  }

  public static OptimizeExecuteService getOptimizeExecuteService() {
    checkNotClosed();
    if (optimizeExecuteService == null) {
      synchronized (ServiceContainer.class) {
        if (optimizeExecuteService == null) {
          optimizeExecuteService = new OptimizeExecuteService();
        }
      }
    }
    return optimizeExecuteService;
  }

  public static ContainerMetaService getContainerMetaService() {
    checkNotClosed();
    if (containerMetaService == null) {
      synchronized (ServiceContainer.class) {
        if (containerMetaService == null) {
          containerMetaService = new ContainerMetaService();
        }
      }
    }
    return containerMetaService;
  }

  public static PlatformFileInfoService getPlatformFileInfoService() {
    checkNotClosed();
    if (platformFileInfoService == null) {
      synchronized (ServiceContainer.class) {
        if (platformFileInfoService == null) {
          platformFileInfoService = new PlatformFileInfoService();
        }
      }
    }
    return platformFileInfoService;
  }

  public static TableBlockerService getTableBlockerService() {
    checkNotClosed();
    if (tableBlockerService == null) {
      synchronized (ServiceContainer.class) {
        if (tableBlockerService == null) {
          tableBlockerService = new TableBlockerService(ArcticMetaStore.conf);
        }
      }
    }
    return tableBlockerService;
  }

  private static void checkNotClosed() {
    if (closed) {
      throw new IllegalStateException("ServiceContainer has been closed");
    }
  }

  private static void close(Closeable closeable) {
    if (closeable == null) {
      return;
    }
    try {
      closeable.close();
    } catch (IOException e) {
      LOG.warn("failed to close {}, ignore", closeable);
    }
  }
  
  public static void reset() {
    clear();
    closed = false;
  }

  public static void clear() {
    closed = true;

    close(optimizeService);
    optimizeService = null;

    close(tableExpireService);
    tableExpireService = null;

    close(orphanFilesCleanService);
    orphanFilesCleanService = null;

    close(orphanFilesCleanService);
    orphanFilesCleanService = null;

    close(trashCleanService);
    trashCleanService = null;

    close(optimizeQueueService);
    optimizeQueueService = null;

    close(metaService);
    metaService = null;

    close(quotaService);
    quotaService = null;

    close(optimizeExecuteService);
    optimizeExecuteService = null;

    close(optimizeManagerHandler);
    optimizeManagerHandler = null;

    close(optimizerService);
    optimizerService = null;

    close(containerMetaService);
    containerMetaService = null;

    close(catalogMetadataService);
    catalogMetadataService = null;

    close(fileInfoCacheService);
    fileInfoCacheService = null;

    close(tableTaskHistoryService);
    tableTaskHistoryService = null;

    close(arcticTransactionService);
    arcticTransactionService = null;

    close(tableInfoService);
    tableInfoService = null;

    close(tableMetastoreHandler);
    tableMetastoreHandler = null;

    close(ddlTracerService);
    ddlTracerService = null;

    close(runtimeDataExpireService);
    runtimeDataExpireService = null;

    close(adaptHiveService);
    adaptHiveService = null;

    close(supportHiveSyncService);
    supportHiveSyncService = null;

    close(terminalManager);
    terminalManager = null;

    close(platformFileInfoService);
    platformFileInfoService = null;

    close(tableBlockerService);
    tableBlockerService = null;
  }
}
