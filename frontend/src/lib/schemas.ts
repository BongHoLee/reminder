import { z } from "zod";

// 리스트 / reminder 의 클라이언트 검증 스키마. 백엔드 검증과 동일한 한계값.
export const listFormSchema = z.object({
  name: z
    .string()
    .min(1, "목록 이름을 입력해 주세요.")
    .max(100, "목록 이름은 100자 이하여야 합니다."),
  color: z.string().regex(/^#[0-9a-fA-F]{6}$/, "색상 코드가 유효하지 않습니다."),
});
export type ListFormValues = z.infer<typeof listFormSchema>;

export const reminderTitleSchema = z.object({
  title: z
    .string()
    .min(1, "할 일 제목을 입력해 주세요.")
    .max(500, "제목은 500자 이하여야 합니다."),
});
export type ReminderTitleValues = z.infer<typeof reminderTitleSchema>;

export const reminderExpanderSchema = z.object({
  notes: z.string().max(10_000, "메모는 10000자 이하여야 합니다.").nullable(),
  dueAt: z.string().nullable(),
  priority: z.enum(["NONE", "LOW", "MEDIUM", "HIGH"]),
  flagged: z.boolean(),
});
export type ReminderExpanderValues = z.infer<typeof reminderExpanderSchema>;
