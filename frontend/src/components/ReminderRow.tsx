"use client";

import { useState } from "react";
import { Info, Trash2 } from "lucide-react";
import {
  useDeleteReminder,
  useToggleReminder,
  useUpdateReminder,
} from "@/lib/queries/reminders";
import type { Reminder } from "@/lib/types";
import { formatDueDate } from "@/lib/dueDate";
import { useReminderRowKeys } from "@/hooks/useReminderRowKeys";
import { ReminderExpander } from "./ReminderExpander";
import { RowCheckbox } from "./RowCheckbox";
import { RowMeta } from "./RowMeta";

const PRIORITY_PREFIX: Record<Reminder["priority"], string> = {
  NONE: "",
  LOW: "!",
  MEDIUM: "!!",
  HIGH: "!!!",
};

export function ReminderRow({
  reminder,
  accentColor,
  previousSiblingId,
}: {
  reminder: Reminder;
  accentColor: string;
  /** 같은 깊이의 직전 형제 reminder id — Tab 으로 indent 시 parent 로 사용 */
  previousSiblingId?: number | null;
}) {
  const toggle = useToggleReminder();
  const update = useUpdateReminder();
  const del = useDeleteReminder();

  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState(reminder.title);
  const [expanded, setExpanded] = useState(false);

  async function commitTitle() {
    const trimmed = draft.trim();
    if (trimmed && trimmed !== reminder.title) {
      await update.mutateAsync({
        id: reminder.id,
        listId: reminder.listId,
        title: trimmed,
      });
    } else {
      setDraft(reminder.title);
    }
    setEditing(false);
  }

  function cancelEdit() {
    setDraft(reminder.title);
    setEditing(false);
  }

  const handleKeyDown = useReminderRowKeys({
    reminder,
    previousSiblingId,
    update,
    onCommitTitle: commitTitle,
    onCancelEdit: cancelEdit,
  });

  const dueLabel = formatDueDate(reminder.dueAt);
  const prefix = PRIORITY_PREFIX[reminder.priority];

  return (
    <div className={`flex flex-col ${reminder.parentId ? "pl-8" : ""}`}>
      <div className="group flex items-start gap-3 rounded-[var(--radius-row)] px-2 py-2 hover:bg-[var(--sidebar-bg)]">
        <RowCheckbox
          completed={reminder.completed}
          accentColor={accentColor}
          onToggle={() =>
            toggle.mutate({
              id: reminder.id,
              listId: reminder.listId,
            })
          }
        />

        <div className="flex-1 min-w-0" onClick={() => !editing && setEditing(true)}>
          {editing ? (
            <input
              autoFocus
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={handleKeyDown}
              onBlur={commitTitle}
              className="w-full bg-transparent text-sm outline-none"
            />
          ) : (
            <p
              className={`text-sm ${
                reminder.completed ? "text-[var(--muted)] line-through" : ""
              }`}
            >
              {prefix && <span className="mr-1 text-red">{prefix}</span>}
              {reminder.title}
            </p>
          )}

          {dueLabel && <p className="mt-0.5 text-xs text-[var(--muted)]">{dueLabel}</p>}
        </div>

        <RowMeta flagged={reminder.flagged} />

        <button
          aria-label="상세"
          onClick={() => setExpanded((e) => !e)}
          className="invisible mt-0.5 text-[var(--muted)] group-hover:visible group-focus-within:visible focus:visible"
        >
          <Info size={14} />
        </button>

        <button
          aria-label="삭제"
          onClick={() => del.mutate({ id: reminder.id, listId: reminder.listId })}
          className="invisible mt-1 text-[var(--muted)] group-hover:visible group-focus-within:visible focus:visible"
        >
          <Trash2 size={14} />
        </button>
      </div>

      {expanded && <ReminderExpander reminder={reminder} />}
    </div>
  );
}
