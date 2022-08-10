package com.netease.arctic.spark.hive;

import com.netease.arctic.table.ArcticTable;
import com.netease.arctic.table.UnkeyedTable;
import org.apache.iceberg.DataFile;
import org.apache.iceberg.util.StructLikeMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class TestDelegateHiveTable extends SparkHiveTestContext {

    private final String database = "test_db";
    private final String table = "delegate_hive_table";
    private final String delegateHiveTable = "true";


    @Before
    public void setUpArcticDatabase(){
        sql("set arctic.sql.delegate-hive-table={0}",delegateHiveTable);
        System.out.println("arctic.sql.delegate-hive-table = " + spark.conf().get("arctic.sql.delegate-hive-table"));
        sql("create database if not exists {0}", database);
    }

    @After
    public void cleanUp(){
        sql("drop table {0}.{1}", database, table);
    }

    @Test
    public void testDelegateHiveTable(){
        sql("create table {0}.{1} (" +
                " id int , data string , pt string " +
                ") partitioned by (pt) " +
                "stored as parquet ", database, table);
        sql("insert overwrite {0}.{1} " +
                " partition( pt = ''0001'' ) values \n" +
                " ( 1, ''aaa'' ), (2, ''bbb'' ) ", database, table);

        sql("insert overwrite {0}.{1} " +
                " partition( pt = ''0002'' ) values \n" +
                " ( 3, ''ccc'' ), (4, ''ddd'' ) ", database, table);

        sql("insert overwrite {0}.{1} " +
                " partition( pt = ''0003'' ) values \n" +
                " ( 5, ''eee'' ), (6, ''fff'' ) ", database, table);
        rows = sql("select * from {0}.{1}", database, table);
        Assert.assertEquals(6, rows.size());
        ArcticTable t = loadTable(catalogName, database, table);
        UnkeyedTable unkey = t.asUnkeyedTable();
        StructLikeMap<List<DataFile>> partitionFiles = partitionFiles(unkey);
        Assert.assertEquals(3, partitionFiles.size());
    }

    @Test
    public void testNoPartitionDelegateHiveTable(){
        sql("create table {0}.{1} (" +
                " id int , data string , pt string " +
                ") " +
                "stored as parquet ", database, table);

        sql("insert overwrite {0}.{1} values " +
                " ( 1, ''aaa'', ''0001'' ), \n " +
                " ( 2, ''bbb'', ''0001'' ), \n " +
                " ( 3, ''bbb'', ''0002'' ), \n" +
                " ( 4, ''bbb'', ''0002'' ), \n" +
                " ( 5, ''bbb'', ''0003'' ) ", database, table);
        rows = sql("select * from {0}.{1}", database, table);
        Assert.assertEquals(5, rows.size());

        ArcticTable t = loadTable(catalogName, database, table);
        UnkeyedTable unkey = t.asUnkeyedTable();
        StructLikeMap<List<DataFile>> partitionFiles = partitionFiles(unkey);
        Assert.assertEquals(1, partitionFiles.size());
    }

    @Test
    public void testArcticTable(){
        sql("create table {0}.{1} (" +
                " id int , data string , pt string " +
                ") " +
                "using arctic ", database, table);
        sql("insert overwrite {0}.{1} values " +
                " ( 1, ''aaa'', ''0001'' ), \n " +
                " ( 2, ''bbb'', ''0001'' ), \n " +
                " ( 3, ''bbb'', ''0002'' ), \n" +
                " ( 4, ''bbb'', ''0002'' ), \n" +
                " ( 5, ''bbb'', ''0003'' ) ", database, table);
        rows = sql("select * from {0}.{1}", database, table);
        Assert.assertEquals(5, rows.size());
    }

}
