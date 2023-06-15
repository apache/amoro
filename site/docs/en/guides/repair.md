# Repair
Arctic provides a temporary solution to address the issue of Arctic table unavailability and restore table availability in a timely manner, thereby avoiding significant losses caused by prolonged unavailability.

## The scenario of table unavailability
Here is a summary of several scenarios of table unavailability and their corresponding repair solutions:
### File loss
File loss includes three types of files: DataFile, Equality Delete File, and Position Delete File.
Causes: The main reasons for this situation are bugs in the logic of cleaning up isolated or expired files, or human operational errors such as mistaken deletion.
Repair solutions:
- First, look for files with the same name in the trash directory. If found, restore the recycled bin data to the corresponding data directory.
- If the file cannot be found in the trash directory, provide the user with two options:
  - Delete the file directly in the metadata.
  - Roll back to a usable version.
  
> In the Mixed-Hive format, Iceberg indexes files that are deleted by Hive's insert overwrite-related operations. However, it is difficult to distinguish whether it was caused by Hive operations or abnormal file loss. Therefore, in this context, only the Hive sync function is provided to attempt to synchronize metadata.

### Manifest loss
Causes: The main reasons for this situation are bugs in the logic of cleaning up isolated or expired files, or human operational errors such as mistaken deletion.
Repair solutions:

- First, look for files with the same name in the trash directory. If found, restore the recycled bin data to the corresponding data directory.
- If the file cannot be found in the trash directory, provide the user with two options:
  - Delete the corresponding Manifest record from the ManifestList.
  - Roll back to the latest version that does not contain this Manifest file.

### ManifestList loss
Causes: The main reasons for this situation are bugs in the logic of cleaning up isolated or expired files, or human operational errors such as mistaken deletion.
Repair solutions:

- First, look for files with the same name in the trash directory. If found, restore the recycled bin data to the corresponding data directory.
- If the file cannot be found in the trash directory, provide the user with the option to roll back to the previous version.

### Metadata loss
Causes: The main reasons for this situation are bugs in the logic of cleaning up isolated or expired files, or human operational errors such as mistaken deletion. The main form is the loss of the metadata file corresponding to the version in the version-hit.txt file. If version-hit.txt does not exist or is damaged, no repair is needed, and the table will automatically roll back to the latest available version.
Repair solutions:

- First, look for files with the same name in the trash directory. If found, restore the recycled bin data to the corresponding data directory.
- If the file cannot be found in the trash directory and metadata files still exist, then delete the version-hit.txt file. This will cause the table to automatically roll back versions. If there are no available metadata files, clear the tabl

## User Manual
The repair tool will be deployed along with AMS and provide a script to start the repair tool. After starting, it will enter an interactive terminal.
### Script Startup
Under the "bin" directory of the AMS deployment directory, there will be a script for the Repair tool named `repair.sh`. The script provides three options, of which only one of the '-t' and '-c' parameters needs to be specified:
```
    -t        Your thrift address.
              Format: thrift://{AMS_HOST}:{AMS_PORT}/{AMS_CATALOG_NAME}
              Example: -t thrift://localhost:1260/catalog_name

    -c        Your catalog name.
              We will read the thrift port number from {ARCTIC_HOME}/conf/config.yaml and use localhost as {AMS_HOST} to form a complete thrift address.
              Example: -c local_catalog

    -m        The amount of memory allocated to the Repair tool.
              Unit: GB
              Default value: 2GB (it is recommended not to be less than 2GB)
              Example: -m 3 (means allocating 3GB of memory to the Repair tool)
```
You can start the Repair tool under the directory where AMS is deployed with the following command:
：
```shell
$ ./bin/repair.sh       #To start the Repair tool directly, read the thrift port number from $ARCTIC_HOME/conf/config.yaml and use localhost as the address without setting a catalog
$ ./bin/repair.sh -t thrift://localhost:1260/local_catalog          #thrift address 
$ ./bin/repair.sh -t thrift://localhost:1260                        #specify the Thrift address (without a catalog)
$ ./bin/repair.sh -c local_catalog                                  #specify the catalog name
$ ./bin/repair.sh -t thrift://localhost:1260/local_catalog -m 4     #specify the Thrift address and allocate 4GB of memory
$ ./bin/repair.sh -h                                                #help
```

### Repair tools instruction
The Repair tool supports the following commands, and you can also get help by entering "help" in the tool.

#### SHOW
`SHOW [ CATALOGS | DATABASES | TABLES ]`

To display a list of catalogs, databases, or tables:
```
SHOW CATALOGS
SHOW DATABASES
SHOW TABLES
```

#### USE
`USE [ ${catalog_name} | ${database_name} ]`

To use a Catalog or Database
```
USE local_catalog
USE my_database
```

#### ANALYZE
`ANALYZE ${table_name}`

To analyze a table to find missing files and provide repair options, ${table_name} supports binary and ternary tuples:
```
ANALYZE user
ANALYZE db.user
ANALYZE catalog.db.user
```
The returned result will be in the following format:
```
FILE_LOSE:
      hdfs://xxxx/xxxx/xxx
YOU CAN:
      FIND_BACK
      SYNC_METADATA
      ROLLBACK:
           597568753507019307
           512339827482937422
```

#### Repair
The Repair command currently provides three methods for repairing
- `REPAIR ${table_name} THROUGH FIND_BACK`：Retrieve the file from the Recycle Bin.
- `REPAIR ${table_name} THROUGH SYNC_METADATA`：Delete non-existent files from metadata or synchronize metadata.
- `REPAIR ${table_name} THROUGH ROLLBACK ${snapshot_id}`：Roll back to a version that does not contain the lost file.
- `REPAIR ${table_name} THROUGH DROP_TABLE`：Clear the metadata of a table.

```
REPAIR user THROUGH FIND_BACK              
REPAIR user THROUGH SYNC_METADATA          
REPAIR user THROUGH ROLLBACK 597568753507019307
REPAIR user THROUGH DROP_TABLE
```

#### OPTIMIZE
`OPTIMIZE [ STOP | START ] ${table_name}`

stop or start the Optimize service for a specified table.
```
OPTIMIZE STOP user
OPTIMIZE START user
```

#### TABLE
`TABLE ${table_name} [ REFRESH | SYNC_HIVE_METADATA | SYNC_HIVE_DATA | SYNC_HIVE_DATA_FORCE | DROP_METADATA ]`


Perform operations on the specified table: REFRESH: refreshes the file cache of the table, SYNC_HIVE_METADATA: synchronizes metadata of Hive-compatible tables, SYNC_HIVE_DATA: synchronizes data of Hive-compatible tables, DROP_METADATA: deletes the metadata of the Arctic table, and once the data files are deleted, users can reconstruct the table from the current data.
```
REFRESH FILE_CACHE user 
```

#### QUIT
`QUIT`

Exit Repair Tool.

> Tips: All keywords in Repair Tool are case-insensitive.
> The steps for safely recovering a table are: 'OPTIMIZE STOP -> TABLE REFRESH -> REPAIR -> TABLE REFRESH -> OPTIMIZE START'



