"use client";

import { Search } from "lucide-react";
import { useRouter, useSearchParams, usePathname } from "next/navigation";
import { Suspense, useEffect, useRef, useState } from "react";

export function SearchBar() {
  return (
    <Suspense fallback={null}>
      <SearchBarInner />
    </Suspense>
  );
}

function SearchBarInner() {
  const router = useRouter();
  const pathname = usePathname();
  const sp = useSearchParams();
  const [value, setValue] = useState(
    pathname === "/search" ? (sp.get("q") ?? "") : "",
  );
  const debounceRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      const trimmed = value.trim();
      if (!trimmed) {
        if (pathname === "/search") router.push("/");
        return;
      }
      router.push(`/search?q=${encodeURIComponent(trimmed)}`);
    }, 250);
    return () => {
      if (debounceRef.current) clearTimeout(debounceRef.current);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [value]);

  return (
    <div className="mb-3 flex items-center gap-2 rounded-[var(--radius-row)] bg-[var(--background)] px-2 py-1.5 text-sm">
      <Search size={14} className="text-[var(--muted)]" />
      <input
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="검색"
        className="flex-1 bg-transparent outline-none placeholder:text-[var(--muted)]"
      />
    </div>
  );
}
