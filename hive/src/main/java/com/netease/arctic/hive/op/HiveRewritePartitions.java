package com.netease.arctic.hive.op;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.netease.arctic.hive.CachedHiveClientPool;
import com.netease.arctic.op.KeyedPartitionRewrite;
import com.netease.arctic.table.KeyedTable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.PartitionKey;
import org.apache.iceberg.StructLike;
import org.apache.iceberg.Transaction;
import org.apache.iceberg.types.Type;
import org.apache.iceberg.types.Types;
import org.apache.iceberg.util.StructLikeMap;
import org.apache.thrift.TException;
import org.mortbay.util.StringUtil;

public class HiveRewritePartitions extends KeyedPartitionRewrite {
  private CachedHiveClientPool client;
  private final String db;
  private final String tableName;
  private final KeyedTable keyedTable;

  private List<Partition> rewritePartitions;
  private String hiveLocation ;

  public HiveRewritePartitions(KeyedTable keyedTable, CachedHiveClientPool client) {
    super(keyedTable);
    this.client = client;
    this.db = keyedTable.id().getDatabase();
    this.tableName = keyedTable.id().getTableName();
    this.keyedTable = keyedTable;
  }



  @Override
  protected StructLikeMap<Long> apply(Transaction transaction, StructLikeMap<Long> partitionMaxTxId) {
    StructLikeMap<Long> partitionMaxId = super.apply(transaction, partitionMaxTxId);
    if (!addFiles.isEmpty()){
      try{
        if (keyedTable.spec().isUnpartitioned()){
          hiveLocation = dataFileDirPath(addFiles.get(0));
        }else {
          rewritePartitions = rewritePartitions();
        }
      }catch (TException|InterruptedException e){
        throw new IllegalStateException("failed to load partition information from hive metastore");
      }
    }
    return partitionMaxId;
  }

  @Override
  public void commit() {
    super.commit();

    try {
      if (keyedTable.spec().isUnpartitioned() && !StringUtils.isEmpty(hiveLocation)){
        client.run( c -> {
          Table tbl = c.getTable(db, tableName);
          tbl.getSd().setLocation(hiveLocation);
          c.alter_table(db, tableName, tbl);
          return 0;
        });
      } else if (!keyedTable.spec().isUnpartitioned() && !rewritePartitions.isEmpty()){
        client.run(
            c -> {
              c.alter_partitions(db, tableName, rewritePartitions);
              return 0;
            }
        );
      }
    }catch (TException|InterruptedException e){
      throw new IllegalStateException("failed to update data location in hive metastore");
    }

  }

  private String dataFileDirPath(DataFile d){
    Path path = new Path(d.path().toString());
    return path.getParent().toString();
  }

  private List<Partition> rewritePartitions() throws TException, InterruptedException {
    Types.StructType partitionSchema = keyedTable.spec().partitionType();

    // partitionValue -> partitionLocation.
    Map<String, String> partitionLocationMap = Maps.newHashMap();
    for (DataFile d: addFiles){
      String[] partitionValues = partitionValues(d.partition(), partitionSchema);
      String value = Joiner.on("/").join(partitionValues);
      String location = dataFileDirPath(d);
      partitionLocationMap.put(value, location);
    }


    List<Partition> tablePartitions = client.run(
        c -> c.listPartitions(db, tableName, (short) -1)
    );

    List<Partition> rewritePartition = Lists.newArrayList();
    for(Partition p: tablePartitions){
      String values = Joiner.on("/").join(p.getValues());
      if (partitionLocationMap.containsKey(values)){
        String newLocation = partitionLocationMap.get(values);

        p.getSd().setLocation(newLocation);
        rewritePartition.add(p);
      }
    }
    return rewritePartition;
  }

  private static String[] partitionValues(StructLike partitionData, Types.StructType partitionSchema) {
    List<Types.NestedField> fields = partitionSchema.fields();
    String[] values = new String[fields.size()];
    for (int i = 0; i < values.length; i++) {
      Type type = fields.get(i).type();
      Object value = partitionData.get(i, type.typeId().javaClass());
      values[i] = value.toString();
    }
    return values;
  }

}
