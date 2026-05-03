import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // lucide-react 는 tree-shaking 이 약해 default import 만으로도 전체 번들이 끌려옴 → 명시적 최적화 대상으로 등록.
  experimental: {
    optimizePackageImports: ["lucide-react"],
  },
};

export default nextConfig;
