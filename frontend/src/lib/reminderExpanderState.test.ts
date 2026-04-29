import { describe, expect, it } from "vitest";
import {
  buildPatchPayload,
  reminderToFormState,
  type ExpanderFormState,
} from "./reminderExpanderState";
import type { Reminder } from "./types";

const baseReminder: Reminder = {
  id: 10,
  listId: 1,
  parentId: null,
  title: "원본",
  notes: "메모",
  dueAt: "2026-04-30T08:00:00Z",
  priority: "MEDIUM",
  completed: false,
  completedAt: null,
  flagged: false,
  sortOrder: 0,
  createdAt: "2026-04-29T00:00:00Z",
  updatedAt: "2026-04-29T00:00:00Z",
};

describe("reminderToFormState", () => {
  it("reminder 의 notes/dueAt/priority/flagged 를 폼 초기 state 로 변환한다", () => {
    const s = reminderToFormState(baseReminder);
    expect(s.notes).toBe("메모");
    expect(s.priority).toBe("MEDIUM");
    expect(s.flagged).toBe(false);
    // dueAt 은 datetime-local input 포맷 (브라우저 timezone 기준이라 형식만 확인)
    expect(s.dueAtLocal).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/);
  });

  it("notes 가 null 이면 빈 문자열로 변환", () => {
    const s = reminderToFormState({ ...baseReminder, notes: null });
    expect(s.notes).toBe("");
  });

  it("dueAt 이 null 이면 빈 문자열", () => {
    const s = reminderToFormState({ ...baseReminder, dueAt: null });
    expect(s.dueAtLocal).toBe("");
  });
});

describe("buildPatchPayload", () => {
  const form: ExpanderFormState = {
    notes: "수정된 메모",
    dueAtLocal: "2026-05-01T09:00",
    priority: "HIGH",
    flagged: true,
  };

  it("폼 state 를 PATCH 페이로드로 변환한다", () => {
    const p = buildPatchPayload(form);
    expect(p.notes).toBe("수정된 메모");
    expect(p.priority).toBe("HIGH");
    expect(p.flagged).toBe(true);
    expect(p.dueAt).toMatch(/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}/);
  });

  it("notes 가 빈 문자열이면 null 로 보낸다 (의도적 clear 인지 노출)", () => {
    const p = buildPatchPayload({ ...form, notes: "" });
    expect(p.notes).toBeNull();
  });

  it("dueAtLocal 이 빈 문자열이면 dueAt: null", () => {
    const p = buildPatchPayload({ ...form, dueAtLocal: "" });
    expect(p.dueAt).toBeNull();
  });
});
