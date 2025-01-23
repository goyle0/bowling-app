<template>
    <div class="game">
        <h1>ボウリングゲーム</h1>
        <div v-if="loading" class="loading">
            <div class="loading-spinner"></div>
            Loading...
        </div>
        <div v-else-if="error" class="error">
            {{ error }}
        </div>
        <div v-else>
            <div class="score-board">
                <div class="frames">
                    <div v-for="(frame, index) in frames" 
                         :key="index" 
                         class="frame"
                         :class="{ 'current-frame': index === currentFrameIndex }">
                        <div class="frame-number">{{ index + 1 }}</div>
                        <div class="rolls">
                            <span class="roll" :class="{ 'strike': isStrike(frame.firstRoll) }">
                                {{ formatRoll(frame.firstRoll) }}
                            </span>
                            <span class="roll" :class="{ 'spare': isSpare(frame) }">
                                {{ formatRoll(frame.secondRoll) }}
                            </span>
                            <span v-if="index === 9" class="roll">
                                {{ formatRoll(frame.thirdRoll) }}
                            </span>
                        </div>
                        <div class="frame-score">{{ frame.frameScore }}</div>
                    </div>
                </div>
                <div class="total-score">
                    合計スコア: <span class="score-value">{{ totalScore }}</span>
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
                        :class="{ 'disabled': loading }"
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

/**
 * ボウリングゲーム画面のメインコンポーネント。
 * ゲームの進行状況表示、スコアボード、投球入力インターフェースを提供する。
 */
export default {
    name: 'Game',
    
    /**
     * コンポーネントのローカル状態を定義する。
     * @returns {Object} コンポーネントの状態
     * @property {number|null} gameId - 現在のゲームID
     * @property {Array} frames - ゲームの全フレーム情報
     * @property {number} currentFrameIndex - 現在のフレーム番号（0-9）
     * @property {number} currentRoll - 現在の投球番号（1-3）
     * @property {boolean} loading - データ読み込み中フラグ
     * @property {string|null} error - エラーメッセージ
     */
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

    /**
     * コンポーネント作成時の初期化処理。
     * 新しいゲームを作成し、フレーム情報を取得する。
     */
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
        /**
         * 現在の合計スコアを計算する。
         * @returns {number} 全フレームのスコアの合計
         */
        totalScore() {
            return this.frames.reduce((total, frame) => total + (frame.frameScore || 0), 0)
        },

        /**
         * 現在投球可能なピン数の配列を計算する。
         * フレームの状態と投球回数に応じて、選択可能なピン数を制限する。
         * @returns {number[]} 選択可能なピン数の配列
         */
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
        /**
         * 投球の表示形式を整形する。
         * @param {number|null} roll - 投球で倒したピン数
         * @returns {string} 表示用の文字列
         */
        formatRoll(roll) {
            if (roll === null) return ''
            if (roll === 10) return 'X'
            if (this.currentRoll === 2 && roll + this.frames[this.currentFrameIndex].firstRoll === 10) return '/'
            return roll.toString()
        },

        /**
         * ストライクかどうかを判定する。
         * @param {number|null} roll - 投球で倒したピン数
         * @returns {boolean} ストライクの場合true
         */
        isStrike(roll) {
            return roll === 10
        },

        /**
         * スペアかどうかを判定する。
         * @param {Object} frame - フレーム情報
         * @returns {boolean} スペアの場合true
         */
        isSpare(frame) {
            return frame.firstRoll + frame.secondRoll === 10 && frame.firstRoll !== 10
        },

        /**
         * 投球を記録し、ゲーム状態を更新する。
         * @param {number} pins - 倒したピン数
         */
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

        /**
         * 新しいゲームを開始する。
         * 全ての状態を初期化し、新しいゲームを作成する。
         */
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
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.loading {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
}

.loading-spinner {
    width: 20px;
    height: 20px;
    border: 3px solid #f3f3f3;
    border-top: 3px solid #3498db;
    border-radius: 50%;
    animation: spin 1s linear infinite;
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
    background: #f5f5f5;
    padding: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.frames {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(70px, 1fr));
    gap: 10px;
    margin-bottom: 20px;
}

.frame {
    border: 1px solid #ddd;
    padding: 10px;
    border-radius: 4px;
    transition: all 0.3s ease;
}

.current-frame {
    background: #e3f2fd;
    border-color: #2196f3;
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
    display: inline-block;
    width: 20px;
    height: 20px;
    text-align: center;
    margin: 0 2px;
    font-weight: bold;
}

.strike { color: #f44336; }
.spare { color: #2196f3; }

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
    padding: 10px 15px;
    margin: 5px;
    border: none;
    border-radius: 4px;
    background: #2196f3;
    color: white;
    cursor: pointer;
    transition: all 0.3s ease;
}

.pin-button:hover {
    background: #1976d2;
}

.pin-button.disabled {
    background: #bdbdbd;
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

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}

@media (max-width: 600px) {
    .frames {
        grid-template-columns: repeat(5, 1fr);
    }
    
    .frame {
        font-size: 14px;
    }
}
</style>
