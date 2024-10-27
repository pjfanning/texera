export default {
  preset: "jest-preset-angular",
  setupFilesAfterEnv: [`${__dirname}/setup.ts`],
  moduleNameMapper: {
    "\\.(jpg|jpeg|png)$": `${__dirname}/mock-module.js`,
  },
};
