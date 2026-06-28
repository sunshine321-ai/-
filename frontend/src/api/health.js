import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getHealth = async () => {
  try {
    const response = await axios.get(`${API_BASE}/health`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '健康检查失败',
      error: data.msg || '健康检查失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
