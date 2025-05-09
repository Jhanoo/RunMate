// src/index.ts
import http from 'http'
import app from './app'
import setupSocket from './socket/index'
import config from './config'

// HTTP 서버 생성
const server = http.createServer(app)

// Socket.IO 설정 및 이벤트 연결
setupSocket(server)

// 서버 실행
server.listen(config.port, () => {
  console.log(`🚀 서버 실행: http://localhost:${config.port}`)
})
