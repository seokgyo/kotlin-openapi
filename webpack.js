const path = require('path');

module.exports = {
    entry: {
        'swagger-ui': path.resolve(__dirname, 'src/main/js/swagger-ui/index.js'),
    },
    module: {
        rules: [
            {
                test: /\.m?js$/,
                exclude: /(node_modules)/,
                use: [{
                    loader: 'babel-loader',
                    options: {
                        presets: ["@babel/preset-env", "@babel/preset-react"],
                    }
                }]
            }, {
                test: /\.css$/i,
                use: ['style-loader', 'css-loader'],
            }
        ]
    },
    output: {
        path: path.resolve(__dirname, 'src/main/resources/static/built'),
        filename: '[name].bundle.js'
    },
    mode: 'production',
    devtool: 'source-map',
};
