export default [
  {
    url: '/mock/api/database',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: ['database1', 'database2']
    }),
  },
]
