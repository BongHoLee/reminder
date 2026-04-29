"use client";

import Link from "next/link";
import { Calendar, Clock, Inbox, Flag, CheckCircle2 } from "lucide-react";
import type { LucideIcon } from "lucide-react";
import { useSmartViewCounts } from "@/lib/queries/views";
import type { SmartViewType } from "@/lib/types";

type Card = {
  type: SmartViewType;
  label: string;
  color: string;
  icon: LucideIcon;
};

const CARDS: Card[] = [
  { type: "today", label: "오늘", color: "#0A84FF", icon: Calendar },
  { type: "scheduled", label: "예정", color: "#FF3B30", icon: Clock },
  { type: "all", label: "전체", color: "#1C1C1E", icon: Inbox },
  { type: "flagged", label: "깃발 표시", color: "#FF9500", icon: Flag },
  { type: "completed", label: "완료됨", color: "#8E8E93", icon: CheckCircle2 },
];

export function SmartListGrid() {
  const { data: counts } = useSmartViewCounts();

  return (
    <div className="grid grid-cols-2 gap-2">
      {CARDS.map((c) => {
        const value = counts ? counts[c.type] : 0;
        const Icon = c.icon;
        return (
          <Link
            key={c.type}
            href={`/views/${c.type}`}
            className="flex flex-col rounded-[var(--radius-card)] bg-[var(--background)] px-3 py-2 transition hover:opacity-90"
          >
            <div className="flex items-center justify-between">
              <span
                className="grid h-7 w-7 place-items-center rounded-full text-white"
                style={{ background: c.color }}
              >
                <Icon size={14} />
              </span>
              <span className="text-2xl font-semibold tabular-nums">
                {counts ? value : ""}
              </span>
            </div>
            <span className="mt-1 text-xs text-[var(--muted)]">{c.label}</span>
          </Link>
        );
      })}
    </div>
  );
}
