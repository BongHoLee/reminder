"use client";

import { useEffect } from "react";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { APPLE_COLORS } from "@/lib/colors";
import { useCreateList } from "@/lib/queries/lists";
import { listFormSchema, type ListFormValues } from "@/lib/schemas";

const DEFAULT_VALUES: ListFormValues = {
  name: "",
  color: APPLE_COLORS[7].value,
};

export function NewListDialog({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const create = useCreateList();
  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors, isSubmitting, isValid },
  } = useForm<ListFormValues>({
    resolver: zodResolver(listFormSchema),
    defaultValues: DEFAULT_VALUES,
    mode: "onChange",
  });

  useEffect(() => {
    if (open) reset(DEFAULT_VALUES);
  }, [open, reset]);

  if (!open) return null;

  async function onSubmit(values: ListFormValues) {
    await create.mutateAsync({ name: values.name.trim(), color: values.color });
    onClose();
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/30"
      onClick={onClose}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="new-list-title"
        className="w-[360px] rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--background)] p-5 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="new-list-title" className="text-lg font-semibold mb-4">
          새 목록
        </h2>

        <form onSubmit={handleSubmit(onSubmit)}>
          <input
            autoFocus
            {...register("name")}
            onKeyDown={(e) => {
              if (e.key === "Escape") {
                e.preventDefault();
                onClose();
              }
            }}
            placeholder="목록 이름"
            aria-invalid={!!errors.name}
            className="w-full rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-3 py-2 text-sm outline-none focus:border-[var(--accent)]"
          />
          {errors.name && (
            <p className="mt-1 text-xs text-red" role="alert">
              {errors.name.message}
            </p>
          )}

          <div className="mt-4">
            <p className="mb-2 text-xs text-[var(--muted)]">색상</p>
            <Controller
              name="color"
              control={control}
              render={({ field }) => (
                <div className="grid grid-cols-7 gap-2">
                  {APPLE_COLORS.map((c) => (
                    <button
                      key={c.value}
                      type="button"
                      aria-label={c.name}
                      aria-pressed={field.value === c.value}
                      onClick={() => field.onChange(c.value)}
                      className="h-7 w-7 rounded-full border-2"
                      style={{
                        background: c.value,
                        borderColor:
                          field.value === c.value ? "var(--foreground)" : "transparent",
                      }}
                    />
                  ))}
                </div>
              )}
            />
          </div>

          <div className="mt-5 flex justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="rounded-[var(--radius-row)] px-3 py-1.5 text-sm text-[var(--muted)] hover:bg-[var(--sidebar-bg)]"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={!isValid || isSubmitting || create.isPending}
              className="rounded-[var(--radius-row)] bg-[var(--accent)] px-3 py-1.5 text-sm text-white disabled:opacity-50"
            >
              만들기
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
