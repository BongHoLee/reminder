"use client";

import { use } from "react";
import { notFound } from "next/navigation";
import { ReminderList } from "@/components/ReminderList";
import { groupByLocalDate } from "@/lib/groupByLocalDate";
import { useSmartView } from "@/lib/queries/views";
import type { Reminder, SmartViewType } from "@/lib/types";

const VALID: Record<SmartViewType, { label: string; color: string }> = {
  today: { label: "오늘", color: "#0A84FF" },
  scheduled: { label: "예정", color: "#FF3B30" },
  all: { label: "전체", color: "#1C1C1E" },
  flagged: { label: "깃발 표시", color: "#FF9500" },
  completed: { label: "완료됨", color: "#8E8E93" },
};

export default function SmartViewPage({
  params,
}: {
  params: Promise<{ type: string }>;
}) {
  const { type } = use(params);
  const meta = VALID[type as SmartViewType];
  if (!meta) notFound();

  const { data = [], isLoading } = useSmartView(type as SmartViewType);

  return (
    <main className="flex flex-1 flex-col p-10">
      <header className="mb-6">
        <h1 className="text-3xl font-semibold" style={{ color: meta.color }}>
          {meta.label}
        </h1>
      </header>

      {isLoading ? (
        <p className="px-2 py-2 text-sm text-[var(--muted)]">로딩 중…</p>
      ) : type === "scheduled" ? (
        <ScheduledGrouped reminders={data} accentColor={meta.color} />
      ) : (
        <ReminderList reminders={data} accentColor={meta.color} />
      )}
    </main>
  );
}

function ScheduledGrouped({
  reminders,
  accentColor,
}: {
  reminders: Reminder[];
  accentColor: string;
}) {
  if (reminders.length === 0) {
    return <p className="px-2 py-6 text-sm text-[var(--muted)]">예정된 항목 없음</p>;
  }

  const tz =
    Intl.DateTimeFormat().resolvedOptions().timeZone ?? "UTC";
  const groups = groupByLocalDate(reminders, tz);

  return (
    <div className="flex flex-col gap-6">
      {Array.from(groups.entries()).map(([key, group]) => {
        const label =
          key === "no-date"
            ? "날짜 없음"
            : new Intl.DateTimeFormat("ko-KR", {
                year: "numeric",
                month: "long",
                day: "numeric",
                weekday: "long",
                timeZone: tz,
              }).format(new Date(`${key}T00:00:00`));
        return (
          <section key={key}>
            <h2 className="mb-2 px-2 text-xs uppercase tracking-wide text-[var(--muted)]">
              {label}
            </h2>
            <ReminderList reminders={group} accentColor={accentColor} />
          </section>
        );
      })}
    </div>
  );
}
