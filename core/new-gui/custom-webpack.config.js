module.exports = {
  module: {
    rules: [
      {
        test: /\.css$/,
        use: ["style-loader", "css-loader"],
        include: [require("path").resolve(__dirname, "node_modules/monaco-editor")],
        include: [require("path").resolve(__dirname, "node_modules/vscode")],
      },
      {
          test: /\.(mp3|wasm|ttf)$/i,
          type: 'asset/resource'
      }
    ],
    parser: {
      javascript: {
          url: true
      }
    }
  }
};
