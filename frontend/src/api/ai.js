import axios from 'axios'
import { API_BASE } from '@/config/api'

export const askAi = async (message, context = 'study', systemPrompt = '') => {
  try {
    const response = await axios.post(`${API_BASE}/ai/chat`, {
      message,
      context,
      systemPrompt
    })
    const data = response.data

    if (data.code === 200) {
      const payload = data.data
      const content = typeof payload === 'string'
        ? payload
        : payload?.message || payload?.content || data.message || data.content || data.answer || ''
      return { success: true, data: content, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || 'AI 请求失败',
      error: data.msg || 'AI 请求失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const analyzeVideoChapters = async (videoTitle, durationSeconds, frames) => {
  try {
    const response = await axios.post(`${API_BASE}/ai/video-chapters`, {
      videoTitle,
      durationSeconds,
      frames
    })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data?.chapters || data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || 'AI 识别章节失败',
      error: data.msg || 'AI 识别章节失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
