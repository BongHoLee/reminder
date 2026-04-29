export type Priority = "NONE" | "LOW" | "MEDIUM" | "HIGH";

export type ReminderList = {
  id: number;
  name: string;
  color: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
};

export type Reminder = {
  id: number;
  listId: number;
  parentId: number | null;
  title: string;
  notes: string | null;
  dueAt: string | null;
  priority: Priority;
  completed: boolean;
  completedAt: string | null;
  flagged: boolean;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
};

export type SmartViewType = "today" | "scheduled" | "all" | "flagged" | "completed";

export type SmartViewCounts = {
  today: number;
  scheduled: number;
  all: number;
  flagged: number;
  completed: number;
};
