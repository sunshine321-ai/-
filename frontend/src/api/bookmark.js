import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getBookmarks = async () => {
  try {
    const response = await axios.get(`${API_BASE}/bookmarks`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '读取书签失败',
      error: data.msg || '读取书签失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const createBookmark = async (videoTime, label) => {
  try {
    const response = await axios.post(`${API_BASE}/bookmarks`, { videoTime, label })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '添加书签失败',
      error: data.msg || '添加书签失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const deleteBookmark = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE}/bookmarks/${id}`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '删除书签失败',
      error: data.msg || '删除书签失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const clearBookmarks = async () => {
  try {
    const response = await axios.delete(`${API_BASE}/bookmarks`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '清空书签失败',
      error: data.msg || '清空书签失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
