const path = require('path');
const { IgnorePlugin } = require('webpack');
const TerserPlugin = require('terser-webpack-plugin');
/** @type {import('webpack').Configuration} */
module.exports = {
  entry: './jsdom-exporter.js',
  target: 'node',
  mode: 'production',
  output: {
    library: {
      type: 'commonjs2'
    },
    filename: 'jsdom-exporter-bundle.js',
    path: path.resolve(__dirname, 'src/main/resources/META-INF/frontend/generated/')
  },
  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin({
        extractComments: false
      })
    ]
  },

  plugins: [
    new IgnorePlugin({ resourceRegExp: /canvas/ }),
    new IgnorePlugin({ resourceRegExp: /utf-8-validate/ }),
    new IgnorePlugin({ resourceRegExp: /bufferutil/ }),
  ]
}