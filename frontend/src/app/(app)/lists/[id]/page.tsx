"use client";

import { use } from "react";
import { NewReminderInput } from "@/components/NewReminderInput";
import { ReminderRow } from "@/components/ReminderRow";
import { useLists } from "@/lib/queries/lists";
import { useReminders } from "@/lib/queries/reminders";

export default function ListDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const listId = Number(id);

  const { data: lists } = useLists();
  const list = lists?.find((l) => l.id === listId);
  const accent = list?.color ?? "var(--accent)";

  const { data: reminders = [], isLoading } = useReminders(listId);
  const incomplete = reminders.filter((r) => !r.completed);
  const completed = reminders.filter((r) => r.completed);

  return (
    <main className="flex flex-1 flex-col p-10">
      <header className="mb-6">
        <h1 className="text-3xl font-semibold" style={{ color: accent }}>
          {list?.name ?? `리스트 #${listId}`}
        </h1>
      </header>

      <NewReminderInput listId={listId} accentColor={accent} />

      <section className="mt-2 flex flex-col">
        {isLoading && (
          <p className="px-2 py-2 text-sm text-[var(--muted)]">로딩 중…</p>
        )}
        {!isLoading && incomplete.length === 0 && (
          <p className="px-2 py-6 text-sm text-[var(--muted)]">
            미리 알림 없음
          </p>
        )}
        {incomplete.map((r, idx) => {
          const prev = idx > 0 ? incomplete[idx - 1] : null;
          const previousSiblingId = prev ? (prev.parentId ?? prev.id) : null;
          return (
            <ReminderRow
              key={r.id}
              reminder={r}
              accentColor={accent}
              previousSiblingId={previousSiblingId}
            />
          );
        })}
      </section>

      {completed.length > 0 && (
        <section className="mt-8">
          <h2 className="mb-2 px-2 text-xs uppercase tracking-wide text-[var(--muted)]">
            완료됨 ({completed.length})
          </h2>
          {completed.map((r) => (
            <ReminderRow key={r.id} reminder={r} accentColor={accent} />
          ))}
        </section>
      )}
    </main>
  );
}
