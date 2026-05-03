"use client";

type RowCheckboxProps = {
  completed: boolean;
  accentColor: string;
  onToggle: () => void;
};

export function RowCheckbox({ completed, accentColor, onToggle }: RowCheckboxProps) {
  return (
    <button
      aria-label={completed ? "미완료로 되돌리기" : "완료"}
      onClick={onToggle}
      className="mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full border-2 transition"
      style={{
        borderColor: accentColor,
        background: completed ? accentColor : "transparent",
      }}
    >
      {completed && (
        <svg
          width="10"
          height="10"
          viewBox="0 0 10 10"
          fill="none"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M1.5 5.5L4 8L8.5 2"
            stroke="white"
            strokeWidth="1.6"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      )}
    </button>
  );
}
