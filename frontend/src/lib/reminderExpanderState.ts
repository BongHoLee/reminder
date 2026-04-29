import type { Priority, Reminder } from "./types";

export type ExpanderFormState = {
  notes: string;
  dueAtLocal: string;
  priority: Priority;
  flagged: boolean;
};

export type ExpanderPatchPayload = {
  notes: string | null;
  dueAt: string | null;
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

/** 폼 state → 백엔드 PATCH 바디. */
export function buildPatchPayload(form: ExpanderFormState): ExpanderPatchPayload {
  return {
    notes: form.notes === "" ? null : form.notes,
    dueAt: form.dueAtLocal ? new Date(form.dueAtLocal).toISOString() : null,
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
