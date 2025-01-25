<template>
    <div class="home">
        <h1>ボウリングスコア管理アプリ</h1>
        <div class="actions">
            <router-link to="/game" class="start-button">
                新しいゲームを開始
            </router-link>
        </div>
        <div class="instructions">
            <h2>使い方</h2>
            <ul>
                <li>「新しいゲームを開始」をクリックしてゲームを始めます</li>
                <li>各フレームで投球した際のピンの数を入力します</li>
                <li>スコアは自動的に計算されます</li>
                <li>ストライクやスペアの場合、次の投球の結果によってボーナスが追加されます</li>
            </ul>
        </div>
    </div>
</template>

<script>
/**
 * アプリケーションのホーム画面コンポーネント。
 * ユーザーに以下の機能を提供する：
 * - 新しいゲームの開始
 * - アプリケーションの使用方法の説明
 * - ボウリングスコアの計算ルールの概要
 */
export default {
    name: 'Home'
}
</script>

<style scoped>
.home {
    max-width: 800px;
    margin: 0 auto;
    padding: 20px;
}

.actions {
    margin: 40px 0;
}

.start-button {
    display: inline-block;
    background-color: #42b983;
    color: white;
    padding: 15px 30px;
    border-radius: 4px;
    text-decoration: none;
    font-size: 1.2em;
    transition: background-color 0.3s;
}

.start-button:hover {
    background-color: #3aa876;
}

.instructions {
    text-align: left;
    background-color: #f5f5f5;
    padding: 20px;
    border-radius: 4px;
    margin-top: 40px;
}

.instructions h2 {
    color: #2c3e50;
    margin-bottom: 20px;
}

.instructions ul {
    padding-left: 20px;
}

.instructions li {
    margin-bottom: 10px;
    color: #666;
}
</style>
