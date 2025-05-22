// src/socket/index.ts
import http from 'http'
import { Server } from 'socket.io'
import groupEvents from './group.events'
import config from '../config'

export default (server: http.Server) => {
  const io = new Server(server, {
    cors: { origin: config.corsOrigin, methods: ['GET', 'POST'] },
  })

  io.use((socket, next) => {
    const { userId, nickname, profileImage } = socket.handshake.auth
    if (!userId || !nickname) {
      return next(new Error('Invalid user data'))
    }

    socket.data.user = { userId, nickname, profileImage }
    next()
  })

  io.on('connection', (socket) => {
    console.log(`🟢 연결: ${socket.id}`)
    groupEvents(io, socket)
  })
}
