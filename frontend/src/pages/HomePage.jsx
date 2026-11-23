import {
  ArrowDownRight,
  ArrowRight,
  ArrowUpRight,
  CalendarDays,
  ChevronDown,
  CirclePlay,
  Compass,
  MapPin,
  Search,
  ShieldCheck,
  Sparkles,
  TicketCheck,
} from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import Brand from "../components/Brand.jsx";
import EmptyState from "../components/EmptyState.jsx";

const categories = [
  { name: "Music", icon: "♫", className: "bg-[#f9e9f2] text-[#c74d8c]" },
  { name: "Technology", icon: "⌘", className: "bg-[#ece9ff] text-[#6c55cf]" },
  { name: "Food & drink", icon: "✳", className: "bg-[#fff1de] text-[#c77c23]" },
  { name: "Arts & culture", icon: "✺", className: "bg-[#e6f4f0] text-[#398976]" },
  { name: "Business", icon: "↗", className: "bg-[#e7effb] text-[#4c78b5]" },
  { name: "Sports", icon: "◉", className: "bg-[#f9eae7] text-[#c9614d]" },
];

export default function HomePage({ showEventNotice = false }) {
  const [query, setQuery] = useState("");
  const [location, setLocation] = useState("");
  const navigate = useNavigate();

  function handleSearch(event) {
    event.preventDefault();
    const params = new URLSearchParams();
    if (query.trim()) params.set("q", query.trim());
    if (location.trim()) params.set("city", location.trim());
    navigate(`/events${params.size ? `?${params}` : ""}`);
  }

  return (
    <div className="min-h-screen overflow-hidden bg-lavender text-ink">
      <header className="relative z-10 mx-auto flex max-w-[1320px] items-center justify-between px-5 py-5 sm:px-8 lg:px-12">
        <Brand />
        <nav className="hidden items-center gap-9 text-sm font-semibold text-[#5e5972] md:flex" aria-label="Main navigation">
          <a className="nav-link" href="#discover">Discover</a>
          <a className="nav-link" href="#categories">Categories</a>
          <a className="nav-link" href="#about">How it works</a>
        </nav>
        <div className="flex items-center gap-3">
          <Link className="hidden rounded-full px-4 py-2.5 text-sm font-semibold text-ink transition hover:bg-white md:inline-flex" to="/login">
            Sign in
          </Link>
          <Link className="rounded-full bg-ink px-5 py-2.5 text-sm font-semibold text-white shadow-lg shadow-ink/10 transition hover:-translate-y-0.5 hover:bg-violet" to="/organizer/events">
            Start organizing
          </Link>
        </div>
      </header>

      <main>
        <section className="relative mx-auto grid max-w-[1320px] items-center gap-12 px-5 pb-20 pt-9 sm:px-8 md:pt-14 lg:grid-cols-[1.05fr_.95fr] lg:px-12 lg:pb-28 lg:pt-16">
          <div className="pointer-events-none absolute -left-40 top-12 h-80 w-80 rounded-full bg-[#ddcef8]/40 blur-3xl" />
          <div className="relative z-[1]">
            <div className="mb-6 inline-flex items-center gap-2 rounded-full border border-white bg-white/70 px-3.5 py-2 text-xs font-semibold text-violet shadow-sm">
              <Sparkles size={14} />
              Make room for a little wonder
            </div>
            <h1 className="max-w-[660px] text-[clamp(3.35rem,7.1vw,6.6rem)] font-extrabold leading-[.99] tracking-[-.075em]">
              Find your
              <br />
              <span className="gradient-text">next feeling.</span>
            </h1>
            <p className="mt-6 max-w-lg text-base leading-7 text-muted sm:text-lg sm:leading-8">
              The best nights, bright ideas, and shared moments start with a
              ticket. Find the experience that feels like you.
            </p>

            <form className="search-panel mt-9 flex flex-col gap-2 rounded-[24px] bg-white p-3 shadow-panel sm:flex-row sm:items-center" onSubmit={handleSearch}>
              <label className="flex min-w-0 flex-1 items-center gap-3 rounded-2xl px-3 py-3 sm:py-2">
                <Search className="shrink-0 text-violet" size={19} />
                <span className="min-w-0 flex-1">
                  <span className="block text-[10px] font-bold uppercase tracking-[.13em] text-[#9994a8]">Find your event</span>
                  <input aria-label="Event name or category" className="mt-1 w-full border-0 bg-transparent p-0 text-sm font-semibold text-ink outline-none placeholder:font-normal placeholder:text-[#aaa6b5] focus:ring-0" onChange={(event) => setQuery(event.target.value)} placeholder="Artist, event, or category" value={query} />
                </span>
              </label>
              <span className="hidden h-10 w-px bg-[#efedf4] sm:block" />
              <label className="flex min-w-0 flex-1 items-center gap-3 rounded-2xl px-3 py-3 sm:py-2">
                <MapPin className="shrink-0 text-violet" size={19} />
                <span className="min-w-0 flex-1">
                  <span className="block text-[10px] font-bold uppercase tracking-[.13em] text-[#9994a8]">Where</span>
                  <input aria-label="City or location" className="mt-1 w-full border-0 bg-transparent p-0 text-sm font-semibold text-ink outline-none placeholder:font-normal placeholder:text-[#aaa6b5] focus:ring-0" onChange={(event) => setLocation(event.target.value)} placeholder="City or anywhere" value={location} />
                </span>
                <ChevronDown className="hidden shrink-0 text-[#aaa6b5] sm:block" size={15} />
              </label>
              <button aria-label="Search events" className="flex h-12 shrink-0 items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-violet to-[#9860d7] px-6 text-sm font-bold text-white shadow-lg shadow-violet/20 transition hover:brightness-105" type="submit">
                <Search size={17} />
                <span className="sm:hidden">Search events</span>
              </button>
            </form>

            <div className="mt-7 flex flex-wrap items-center gap-x-5 gap-y-3 text-xs font-medium text-muted">
              <span className="font-bold text-[#5b566d]">Popular:</span>
              {["Live music", "Tech", "Food festivals"].map((item) => (
                <button className="transition hover:text-violet" key={item} onClick={() => setQuery(item)} type="button">{item}</button>
              ))}
            </div>
          </div>

          <div className="relative mx-auto h-[430px] w-full max-w-[540px] lg:h-[550px]">
            <div className="absolute inset-[8%_7%_5%_12%] rotate-[5deg] rounded-[42%_58%_44%_56%/47%_40%_60%_53%] bg-gradient-to-br from-[#f0c4e1] via-[#d7c8f6] to-[#abb8ee]" />
            <div className="absolute inset-[14%_13%_11%_18%] rotate-[-4deg] rounded-[46%_54%_56%_44%/43%_55%_45%_57%] bg-gradient-to-br from-[#f8d3e2] via-[#f2b9d1] to-[#d2a7e6] opacity-80" />
            <div className="hero-art absolute inset-[12%_12%_8%_13%] overflow-hidden rounded-[47%_53%_44%_56%/51%_39%_61%_49%] bg-gradient-to-br from-[#b79ce8] via-[#e48ebc] to-[#f4b77a]">
              <div className="hero-orb absolute -right-10 top-4 h-64 w-64 rounded-full bg-[#ffe9bc]/70 blur-[2px]" />
              <div className="absolute bottom-[-18%] left-[12%] h-[69%] w-[57%] rounded-t-[48%] bg-gradient-to-b from-[#332c5a] to-[#17162d] shadow-2xl">
                <div className="absolute -left-[17%] top-[23%] h-[48%] w-[43%] rotate-[-16deg] rounded-full bg-[#272448]" />
                <div className="absolute -right-[22%] top-[27%] h-[50%] w-[38%] rotate-[17deg] rounded-full bg-[#272448]" />
                <div className="absolute left-[37%] top-[-15%] h-28 w-28 rounded-full bg-[#d68d82] shadow-inner" />
                <div className="absolute left-[35%] top-[-22%] h-20 w-32 -rotate-12 rounded-[50%_50%_22%_22%] bg-[#26223f]" />
              </div>
              <div className="absolute bottom-[-26%] right-[4%] h-[53%] w-[45%] rounded-t-[48%] bg-gradient-to-b from-[#f5d6bd] to-[#dfa58e]">
                <div className="absolute -left-[15%] top-[20%] h-[54%] w-[43%] -rotate-[12deg] rounded-full bg-[#f3c8ae]" />
                <div className="absolute left-[34%] top-[-15%] h-24 w-24 rounded-full bg-[#d99d8d]" />
                <div className="absolute left-[27%] top-[-24%] h-16 w-32 rotate-[10deg] rounded-[50%_50%_20%_20%] bg-[#493344]" />
              </div>
              <div className="absolute bottom-[6%] left-[9%] h-2 w-2 rounded-full bg-white/90 shadow-[0_0_26px_9px_rgba(255,255,255,.55)]" />
              <div className="absolute right-[18%] top-[28%] h-2 w-2 rounded-full bg-white/90 shadow-[0_0_25px_8px_rgba(255,255,255,.5)]" />
              <div className="absolute left-[35%] top-[19%] text-white/80"><Sparkles size={23} /></div>
            </div>
            <div className="absolute left-0 top-[22%] flex items-center gap-3 rounded-2xl border border-white/80 bg-white/90 p-3.5 shadow-panel backdrop-blur">
              <span className="grid h-10 w-10 place-items-center rounded-xl bg-[#f5e5f1] text-[#bd5799]"><CirclePlay size={19} /></span>
              <span><span className="block text-xs font-bold">Live the moment</span><span className="mt-1 block text-[10px] text-muted">Make it a night to remember</span></span>
            </div>
            <div className="absolute bottom-[16%] right-0 flex items-center gap-3 rounded-2xl border border-white/80 bg-white/90 p-3.5 shadow-panel backdrop-blur">
              <span className="grid h-10 w-10 place-items-center rounded-xl bg-[#ece8ff] text-violet"><TicketCheck size={19} /></span>
              <span><span className="block text-xs font-bold">Your next story</span><span className="mt-1 block text-[10px] text-muted">Starts with one little ticket</span></span>
            </div>
            <span className="absolute right-[7%] top-[8%] text-violet"><Sparkles size={26} /></span>
            <span className="absolute bottom-[9%] left-[6%] text-[#d979af]"><Sparkles size={19} /></span>
          </div>
        </section>

        <section className="relative bg-white py-14 sm:py-20" id="discover">
          <div className="mx-auto max-w-[1320px] px-5 sm:px-8 lg:px-12">
            <div className="mb-8 flex flex-wrap items-end justify-between gap-5">
              <div>
                <p className="eyebrow">A good time is closer than you think</p>
                <h2 className="section-title mt-3">Find your kind of <span className="gradient-text">wonder.</span></h2>
              </div>
              <Link className="inline-flex items-center gap-2 text-sm font-bold text-violet transition hover:gap-3" to="/events">Explore events <ArrowRight size={17} /></Link>
            </div>
            {showEventNotice && (
              <p className="mb-5 rounded-xl bg-lavender px-4 py-3 text-sm text-muted" role="status">
                Event search is ready for the catalog API; event results will appear when the backend is connected.
              </p>
            )}
            <EmptyState
              title="The next great event is just around the corner"
              description="Event listings will appear here once the catalog is connected. In the meantime, explore the kinds of experiences Ventixe is made for."
            />
          </div>
        </section>

        <section className="bg-[#fbfaff] py-14 sm:py-20" id="categories">
          <div className="mx-auto max-w-[1320px] px-5 sm:px-8 lg:px-12">
            <div className="mb-8">
              <p className="eyebrow">Follow what moves you</p>
              <h2 className="section-title mt-3">Explore by <span className="gradient-text">interest.</span></h2>
            </div>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
              {categories.map((category) => (
                <Link className="group rounded-2xl border border-[#f0edf6] bg-white p-4 transition hover:-translate-y-1 hover:shadow-panel" key={category.name} to={`/events?category=${encodeURIComponent(category.name)}`}>
                  <span className={`grid h-11 w-11 place-items-center rounded-[14px] text-xl font-bold ${category.className}`}>{category.icon}</span>
                  <span className="mt-5 block text-sm font-bold">{category.name}</span>
                  <span className="mt-2 inline-flex items-center gap-1 text-[11px] font-semibold text-muted transition group-hover:text-violet">Explore <ArrowUpRight size={12} /></span>
                </Link>
              ))}
            </div>
          </div>
        </section>

        <section className="bg-white py-16 sm:py-24" id="about">
          <div className="mx-auto grid max-w-[1320px] gap-12 px-5 sm:px-8 lg:grid-cols-2 lg:items-center lg:px-12">
            <div>
              <p className="eyebrow">Less planning. More feeling.</p>
              <h2 className="section-title mt-3 max-w-xl">A little easier to say <span className="gradient-text">“I was there.”</span></h2>
              <p className="mt-5 max-w-lg text-sm leading-7 text-muted sm:text-base">From the first search to the final encore, Ventixe brings the details together so you can focus on the moments worth remembering.</p>
              <Link className="mt-7 inline-flex items-center gap-2 rounded-full bg-ink px-5 py-3 text-sm font-bold text-white transition hover:bg-violet" to="/organizer/events">Bring your event to life <ArrowRight size={16} /></Link>
            </div>
            <div className="grid gap-4 sm:grid-cols-2">
              <FeatureCard icon={<Compass size={20} />} title="Find your people" text="Discover experiences that fit your interests and your city." color="violet" />
              <FeatureCard icon={<TicketCheck size={20} />} title="Tickets, together" text="Keep your booking details and digital tickets in one place." color="rose" />
              <FeatureCard icon={<ShieldCheck size={20} />} title="Book with confidence" text="Clear event details and secure payment flows, end to end." color="green" />
              <FeatureCard icon={<CalendarDays size={20} />} title="Make a plan" text="Keep the dates that matter close and easy to find." color="amber" />
            </div>
          </div>
        </section>

        <section className="bg-ink px-5 py-14 text-white sm:px-8 sm:py-20" id="organizers">
          <div className="mx-auto flex max-w-[1320px] flex-col items-start justify-between gap-7 md:flex-row md:items-center lg:px-4">
            <div>
              <p className="text-xs font-bold uppercase tracking-[.18em] text-[#c6b7ff]">For the people who make it happen</p>
              <h2 className="mt-3 max-w-2xl text-3xl font-extrabold tracking-[-.045em] sm:text-4xl">Your event deserves a <span className="text-[#d6b1f5]">full house.</span></h2>
              <p className="mt-3 max-w-xl text-sm leading-6 text-white/60">Create an event, bring your audience together, and keep the details organized.</p>
            </div>
            <Link className="inline-flex shrink-0 items-center gap-2 rounded-full bg-white px-5 py-3 text-sm font-bold text-ink transition hover:bg-[#eee8ff]" to="/organizer/events">Explore organizer tools <ArrowRight size={16} /></Link>
          </div>
        </section>
      </main>

      <footer className="bg-[#191529] px-5 py-7 text-white/55 sm:px-8">
        <div className="mx-auto flex max-w-[1320px] flex-col items-center justify-between gap-4 sm:flex-row">
          <Brand light />
          <p className="text-xs">Made for moments that stay with you.</p>
          <Link className="inline-flex items-center gap-1 text-xs transition hover:text-white" to="/dashboard/admin">Platform preview <ArrowDownRight size={13} /></Link>
        </div>
      </footer>
    </div>
  );
}

function FeatureCard({ icon, title, text, color }) {
  const colors = {
    violet: "bg-[#eee9ff] text-violet",
    rose: "bg-[#fceaf3] text-[#cf5e99]",
    green: "bg-[#e4f4ee] text-[#38836d]",
    amber: "bg-[#fff1df] text-[#be7a28]",
  };
  return (
    <div className="rounded-[22px] border border-[#f0edf6] bg-[#fdfcff] p-5">
      <span className={`grid h-11 w-11 place-items-center rounded-[14px] ${colors[color]}`}>{icon}</span>
      <h3 className="mt-4 text-sm font-bold">{title}</h3>
      <p className="mt-2 text-xs leading-5 text-muted">{text}</p>
    </div>
  );
}
