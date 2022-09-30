package com.netease.arctic.hive;

import org.apache.hadoop.hive.conf.HiveConf;
import org.apache.hadoop.hive.metastore.HiveMetaStoreClient;
import org.apache.hadoop.hive.metastore.PartitionDropOptions;
import org.apache.hadoop.hive.metastore.api.Database;
import org.apache.hadoop.hive.metastore.api.EnvironmentContext;
import org.apache.hadoop.hive.metastore.api.MetaException;
import org.apache.hadoop.hive.metastore.api.Partition;
import org.apache.hadoop.hive.metastore.api.Table;
import org.apache.thrift.TException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class HMSClientImpl implements HMSClient {

  private static HiveConf hiveConf;

  private HiveMetaStoreClient client;

  public HMSClientImpl(HiveConf hiveConf) throws MetaException {
    this.hiveConf = hiveConf;
  }

  public HMSClientImpl(HiveMetaStoreClient client) {
    this.client = client;
  }


  public HiveMetaStoreClient getClient() {
    if (client == null) {
      try {
        this.client = new HiveMetaStoreClient(hiveConf);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return this.client;
  }

  @Override
  public void close() {
    getClient().close();
  }

  @Override
  public void reconnect() throws MetaException {
    getClient().reconnect();
  }

  @Override
  public List<String> getAllDatabases() throws TException {
    return getClient().getAllDatabases();
  }

  @Override
  public Partition getPartition(String dbName, String tblName, List<String> partVals) throws TException {
    return getClient().getPartition(dbName, tblName, partVals);
  }

  @Override
  public Partition getPartition(String dbName, String tblName, String name) throws TException {
    return getClient().getPartition(dbName, tblName, name);
  }

  @Override
  public Table getTable(String dbName, String tableName) throws TException {
    return getClient().getTable(dbName, tableName);
  }

  @Override
  public void alter_table(String defaultDatabaseName, String tblName, Table table) throws TException {
    getClient().alter_table(defaultDatabaseName, tblName, table);
  }

  @Override
  public List<Partition> listPartitions(String dbName, String tblName, short maxParts) throws TException {
    return getClient().listPartitions(dbName, tblName, maxParts);
  }

  @Override
  public List<Partition> listPartitions(String dbName, String tblName,
                                        List<String> partVals, short maxParts) throws TException {
    return getClient().listPartitions(dbName, tblName, partVals, maxParts);
  }

  @Override
  public List<String> listPartitionNames(String dbName, String tblName, short maxParts) throws TException {
    return getClient().listPartitionNames(dbName, tblName, maxParts);
  }

  @Override
  public void createDatabase(Database db) throws TException {
    getClient().createDatabase(db);
  }

  @Override
  public void dropDatabase(String name, boolean deleteData,
                           boolean ignoreUnknownDb, boolean cascade) throws TException {
    getClient().dropDatabase(name, deleteData, ignoreUnknownDb, cascade);
  }

  @Override
  public void dropTable(String dbname, String tableName,
                        boolean deleteData, boolean ignoreUnknownTab) throws TException {
    getClient().dropTable(dbname, tableName, deleteData, ignoreUnknownTab);
  }

  @Override
  public void createTable(Table tbl) throws TException {
    getClient().createTable(tbl);
  }

  @Override
  public Database getDatabase(String databaseName) throws TException {
    return getClient().getDatabase(databaseName);
  }

  @Override
  public Partition add_partition(Partition partition) throws TException {
    return getClient().add_partition(partition);
  }

  @Override
  public boolean dropPartition(String dbName, String tblName,
                               List<String> partVals, PartitionDropOptions options) throws TException {
    return getClient().dropPartition(dbName, tblName, partVals, options);
  }

  @Override
  public int add_partitions(List<Partition> partitions) throws TException {
    return getClient().add_partitions(partitions);
  }


  @Override
  public List<String> getAllTables(String dbName) throws TException {
    return getClient().getAllTables(dbName);
  }

  @Override
  public void alter_partitions(String dbName, String tblName,
                               List<Partition> newParts, EnvironmentContext environmentContext)
      throws IllegalAccessException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
    String hiveVersion = HiveMetaStoreClient.class.getClassLoader()
        .loadClass("org.apache.hadoop.hive.metastore.HiveMetaStoreClient")
        .getPackage().getImplementationVersion();
    Class<HiveMetaStoreClient> hmsClass = HiveMetaStoreClient.class;
    if (hiveVersion.equals("1.2.1.spark2")) {
      hmsClass.getMethod("alter_partitions", String.class, String.class, List.class)
          .invoke(getClient(), dbName, tblName, newParts);
    } else if (hiveVersion.equals("2.1.1")) {
      hmsClass.getMethod("alter_partitions", String.class, String.class, List.class, EnvironmentContext.class)
          .invoke(getClient(), dbName, tblName, newParts, environmentContext);
    }

  }

  @Override
  public void alter_partition(String dbName, String tblName,
                              Partition newPart, EnvironmentContext environmentContext)
      throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    String hiveVersion = HiveMetaStoreClient.class.getClassLoader()
        .loadClass("org.apache.hadoop.hive.metastore.HiveMetaStoreClient")
        .getPackage().getImplementationVersion();
    Class<HiveMetaStoreClient> hmsClass = HiveMetaStoreClient.class;
    if (hiveVersion.equals("1.2.1.spark2")) {
      hmsClass.getMethod("alter_partition", String.class, String.class, Partition.class)
          .invoke(getClient(), dbName, tblName, newPart);
    } else if (hiveVersion.equals("2.1.1")) {
      hmsClass.getMethod("alter_partition", String.class, String.class, Partition.class, EnvironmentContext.class)
          .invoke(getClient(), dbName, tblName, newPart, environmentContext);
    }
  }
}
