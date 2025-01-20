<template>
    <div class="game">
        <h1>ボウリングゲーム</h1>
        <div v-if="loading" class="loading">
            Loading...
        </div>
        <div v-else-if="error" class="error">
            {{ error }}
        </div>
        <div v-else>
            <div class="score-board">
                <div class="frames">
                    <div v-for="(frame, index) in frames" :key="index" class="frame">
                        <div class="frame-number">{{ index + 1 }}</div>
                        <div class="rolls">
                            <span class="roll">{{ formatRoll(frame.firstRoll) }}</span>
                            <span class="roll">{{ formatRoll(frame.secondRoll) }}</span>
                            <span v-if="index === 9" class="roll">{{ formatRoll(frame.thirdRoll) }}</span>
                        </div>
                        <div class="frame-score">{{ frame.frameScore }}</div>
                    </div>
                </div>
                <div class="total-score">
                    合計スコア: {{ totalScore }}
                </div>
            </div>
            
            <div class="input-section" v-if="currentFrameIndex < 10">
                <h2>フレーム {{ currentFrameIndex + 1 }}</h2>
                <div class="pins-input">
                    <button 
                        v-for="pins in availablePins" 
                        :key="pins"
                        @click="recordRoll(pins)"
                        class="pin-button"
                        :disabled="loading"
                    >
                        {{ pins }}
                    </button>
                </div>
            </div>
            
            <div v-else class="game-complete">
                <h2>ゲーム終了！</h2>
                <button @click="newGame" class="new-game-button" :disabled="loading">
                    新しいゲームを開始
                </button>
            </div>
        </div>
    </div>
</template>

<script>
import { bowlingApi } from '../api/bowlingApi'

export default {
    name: 'Game',
    data() {
        return {
            gameId: null,
            frames: [],
            currentFrameIndex: 0,
            currentRoll: 1,
            loading: false,
            error: null
        }
    },
    async created() {
        try {
            this.loading = true
            const game = await bowlingApi.createGame()
            this.gameId = game.id
            const frames = await bowlingApi.getFrames(this.gameId)
            this.frames = frames
            this.loading = false
        } catch (err) {
            this.error = 'ゲームの作成に失敗しました'
            this.loading = false
        }
    },
    computed: {
        totalScore() {
            return this.frames.reduce((total, frame) => total + (frame.frameScore || 0), 0)
        },
        availablePins() {
            if (this.currentFrameIndex === 9) {
                if (this.currentRoll === 1) {
                    return [...Array(11)].map((_, i) => i)
                } else if (this.currentRoll === 2) {
                    if (this.frames[9].firstRoll === 10) {
                        return [...Array(11)].map((_, i) => i)
                    } else {
                        return [...Array(11 - this.frames[9].firstRoll)].map((_, i) => i)
                    }
                } else if (this.currentRoll === 3) {
                    if (this.frames[9].secondRoll === 10 || 
                        this.frames[9].firstRoll + this.frames[9].secondRoll === 10) {
                        return [...Array(11)].map((_, i) => i)
                    }
                    return []
                }
            } else {
                if (this.currentRoll === 1) {
                    return [...Array(11)].map((_, i) => i)
                } else {
                    return [...Array(11 - this.frames[this.currentFrameIndex].firstRoll)].map((_, i) => i)
                }
            }
            return []
        }
    },
    methods: {
        formatRoll(roll) {
            if (roll === null) return ''
            if (roll === 10) return 'X'
            if (this.currentRoll === 2 && roll + this.frames[this.currentFrameIndex].firstRoll === 10) return '/'
            return roll.toString()
        },
        async recordRoll(pins) {
            try {
                this.loading = true
                const frame = this.frames[this.currentFrameIndex]
                
                if (this.currentRoll === 1) {
                    if (pins === 10 && this.currentFrameIndex < 9) {
                        this.currentFrameIndex++
                    } else {
                        this.currentRoll = 2
                    }
                } else if (this.currentRoll === 2) {
                    if (this.currentFrameIndex < 9) {
                        this.currentFrameIndex++
                        this.currentRoll = 1
                    } else if (frame.firstRoll === 10 || frame.firstRoll + pins === 10) {
                        this.currentRoll = 3
                    } else {
                        this.currentFrameIndex++
                    }
                } else if (this.currentRoll === 3 && this.currentFrameIndex === 9) {
                    this.currentFrameIndex++
                }

                await bowlingApi.recordRoll(this.gameId, this.currentFrameIndex + 1, pins)
                const updatedFrames = await bowlingApi.getFrames(this.gameId)
                this.frames = updatedFrames
                this.loading = false
            } catch (err) {
                this.error = '投球の記録に失敗しました'
                this.loading = false
            }
        },
        async newGame() {
            try {
                this.loading = true
                const game = await bowlingApi.createGame()
                this.gameId = game.id
                const frames = await bowlingApi.getFrames(this.gameId)
                this.frames = frames
                this.currentFrameIndex = 0
                this.currentRoll = 1
                this.error = null
                this.loading = false
            } catch (err) {
                this.error = '新しいゲームの作成に失敗しました'
                this.loading = false
            }
        }
    }
}
</script>

<style scoped>
.game {
    max-width: 1000px;
    margin: 0 auto;
    padding: 20px;
}

.loading, .error {
    text-align: center;
    margin: 20px 0;
    padding: 20px;
    border-radius: 4px;
}

.loading {
    background-color: #f8f9fa;
    color: #666;
}

.error {
    background-color: #f8d7da;
    color: #721c24;
}

.score-board {
    margin: 20px 0;
    background-color: #f5f5f5;
    padding: 20px;
    border-radius: 4px;
}

.frames {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
}

.frame {
    flex: 1;
    margin: 0 5px;
    padding: 10px;
    background-color: white;
    border: 1px solid #ddd;
    border-radius: 4px;
}

.frame-number {
    font-size: 0.8em;
    color: #666;
}

.rolls {
    display: flex;
    justify-content: space-around;
    margin: 5px 0;
}

.roll {
    width: 20px;
    height: 20px;
    display: inline-block;
    border: 1px solid #ddd;
    text-align: center;
    line-height: 20px;
    font-size: 0.9em;
}

.frame-score {
    font-weight: bold;
    color: #42b983;
}

.total-score {
    font-size: 1.5em;
    font-weight: bold;
    text-align: right;
    color: #2c3e50;
}

.input-section {
    margin-top: 40px;
}

.pins-input {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    justify-content: center;
}

.pin-button {
    width: 50px;
    height: 50px;
    border: none;
    border-radius: 25px;
    background-color: #42b983;
    color: white;
    font-size: 1.2em;
    cursor: pointer;
    transition: background-color 0.3s;
}

.pin-button:not(:disabled):hover {
    background-color: #3aa876;
}

.pin-button:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
}

.game-complete {
    text-align: center;
    margin-top: 40px;
}

.new-game-button {
    background-color: #42b983;
    color: white;
    padding: 15px 30px;
    border: none;
    border-radius: 4px;
    font-size: 1.2em;
    cursor: pointer;
    transition: background-color 0.3s;
}

.new-game-button:not(:disabled):hover {
    background-color: #3aa876;
}

.new-game-button:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
}
</style>
