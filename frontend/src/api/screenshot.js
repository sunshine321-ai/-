import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getScreenshots = async () => {
  try {
    const response = await axios.get(`${API_BASE}/screenshots`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '读取截图笔记失败',
      error: data.msg || '读取截图笔记失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const createScreenshot = async (image, videoTime) => {
  try {
    const response = await axios.post(`${API_BASE}/screenshots`, { image, videoTime })
    const data = response.data

    if (data.code === 200) {
      return {
        success: true,
        msg: data.msg,
        data: {
          id: data.data.id,
          url: data.data.imageUrl,
          videoTime: data.data.videoTime,
          createdAt: data.data.createdAt,
          updatedAt: data.data.updatedAt
        }
      }
    }

    return {
      success: false,
      msg: data.msg || '保存截图笔记失败',
      error: data.msg || '保存截图笔记失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const updateScreenshot = async (id, note, aiAnalysis) => {
  try {
    const response = await axios.put(`${API_BASE}/screenshots/${id}`, { note, aiAnalysis })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '更新截图笔记失败',
      error: data.msg || '更新截图笔记失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const deleteScreenshot = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE}/screenshots/${id}`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '删除截图笔记失败',
      error: data.msg || '删除截图笔记失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const analyzeScreenshot = async (imagePath) => {
  try {
    const response = await axios.post(`${API_BASE}/screenshots/analyze`, { imagePath })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: { content: data.data }, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '截图 AI 分析失败',
      error: data.msg || '截图 AI 分析失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
