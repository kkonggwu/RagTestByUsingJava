import axios from 'axios'

// 根据环境变量设置 API 基础 URL
const baseURL = import.meta.env.PROD 
 ? '/api' // 生产环境使用相对路径，适用于前后端部署在同一域名下
 : 'http://localhost:8009/api' // 开发环境指向本地后端服务

const client = axios.create({
  baseURL,
  timeout: 30000
})

export default client

