"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Priority, Reminder } from "@/lib/types";

export const reminderKeys = {
  byList: (listId: number, completed: boolean) =>
    ["reminders", "list", listId, completed] as const,
  children: (parentId: number) => ["reminders", "children", parentId] as const,
};

export function useReminders(listId: number, completed = false) {
  return useQuery({
    queryKey: reminderKeys.byList(listId, completed),
    queryFn: () =>
      api.get<Reminder[]>(`/lists/${listId}/reminders?completed=${completed}`),
    enabled: Number.isFinite(listId),
  });
}

export type CreateReminderInput = {
  listId: number;
  title: string;
  notes?: string | null;
  dueAt?: string | null;
  priority?: Priority;
  flagged?: boolean;
  sortOrder?: number;
  parentId?: number | null;
};

export function useCreateReminder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ listId, ...rest }: CreateReminderInput) =>
      api.post<Reminder>(`/lists/${listId}/reminders`, rest),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["reminders", "list", vars.listId] });
    },
  });
}

export type UpdateReminderInput = {
  id: number;
  listId: number;
  title?: string;
  notes?: string | null;
  notesClear?: boolean;
  dueAt?: string | null;
  dueAtClear?: boolean;
  priority?: Priority;
  flagged?: boolean;
  sortOrder?: number;
  parentId?: number | null;
  parentIdClear?: boolean;
};

export function useUpdateReminder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, listId: _l, ...rest }: UpdateReminderInput) =>
      api.patch<Reminder>(`/reminders/${id}`, rest),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["reminders", "list", vars.listId] });
    },
  });
}

export function useToggleReminder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; listId: number; completed: boolean }) =>
      api.post<Reminder>(`/reminders/${id}/toggle`),

    onMutate: async (vars) => {
      const completedKey = reminderKeys.byList(vars.listId, vars.completed);
      const otherKey = reminderKeys.byList(vars.listId, !vars.completed);
      await qc.cancelQueries({ queryKey: completedKey });
      await qc.cancelQueries({ queryKey: otherKey });

      const prevCompleted = qc.getQueryData<Reminder[]>(completedKey);
      const prevOther = qc.getQueryData<Reminder[]>(otherKey);

      // 토글 즉시 현재 섹션에서 제거 (낙관적 업데이트)
      qc.setQueryData<Reminder[]>(completedKey, (old) =>
        (old ?? []).filter((r) => r.id !== vars.id),
      );
      return { prevCompleted, prevOther, completedKey, otherKey };
    },

    onError: (_err, _vars, ctx) => {
      if (!ctx) return;
      qc.setQueryData(ctx.completedKey, ctx.prevCompleted);
      qc.setQueryData(ctx.otherKey, ctx.prevOther);
    },

    onSettled: (_data, _err, vars) => {
      qc.invalidateQueries({ queryKey: ["reminders", "list", vars.listId] });
    },
  });
}

export function useDeleteReminder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; listId: number }) =>
      api.delete(`/reminders/${id}`),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["reminders", "list", vars.listId] });
    },
  });
}
