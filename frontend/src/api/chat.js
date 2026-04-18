import axios from 'axios'

const instance = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 60000
})

/** 1. 获取左侧会话列表 */
export const getSessions = () => instance.get('/ai/sessions')

/** 2. 获取某个会话的历史消息内容 */
export const getHistory = (conversationId) => instance.get(`/ai/history/${conversationId}`)

/** 3.流式对话
 * @param {string} question
 * @param {string} conversationId
 * @param {boolean} hasImage - 是否携带图片
 */
export async function chatStream(question, conversationId, hasImage = false) {
    // 将 hasImage 拼接到 URL 参数中
    const url = `http://localhost:8080/ai/chat?question=${encodeURIComponent(question)}&conversationId=${encodeURIComponent(conversationId)}&hasImage=${hasImage}`;

    const response = await fetch(url, {
        method: 'POST',
        headers: {
            'Accept': 'text/event-stream'
        }
    });

    if (!response.ok) {
        throw new Error('网络响应错误');
    }
    return response;
}


/** * 4. 文件/图片上传
 * 使用 instance.post 发送 FormData
 * Axios 会自动根据 formData 设置 'Content-Type': 'multipart/form-data'
 */
export const uploadFile = (formData) => instance.post('/ai/upload', formData)