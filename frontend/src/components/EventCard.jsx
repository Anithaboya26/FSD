import { ArrowUpRight, CalendarDays, MapPin, Ticket } from "lucide-react";
import { Link } from "react-router-dom";

export function formatEventDate(value, timezone) {
  const date = new Date(value);
  return new Intl.DateTimeFormat(undefined, {
    weekday: "short",
    month: "short",
    day: "numeric",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    timeZone: timezone,
  }).format(date);
}

export function formatPrice(value, currency) {
  if (value == null || !currency) return "See ticket options";
  return new Intl.NumberFormat(undefined, {
    style: "currency",
    currency,
    maximumFractionDigits: 2,
  }).format(value);
}

export default function EventCard({ event }) {
  return (
    <article className="group overflow-hidden rounded-[22px] border border-[#efedf4] bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-panel">
      <Link aria-label={`View ${event.title}`} className="block" to={`/events/${encodeURIComponent(event.slug)}`}>
        <div className="relative aspect-[1.65/1] overflow-hidden bg-gradient-to-br from-[#d8c8f2] via-[#f2cbe0] to-[#f6dcb7]">
          {event.coverImageUrl ? (
            <img alt="" className="h-full w-full object-cover transition duration-500 group-hover:scale-105" loading="lazy" src={event.coverImageUrl} />
          ) : (
            <div className="absolute inset-0 grid place-items-center">
              <span className="grid h-14 w-14 place-items-center rounded-2xl bg-white/70 text-violet shadow-sm"><Ticket size={24} /></span>
            </div>
          )}
          {event.featured && <span className="absolute left-3 top-3 rounded-full bg-white/90 px-3 py-1 text-[10px] font-bold text-violet shadow-sm">Featured</span>}
          <span className="absolute bottom-3 right-3 grid h-9 w-9 place-items-center rounded-full bg-white text-ink shadow-sm transition group-hover:bg-violet group-hover:text-white"><ArrowUpRight size={17} /></span>
        </div>
        <div className="p-5">
          <div className="flex items-center justify-between gap-3">
            <span className="rounded-full bg-[#f0ecff] px-2.5 py-1 text-[10px] font-bold text-violet">{event.categoryName}</span>
            <span className="text-[11px] font-bold text-ink">{formatPrice(event.minimumTicketPrice, event.currency)}</span>
          </div>
          <h2 className="mt-3 line-clamp-2 text-base font-extrabold leading-snug tracking-[-.03em] text-ink">{event.title}</h2>
          <p className="mt-2 line-clamp-2 min-h-10 text-xs leading-5 text-muted">{event.description}</p>
          <div className="mt-4 space-y-2 border-t border-[#f1eff5] pt-4 text-[11px] font-medium text-muted">
            <p className="flex items-center gap-2"><CalendarDays className="shrink-0 text-violet" size={14} />{formatEventDate(event.startsAt, event.timezone)}</p>
            <p className="flex items-center gap-2"><MapPin className="shrink-0 text-violet" size={14} />{event.venueName}, {event.city}</p>
          </div>
        </div>
      </Link>
    </article>
  );
}
