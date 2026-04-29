"use client";

import { useState } from "react";
import { APPLE_COLORS } from "@/lib/colors";
import { useCreateList } from "@/lib/queries/lists";

export function NewListDialog({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) {
  const [name, setName] = useState("");
  const [color, setColor] = useState(APPLE_COLORS[7].value);
  const create = useCreateList();

  if (!open) return null;

  async function submit() {
    if (!name.trim()) return;
    await create.mutateAsync({ name: name.trim(), color });
    setName("");
    onClose();
  }

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center bg-black/30"
      onClick={onClose}
    >
      <div
        className="w-[360px] rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--background)] p-5 shadow-lg"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 className="text-lg font-semibold mb-4">새 목록</h2>

        <input
          autoFocus
          value={name}
          onChange={(e) => setName(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") submit();
            if (e.key === "Escape") onClose();
          }}
          placeholder="목록 이름"
          className="w-full rounded-[var(--radius-row)] border border-[var(--border)] bg-transparent px-3 py-2 text-sm outline-none focus:border-[var(--accent)]"
        />

        <div className="mt-4">
          <p className="mb-2 text-xs text-[var(--muted)]">색상</p>
          <div className="grid grid-cols-7 gap-2">
            {APPLE_COLORS.map((c) => (
              <button
                key={c.value}
                type="button"
                aria-label={c.name}
                onClick={() => setColor(c.value)}
                className="h-7 w-7 rounded-full border-2"
                style={{
                  background: c.value,
                  borderColor: color === c.value ? "var(--foreground)" : "transparent",
                }}
              />
            ))}
          </div>
        </div>

        <div className="mt-5 flex justify-end gap-2">
          <button
            onClick={onClose}
            className="rounded-[var(--radius-row)] px-3 py-1.5 text-sm text-[var(--muted)] hover:bg-[var(--sidebar-bg)]"
          >
            취소
          </button>
          <button
            onClick={submit}
            disabled={!name.trim() || create.isPending}
            className="rounded-[var(--radius-row)] bg-[var(--accent)] px-3 py-1.5 text-sm text-white disabled:opacity-50"
          >
            만들기
          </button>
        </div>
      </div>
    </div>
  );
}
