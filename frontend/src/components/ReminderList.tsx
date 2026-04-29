"use client";

import { ReminderRow } from "./ReminderRow";
import type { Reminder } from "@/lib/types";

export function ReminderList({
  reminders,
  accentColor,
  emptyText = "미리 알림 없음",
}: {
  reminders: Reminder[];
  accentColor: string;
  emptyText?: string;
}) {
  if (reminders.length === 0) {
    return (
      <p className="px-2 py-6 text-sm text-[var(--muted)]">{emptyText}</p>
    );
  }
  return (
    <div className="flex flex-col">
      {reminders.map((r) => (
        <ReminderRow key={r.id} reminder={r} accentColor={accentColor} />
      ))}
    </div>
  );
}
