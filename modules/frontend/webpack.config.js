const ScalaJS = require("./scalajs.webpack.config");
const merge = require('webpack-merge')
var HtmlWebpackPlugin = require('html-webpack-plugin');

const WebApp = merge(ScalaJS, {
  devServer: {
    proxy : {
      '/api': 'http://localhost:8080',
    },
    watchContentBase: true,
    hot: false,
    hotOnly: false, // only reload when build is successful
    inline: true // show build errors in browser console
  },
  watchOptions: {
    //Files or folders that are not monitored
    ignored: /node_modules/
  }
});

module.exports = WebApp;
module.exports.plugins = [
  new HtmlWebpackPlugin({
    title: "cask-laminar",
    template: './index.html'
  })
];