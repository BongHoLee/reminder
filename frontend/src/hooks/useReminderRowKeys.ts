"use client";

import type { KeyboardEvent } from "react";
import type { Reminder } from "@/lib/types";
import type { useUpdateReminder } from "@/lib/queries/reminders";

type UpdateMutation = ReturnType<typeof useUpdateReminder>;

type UseReminderRowKeysParams = {
  reminder: Reminder;
  previousSiblingId?: number | null;
  update: UpdateMutation;
  onCommitTitle: () => void;
  onCancelEdit: () => void;
};

// 인라인 편집 입력의 키 핸들러: Enter 커밋, Esc 취소, Tab indent / Shift+Tab outdent.
// Tab indent/outdent 시 현재 제목 편집 중이라면 먼저 commit (draft 손실 방지) 후 mutate. 입력은 blur (편집 종료).
export function useReminderRowKeys({
  reminder,
  previousSiblingId,
  update,
  onCommitTitle,
  onCancelEdit,
}: UseReminderRowKeysParams) {
  return function handleKeyDown(e: KeyboardEvent<HTMLInputElement>) {
    if (e.key === "Enter") onCommitTitle();
    if (e.key === "Escape") onCancelEdit();
    if (e.key === "Tab" && !e.shiftKey && previousSiblingId) {
      e.preventDefault();
      onCommitTitle();
      e.currentTarget.blur();
      update.mutate({
        id: reminder.id,
        listId: reminder.listId,
        parentId: previousSiblingId,
      });
    }
    if (e.key === "Tab" && e.shiftKey && reminder.parentId) {
      e.preventDefault();
      onCommitTitle();
      e.currentTarget.blur();
      update.mutate({
        id: reminder.id,
        listId: reminder.listId,
        parentIdClear: true,
      });
    }
  };
}
