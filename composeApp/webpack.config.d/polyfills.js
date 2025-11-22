
// Webpack 5 does not include polyfills for node.js core modules by default.
// This configuration tells webpack to ignore these modules as they are not needed for the browser
// or to use empty implementations.

config.resolve = {
    ...config.resolve,
    fallback: {
        ...config.resolve?.fallback,
        "zlib": false,
        "stream": false,
        "net": false,
        "tls": false,
        "crypto": false,
        "http": false,
        "https": false,
        "url": false,
        "fs": false,
        "path": false,
        "os": false,
        "bufferutil": false,
        "utf-8-validate": false,
        "buffer": false
    }
};

