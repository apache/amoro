export default [
  {
    url: '/mock/ams/v1/terminal/examples',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        'CreateTable',
        'EditTable',
        'DeleteTable'
      ]
    }),
  },
  {
    url: '/mock/ams/v1/terminal/3/result',
    method: 'get',
    response: () => ({
      code: 200,
      msg: 'success',
      result: [
        {
          id: 'Result1',
          status: 'Failed',
          columns: [
            'namespace'
          ],
          rowData: [
            [
              'arctic_test'
            ],
            [
              'arctic_test_2'
            ],
            [
              'hellowrld'
            ]
          ]
        },
        {
          id: 'Result2',
          status: 'Failed',
          columns: [
            'namespace'
          ],
          rowData: [
            [
              'arctic_test222222222222222222'
            ],
            [
              'arctic_test_24444444444444'
            ],
            [
              'hellowrld666666666666666666666'
            ]
          ]
        }
      ]
    }),
  },
]
