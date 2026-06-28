import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getStudyNote = async () => {
  try {
    const response = await axios.get(`${API_BASE}/notes`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '获取学习笔记失败',
      error: data.msg || '获取学习笔记失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const saveStudyNote = async (content) => {
  try {
    const response = await axios.put(`${API_BASE}/notes`, { content })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '保存学习笔记失败',
      error: data.msg || '保存学习笔记失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
