import { createProdMockServer } from 'vite-plugin-mock/es/createProdMockServer'

import mockModule from '.'

export function setupProdMockServer() {
  createProdMockServer([...mockModule])
}
