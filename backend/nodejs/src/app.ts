// src/app.ts
import express from 'express'
import cors from 'cors'
import config from './config'

const app = express()

// CORS 및 JSON 파싱 설정
app.use(cors({ origin: config.corsOrigin }))
app.use(express.json())

// 에러 핸들러
import { Request, Response, NextFunction } from 'express'

app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
  console.error(err.stack)
  res.status(500).json({ message: err.message })
})

export default app
