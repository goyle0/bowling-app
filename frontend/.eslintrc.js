module.exports = {
    root: true,
    env: {
        browser: true,
        node: true
    },
    extends: [
        'plugin:vue/vue3-recommended',
        'eslint:recommended'
    ],
    parserOptions: {
        ecmaVersion: 2020
    },
    rules: {
        'vue/multi-word-component-names': 'off'
    }
}