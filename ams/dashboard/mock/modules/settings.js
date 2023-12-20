export default [
  {
    url: '/mock/ams/v1/settings/system',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: {
        'arctic.ams.server-host.prefix': '127.0.0.1',
        'arctic.ams.thrift.port': '18112'
      }
    }),
  },
  {
    url: '/mock/ams/v1/settings/containers',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          name: 'flinkcontainer',
          type: 'flink',
          properties: {
            'properties.FLINK_HOME': '/home/arctic/flink-1.12.7/'
          },
          optimizeGroup: [{
            name: 'flinkOp',
            tmMemory: '1024',
            jmMemory: 'sdsa'
          }]
        },
        {
          name: 'flinkcontainer2',
          type: 'flink',
          properties: {
            'properties.FLINK_HOME': '/home/arctic/flink-1.12.7/'
          },
          optimizeGroup: [{
            name: 'flinkOp',
            tmMemory: '1024',
            jmMemory: 'sdsa2'
          }]
        }
      ]
    }),
  },


]
