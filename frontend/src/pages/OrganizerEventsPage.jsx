import { ArrowLeft, ArrowRight, CalendarDays, CirclePlus, LoaderCircle, MapPin, Pencil, Send, Trash2 } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Brand from "../components/Brand.jsx";
import EmptyState from "../components/EmptyState.jsx";
import { formatEventDate, formatPrice } from "../components/EventCard.jsx";
import { archiveEvent, getApiErrorMessage, getMyEvents, submitEvent } from "../api/client.js";

export default function OrganizerEventsPage() {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionEvent, setActionEvent] = useState(null);

  const loadEvents = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setEvents(await getMyEvents());
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadEvents();
  }, [loadEvents]);

  async function handleSubmit(eventId) {
    setActionEvent(eventId);
    setError("");
    try {
      await submitEvent(eventId);
      await loadEvents();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setActionEvent(null);
    }
  }

  async function handleArchive(event) {
    if (!window.confirm(`Archive “${event.title}”? It will be marked as archived.`)) return;
    setActionEvent(event.id);
    setError("");
    try {
      await archiveEvent(event.id);
      await loadEvents();
    } catch (requestError) {
      setError(getApiErrorMessage(requestError));
    } finally {
      setActionEvent(null);
    }
  }

  return (
    <div className="min-h-screen bg-lavender text-ink">
      <header className="border-b border-[#efedf4] bg-white">
        <div className="mx-auto flex max-w-[1120px] items-center justify-between px-5 py-4 sm:px-8">
          <Brand />
          <Link className="inline-flex items-center gap-2 text-xs font-semibold text-muted transition hover:text-violet" to="/dashboard/organizer"><ArrowLeft size={14} /> Dashboard</Link>
        </div>
      </header>
      <main className="mx-auto max-w-[1120px] px-5 py-8 sm:px-8 sm:py-11">
        <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
          <div>
            <p className="eyebrow">Organizer workspace</p>
            <h1 className="section-title mt-3">Your <span className="gradient-text">events.</span></h1>
            <p className="mt-3 text-sm text-muted">Create drafts, manage your event details, and submit events for review.</p>
          </div>
          <Link className="inline-flex items-center justify-center gap-2 rounded-xl bg-ink px-4 py-3 text-sm font-bold text-white transition hover:bg-violet" to="/organizer/events/new"><CirclePlus size={17} /> Create event</Link>
        </div>

        {error && <p className="mt-6 rounded-xl border border-rose/20 bg-[#fff4f8] px-4 py-3 text-sm text-[#9b365e]" role="alert">{error}</p>}
        {loading ? (
          <div className="grid min-h-64 place-items-center text-violet" role="status"><LoaderCircle className="animate-spin" size={28} /></div>
        ) : events.length ? (
          <section aria-label="Your events" className="mt-7 space-y-4">
            {events.map((event) => (
              <article className="rounded-[22px] border border-[#efedf4] bg-white p-5 shadow-sm sm:p-6" key={event.id}>
                <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-start">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="rounded-full bg-[#f0ecff] px-2.5 py-1 text-[10px] font-bold text-violet">{event.categoryName}</span>
                      <EventStatus status={event.status} />
                    </div>
                    <h2 className="mt-3 text-lg font-extrabold tracking-[-.03em]">{event.title}</h2>
                    <div className="mt-3 flex flex-wrap gap-x-5 gap-y-2 text-xs text-muted">
                      <span className="inline-flex items-center gap-1.5"><CalendarDays size={14} className="text-violet" />{formatEventDate(event.startsAt, event.timezone)}</span>
                      <span className="inline-flex items-center gap-1.5"><MapPin size={14} className="text-violet" />{event.venueName}, {event.city}</span>
                    </div>
                    <div className="mt-4 flex flex-wrap gap-2">
                      {event.ticketTypes.map((ticket) => <span className="rounded-lg bg-[#faf9fd] px-2.5 py-1.5 text-[10px] font-semibold text-[#625d73]" key={ticket.id}>{ticket.name} · {formatPrice(ticket.price, ticket.currency)} · {ticket.availableQuantity} left</span>)}
                    </div>
                  </div>
                  <div className="flex shrink-0 flex-wrap gap-2">
                    {(event.status === "DRAFT" || event.status === "REJECTED") && (
                      <>
                        <Link className="inline-flex h-9 items-center gap-1.5 rounded-lg border border-[#eae7f1] px-3 text-xs font-bold text-ink transition hover:border-violet/30 hover:text-violet" to={`/organizer/events/${event.id}/edit`}><Pencil size={14} /> Edit</Link>
                        <button className="inline-flex h-9 items-center gap-1.5 rounded-lg bg-violet px-3 text-xs font-bold text-white transition hover:brightness-105 disabled:opacity-60" disabled={actionEvent === event.id} onClick={() => handleSubmit(event.id)}><Send size={14} /> Submit</button>
                      </>
                    )}
                    {event.status !== "ARCHIVED" && event.status !== "COMPLETED" && event.status !== "PUBLISHED" && (
                      <button aria-label={`Archive ${event.title}`} className="inline-flex h-9 items-center justify-center rounded-lg border border-[#efedf4] px-3 text-muted transition hover:border-rose/30 hover:text-[#bd416b] disabled:opacity-60" disabled={actionEvent === event.id} onClick={() => handleArchive(event)} title="Archive event"><Trash2 size={14} /></button>
                    )}
                  </div>
                </div>
              </article>
            ))}
          </section>
        ) : !error ? (
          <div className="mt-7">
            <EmptyState title="Your first event starts here" description="Create an event draft, add a venue and ticket types, then submit it for review before it appears in the public catalog." />
            <div className="mt-5 text-center"><Link className="inline-flex items-center gap-2 text-sm font-bold text-violet" to="/organizer/events/new">Create your first event <ArrowRight size={16} /></Link></div>
          </div>
        ) : null}
      </main>
    </div>
  );
}

function EventStatus({ status }) {
  const classes = {
    DRAFT: "bg-[#f3f1f6] text-[#77738a]",
    PENDING_APPROVAL: "bg-[#fff1df] text-[#a76719]",
    PUBLISHED: "bg-[#e4f4ee] text-[#38836d]",
    REJECTED: "bg-[#fff0f2] text-[#bd416b]",
    CANCELLED: "bg-[#f3f1f6] text-[#77738a]",
    COMPLETED: "bg-[#e7effb] text-[#4c78b5]",
    ARCHIVED: "bg-[#f3f1f6] text-[#77738a]",
  };
  return <span className={`rounded-full px-2.5 py-1 text-[10px] font-bold ${classes[status] || classes.DRAFT}`}>{status.replaceAll("_", " ")}</span>;
}
