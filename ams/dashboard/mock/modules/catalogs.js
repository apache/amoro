export default [
  {
    url: '/mock/ams/v1/catalogs',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          catalogName: 'catalog1'
        },
        {
          catalogName: 'catalog2'
        }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/catalogs/opensource_arctic/databases',
    method: 'get',
    response: () => {
      return {
        code: 200,
        msg: 'success',
        result: ['database1', 'database2',
          'arctic_spark_test267',
          'arctic_spark_test3',
          'arctic_test',
          'arctic_test_2',
          'chbenchmark1',
          'hellowrld',
          'hwtest1',
          'arctic_spark_test36',
          'uu',
          'arctic_test_2sw',
          'chbenchmark2',
          'tt',
          'yuyu',
          'arctic_spark_test32',
          'arctic_testty',
          'arctic_test_2ty',
          'chbenchmark',
          'hellowrltd',
          'rt',
          'arctic_spark_test3',
          'arctic_test_2yy',
          'chbenchmark5',
          'hellowrld7',
          'hwtesrtrt1'
        ]
      };
    },
  },
  {
    url: '/mock/ams/v1/catalogs/opensource_arctic/databases/arctic_test/tables',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: ['table11', 'table22', 'arctic_spark_test267',
        'arctic_spark_test3',
        'arctic_test',
        'arctic_test_2',
        'chbenchmark1',
        'hellowrld',
        'hwtest1',
        'arctic_spark_test36',
        'uu',
        'arctic_test_2sw',
        'chbenchmark2',
        'tt',
        'yuyu',
        'arctic_spark_test32',
        'arctic_testty',
        'arctic_test_2ty',
        'chbenchmark',
        'hellowrltd',
        'rt',
        'arctic_spark_test3',
        'arctic_test_2yy',
        'chbenchmark5',
        'hellowrld7',
        'hwtesrtrt1']
    }),
  },
  {
    url: '/mock/ams/v1/catalogs/trino_online_env/databases/arctic100wdynamic/tables',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          name: 'arctic_spark_test3'
        },
        {
          name: 'arctic_test'
        }
      ]
    })
  },
  {
    url: '/mock/ams/v1/catalogs/trino_online_env/databases/arctic100wfileSize/tables',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          name: 'arctic_5555'
        },
        {
          name: 'arctic_666'
        }
      ]
    })
  },
  {
    url: '/mock/ams/v1/catalogs/local_catalog/databases/db/tables',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          name: 'arctic_7777'
        },
        {
          name: 'arctic_8888'
        }
      ]
    })
  },
  {
    url: '/mock/ams/v1/catalogs/arctic_catalog_dev/dbs/ndc_test_db/tables/user_order_unpk/upgradeStatus',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        status: 'upgrade',
        errorMessage: 'errorMessage'
      }
    })
  },
  {
    url: '/mock/ams/v1/catalogs/arctic_catalog_dev/dbs/ndc_test_db/tables/user_order_unpk/hive/details',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        tableType: 'HIVE',
        tableIdentifier: {
          catalog: 'bdmstest_arctic',
          database: 'default',
          tableName: 'zyxtest'
        },
        schema: [
          {
            field: 'id1',
            type: 'int',
            description: 'rZiGhjpbqj'
          },
          {
            field: 'id2',
            type: 'int',
            description: 'x6T9Y8D7wi'
          },
          {
            field: 'id3',
            type: 'int',
            description: 'AWpoSVLR6f'
          },
          {
            field: 'id4',
            type: 'int',
            description: 'rZiGhjpbqj'
          },
          {
            field: 'id5',
            type: 'int',
            description: 'x6T9Y8D7wi'
          },
          {
            field: 'id6',
            type: 'int',
            description: 'AWpoSVLR6f'
          }
        ],
        partitionColumnList: [
          {
            field: 'TqgUCqOfr0',
            type: 'bZpDUpDo2l',
            description: 'D3SVsvwmuD'
          },
          {
            field: 'g1tpuaWFg6',
            type: 'tJr2zYltbL',
            description: 'F5z48Arinv'
          },
          {
            field: 'I61mT0lDBP',
            type: 'dSDu69M3Ph',
            description: 'X6Nx4K7S8t'
          },
          {
            field: 'I61mT0welDBP',
            type: 'dSDu69M3Ph',
            description: 'X6Nwex4K7S8t'
          }
        ],
        properties: {
          xxxx: '148'
        }
      }
    })
  },
  {
    url: '/mock/ams/v1/catalogs/arctic_catalog_dev/dbs/ndc_test_db/tables/user_order_unpk/upgrade',
    method: 'post',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {}
    })
  },
  {
    url: '/mock/ams/v1/catalogs/trino_online_env/dbs/arctic10wforOptimizeContinue/tables/nation/operations',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        list: [
          {
            ts: 11234567890123,
            operation: 'sdsd'
          }
        ]
      }
    })
  },
  {
    url: '/mock/ams/v1/catalog/metastore/typestypes',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        'hadoop',
        'hive'
      ]
    })
  },
  {
    url: '/mock/ams/v1/catalogs/bdms_test_catalog_hive',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        name: 'bdms_test_catalog_hive',
        type: 'hive',
        storageConfig: {
          'storage_config.storage.type': 'hdfs',
          'storage_config.core-site': {
            fileName: 'fileName1',
		        filrUrl: 'http://afk.qqzdvuzccctprn.bkfj'
          },
          'storage_config.hdfs-site': {
            fileName: 'fileName2',
		        filrUrl: 'http://afk.qqzdvuzccctprn.bkfj'
          },
          'storage_config.hive-site': {
            fileName: 'fileName3',
		        filrUrl: 'http://afk.qqzdvuzccctprn.bkfj'
          }
        },
        authConfig: {
          'auth_config.type': 'simpel',
          'auth_config.hadoop_username': 'omPRZh6bc8',
          'auth_config.principal': 'L2TeTS0OzC',
          'auth_config.keytab': {
            fileName: 'fileNamekeytab',
            fileUrl: 'http://bfu.qynoircxcut.civkj'
          },
          'auth_config.krb5': {
            fileName: 'fileNamekrb5',
            fileUrl: 'http://bfu.qynoircxcut.civkj'
          }
        },
        properties: {
          key1: 'value1'
        }
      }
    })
  },
  {
    url: '/mock/ams/v1/catalogs/bdms_test_catalog_hive',
    method: 'delete',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {}
    })
  },
  {
    url: '/mock/ams/v1/catalogs/bdms_test_catalog_hive',
    method: 'put',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {}
    })
  },
  {
    url: '/mock/ams/v1/catalogs/bdms_test_catalog_hive',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {}
    })
  },
  {
    url: '/mock/ams/v1/catalogs/bdms_test_catalog_hive/delete/check',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: true
    })
  },
]
