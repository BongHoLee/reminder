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
      update.mutate({
        id: reminder.id,
        listId: reminder.listId,
        parentId: previousSiblingId,
      });
    }
    if (e.key === "Tab" && e.shiftKey && reminder.parentId) {
      e.preventDefault();
      update.mutate({
        id: reminder.id,
        listId: reminder.listId,
        parentIdClear: true,
      });
    }
  };
}
