// src/socket/group.events.ts
import { Server, Socket } from 'socket.io'

export default (io: Server, socket: Socket) => {
  // 그룹 참여
  socket.on('joinGroup', ({ groupId }: { groupId: string }) => {
    const { userId, nickname, profileImage } = socket.data.user as any
    socket.data.user.groupId = groupId
    socket.join(groupId)
    console.log(`${userId}: ${nickname}님이 그룹 ${groupId}에 참여`)
  })

  // 위치 업데이트
  socket.on('locationUpdate', ({ lat, lng }: { lat: number; lng: number }) => {
    const { userId, nickname, profileImage, groupId } = socket.data.user as any
    socket
      .to(groupId)
      .emit('locationUpdate', { userId, nickname, profileImage, lat, lng, timestamp: Date.now() })
  })

  // 그룹 탈퇴
  socket.on('leaveGroup', () => {
    const { userId, nickname, groupId } = socket.data.user as any
    socket.leave(groupId)
    socket.data.user.groupId = null
    socket.to(groupId).emit('memberLeaved', { userId })
    console.log(`${userId}: ${nickname}님이 그룹 ${groupId} 탈퇴`)
  })

  // 연결 해제
  socket.on('disconnect', (reason) => {
    console.log(`🔴 연결 해제: ${socket.id} (${reason})`)
  })
}