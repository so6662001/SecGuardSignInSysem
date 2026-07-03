import axios from 'axios'
import { showToast } from 'vant'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('fv_m_token')
  if (token) { config.headers = config.headers || {}; config.headers.Authorization = `Bearer ${token}` }
  return config
})

request.interceptors.response.use(
  (resp) => {
    const body = resp.data
    if (body && typeof body.code !== 'undefined') {
      if (body.code === 0) return body.data
      showToast(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body
  },
  (error) => {
    showToast(error?.response?.data?.message || error.message || '网络异常')
    return Promise.reject(error)
  }
)

export default request
