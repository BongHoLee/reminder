"use client";

import { useState } from "react";
import { useCreateReminder } from "@/lib/queries/reminders";

export function NewReminderInput({
  listId,
  accentColor,
}: {
  listId: number;
  accentColor: string;
}) {
  const [title, setTitle] = useState("");
  const create = useCreateReminder();

  async function submit() {
    const trimmed = title.trim();
    if (!trimmed) return;
    await create.mutateAsync({ listId, title: trimmed });
    setTitle("");
  }

  return (
    <div className="flex items-center gap-3 rounded-[var(--radius-row)] px-2 py-2">
      <span
        className="h-5 w-5 shrink-0 rounded-full border-2"
        style={{ borderColor: accentColor }}
      />
      <input
        value={title}
        onChange={(e) => setTitle(e.target.value)}
        onKeyDown={(e) => {
          if (e.key === "Enter") submit();
        }}
        placeholder="새 미리 알림"
        className="flex-1 bg-transparent text-sm outline-none placeholder:text-[var(--muted)]"
      />
    </div>
  );
}
