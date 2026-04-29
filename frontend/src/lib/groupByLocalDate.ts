import type { Reminder } from "./types";

/**
 * Reminder 들을 `dueAt` 의 **타임존 로컬 날짜**(`YYYY-MM-DD`) 기준으로 그룹핑.
 *
 * `Date.toISOString()` 은 항상 UTC 라서 KST 자정 직후의 항목이 UTC 기준 전날로
 * 잡히는 버그를 방지하기 위해 `Intl.DateTimeFormat` 의 `timeZone` 옵션 사용.
 *
 * dueAt 이 null 인 reminder 는 `"no-date"` 키로 묶인다.
 */
export function groupByLocalDate(
  reminders: Reminder[],
  timeZone: string,
): Map<string, Reminder[]> {
  const formatter = new Intl.DateTimeFormat("en-CA", {
    timeZone,
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
  });

  const groups = new Map<string, Reminder[]>();
  for (const r of reminders) {
    const key = r.dueAt ? formatter.format(new Date(r.dueAt)) : "no-date";
    const arr = groups.get(key) ?? [];
    arr.push(r);
    groups.set(key, arr);
  }
  return groups;
}
