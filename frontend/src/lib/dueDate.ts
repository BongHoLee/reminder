/**
 * Apple Reminders 풍의 한국어 마감일 라벨.
 * - "오늘, 오후 3:00"
 * - "내일, 오전 9:30"
 * - "어제, 오후 8:00"
 * - "2026년 5월 1일 금요일" (그 외)
 */
export function formatDueDate(iso: string | null | undefined, now: Date = new Date()): string | null {
  if (!iso) return null;
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return null;

  const dayDiff = startOfDay(d).getTime() - startOfDay(now).getTime();
  const oneDay = 24 * 60 * 60 * 1000;

  const time = new Intl.DateTimeFormat("ko-KR", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  }).format(d);

  if (dayDiff === 0) return `오늘, ${time}`;
  if (dayDiff === oneDay) return `내일, ${time}`;
  if (dayDiff === -oneDay) return `어제, ${time}`;

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    weekday: "long",
  }).format(d);
}

function startOfDay(d: Date): Date {
  const c = new Date(d);
  c.setHours(0, 0, 0, 0);
  return c;
}
