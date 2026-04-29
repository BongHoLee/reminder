import { describe, expect, it } from "vitest";
import { groupByLocalDate } from "./groupByLocalDate";
import type { Reminder } from "./types";

function r(id: number, dueAt: string | null): Reminder {
  return {
    id,
    listId: 1,
    parentId: null,
    title: `r${id}`,
    notes: null,
    dueAt,
    priority: "NONE",
    completed: false,
    completedAt: null,
    flagged: false,
    sortOrder: 0,
    createdAt: "2026-04-29T00:00:00Z",
    updatedAt: "2026-04-29T00:00:00Z",
  };
}

describe("groupByLocalDate", () => {
  it("KST 기준으로 5/1 새벽 0시(UTC 4/30 15:00)는 5/1 그룹에 들어간다", () => {
    const reminders = [r(1, "2026-04-30T15:00:00Z")]; // KST 5/1 00:00
    const groups = groupByLocalDate(reminders, "Asia/Seoul");
    expect([...groups.keys()]).toEqual(["2026-05-01"]);
  });

  it("KST 4/30 23:59(UTC 14:59)와 5/1 00:00(UTC 15:00)는 다른 그룹", () => {
    const reminders = [
      r(1, "2026-04-30T14:59:00Z"), // KST 4/30 23:59
      r(2, "2026-04-30T15:00:00Z"), // KST 5/1 00:00
    ];
    const groups = groupByLocalDate(reminders, "Asia/Seoul");
    expect([...groups.keys()].sort()).toEqual(["2026-04-30", "2026-05-01"]);
    expect(groups.get("2026-04-30")?.map((r) => r.id)).toEqual([1]);
    expect(groups.get("2026-05-01")?.map((r) => r.id)).toEqual([2]);
  });

  it("dueAt 이 null 이면 'no-date' 키", () => {
    const groups = groupByLocalDate([r(1, null)], "Asia/Seoul");
    expect(groups.get("no-date")?.map((r) => r.id)).toEqual([1]);
  });

  it("같은 날 reminder 는 입력 순서대로 한 배열로 묶인다", () => {
    const reminders = [
      r(1, "2026-04-30T18:00:00Z"), // KST 5/1 03:00
      r(2, "2026-04-30T20:00:00Z"), // KST 5/1 05:00
    ];
    const groups = groupByLocalDate(reminders, "Asia/Seoul");
    expect(groups.get("2026-05-01")?.map((r) => r.id)).toEqual([1, 2]);
  });

  it("UTC timeZone 기준이면 UTC 날짜로 분리", () => {
    const reminders = [r(1, "2026-04-30T15:00:00Z")];
    const groups = groupByLocalDate(reminders, "UTC");
    expect([...groups.keys()]).toEqual(["2026-04-30"]);
  });
});
