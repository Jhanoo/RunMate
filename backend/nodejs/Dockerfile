# 1) 베이스 이미지
FROM node:18

# 2) 작업 디렉토리
WORKDIR /app

# 3) package.json 먼저 복사 → 의존성 설치
COPY package*.json ./
RUN npm install

# 4) 소스 전체 복사
COPY . .

# 5) TypeScript 빌드
RUN npm run build

# 6) 컨테이너 내부 포트 열기
EXPOSE 3000

# 7) 프로덕션 런치
CMD ["node", "dist/index.js"]