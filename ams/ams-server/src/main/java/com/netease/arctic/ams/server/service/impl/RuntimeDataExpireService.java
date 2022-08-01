package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.server.ArcticMetaStore;
import com.netease.arctic.ams.server.config.ArcticMetaStoreConf;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.service.IMetaService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.table.TableIdentifier;
import java.util.List;

public class RuntimeDataExpireService {
  private final FileInfoCacheService fileInfoCacheService;
  private final ArcticTransactionService transactionService;
  private final IMetaService metaService;

  Long txDataExpireInterval = 24 * 60 * 60 * 1000L;

  public RuntimeDataExpireService() {
    this.fileInfoCacheService = ServiceContainer.getFileInfoCacheService();
    this.transactionService = ServiceContainer.getArcticTransactionService();
    this.metaService = ServiceContainer.getMetaService();
  }

  public void doExpire() {
    List<TableMetadata> tableMetadata = metaService.listTables();
    tableMetadata.forEach(meta -> {
      TableIdentifier identifier = meta.getTableIdentifier();
      fileInfoCacheService.expiredCache(
          System.currentTimeMillis() - ArcticMetaStore.conf.getLong(ArcticMetaStoreConf.FILE_CACHE_EXPIRED_INTERVAL),
          identifier);
      transactionService.expire(
          identifier.buildTableIdentifier(),
          System.currentTimeMillis() - this.txDataExpireInterval);
    });
  }
}
