import { describe, expect, it, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { RowCheckbox } from "./RowCheckbox";

describe("RowCheckbox", () => {
  it("미완료 상태에서 클릭하면 onToggle 이 호출된다", async () => {
    const user = userEvent.setup();
    const onToggle = vi.fn();
    render(<RowCheckbox completed={false} accentColor="#0a84ff" onToggle={onToggle} />);

    await user.click(screen.getByRole("button", { name: "완료" }));

    expect(onToggle).toHaveBeenCalledTimes(1);
  });

  it("완료 상태이면 aria-label 이 '미완료로 되돌리기' 로 바뀐다", () => {
    render(<RowCheckbox completed accentColor="#0a84ff" onToggle={() => {}} />);

    expect(screen.getByRole("button", { name: "미완료로 되돌리기" })).toBeTruthy();
  });
});
