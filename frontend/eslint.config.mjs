import next from "eslint-config-next";

const config = [
  ...next,
  {
    ignores: ["node_modules/**", ".next/**", "coverage/**", "next-env.d.ts"],
  },
];

export default config;
