package com.netease.arctic.ams.server.service.impl;

import com.netease.arctic.ams.api.DDLCommitMeta;
import com.netease.arctic.ams.api.TableIdentifier;
import com.netease.arctic.ams.api.UpdateColumn;
import com.netease.arctic.ams.server.mapper.DDLRecordMapper;
import com.netease.arctic.ams.server.model.DDLInfo;
import com.netease.arctic.ams.server.model.TableMetadata;
import com.netease.arctic.ams.server.service.IJDBCService;
import com.netease.arctic.ams.server.service.ServiceContainer;
import com.netease.arctic.ams.server.utils.TableMetadataUtil;
import com.netease.arctic.catalog.ArcticCatalog;
import com.netease.arctic.catalog.CatalogLoader;
import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.trace.DDLTracer;
import org.apache.ibatis.session.SqlSession;
import org.apache.iceberg.Schema;
import org.apache.iceberg.Table;
import org.apache.iceberg.types.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DDLTracerService extends IJDBCService {

  private static final Logger LOG = LoggerFactory.getLogger(DDLTracerService.class);

  private static final String ALTER_TABLE = "ALTER TABLE %s ";
  private static final String ADD_COLUMN = " ADD COLUMN ";
  private static final String ALTER_COLUMN = " ALTER COLUMN %s ";
  private static final String MOVE_FIRST = " ALTER COLUMN %s FIRST ";
  private static final String MOVE_AFTER_COLUMN = "ALTER COLUMN %s AFTER %s";
  private static final String RENAME_COLUMN = "RENAME COLUMN %s TO %s ";
  private static final String DROP_COLUMNS = "DROP COLUMN %s";
  private static final String SET_PROPERTIES = " SET TBLPROPERTIES (%s)";
  private static final String UNSET_PROPERTIES = " UNSET TBLPROPERTIES (%s)";
  private static final String IS_OPTIONAL = " DROP NOT NULL ";
  private static final String NOT_OPTIONAL = " SET NOT NULL ";
  private static final String DOC = " COMMENT %s";
  private static final String TYPE = " TYPE %s ";

  public void commit(DDLCommitMeta commitMeta) {
    TableIdentifier tableIdentifier = commitMeta.getTableIdentifier();
    Long commitTime = System.currentTimeMillis();
    int schemaId = commitMeta.getSchemaId();
    ArcticCatalog catalog =
        CatalogLoader.load(ServiceContainer.getTableMetastoreHandler(), tableIdentifier.getCatalog());
    com.netease.arctic.table.TableIdentifier tmp = com.netease.arctic.table.TableIdentifier.of(
        tableIdentifier.getCatalog(),
        tableIdentifier.getDatabase(),
        tableIdentifier.getTableName());
    ArcticTable arcticTable = catalog.loadTable(tmp);
    Table table = arcticTable.isKeyedTable() ? arcticTable.asKeyedTable().baseTable() : arcticTable.asUnkeyedTable();
    StringBuilder schemaSql = new StringBuilder();
    for (UpdateColumn updateColumn : commitMeta.getUpdateColumns()) {
      String operateType = updateColumn.getOperate();
      StringBuilder sql =
          new StringBuilder(String.format(ALTER_TABLE, TableMetadataUtil.getTableAllIdentifyName(tableIdentifier)));
      switch (DDLTracer.SchemaOperateType.valueOf(operateType)) {
        case ADD:
          sql.append(ADD_COLUMN).append(updateColumn.getName());
          if (updateColumn.getType() != null) {
            sql.append(" ").append(updateColumn.getType()).append(" ");
          }
          if (updateColumn.getDoc() != null) {
            sql.append(String.format(DOC, updateColumn.getDoc()));
          }
          break;
        case DROP:
          sql.append(String.format(DROP_COLUMNS, updateColumn.getName()));
          break;
        case RENAME:
          sql.append(String.format(RENAME_COLUMN, updateColumn.getName(), updateColumn.getNewName()));
          break;
        case ALERT:
          sql.append(String.format(ALTER_COLUMN, updateColumn.getName()));
          if (updateColumn.getType() != null) {
            sql.append(String.format(TYPE, updateColumn.getType()));
          }
          if (updateColumn.getDoc() != null) {
            sql.append(String.format(DOC, updateColumn.getDoc()));
          }
          if (updateColumn.getIsOptional() != null) {
            if (Boolean.parseBoolean(updateColumn.getIsOptional())) {
              sql.append(IS_OPTIONAL);
            } else {
              sql.append(NOT_OPTIONAL);
            }
          }
          break;
        case MOVE_BEFORE:
          Integer referencePosition = null;
          Schema schema = table.schemas().get(schemaId);
          for (int i = 0; i < schema.columns().size(); i++) {
            Types.NestedField field = schema.columns().get(i);
            if (field.name().equals(updateColumn.getName())) {
              referencePosition = i - 1;
            }
          }
          if (referencePosition != null) {
            if (referencePosition == -1) {
              sql.append(String.format(MOVE_FIRST, updateColumn.getName()));
            } else {
              String referenceName = schema.columns().get(referencePosition).name();
              sql.append(String.format(MOVE_AFTER_COLUMN, updateColumn.getName(), referenceName));
            }
          }
          break;
        case MOVE_AFTER:
          sql.append(String.format(MOVE_AFTER_COLUMN, updateColumn.getName(), updateColumn.getNewName()));
          break;
        case MOVE_FIRST:
          sql.append(String.format(MOVE_FIRST, updateColumn.getName()));
          break;
        default:
          break;
      }
      if (sql.length() > 0) {
        sql.append(";\\n");
        schemaSql.append(sql);
      }
    }
    DDLInfo ddlInfo =
        DDLInfo.of(tableIdentifier, schemaSql.toString(), schemaId, DDLType.UPDATE_SCHEMA.name(), commitTime);
    insert(ddlInfo);
  }

  public void commitProperties(TableIdentifier tableIdentifier, Map<String, String> before, Map<String, String> after) {
    String tableName = TableMetadataUtil.getTableAllIdentifyName(tableIdentifier);
    Long commitTime = System.currentTimeMillis();
    StringBuilder setSql = new StringBuilder();
    StringBuilder unsetSql = new StringBuilder();
    StringBuilder unsetPro = new StringBuilder();
    for (String oldPro : before.keySet()) {
      if (!after.containsKey(oldPro)) {
        unsetPro.append(oldPro).append(",").append("\\n");
      }
    }
    StringBuilder setPro = new StringBuilder();
    for (String newPro : after.keySet()) {
      if (!before.containsKey(newPro)) {
        setPro.append(String.format("'%s'='%s',", newPro, after.get(newPro))).append("\\n");
      }
    }
    if (setPro.length() > 0) {
      setSql.append(String.format(ALTER_TABLE, tableName))
          .append(String.format(SET_PROPERTIES, setPro.deleteCharAt(setPro.length())));
    }
    if (unsetPro.length() > 0) {
      unsetSql.append(String.format(ALTER_TABLE, tableName))
          .append(String.format(UNSET_PROPERTIES, unsetPro.deleteCharAt(unsetPro.length())));
    }
    if (setSql.length() > 0) {
      DDLInfo ddlInfo =
          DDLInfo.of(tableIdentifier, setSql.toString(), null, DDLType.UPDATE_PROPERTIES.name(), commitTime);
      insert(ddlInfo);
    }
    if (unsetSql.length() > 0) {
      DDLInfo ddlInfo =
          DDLInfo.of(tableIdentifier, unsetSql.toString(), null, DDLType.UPDATE_PROPERTIES.name(), commitTime);
      insert(ddlInfo);
      insert(ddlInfo);
    }
  }

  public Integer getCurrentSchemaId(TableIdentifier identifier) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      DDLRecordMapper ddlRecordMapper = getMapper(sqlSession, DDLRecordMapper.class);
      List<Integer> ids = ddlRecordMapper.getCurrentSchemaId(identifier);
      return ids.get(0) == null ? 0 : ids.get(0);
    }
  }

  public enum DDLType {
    UPDATE_SCHEMA,
    UPDATE_PROPERTIES
  }

  public void insert(DDLInfo ddlInfo) {
    try (SqlSession sqlSession = getSqlSession(true)) {
      DDLRecordMapper ddlRecordMapper = getMapper(sqlSession, DDLRecordMapper.class);
      ddlRecordMapper.insert(ddlInfo);
    }
  }

  public static class DDLSyncTask {

    public void doTask() {
      LOG.info("start execute syncDDl");
      List<TableMetadata> tableMetadata = ServiceContainer.getMetaService().listTables();
      tableMetadata.forEach(meta -> {
        if (meta.getTableIdentifier() == null) {
          return;
        }
        TableIdentifier tableIdentifier = new TableIdentifier();
        tableIdentifier.catalog = meta.getTableIdentifier().getCatalog();
        tableIdentifier.database = meta.getTableIdentifier().getDatabase();
        tableIdentifier.tableName = meta.getTableIdentifier().getTableName();
        syncDDl(tableIdentifier);
      });
    }

    public void syncDDl(TableIdentifier identifier) {
      ArcticCatalog catalog = CatalogLoader.load(ServiceContainer.getTableMetastoreHandler(), identifier.getCatalog());
      com.netease.arctic.table.TableIdentifier tmp = com.netease.arctic.table.TableIdentifier.of(
          identifier.getCatalog(),
          identifier.getDatabase(),
          identifier.getTableName());
      ArcticTable arcticTable = catalog.loadTable(tmp);
      Table table = arcticTable.isKeyedTable() ? arcticTable.asKeyedTable().baseTable() : arcticTable.asUnkeyedTable();
      table.refresh();
      int cacheSchemaId = ServiceContainer.getDdlTracerService().getCurrentSchemaId(identifier);
      Map<Integer, Schema> allSchemas = table.schemas();
      List<Integer> newSchemas =
          allSchemas.keySet().stream().filter(e -> e > cacheSchemaId).sorted().collect(Collectors.toList());

      Schema cacheSchema = allSchemas.get(cacheSchemaId);
      for (int id : newSchemas) {
        Long commitTime = System.currentTimeMillis();
        StringBuilder sql = new StringBuilder();
        sql.append(compareSchema(identifier, cacheSchema, allSchemas.get(id)));
        cacheSchema = allSchemas.get(id);
        if (sql.length() > 0) {
          DDLInfo ddlInfo =
              DDLInfo.of(identifier, sql.toString(), id, DDLType.UPDATE_SCHEMA.name(), commitTime);
          ServiceContainer.getDdlTracerService().insert(ddlInfo);
        }
      }
    }

    private String compareSchema(TableIdentifier identifier, Schema before, Schema after) {
      StringBuilder rs = new StringBuilder();
      String tableName = TableMetadataUtil.getTableAllIdentifyName(identifier);
      for (Types.NestedField field : before.columns()) {
        StringBuilder sb = new StringBuilder();
        if (after.findField(field.fieldId()) == null) {
          // drop col
          sb.append(String.format(ALTER_TABLE, tableName));
          sb.append(String.format(DROP_COLUMNS, field.name()));
        }
        if (sb.length() > 0) {
          rs.append(sb).append(";").append("\\n");
        }
      }

      for (int i = 0; i < after.columns().size(); i++) {
        Types.NestedField field = after.columns().get(i);
        StringBuilder sb = new StringBuilder();
        if (before.findField(field.fieldId()) == null) {
          // add col
          sb.append(String.format(ALTER_TABLE, tableName));
          sb.append(ADD_COLUMN);
          sb.append(field.name()).append(" ");
          sb.append(field.type().toString()).append(" ");
          if (field.doc() != null) {
            sb.append(String.format(DOC, field.doc()));
          }
          if (i == 0) {
            sb.append(" FIRST");
          } else if (i < before.columns().size()) {
            sb.append(" AFTER ").append(before.columns().get(i - 1).name());
          }
        } else if (!before.findField(field.fieldId()).equals(field)) {
          sb.append(String.format(ALTER_TABLE, tableName));
          //alter col
          Types.NestedField oldField = before.findField(field.fieldId());
          //rename
          if (!oldField.name().equals(field.name())) {
            sb.append(String.format(RENAME_COLUMN, oldField.name(), field.name()));
          } else {
            sb.append(String.format(ALTER_COLUMN, oldField.name()));

            if (!oldField.type().equals(field.type())) {
              sb.append(String.format(TYPE, field.type().toString()));
            }

            if (!Objects.equals(oldField.doc(), field.doc())) {
              if (field.doc() != null) {
                sb.append(String.format(DOC, field.doc()));
              }
            }

            if (oldField.isOptional() != field.isOptional()) {
              if (field.isOptional()) {
                sb.append(IS_OPTIONAL);
              } else {
                sb.append(NOT_OPTIONAL);
              }
            }
          }
        }
        if (sb.length() > 0) {
          rs.append(sb).append(";").append("\\n");
        }
      }
      return rs.toString();
    }
  }
}
