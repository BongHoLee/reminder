"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { ReminderList } from "@/lib/types";

export const listKeys = {
  all: ["lists"] as const,
};

export function useLists() {
  return useQuery({
    queryKey: listKeys.all,
    queryFn: () => api.get<ReminderList[]>("/lists"),
  });
}

export type CreateListInput = {
  name: string;
  color: string;
  sortOrder?: number;
};

export function useCreateList() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (input: CreateListInput) =>
      api.post<ReminderList>("/lists", {
        name: input.name,
        color: input.color,
        sortOrder: input.sortOrder ?? 0,
      }),
    onSuccess: () => qc.invalidateQueries({ queryKey: listKeys.all }),
  });
}

export type UpdateListInput = {
  id: number;
  name?: string;
  color?: string;
  sortOrder?: number;
};

export function useUpdateList() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, ...rest }: UpdateListInput) =>
      api.patch<ReminderList>(`/lists/${id}`, rest),
    onSuccess: () => qc.invalidateQueries({ queryKey: listKeys.all }),
  });
}

export function useDeleteList() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.delete(`/lists/${id}`),
    onSuccess: () => qc.invalidateQueries({ queryKey: listKeys.all }),
  });
}
