"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Reminder } from "@/lib/types";

export function useSearch(q: string) {
  const trimmed = q.trim();
  return useQuery({
    queryKey: ["search", trimmed],
    queryFn: () => api.get<Reminder[]>(`/search?q=${encodeURIComponent(trimmed)}`),
    enabled: trimmed.length > 0,
    staleTime: 0,
  });
}
