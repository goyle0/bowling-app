import { createApp } from 'vue'
import { createStore } from 'vuex'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import Home from './views/Home.vue'
import Game from './views/Game.vue'

// Store設定
const store = createStore({
    state() {
        return {
            currentGame: {
                frames: [],
                totalScore: 0
            }
        }
    },
    mutations: {
        addFrame(state, frame) {
            state.currentGame.frames.push(frame)
        },
        updateTotalScore(state, score) {
            state.currentGame.totalScore = score
        }
    }
})

// ルーター設定
const router = createRouter({
    history: createWebHistory(),
    routes: [
        {
            path: '/',
            name: 'Home',
            component: Home
        },
        {
            path: '/game',
            name: 'Game',
            component: Game
        }
    ]
})

const app = createApp(App)
app.use(store)
app.use(router)
app.mount('#app')
