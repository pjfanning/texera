// Karma configuration file
module.exports = function (config) {
  config.set({
    basePath: "",
    frameworks: ["jasmine", "@angular-devkit/build-angular"],
    plugins: [
      require("karma-jasmine"),
      require("karma-chrome-launcher"),
      require("@angular-devkit/build-angular/plugins/karma")
    ],
    client: {
      clearContext: config.singleRun, // Leave Jasmine Spec Runner output visible in the browser
      jasmine: {
        random: false, // Disable random order for consistent test results
      },
    },
    customLaunchers: {
      ChromeHeadlessCustom: {
        base: "ChromeHeadless",
        flags: [
          "--no-sandbox",
          "--headless",
          "--disable-gpu",
          "--disable-translate",
          "--disable-extensions",
          "--remote-debugging-port=9222", // Enable remote debugging for better error output
          "--disable-dev-shm-usage", // Avoid /dev/shm issues in CI environments
        ],
      },
    },
    reporters: ["dots"], // Use basic progress and HTML reporters
    port: 9876, // Karma's web server port
    colors: true, // Enable colors in the output (reporters and logs)
    logLevel: config.LOG_DEBUG, // Set log level
    autoWatch: false, // Disable auto-watch to prevent re-runs in CI
    browsers: ["ChromeHeadlessCustom"], // Run tests in headless Chrome
    singleRun: true, // Ensure Karma exits after running tests once (useful for CI)
    restartOnFileChange: false, // Disable file change restarts in CI
    captureTimeout: 10000, // 10-second timeout for capturing the browser
    browserDisconnectTimeout: 10000, // 10-second disconnect timeout
    browserDisconnectTolerance: 3, // Allow up to 3 disconnects before failing
    browserNoActivityTimeout: 10000, // 10-second no-activity timeout
  });
};
