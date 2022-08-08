package com.netease.arctic.trace;

import com.netease.arctic.AmsClient;
import com.netease.arctic.ams.api.DDLCommitMeta;
import com.netease.arctic.table.ArcticTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.DeleteFile;
import org.apache.iceberg.Schema;
import org.apache.iceberg.types.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DDLTracer implements TableTracer {

  private static final Logger LOG = LoggerFactory.getLogger(DDLTracer.class);

  private final AmsClient client;
  private final ArcticTable table;
  private final List<UpdateColumn> updateColumns = new ArrayList<>();

  public DDLTracer(ArcticTable table, AmsClient client) {
    this.client = client;
    this.table = table;
  }

  private Schema schema;

  @Override
  public void addDataFile(DataFile dataFile) {

  }

  @Override
  public void deleteDataFile(DataFile dataFile) {

  }

  @Override
  public void addDeleteFile(DeleteFile deleteFile) {

  }

  @Override
  public void deleteDeleteFile(DeleteFile deleteFile) {

  }

  @Override
  public void replaceProperties(Map<String, String> newProperties) {

  }

  @Override
  public void updateColumn(UpdateColumn updateColumn) {
    updateColumns.add(updateColumn);
  }

  @Override
  public void newSchema(Schema schema) {
    this.schema = schema;
  }

  @Override
  public void commit() {
    int schemaId = schema.schemaId();
    DDLCommitMeta ddlCommitMeta = new DDLCommitMeta();
    ddlCommitMeta.setSchemaId(schemaId);
    ddlCommitMeta.setTableIdentifier(table.id().buildTableIdentifier());
    List<com.netease.arctic.ams.api.UpdateColumn> commitUpdateColumns =
        updateColumns.stream().map(DDLTracer::covert).collect(Collectors.toList());
    ddlCommitMeta.setUpdateColumns(commitUpdateColumns);
    try {
      client.ddlCommit(ddlCommitMeta);
    } catch (Throwable t) {
      LOG.warn("trace table commit ddl failed", t);
    }
  }

  private static com.netease.arctic.ams.api.UpdateColumn covert(UpdateColumn updateColumn) {
    com.netease.arctic.ams.api.UpdateColumn commit = new com.netease.arctic.ams.api.UpdateColumn();
    commit.setName(updateColumn.getName());
    commit.setParent(updateColumn.getParent());
    commit.setType(updateColumn.getType() == null ? null : updateColumn.getType().toString());
    commit.setDoc(updateColumn.getDoc());
    commit.setOperate(updateColumn.getOperate().name());
    commit.setIsOptional(updateColumn.getOptional() == null ? null : updateColumn.getOptional().toString());
    commit.setNewName(updateColumn.getNewName());
    return commit;
  }

  public enum SchemaOperateType {
    ADD,
    DROP,
    ALERT,
    RENAME,
    MOVE_BEFORE,
    MOVE_AFTER,
    MOVE_FIRST
  }

  public enum PropertiesOPType {
    SET,
    UNSET
  }

  public static class UpdateColumn {
    private final String parent;
    private final String name;
    private final Type type;
    private final String doc;
    private final SchemaOperateType operate;
    private final Boolean isOptional;
    private final String newName;

    public UpdateColumn(
        String name,
        String parent,
        Type type,
        String doc,
        SchemaOperateType operate,
        Boolean isOptional,
        String newName) {
      this.parent = parent;
      this.name = name;
      this.type = type;
      this.doc = doc;
      this.operate = operate;
      this.isOptional = isOptional;
      this.newName = newName;
    }

    public String getParent() {
      return parent;
    }

    public String getName() {
      return name;
    }

    public Type getType() {
      return type;
    }

    public String getDoc() {
      return doc;
    }

    public SchemaOperateType getOperate() {
      return operate;
    }

    public Boolean getOptional() {
      return isOptional;
    }

    public String getNewName() {
      return newName;
    }
  }
}
