export default [
  {
    url: '/mock/ams/v1/overview/summary',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        catalogCnt: 2,
        tableCnt: 37944,
        tableTotalSize: 10585900,
        totalCpu: '6',
        totalMemory: 62464
      }
    }),
  },
  {
    url: '/mock/ams/v1/overview/top/tables',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          tableName: 'trino_online_env_hive.spark_test.ctpri',
          size: 12938982,
          fileCnt: 57889
        },
        {
          tableName: 'trino_online_env_hive.spark_test.ctpp_col',
          size: 329043290,
          fileCnt: 79910
        }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/overview/metric/optimize/resource',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        timeLine: [
          '10-09 14:48'
        ],
        usedCpu: [
          '83.24'
        ],
        usedCpuDivision: [
          '1828核/2196核'
        ],
        usedCpuPercent: [
          '83.24%'
        ],
        usedMem: [
          '83.24'
        ],
        usedMemDivision: [
          '1828核/2196核10364G'
        ],
        usedMemPercent: [
          '83.24%'
        ]
      }
    }),
  },
]
