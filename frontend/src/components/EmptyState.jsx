import { CalendarDays } from "lucide-react";

export default function EmptyState({
  title = "Your events are on the way",
  description = "Once the event catalog is connected, published events will show up here.",
}) {
  return (
    <div className="flex min-h-52 flex-col items-center justify-center rounded-2xl border border-dashed border-violet/20 bg-white/70 px-6 py-10 text-center">
      <span className="mb-4 grid h-12 w-12 place-items-center rounded-2xl bg-lavender text-violet">
        <CalendarDays size={22} />
      </span>
      <h3 className="text-base font-bold text-ink">{title}</h3>
      <p className="mt-2 max-w-md text-sm leading-6 text-muted">{description}</p>
    </div>
  );
}
