const webpack = require('webpack');
const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const { spawn } = require('child_process');

// ...


// Config directories
const SRC_DIR = path.resolve(__dirname, 'src');
const OUTPUT_DIR = path.resolve(__dirname, 'dist');

// Any directories you will be adding code/files into, need to be added to this array so webpack will pick them up
const defaultInclude = [ SRC_DIR ];

module.exports = {
  entry: [
    'react-hot-loader/patch', // activate HMR for React
    SRC_DIR + '/index.js' // main entry point for app
  ],
  resolve: {
    alias: {
      common: path.resolve(__dirname, 'src/common/'),
      'viewserver-client': path.resolve(__dirname, 'src/viewserver-client/'),
      'custom-redux': path.resolve(__dirname, 'src/redux/'),
      'components': path.resolve(__dirname, 'src/components/'),
      'canv-grid': path.resolve(__dirname, 'src/components/common/grid'),
      'global-actions': path.resolve(__dirname, 'src/actions'),
      'dao': path.resolve(__dirname, 'src/dao'),
      'common-components': path.resolve(__dirname, 'src/components/common'),
    }
  },
  output: {
    path: OUTPUT_DIR,
    libraryTarget: 'umd',
    publicPath: '/',
    filename: 'bundle.js'
  },
  module: {
    rules: [
      { test: /\.css$/, use: [
        { loader: 'style-loader' },
        { loader: 'css-loader' }
      ], include: defaultInclude },
      { test: /\.less$/, use: [
        { loader: 'style-loader' },
        { loader: 'css-loader', options: { modules: true } },
        { loader: 'less-loader' }
      ], include: defaultInclude },
      { test: /\.js?$/, use: [
        { loader: 'babel-loader', options: { forceEnv: 'development' } }
      ], include: defaultInclude },
      { test: /\.(jpe?g|png|gif)$/, use: [
        { loader: 'file-loader?name=img/[name]__[hash:base64:5].[ext]' }
      ], include: defaultInclude },
      { test: /\.(eot|svg|ttf|woff|woff2)$/, use: [
        { loader: 'file-loader?name=font/[name]__[hash:base64:5].[ext]' }
      ], include: defaultInclude }
    ]
  },
  target: 'electron-renderer',
  plugins: [
    new HtmlWebpackPlugin(),
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify('development')
		}),
    new webpack.NamedModulesPlugin()
  ],
  devtool: "cheap-source-map",
  devServer: {
    contentBase: OUTPUT_DIR,
    stats: {
      colors: true,
      chunks: false,
      children: false
    },
    setup() {
      spawn(
        'electron',
        ['.'],
        { shell: true, env: process.env, stdio: 'inherit' }
      )
      .on('close', code => process.exit(0))
      .on('error', spawnError => console.error(spawnError));
    }
  }
};
