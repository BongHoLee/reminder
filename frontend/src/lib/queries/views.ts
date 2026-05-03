"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Reminder, SmartViewCounts, SmartViewType } from "@/lib/types";

export const viewKeys = {
  list: (type: SmartViewType, tz: string) => ["views", type, tz] as const,
  counts: (tz: string) => ["views", "counts", tz] as const,
};

function resolveDefaultTimezone(): string {
  if (typeof Intl !== "undefined") {
    try {
      return Intl.DateTimeFormat().resolvedOptions().timeZone;
    } catch {
      // ignore
    }
  }
  return "UTC";
}

const DEFAULT_TZ = resolveDefaultTimezone();

export function useSmartView(type: SmartViewType, tz: string = DEFAULT_TZ) {
  return useQuery({
    queryKey: viewKeys.list(type, tz),
    queryFn: () => api.get<Reminder[]>(`/views/${type}?tz=${encodeURIComponent(tz)}`),
  });
}

export function useSmartViewCounts(tz: string = DEFAULT_TZ) {
  return useQuery({
    queryKey: viewKeys.counts(tz),
    queryFn: () =>
      api.get<SmartViewCounts>(`/views/counts?tz=${encodeURIComponent(tz)}`),
  });
}
