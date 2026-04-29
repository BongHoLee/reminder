"use client";

import { useEffect, useRef, useState } from "react";
import { useUpdateReminder } from "@/lib/queries/reminders";
import type { Priority, Reminder } from "@/lib/types";

const PRIORITIES: { value: Priority; label: string }[] = [
  { value: "NONE", label: "없음" },
  { value: "LOW", label: "낮음 !" },
  { value: "MEDIUM", label: "중간 !!" },
  { value: "HIGH", label: "높음 !!!" },
];

export function ReminderExpander({ reminder }: { reminder: Reminder }) {
  const [notes, setNotes] = useState(reminder.notes ?? "");
  const [dueAtLocal, setDueAtLocal] = useState(toLocalInput(reminder.dueAt));
  const [priority, setPriority] = useState<Priority>(reminder.priority);
  const [flagged, setFlagged] = useState(reminder.flagged);

  const update = useUpdateReminder();
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // 변경이 발생할 때마다 500ms 디바운스 후 PATCH
  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      update.mutate({
        id: reminder.id,
        listId: reminder.listId,
        notes: notes === "" ? null : notes,
        dueAt: dueAtLocal ? new Date(dueAtLocal).toISOString() : null,
        priority,
        flagged,
      });
    }, 500);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [notes, dueAtLocal, priority, flagged]);

  return (
    <div className="ml-8 mr-2 mb-2 rounded-[var(--radius-row)] border border-[var(--border)] bg-[var(--sidebar-bg)] p-3">
      <div className="grid grid-cols-2 gap-3 mb-3">
        <label className="flex flex-col gap-1 text-xs text-[var(--muted)]">
          마감일
          <input
            type="datetime-local"
            value={dueAtLocal}
            onChange={(e) => setDueAtLocal(e.target.value)}
            className="rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-2 py-1 text-sm outline-none"
          />
        </label>

        <label className="flex flex-col gap-1 text-xs text-[var(--muted)]">
          우선순위
          <select
            value={priority}
            onChange={(e) => setPriority(e.target.value as Priority)}
            className="rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-2 py-1 text-sm outline-none"
          >
            {PRIORITIES.map((p) => (
              <option key={p.value} value={p.value}>
                {p.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      <label className="mb-3 flex items-center gap-2 text-sm">
        <input
          type="checkbox"
          checked={flagged}
          onChange={(e) => setFlagged(e.target.checked)}
        />
        깃발 표시
      </label>

      <label className="flex flex-col gap-1 text-xs text-[var(--muted)]">
        메모
        <textarea
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          rows={3}
          className="resize-y rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-2 py-1 text-sm outline-none"
        />
      </label>
    </div>
  );
}

function toLocalInput(iso: string | null | undefined): string {
  if (!iso) return "";
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return "";
  const pad = (n: number) => n.toString().padStart(2, "0");
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
}
