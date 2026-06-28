import axios from 'axios'
import { API_BASE } from '@/config/api'

export const getWrongQuestions = async () => {
  try {
    const response = await axios.get(`${API_BASE}/wrong-questions`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '读取错题失败',
      error: data.msg || '读取错题失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const createWrongQuestion = async (question) => {
  try {
    const response = await axios.post(`${API_BASE}/wrong-questions`, question)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '保存错题失败',
      error: data.msg || '保存错题失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const deleteWrongQuestion = async (id) => {
  try {
    const response = await axios.delete(`${API_BASE}/wrong-questions/${id}`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '删除错题失败',
      error: data.msg || '删除错题失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const clearWrongQuestions = async () => {
  try {
    const response = await axios.delete(`${API_BASE}/wrong-questions`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '清空错题失败',
      error: data.msg || '清空错题失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
