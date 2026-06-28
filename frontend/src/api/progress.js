import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getLearningProgress = async () => {
  try {
    const response = await axios.get(`${API_BASE}/progress`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '读取学习进度失败',
      error: data.msg || '读取学习进度失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const saveLearningProgress = async (chapterKey, progress, completed, detailJson = null, durationSeconds = 0) => {
  try {
    const response = await axios.put(`${API_BASE}/progress`, {
      chapterKey,
      progress,
      completed,
      detailJson,
      durationSeconds
    })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '保存学习进度失败',
      error: data.msg || '保存学习进度失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const resetLearningProgress = async () => {
  try {
    const response = await axios.delete(`${API_BASE}/progress`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '重置学习进度失败',
      error: data.msg || '重置学习进度失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
