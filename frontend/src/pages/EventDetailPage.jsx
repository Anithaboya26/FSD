import { ArrowLeft, CalendarDays, Clock3, MapPin, Navigation, Ticket } from "lucide-react";
import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import Brand from "../components/Brand.jsx";
import { formatEventDate, formatPrice } from "../components/EventCard.jsx";
import EmptyState from "../components/EmptyState.jsx";
import { getApiErrorMessage, getEvent } from "../api/client.js";

export default function EventDetailPage() {
  const { slug } = useParams();
  const [detail, setDetail] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    getEvent(slug)
      .then((data) => {
        if (active) setDetail(data);
      })
      .catch((requestError) => {
        if (active) setError(getApiErrorMessage(requestError));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [slug]);

  if (loading) {
    return <main className="grid min-h-screen place-items-center bg-lavender text-sm text-muted" role="status">Loading event details…</main>;
  }
  if (error || !detail?.event) {
    return (
      <main className="min-h-screen bg-lavender px-5 py-10 sm:px-8">
        <div className="mx-auto max-w-3xl"><Brand /><div className="mt-10"><EmptyState title="This event isn’t available" description={error || "It may have been unpublished or moved."} /><Link className="mt-5 inline-flex items-center gap-2 text-sm font-bold text-violet" to="/events"><ArrowLeft size={15} /> Browse events</Link></div></div>
      </main>
    );
  }

  const { event, venue, ticketTypes } = detail;
  return (
    <div className="min-h-screen bg-lavender text-ink">
      <header className="border-b border-[#efedf4] bg-white">
        <div className="mx-auto flex max-w-[1200px] items-center justify-between px-5 py-4 sm:px-8">
          <Brand />
          <Link className="inline-flex items-center gap-2 text-xs font-semibold text-muted transition hover:text-violet" to="/events"><ArrowLeft size={14} /> All events</Link>
        </div>
      </header>
      <main className="mx-auto max-w-[1200px] px-5 py-7 sm:px-8 sm:py-10">
        <div className="overflow-hidden rounded-[28px] bg-gradient-to-br from-[#cbb8ef] via-[#f0c3dc] to-[#f3d9b8]">
          {event.coverImageUrl
            ? <img alt="" className="max-h-[490px] min-h-64 w-full object-cover" src={event.coverImageUrl} />
            : <div className="grid min-h-64 place-items-center sm:min-h-[350px]"><span className="grid h-20 w-20 place-items-center rounded-3xl bg-white/70 text-violet"><Ticket size={32} /></span></div>}
        </div>
        <div className="mt-7 grid gap-6 lg:grid-cols-[1fr_360px]">
          <section className="rounded-[24px] border border-[#efedf4] bg-white p-6 sm:p-8">
            <span className="rounded-full bg-[#f0ecff] px-3 py-1.5 text-[10px] font-bold text-violet">{event.categoryName}</span>
            <h1 className="mt-4 text-3xl font-extrabold leading-tight tracking-[-.055em] sm:text-[42px]">{event.title}</h1>
            <p className="mt-5 whitespace-pre-line text-sm leading-7 text-muted">{detail.description}</p>
            <div className="mt-8 grid gap-4 border-t border-[#f0edf4] pt-6 sm:grid-cols-2">
              <Info icon={<CalendarDays size={17} />} label="Date and time" value={formatEventDate(event.startsAt, event.timezone)} />
              <Info icon={<Clock3 size={17} />} label="Ends" value={formatEventDate(event.endsAt, event.timezone)} />
              <Info icon={<MapPin size={17} />} label="Venue" value={`${venue.name}, ${venue.city}`} />
              <Info icon={<Navigation size={17} />} label="Address" value={[venue.addressLine1, venue.addressLine2, venue.city, venue.region, venue.postalCode, venue.countryCode].filter(Boolean).join(", ")} href={venue.mapUrl} />
            </div>
          </section>
          <aside className="h-fit rounded-[24px] border border-[#efedf4] bg-white p-6">
            <h2 className="text-lg font-extrabold tracking-[-.03em]">Ticket options</h2>
            <p className="mt-1 text-xs text-muted">Availability is live; your place is confirmed at checkout.</p>
            <div className="mt-5 space-y-3">
              {ticketTypes?.length ? ticketTypes.map((ticket) => (
                <div className="rounded-2xl border border-[#efedf4] p-4" key={ticket.id}>
                  <div className="flex items-start justify-between gap-3">
                    <div><h3 className="text-sm font-bold">{ticket.name}</h3>{ticket.description && <p className="mt-1 text-xs text-muted">{ticket.description}</p>}</div>
                    <span className="shrink-0 text-sm font-extrabold text-violet">{formatPrice(ticket.price, ticket.currency)}</span>
                  </div>
                  <p className="mt-3 text-[11px] text-muted">{ticket.availableQuantity} available</p>
                </div>
              )) : <p className="rounded-xl bg-lavender p-4 text-xs text-muted">Ticket options are not available right now.</p>}
            </div>
            <Link className="mt-5 block rounded-xl bg-ink px-4 py-3 text-center text-sm font-bold text-white transition hover:bg-violet" to="/login">Sign in to book</Link>
            <p className="mt-3 text-center text-[10px] leading-5 text-muted">Booking and payment checkout will be added in the next phase.</p>
          </aside>
        </div>
      </main>
    </div>
  );
}

function Info({ icon, label, value, href }) {
  return (
    <div className="flex gap-3">
      <span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-[#f0ecff] text-violet">{icon}</span>
      <div className="min-w-0"><p className="text-[10px] font-bold uppercase tracking-[.12em] text-[#aaa6b5]">{label}</p>{href ? <a className="mt-1 block text-xs font-semibold leading-5 text-violet hover:underline" href={href} rel="noreferrer" target="_blank">{value}</a> : <p className="mt-1 text-xs font-semibold leading-5 text-ink">{value}</p>}</div>
    </div>
  );
}
