module.exports = {
    devServer: {
        port: 8080,
        proxy: {
            '/api': {
                target: 'http://backend:8081',
                changeOrigin: true
            }
        }
    },
    publicPath: process.env.NODE_ENV === 'production' ? '/' : '/'
}
