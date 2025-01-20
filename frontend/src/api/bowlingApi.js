import axios from 'axios'

const BASE_URL = '/api'

export const bowlingApi = {
    createGame: async () => {
        const response = await axios.post(`${BASE_URL}/games`)
        return response.data
    },

    getGame: async (gameId) => {
        const response = await axios.get(`${BASE_URL}/games/${gameId}`)
        return response.data
    },

    getFrames: async (gameId) => {
        const response = await axios.get(`${BASE_URL}/games/${gameId}/frames`)
        return response.data
    },

    recordRoll: async (gameId, frameNumber, pins) => {
        const response = await axios.post(`${BASE_URL}/games/${gameId}/rolls`, {
            frameNumber,
            pins
        })
        return response.data
    }
}
