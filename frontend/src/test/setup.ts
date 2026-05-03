import "@testing-library/dom";
import { afterEach } from "vitest";
import { cleanup } from "@testing-library/react";

// 각 테스트 사이에 DOM 잔존 컴포넌트 정리.
afterEach(() => {
  cleanup();
});
