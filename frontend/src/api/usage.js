import axios from 'axios'
import { API_BASE } from '@/config/api'

export const recordUsageEvent = async (moduleKey, eventType, durationSeconds = 0) => {
  try {
    const response = await axios.post(`${API_BASE}/usage/events`, {
      moduleKey,
      eventType,
      durationSeconds
    })
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '使用行为记录失败',
      error: data.msg || '使用行为记录失败'
    }
  } catch (error) {
    console.error('使用行为记录失败', error)
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}

export const getUsageSummary = async () => {
  try {
    const response = await axios.get(`${API_BASE}/usage/summary`)
    const data = response.data

    if (data.code === 200) {
      return { success: true, data: data.data, msg: data.msg }
    }

    return {
      success: false,
      msg: data.msg || '使用统计读取失败',
      error: data.msg || '使用统计读取失败'
    }
  } catch (error) {
    return {
      success: false,
      msg: error.response?.data?.msg || error.message || '网络请求失败',
      error: error.response?.data?.msg || error.message || '网络请求失败'
    }
  }
}
