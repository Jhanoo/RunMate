# 그룹 달리기 실시간 위치 공유 서버

TypeScript + Socket.IO 기반의 **그룹 달리기** 중인 사용자들이 실시간으로 위치를 공유하도록 설계된 서버 프로젝트입니다.

---

## 주요 기능

- **joinGroup**: 클라이언트가 그룹에 입장하면 Socket.IO 방(Room)에 할당
- **locationUpdate**: 같은 그룹 내 다른 클라이언트에게 위치(위도·경도) 전파
- **leaveGroup**: 그룹 탈퇴 처리
- **disconnect**: 연결 해제 시 자동 로그 출력

---

## 프로젝트 구조

```
nodejs/
├── .env                   # 환경변수 설정
├── package.json           # 의존성·스크립트 목록
├── tsconfig.json          # TypeScript 컴파일 옵션
└── src/
    ├── index.ts           # 서버 진입점 (HTTP + Socket.IO 시작)
    ├── app.ts             # Express 앱 설정 (CORS, JSON 파싱, 에러 핸들러)
    ├── config/
    │   └── index.ts       # dotenv로 환경변수 로드
    └── socket/
        ├── index.ts       # Socket.IO 초기화 및 auth 미들웨어
        └── group.events.ts# 그룹 관련 이벤트 핸들러
```

---

## 버전 관리

- Node.js v22.13.0
- npm v11.2.0

---

## 환경 변수 (`.env`)

```env
PORT=3000 // 원하는 포트번호 설정정
CORS_ORIGIN=*
```

---

## 의존성 및 스크립트 (`package.json`)

```jsonc
{
  "scripts": {
    "dev": "ts-node-dev --respawn --transpile-only src/index.ts",
    "build": "tsc",
    "start": "node dist/index.js"
  },
  "dependencies": {
    "express": "^5.1.0",
    "socket.io": "^4.8.1",
    "cors": "^2.8.5",
    "dotenv": "^16.5.0"
  },
  "devDependencies": {
    "typescript": "^5.8.3",
    "ts-node-dev": "^2.0.0",
    "@types/node": "^22.15.17",
    "@types/express": "^5.0.1",
    "@types/socket.io": "^3.0.1",
    "@types/cors": "^2.8.18"
  }
}
```

---

## 설치 및 실행

1. **의존성 설치**

   ```bash
   npm install
   ```

2. **.env 생성**

   ```bash
    PORT=3000           # 원하는 포트번호 설정
    CORS_ORIGIN=*
   ```

3. **개발 모드**

   ```bash
   npm run dev
   ```

   - `http://localhost:3000` 에서 Express API 및 Socket.IO 서버 실행

4. **프로덕션 모드 빌드 및 실행**

   ```bash
   npm run build
   npm start
   ```

---

## 소켓(Socket.IO) 이벤트 상세

아래는 이 서버에서 지원하는 Socket.IO 이벤트 목록과 페이로드 구조입니다.

### 클라이언트 → 서버

| 이벤트명         | 설명             | 데이터 형식                     |
| ---------------- | ---------------- | ------------------------------- |
| `joinGroup`      | 그룹에 참여 요청 | `{ groupId: string }`           |
| `locationUpdate` | 현재 위치 전송   | `{ lat: number; lng: number; }` |
| `leaveGroup`     | 그룹 탈퇴 요청   | _(데이터 없음)_                 |

**예시 코드 (Kotlin/Android)**

```kotlin
// joinGroup
socket.emit("joinGroup", mapOf("groupId" to groupId))

// locationUpdate
socket.emit("locationUpdate", mapOf("lat" to latitude, "lng" to longitude))

// leaveGroup
socket.emit("leaveGroup")
```

### 서버 → 클라이언트

| 이벤트명         | 설명                            | 데이터 형식                                                                                                |
| ---------------- | ------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| `locationUpdate` | 그룹 내 다른 사용자의 위치 수신 | `{ userId: string; nickname: string; profileImage: string; lat: number; lng: number; timestamp: number; }` |

### 인증 절차

1. 클라이언트는 연결 시 다음과 같이 `auth` 옵션에 사용자 정보를 담아 보냅니다:

   ```js
   const socket = io(url, {
     auth: { userId, nickname, profileImage },
     transports: ['websocket'],
   })
   ```

2. 서버의 `io.use` 미들웨어에서 `socket.handshake.auth`를 검증하여 `socket.data.user`에 저장합니다.
3. 이후 모든 이벤트 핸들러에서 `socket.data.user`를 통해 사용자 정보를 참조할 수 있습니다.
