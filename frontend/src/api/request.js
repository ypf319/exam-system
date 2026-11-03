import axios from 'axios'
import { Message } from 'element-ui'
import { removeUser } from '../utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000,
  withCredentials: true
})

request.interceptors.response.use(
  response => {
    const result = response.data
    if (result && result.code === 200) {
      return result
    }
    if (result && result.code === 401) {
      removeUser()
      if (window.location.hash !== '#/login') {
        window.location.hash = '#/login'
      }
    }
    Message.error((result && result.msg) || '请求失败')
    return Promise.reject(new Error((result && result.msg) || '请求失败'))
  },
  error => {
    const data = error.response && error.response.data
    Message.error((data && data.msg) || '网络异常，请稍后重试')
    return Promise.reject(error)
  }
)

export default request
