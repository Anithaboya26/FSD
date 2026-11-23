import { ArrowLeft, ChevronDown, ChevronLeft, ChevronRight, LoaderCircle, Search, SlidersHorizontal } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Brand from "../components/Brand.jsx";
import EmptyState from "../components/EmptyState.jsx";
import EventCard from "../components/EventCard.jsx";
import { getApiErrorMessage, getEventCategories, searchEvents } from "../api/client.js";

const PAGE_SIZE = 12;

export default function BrowseEventsPage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [categories, setCategories] = useState([]);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const filters = useMemo(() => ({
    q: searchParams.get("q") || "",
    city: searchParams.get("city") || searchParams.get("location") || "",
    category: searchParams.get("category") || "",
    from: searchParams.get("from") || "",
    to: searchParams.get("to") || "",
    sort: searchParams.get("sort") || "soonest",
    page: Number(searchParams.get("page") || 0),
  }), [searchParams]);

  useEffect(() => {
    let active = true;
    getEventCategories()
      .then((items) => {
        if (active) setCategories(items);
      })
      .catch(() => {
        if (active) setCategories([]);
      });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    const category = categories.find((item) =>
      item.slug === filters.category || item.name.toLowerCase() === filters.category.toLowerCase(),
    )?.slug || filters.category;
    const params = {
      q: filters.q || undefined,
      city: filters.city || undefined,
      category: category || undefined,
      from: filters.from || undefined,
      to: filters.to || undefined,
      sort: filters.sort,
      page: filters.page,
      size: PAGE_SIZE,
    };
    searchEvents(params)
      .then((data) => {
        if (active) setResult(data);
      })
      .catch((requestError) => {
        if (active) {
          setResult(null);
          setError(getApiErrorMessage(requestError));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [categories, filters]);

  function updateFilter(name, value) {
    const next = new URLSearchParams(searchParams);
    if (value) next.set(name, value);
    else next.delete(name);
    next.delete("page");
    setSearchParams(next);
  }

  function setPage(page) {
    const next = new URLSearchParams(searchParams);
    if (page > 0) next.set("page", String(page));
    else next.delete("page");
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  return (
    <div className="min-h-screen bg-lavender text-ink">
      <header className="border-b border-[#efedf4] bg-white">
        <div className="mx-auto flex max-w-[1320px] items-center justify-between px-5 py-4 sm:px-8 lg:px-12">
          <Brand />
          <div className="flex items-center gap-3">
            <Link className="hidden items-center gap-2 text-xs font-semibold text-muted transition hover:text-violet sm:inline-flex" to="/"><ArrowLeft size={14} /> Home</Link>
            <Link className="rounded-full bg-ink px-4 py-2.5 text-xs font-bold text-white transition hover:bg-violet" to="/login">Sign in</Link>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-[1320px] px-5 py-9 sm:px-8 sm:py-12 lg:px-12">
        <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
          <div>
            <p className="eyebrow">Good things are happening</p>
            <h1 className="section-title mt-3">Find your next <span className="gradient-text">experience.</span></h1>
            <p className="mt-3 text-sm text-muted">Search the live Ventixe catalog and find something worth looking forward to.</p>
          </div>
          <p className="text-xs font-semibold text-muted" aria-live="polite">
            {loading ? "Finding events…" : `${result?.totalElements ?? 0} events found`}
          </p>
        </div>

        <section aria-label="Event filters" className="mt-7 rounded-[22px] border border-[#efedf4] bg-white p-4 shadow-sm sm:p-5">
          <div className="mb-4 flex items-center gap-2 text-xs font-bold text-ink"><SlidersHorizontal size={15} className="text-violet" /> Find your fit</div>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-6">
            <label className="relative sm:col-span-2 lg:col-span-2">
              <span className="sr-only">Search events</span>
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-violet" size={16} />
              <input aria-label="Search events" className="h-11 w-full rounded-xl border border-[#efedf4] bg-[#faf9fd] pl-9 pr-3 text-xs outline-none focus:border-violet/40 focus:ring-2 focus:ring-violet/10" onChange={(event) => updateFilter("q", event.target.value)} placeholder="Event, artist, or venue" value={filters.q} />
            </label>
            <label>
              <span className="sr-only">City</span>
              <input aria-label="City" className="h-11 w-full rounded-xl border border-[#efedf4] bg-[#faf9fd] px-3 text-xs outline-none focus:border-violet/40 focus:ring-2 focus:ring-violet/10" onChange={(event) => updateFilter("city", event.target.value)} placeholder="City" value={filters.city} />
            </label>
            <label className="relative">
              <span className="sr-only">Category</span>
              <select aria-label="Category" className="h-11 w-full appearance-none rounded-xl border border-[#efedf4] bg-[#faf9fd] px-3 pr-8 text-xs text-[#5e5972] outline-none focus:border-violet/40 focus:ring-2 focus:ring-violet/10" onChange={(event) => updateFilter("category", event.target.value)} value={filters.category}>
                <option value="">All categories</option>
                {categories.map((category) => <option key={category.id} value={category.slug}>{category.name}</option>)}
              </select>
              <ChevronDown className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-muted" size={13} />
            </label>
            <label>
              <span className="sr-only">Sort events</span>
              <select aria-label="Sort events" className="h-11 w-full rounded-xl border border-[#efedf4] bg-[#faf9fd] px-3 text-xs text-[#5e5972] outline-none focus:border-violet/40 focus:ring-2 focus:ring-violet/10" onChange={(event) => updateFilter("sort", event.target.value)} value={filters.sort}>
                <option value="soonest">Soonest</option>
                <option value="latest">Latest</option>
                <option value="name">Name</option>
              </select>
            </label>
            <label>
              <span className="sr-only">From date</span>
              <input aria-label="From date" className="h-11 w-full rounded-xl border border-[#efedf4] bg-[#faf9fd] px-3 text-xs text-[#5e5972] outline-none focus:border-violet/40 focus:ring-2 focus:ring-violet/10" onChange={(event) => updateFilter("from", event.target.value)} type="date" value={filters.from} />
            </label>
            <label>
              <span className="sr-only">To date</span>
              <input aria-label="To date" className="h-11 w-full rounded-xl border border-[#efedf4] bg-[#faf9fd] px-3 text-xs text-[#5e5972] outline-none focus:border-violet/40 focus:ring-2 focus:ring-violet/10" onChange={(event) => updateFilter("to", event.target.value)} type="date" value={filters.to} />
            </label>
          </div>
        </section>

        {error && <div className="mt-6 rounded-xl border border-rose/20 bg-[#fff4f8] px-4 py-3 text-sm text-[#9b365e]" role="alert">{error}</div>}
        {loading ? (
          <div className="grid min-h-64 place-items-center text-violet" role="status"><LoaderCircle className="animate-spin" size={28} /></div>
        ) : result?.content?.length ? (
          <>
            <section aria-label="Event results" className="mt-7 grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
              {result.content.map((event) => <EventCard event={event} key={event.id} />)}
            </section>
            <nav aria-label="Event pages" className="mt-8 flex items-center justify-center gap-4">
              <button className="flex h-10 items-center gap-1 rounded-xl border border-[#efedf4] bg-white px-3 text-xs font-bold text-ink disabled:cursor-not-allowed disabled:opacity-40" disabled={filters.page <= 0} onClick={() => setPage(filters.page - 1)}><ChevronLeft size={15} /> Previous</button>
              <span className="text-xs text-muted">Page {filters.page + 1} of {Math.max(result.totalPages, 1)}</span>
              <button className="flex h-10 items-center gap-1 rounded-xl border border-[#efedf4] bg-white px-3 text-xs font-bold text-ink disabled:cursor-not-allowed disabled:opacity-40" disabled={!result.hasNext} onClick={() => setPage(filters.page + 1)}>Next <ChevronRight size={15} /></button>
            </nav>
          </>
        ) : !error ? (
          <div className="mt-7"><EmptyState title="No events match those filters yet" description="Try a different city, category, or date range. New events will show up here once published." /></div>
        ) : null}
      </main>
      <footer className="border-t border-[#efedf4] bg-white px-5 py-6 text-center text-xs text-muted sm:px-8">
        Discover responsibly. If the catalog is empty, organizers may not have published events yet.
      </footer>
    </div>
  );
}
