"use client";

import { Flag } from "lucide-react";

type RowMetaProps = {
  flagged: boolean;
};

// 행 우측에 표시되는 정적 메타 (깃발 등). 마감일은 제목 아래라 RowMeta 가 아닌 RowTitle 영역에서 처리.
export function RowMeta({ flagged }: RowMetaProps) {
  if (!flagged) return null;
  return <Flag size={14} className="mt-1 shrink-0 text-orange" fill="currentColor" />;
}
