import type { Priority, Reminder } from "./types";

export type ExpanderFormState = {
  notes: string;
  dueAtLocal: string;
  priority: Priority;
  flagged: boolean;
};

export type ExpanderPatchPayload = {
  notes?: string | null;
  notesClear?: boolean;
  dueAt?: string | null;
  dueAtClear?: boolean;
  priority: Priority;
  flagged: boolean;
};

/** Reminder → datetime-local input 포맷의 폼 state. */
export function reminderToFormState(reminder: Reminder): ExpanderFormState {
  return {
    notes: reminder.notes ?? "",
    dueAtLocal: toLocalInput(reminder.dueAt),
    priority: reminder.priority,
    flagged: reminder.flagged,
  };
}

/** 폼 state → 백엔드 PATCH 바디. 빈 값은 *Clear=true 로 명시 비움. */
export function buildPatchPayload(form: ExpanderFormState): ExpanderPatchPayload {
  return {
    ...(form.notes === "" ? { notesClear: true } : { notes: form.notes }),
    ...(form.dueAtLocal === ""
      ? { dueAtClear: true }
      : { dueAt: new Date(form.dueAtLocal).toISOString() }),
    priority: form.priority,
    flagged: form.flagged,
  };
}

function toLocalInput(iso: string | null | undefined): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
