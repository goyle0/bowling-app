import axios from 'axios'

/** APIのベースURL */
const BASE_URL = '/api'

/**
 * ボウリングゲームのバックエンドAPIとの通信を担当するモジュール。
 * 全てのメソッドはPromiseを返し、非同期で実行される。
 */
export const bowlingApi = {
    /**
     * 新しいボウリングゲームを作成する。
     * 
     * @returns {Promise<Object>} 作成されたゲームの情報
     * @throws {Error} APIリクエストが失敗した場合
     */
    createGame: async () => {
        const response = await axios.post(`${BASE_URL}/games`)
        return response.data
    },

    /**
     * 指定されたIDのゲーム情報を取得する。
     * 
     * @param {number} gameId 取得するゲームのID
     * @returns {Promise<Object>} ゲームの情報
     * @throws {Error} ゲームが見つからない場合やAPIリクエストが失敗した場合
     */
    getGame: async (gameId) => {
        const response = await axios.get(`${BASE_URL}/games/${gameId}`)
        return response.data
    },

    /**
     * 指定されたゲームのすべてのフレーム情報を取得する。
     * 
     * @param {number} gameId フレームを取得するゲームのID
     * @returns {Promise<Array>} フレーム情報の配列
     * @throws {Error} ゲームが見つからない場合やAPIリクエストが失敗した場合
     */
    getFrames: async (gameId) => {
        const response = await axios.get(`${BASE_URL}/games/${gameId}/frames`)
        return response.data
    },

    /**
     * 投球を記録する。
     * 
     * @param {number} gameId 投球を記録するゲームのID
     * @param {number} frameNumber フレーム番号（1-10）
     * @param {number} pins 倒したピンの数（0-10）
     * @returns {Promise<Object>} 更新されたゲーム情報
     * @throws {Error} 以下の場合にエラーが発生：
     *   - ゲームが見つからない
     *   - 不正なピン数
     *   - 不正なフレーム番号
     *   - フレームが既に完了している
     *   - APIリクエストが失敗した場合
     */
    recordRoll: async (gameId, frameNumber, pins) => {
        const response = await axios.post(`${BASE_URL}/games/${gameId}/rolls`, {
            frameNumber,
            pins
        })
        return response.data
    }
}
