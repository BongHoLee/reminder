"use client";

import { useEffect, useRef, useState } from "react";
import { useUpdateReminder } from "@/lib/queries/reminders";
import {
  buildPatchPayload,
  reminderToFormState,
  type ExpanderFormState,
} from "@/lib/reminderExpanderState";
import type { Priority, Reminder } from "@/lib/types";

const PRIORITIES: { value: Priority; label: string }[] = [
  { value: "NONE", label: "없음" },
  { value: "LOW", label: "낮음 !" },
  { value: "MEDIUM", label: "중간 !!" },
  { value: "HIGH", label: "높음 !!!" },
];

export function ReminderExpander({ reminder }: { reminder: Reminder }) {
  const [form, setForm] = useState<ExpanderFormState>(() => reminderToFormState(reminder));
  const [isDirty, setIsDirty] = useState(false);
  const update = useUpdateReminder();
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // 부모가 새 reminder 데이터를 내려주면 (refetch 등) 폼 state 동기화 + dirty 리셋
  useEffect(() => {
    setForm(reminderToFormState(reminder));
    setIsDirty(false);
  }, [reminder.id, reminder.updatedAt]);

  // 사용자 변경이 있을 때만 500ms debounce 후 PATCH (마운트 시점은 스킵)
  useEffect(() => {
    if (!isDirty) return;
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      update.mutate({
        id: reminder.id,
        listId: reminder.listId,
        ...buildPatchPayload(form),
      });
    }, 500);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [form, isDirty]);

  function patch(partial: Partial<ExpanderFormState>) {
    setForm((prev) => ({ ...prev, ...partial }));
    setIsDirty(true);
  }

  return (
    <div className="ml-8 mr-2 mb-2 rounded-[var(--radius-row)] border border-[var(--border)] bg-[var(--sidebar-bg)] p-3">
      <div className="grid grid-cols-2 gap-3 mb-3">
        <label className="flex flex-col gap-1 text-xs text-[var(--muted)]">
          마감일
          <input
            type="datetime-local"
            value={form.dueAtLocal}
            onChange={(e) => patch({ dueAtLocal: e.target.value })}
            className="rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-2 py-1 text-sm outline-none"
          />
        </label>

        <label className="flex flex-col gap-1 text-xs text-[var(--muted)]">
          우선순위
          <select
            value={form.priority}
            onChange={(e) => patch({ priority: e.target.value as Priority })}
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
          checked={form.flagged}
          onChange={(e) => patch({ flagged: e.target.checked })}
        />
        깃발 표시
      </label>

      <label className="flex flex-col gap-1 text-xs text-[var(--muted)]">
        메모
        <textarea
          value={form.notes}
          onChange={(e) => patch({ notes: e.target.value })}
          rows={3}
          className="resize-y rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-2 py-1 text-sm outline-none"
        />
      </label>
    </div>
  );
}
