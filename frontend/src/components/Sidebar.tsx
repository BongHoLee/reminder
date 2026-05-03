"use client";

import { useState } from "react";
import Link from "next/link";
import { usePathname } from "next/navigation";
import { Plus, Trash2, Pencil } from "lucide-react";
import { ConfirmDialog } from "./ConfirmDialog";
import { NewListDialog } from "./NewListDialog";
import { SearchBar } from "./SearchBar";
import { SmartListGrid } from "./SmartListGrid";
import {
  useDeleteList,
  useLists,
  useUpdateList,
} from "@/lib/queries/lists";
import type { ReminderList } from "@/lib/types";

export function Sidebar() {
  const { data: lists, isLoading } = useLists();
  const [dialogOpen, setDialogOpen] = useState(false);

  return (
    <aside className="flex w-[260px] shrink-0 flex-col border-r border-[var(--border)] bg-[var(--sidebar-bg)]">
      <div className="flex-1 overflow-y-auto px-3 pt-4">
        <SearchBar />
        <div className="mb-6">
          <SmartListGrid />
        </div>

        <div className="mb-2 px-2 text-xs uppercase tracking-wide text-[var(--muted)]">
          내 목록
        </div>

        {isLoading && (
          <p className="px-2 py-1 text-xs text-[var(--muted)]">로딩 중…</p>
        )}

        <ul className="flex flex-col gap-0.5">
          {lists?.map((l) => <SidebarListItem key={l.id} list={l} />) ?? null}
        </ul>
      </div>

      <button
        onClick={() => setDialogOpen(true)}
        className="m-3 flex items-center gap-2 rounded-[var(--radius-row)] px-3 py-2 text-sm text-[var(--accent)] hover:bg-[var(--background)]"
      >
        <Plus size={16} />
        <span>목록 추가</span>
      </button>

      <NewListDialog open={dialogOpen} onClose={() => setDialogOpen(false)} />
    </aside>
  );
}

function SidebarListItem({ list }: { list: ReminderList }) {
  const pathname = usePathname();
  const isActive = pathname === `/lists/${list.id}`;
  const [editing, setEditing] = useState(false);
  const [draftName, setDraftName] = useState(list.name);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const update = useUpdateList();
  const del = useDeleteList();

  async function commit() {
    const trimmed = draftName.trim();
    if (trimmed && trimmed !== list.name) {
      await update.mutateAsync({ id: list.id, name: trimmed });
    } else {
      setDraftName(list.name);
    }
    setEditing(false);
  }

  async function confirmDelete() {
    setConfirmOpen(false);
    await del.mutateAsync(list.id);
  }

  return (
    <li
      className={`group flex items-center gap-2 rounded-[var(--radius-row)] px-2 py-1.5 ${
        isActive ? "bg-[var(--background)]" : "hover:bg-[var(--background)]/60"
      }`}
    >
      <span
        className="inline-block h-3 w-3 shrink-0 rounded-full"
        style={{ background: list.color }}
      />
      {editing ? (
        <input
          autoFocus
          value={draftName}
          onChange={(e) => setDraftName(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter") commit();
            if (e.key === "Escape") {
              setDraftName(list.name);
              setEditing(false);
            }
          }}
          onBlur={commit}
          className="flex-1 bg-transparent text-sm outline-none"
        />
      ) : (
        <Link href={`/lists/${list.id}`} className="flex-1 truncate text-sm">
          {list.name}
        </Link>
      )}

      <button
        onClick={() => setEditing(true)}
        aria-label="이름 변경"
        className="invisible text-[var(--muted)] group-hover:visible group-focus-within:visible focus:visible"
      >
        <Pencil size={14} />
      </button>
      <button
        onClick={() => setConfirmOpen(true)}
        aria-label="삭제"
        className="invisible text-[var(--muted)] group-hover:visible group-focus-within:visible focus:visible"
      >
        <Trash2 size={14} />
      </button>

      <ConfirmDialog
        open={confirmOpen}
        title={`"${list.name}" 목록을 삭제하시겠습니까?`}
        description="모든 미리 알림이 삭제됩니다."
        confirmLabel="삭제"
        cancelLabel="취소"
        onConfirm={confirmDelete}
        onCancel={() => setConfirmOpen(false)}
      />
    </li>
  );
}
