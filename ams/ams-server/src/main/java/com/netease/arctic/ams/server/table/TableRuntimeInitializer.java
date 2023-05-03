package com.netease.arctic.ams.server.table;

import com.netease.arctic.table.ArcticTable;

public interface TableRuntimeInitializer {

  ArcticTable loadTable(ServerTableIdentifier tableIdentifier);

  TableRuntimeHandler getHeadHandler();
}
