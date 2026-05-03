import { describe, expect, it } from "vitest";
import { formatDueDate } from "./dueDate";

// 모든 케이스에서 now 를 명시 주입해 시스템 시계 영향 제거.
// 테스트 환경 timezone 은 vitest 의 기본 (시스템) — 케이스는 KST 를 가정한다 (TZ=Asia/Seoul 또는 시스템).

describe("formatDueDate", () => {
  it("null/undefined 는 null", () => {
    expect(formatDueDate(null)).toBeNull();
    expect(formatDueDate(undefined)).toBeNull();
  });

  it("잘못된 ISO 는 null", () => {
    expect(formatDueDate("not-a-date")).toBeNull();
  });

  it("오늘 마감은 '오늘, 시각' 형식", () => {
    const now = new Date("2026-04-29T12:00:00+09:00");
    const due = "2026-04-29T15:00:00+09:00";
    expect(formatDueDate(due, now)).toMatch(/^오늘, /);
  });

  it("내일 마감은 '내일, 시각' 형식", () => {
    const now = new Date("2026-04-29T12:00:00+09:00");
    const due = "2026-04-30T09:30:00+09:00";
    expect(formatDueDate(due, now)).toMatch(/^내일, /);
  });

  it("어제 마감은 '어제, 시각' 형식", () => {
    const now = new Date("2026-04-29T12:00:00+09:00");
    const due = "2026-04-28T20:00:00+09:00";
    expect(formatDueDate(due, now)).toMatch(/^어제, /);
  });

  it("미래 (모레 이상) 는 long date 포맷", () => {
    const now = new Date("2026-04-29T12:00:00+09:00");
    const due = "2026-05-01T10:00:00+09:00";
    const out = formatDueDate(due, now);
    expect(out).not.toMatch(/^(오늘|내일|어제),/);
    // ko-KR long 포맷은 "년" 또는 "월" 을 반드시 포함.
    expect(out).toMatch(/년|월/);
  });

  it("KST 자정 직후 (00:00:30) 도 '오늘' 그룹에 들어간다", () => {
    const now = new Date("2026-04-29T00:00:30+09:00");
    const due = "2026-04-29T15:00:00+09:00";
    expect(formatDueDate(due, now)).toMatch(/^오늘, /);
  });
});
