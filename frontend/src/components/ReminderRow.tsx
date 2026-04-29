"use client";

import { useState } from "react";
import { Flag, Info, Trash2 } from "lucide-react";
import {
  useDeleteReminder,
  useToggleReminder,
  useUpdateReminder,
} from "@/lib/queries/reminders";
import type { Reminder } from "@/lib/types";
import { formatDueDate } from "@/lib/dueDate";
import { ReminderExpander } from "./ReminderExpander";

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

  const dueLabel = formatDueDate(reminder.dueAt);
  const prefix = PRIORITY_PREFIX[reminder.priority];

  return (
    <div className={`flex flex-col ${reminder.parentId ? "pl-8" : ""}`}>
      <div className="group flex items-start gap-3 rounded-[var(--radius-row)] px-2 py-2 hover:bg-[var(--sidebar-bg)]">
        <button
          aria-label={reminder.completed ? "미완료로 되돌리기" : "완료"}
          onClick={() =>
            toggle.mutate({
              id: reminder.id,
              listId: reminder.listId,
              completed: reminder.completed,
            })
          }
          className="mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full border-2 transition"
          style={{
            borderColor: accentColor,
            background: reminder.completed ? accentColor : "transparent",
          }}
        >
          {reminder.completed && (
            <svg
              width="10"
              height="10"
              viewBox="0 0 10 10"
              fill="none"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                d="M1.5 5.5L4 8L8.5 2"
                stroke="white"
                strokeWidth="1.6"
                strokeLinecap="round"
                strokeLinejoin="round"
              />
            </svg>
          )}
        </button>

        <div
          className="flex-1 min-w-0"
          onClick={() => !editing && setEditing(true)}
        >
          {editing ? (
            <input
              autoFocus
              value={draft}
              onChange={(e) => setDraft(e.target.value)}
              onKeyDown={(e) => {
                if (e.key === "Enter") commitTitle();
                if (e.key === "Escape") {
                  setDraft(reminder.title);
                  setEditing(false);
                }
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
                  // 백엔드는 null = no change 의미라서 outdent 는 현재 미지원.
                  // 향후 'parentIdProvided' 플래그가 추가되면 활성화.
                }
              }}
              onBlur={commitTitle}
              className="w-full bg-transparent text-sm outline-none"
            />
          ) : (
            <p
              className={`text-sm ${
                reminder.completed ? "text-[var(--muted)] line-through" : ""
              }`}
            >
              {prefix && (
                <span className="mr-1" style={{ color: "var(--color-red)" }}>
                  {prefix}
                </span>
              )}
              {reminder.title}
            </p>
          )}

          {dueLabel && (
            <p className="mt-0.5 text-xs text-[var(--muted)]">{dueLabel}</p>
          )}
        </div>

        {reminder.flagged && (
          <Flag
            size={14}
            className="mt-1 shrink-0"
            style={{ color: "var(--color-orange)" }}
            fill="currentColor"
          />
        )}

        <button
          aria-label="상세"
          onClick={() => setExpanded((e) => !e)}
          className="invisible mt-0.5 text-[var(--muted)] group-hover:visible"
        >
          <Info size={14} />
        </button>

        <button
          aria-label="삭제"
          onClick={() => del.mutate({ id: reminder.id, listId: reminder.listId })}
          className="invisible mt-1 text-[var(--muted)] group-hover:visible"
        >
          <Trash2 size={14} />
        </button>
      </div>

      {expanded && <ReminderExpander reminder={reminder} />}
    </div>
  );
}
