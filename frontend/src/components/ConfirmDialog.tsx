"use client";

import { useEffect, useRef } from "react";

type ConfirmDialogProps = {
  open: boolean;
  title: string;
  description?: string;
  confirmLabel?: string;
  cancelLabel?: string;
  destructive?: boolean;
  onConfirm: () => void;
  onCancel: () => void;
};

// Apple 스타일 confirm dialog. Esc 닫기 + 포커스 트랩 + role="dialog" + aria-modal.
export function ConfirmDialog({
  open,
  title,
  description,
  confirmLabel = "삭제",
  cancelLabel = "취소",
  destructive = true,
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  const confirmBtnRef = useRef<HTMLButtonElement>(null);
  const cancelBtnRef = useRef<HTMLButtonElement>(null);
  const previousFocusRef = useRef<HTMLElement | null>(null);

  useEffect(() => {
    if (!open) return;

    previousFocusRef.current = document.activeElement as HTMLElement | null;
    confirmBtnRef.current?.focus();

    function handleKey(e: KeyboardEvent) {
      if (e.key === "Escape") {
        e.preventDefault();
        onCancel();
        return;
      }
      // 포커스 트랩 — Tab 이 두 버튼 사이에서만 이동.
      if (e.key === "Tab") {
        const target = document.activeElement;
        e.preventDefault();
        if (target === confirmBtnRef.current) cancelBtnRef.current?.focus();
        else confirmBtnRef.current?.focus();
      }
    }

    document.addEventListener("keydown", handleKey);
    return () => {
      document.removeEventListener("keydown", handleKey);
      previousFocusRef.current?.focus();
    };
  }, [open, onCancel]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 grid place-items-center bg-black/30"
      onClick={onCancel}
      aria-hidden="true"
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="confirm-dialog-title"
        className="w-[320px] rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--background)] p-5 text-center shadow-xl"
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id="confirm-dialog-title" className="text-base font-semibold">
          {title}
        </h2>
        {description && <p className="mt-2 text-xs text-[var(--muted)]">{description}</p>}
        <div className="mt-4 flex flex-col gap-2">
          <button
            ref={confirmBtnRef}
            type="button"
            onClick={onConfirm}
            className={`rounded-md py-1.5 text-sm font-medium ${
              destructive ? "bg-red text-white" : "bg-accent text-white"
            }`}
          >
            {confirmLabel}
          </button>
          <button
            ref={cancelBtnRef}
            type="button"
            onClick={onCancel}
            className="rounded-md border border-[var(--border)] py-1.5 text-sm"
          >
            {cancelLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
