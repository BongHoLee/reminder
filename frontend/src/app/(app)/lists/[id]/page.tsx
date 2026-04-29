"use client";

import { use } from "react";
import { useLists } from "@/lib/queries/lists";

export default function ListDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const listId = Number(id);
  const { data: lists } = useLists();
  const list = lists?.find((l) => l.id === listId);

  return (
    <main className="flex flex-1 flex-col p-10">
      <header className="mb-6">
        <h1
          className="text-3xl font-semibold"
          style={{ color: list?.color ?? "var(--foreground)" }}
        >
          {list?.name ?? `리스트 #${listId}`}
        </h1>
      </header>

      <section className="rounded-[var(--radius-card)] border border-dashed border-[var(--border)] p-8 text-center text-[var(--muted)]">
        Phase 2 에서 reminder 목록 / 입력 / 토글이 채워집니다.
      </section>
    </main>
  );
}
