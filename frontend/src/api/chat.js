import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getChatMessages = async (context) => {
  try {
    const response = await axios.get(`${API_BASE}/chats/${context}`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '读取对话历史失败',
      error: data.msg || '读取对话历史失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const clearChatMessages = async (context) => {
  try {
    const response = await axios.delete(`${API_BASE}/chats/${context}`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '清空对话历史失败',
      error: data.msg || '清空对话历史失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
