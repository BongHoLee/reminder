"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { ReminderList } from "@/components/ReminderList";
import { useSearch } from "@/lib/queries/search";

export default function SearchPage() {
  return (
    <Suspense fallback={null}>
      <SearchContent />
    </Suspense>
  );
}

function SearchContent() {
  const sp = useSearchParams();
  const q = sp.get("q") ?? "";
  const { data = [], isLoading } = useSearch(q);

  return (
    <main className="flex flex-1 flex-col p-10">
      <header className="mb-6">
        <h1 className="text-3xl font-semibold">검색</h1>
        <p className="mt-1 text-sm text-[var(--muted)]">
          {q ? `"${q}" 결과` : "검색어를 입력하세요"}
        </p>
      </header>

      {q && isLoading && (
        <p className="px-2 py-2 text-sm text-[var(--muted)]">검색 중…</p>
      )}
      {q && !isLoading && (
        <ReminderList
          reminders={data}
          accentColor="var(--accent)"
          emptyText="결과 없음"
        />
      )}
    </main>
  );
}
