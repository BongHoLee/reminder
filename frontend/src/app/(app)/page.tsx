"use client";

import { useQuery } from "@tanstack/react-query";
import { api } from "@/lib/api";

type Health = { status: string };

export default function HomePage() {
  const { data, isLoading, isError, error } = useQuery({
    queryKey: ["health"],
    queryFn: () => api.get<Health>("/health"),
  });

  return (
    <main className="flex flex-1 items-center justify-center p-12">
      <div className="rounded-[var(--radius-card)] border border-[var(--border)] bg-[var(--sidebar-bg)] px-8 py-6 text-center">
        <h1 className="text-2xl font-semibold mb-2">Reminders</h1>
        {isLoading && <p className="text-[var(--muted)]">Backend 연결 확인 중…</p>}
        {isError && (
          <p className="text-red">Backend 연결 실패: {(error as Error).message}</p>
        )}
        {data && <p className="text-green">Backend OK ({data.status})</p>}
      </div>
    </main>
  );
}
