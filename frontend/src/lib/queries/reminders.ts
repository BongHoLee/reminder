"use client";

import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api";
import type { Priority, Reminder } from "@/lib/types";

export const reminderKeys = {
  byList: (listId: number) => ["reminders", "list", listId] as const,
  children: (parentId: number) => ["reminders", "children", parentId] as const,
};

export function useReminders(listId: number) {
  return useQuery({
    queryKey: reminderKeys.byList(listId),
    queryFn: async () => {
      const [incomplete, completed] = await Promise.all([
        api.get<Reminder[]>(`/lists/${listId}/reminders?completed=false`),
        api.get<Reminder[]>(`/lists/${listId}/reminders?completed=true`),
      ]);
      return [...incomplete, ...completed];
    },
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
      qc.invalidateQueries({ queryKey: reminderKeys.byList(vars.listId) });
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
      qc.invalidateQueries({ queryKey: reminderKeys.byList(vars.listId) });
    },
  });
}

export function useToggleReminder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; listId: number }) =>
      api.post<Reminder>(`/reminders/${id}/toggle`),

    onMutate: async (vars) => {
      const key = reminderKeys.byList(vars.listId);
      await qc.cancelQueries({ queryKey: key });

      const prev = qc.getQueryData<Reminder[]>(key);

      // 단일 캐시에서 reminder 의 completed 플래그를 즉시 토글 (낙관적 업데이트)
      qc.setQueryData<Reminder[]>(key, (old) =>
        (old ?? []).map((r) =>
          r.id === vars.id ? { ...r, completed: !r.completed } : r,
        ),
      );
      return { prev, key };
    },

    onError: (_err, _vars, ctx) => {
      if (!ctx) return;
      qc.setQueryData(ctx.key, ctx.prev);
    },

    onSettled: (_data, _err, vars) => {
      qc.invalidateQueries({ queryKey: reminderKeys.byList(vars.listId) });
    },
  });
}

export function useDeleteReminder() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id }: { id: number; listId: number }) =>
      api.delete(`/reminders/${id}`),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: reminderKeys.byList(vars.listId) });
    },
  });
}
