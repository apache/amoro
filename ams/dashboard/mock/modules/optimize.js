export default [
  {
    url: '/mock/ams/v1/optimize/resourceGroups/get',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        { occupationCore: 1, occupationMemory: 12, resourceGroup: { name: 'testName', container: 'container1', properties: { key1: 'value1' } } }
      ]
    }),
  },
  {
    url: '/mock/ams/v1/optimize/containers/get',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: ['container1', 'container2']
    }),
  },
]
