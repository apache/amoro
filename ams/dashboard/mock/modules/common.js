export default [
  {
    url: '/mock/ams/v1/login/current',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: ''
    }),
  },
  {
    url: '/mock/ams/v1/upgrade/properties',
    method: 'get',
    response:() => ({
      code: 200,
      msg: 'success',
      result: {
        key1: 'koYg4SDRzM',
        key2: 'T3ScQHN0hE'
      }
    })
  },
]
