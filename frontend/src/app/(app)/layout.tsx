import { Sidebar } from "@/components/Sidebar";

export default function AppLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex flex-1 min-h-0">
      <Sidebar />
      <section className="flex flex-1 flex-col overflow-hidden">{children}</section>
    </div>
  );
}
