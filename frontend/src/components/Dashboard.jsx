import {
  ArrowDownToLine,
  ArrowLeft,
  ArrowUpRight,
  Bell,
  CalendarDays,
  ChartNoAxesCombined,
  ChevronDown,
  CircleDollarSign,
  ClipboardList,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  ShieldCheck,
  Ticket,
  Users,
  X,
} from "lucide-react";
import { useState } from "react";
import { Link, Navigate, useParams } from "react-router-dom";
import Brand from "./Brand.jsx";
import EmptyState from "./EmptyState.jsx";
import { useAuth } from "../context/AuthContext.jsx";

const roleDetails = {
  customer: {
    title: "Your space",
    subtitle: "Your plans, all in one place.",
    label: "Customer",
    initials: "GU",
    items: [
      ["Overview", LayoutDashboard],
      ["My bookings", Ticket],
      ["My tickets", ClipboardList],
      ["Wishlist", CalendarDays],
    ],
  },
  organizer: {
    title: "Organizer studio",
    subtitle: "Everything you need to bring people together.",
    label: "Organizer",
    initials: "OR",
    items: [
      ["Overview", LayoutDashboard],
      ["My events", CalendarDays],
      ["Attendees", Users],
      ["Analytics", ChartNoAxesCombined],
    ],
  },
  admin: {
    title: "Platform overview",
    subtitle: "A clear view of your event community.",
    label: "Administrator",
    initials: "AD",
    items: [
      ["Overview", LayoutDashboard],
      ["Users", Users],
      ["Event approvals", ShieldCheck],
      ["Transactions", CircleDollarSign],
      ["Reports", ChartNoAxesCombined],
    ],
  },
};

const metricsByRole = {
  customer: [
    { title: "My bookings", icon: Ticket },
    { title: "Upcoming plans", icon: CalendarDays },
    { title: "Saved events", icon: Users },
    { title: "Total spent", icon: CircleDollarSign },
  ],
  organizer: [
    { title: "Total bookings", icon: Ticket },
    { title: "Upcoming events", icon: CalendarDays },
    { title: "Tickets sold", icon: Users },
    { title: "Total revenue", icon: CircleDollarSign },
  ],
  admin: [
    { title: "Registered users", icon: Users },
    { title: "Active organizers", icon: ShieldCheck },
    { title: "Published events", icon: CalendarDays },
    { title: "Platform revenue", icon: CircleDollarSign },
  ],
};

export default function Dashboard() {
  const { role } = useParams();
  const [menuOpen, setMenuOpen] = useState(false);
  const [selectedSection, setSelectedSection] = useState("Overview");
  const [logoutError, setLogoutError] = useState("");
  const { user, signOut } = useAuth();
  const details = roleDetails[role];

  if (!details) return <Navigate replace to="/not-found" />;

  async function handleSignOut() {
    setLogoutError("");
    try {
      await signOut();
    } catch (error) {
      setLogoutError("We couldn't sign you out. Please try again.");
    }
  }

  return (
    <div className="min-h-screen bg-[#f7f5fc] text-ink">
      <aside className={`fixed inset-y-0 left-0 z-30 flex w-[260px] flex-col border-r border-[#efedf4] bg-white px-5 py-6 transition-transform lg:translate-x-0 ${menuOpen ? "translate-x-0 shadow-2xl" : "-translate-x-full"}`}>
        <div className="flex items-center justify-between">
          <Brand />
          <button aria-label="Close navigation" className="rounded-lg p-2 text-muted hover:bg-lavender lg:hidden" onClick={() => setMenuOpen(false)}><X size={18} /></button>
        </div>
        <div className="mt-10 rounded-2xl bg-gradient-to-br from-[#f0ebff] to-[#fff2f8] p-4">
          <p className="text-[10px] font-bold uppercase tracking-[.16em] text-violet">Workspace</p>
          <p className="mt-2 text-sm font-bold">{details.label} space</p>
          <p className="mt-1 text-xs text-muted">VENTIXE · Preview</p>
        </div>
        <p className="mb-3 mt-9 px-3 text-[10px] font-bold uppercase tracking-[.16em] text-[#aaa6b5]">Menu</p>
        <nav className="space-y-1" aria-label="Dashboard navigation">
          {details.items.map(([label, Icon]) => {
            const eventManagementLink = role === "organizer" && label === "My events";
            const classes = `flex w-full items-center gap-3 rounded-xl px-3 py-3 text-left text-sm font-semibold transition ${selectedSection === label ? "bg-[#f0ebff] text-violet" : "text-[#77738a] hover:bg-[#faf9fd] hover:text-ink"}`;
            return eventManagementLink ? (
              <Link className={classes} key={label} onClick={() => setMenuOpen(false)} to="/organizer/events">
                <Icon size={17} strokeWidth={1.9} />{label}
              </Link>
            ) : (
              <button className={classes} key={label} onClick={() => { setSelectedSection(label); setMenuOpen(false); }} type="button">
                <Icon size={17} strokeWidth={1.9} />{label}
                {label === "Event approvals" && <span className="ml-auto h-2 w-2 rounded-full bg-[#e77bad]" />}
              </button>
            );
          })}
        </nav>
        <div className="mt-auto">
          <div className="rounded-2xl border border-[#f0edf6] p-4">
            <div className="flex items-start gap-3">
              <span className="grid h-9 w-9 shrink-0 place-items-center rounded-xl bg-[#eee9ff] text-violet"><ShieldCheck size={17} /></span>
              <div><p className="text-xs font-bold">A little help?</p><p className="mt-1 text-[11px] leading-5 text-muted">Your workspace is taking shape.</p></div>
            </div>
          </div>
          <Link className="mt-4 flex items-center gap-2 px-3 py-2 text-xs font-semibold text-muted transition hover:text-violet" to="/"><ArrowLeft size={14} /> Back to Ventixe</Link>
        </div>
      </aside>

      {menuOpen && <button aria-label="Close menu overlay" className="fixed inset-0 z-20 bg-ink/30 lg:hidden" onClick={() => setMenuOpen(false)} />}

      <div className="min-h-screen lg:pl-[260px]">
        <header className="sticky top-0 z-10 flex h-[76px] items-center justify-between border-b border-[#efedf4] bg-white/90 px-5 backdrop-blur sm:px-8">
          <div className="flex items-center gap-3">
            <button aria-label="Open navigation" className="rounded-lg p-2 text-muted hover:bg-lavender lg:hidden" onClick={() => setMenuOpen(true)}><Menu size={19} /></button>
            <div className="relative hidden sm:block">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-[#aaa6b5]" size={16} />
              <input aria-label="Search dashboard (available after API integration)" className="h-10 w-56 rounded-xl border border-[#efedf4] bg-[#faf9fd] pl-9 pr-3 text-xs outline-none transition focus:border-violet/40 focus:ring-2 focus:ring-violet/10" disabled placeholder="Search after API setup" />
            </div>
          </div>
          <div className="flex items-center gap-3 sm:gap-5">
            {logoutError && <p className="hidden text-xs text-[#bd416b] sm:block" role="alert">{logoutError}</p>}
            <button aria-label="Notifications (available after API integration)" className="relative cursor-not-allowed rounded-xl p-2 text-[#aaa6b5]" disabled title="Notifications will be enabled with the API"><Bell size={18} /></button>
            <span className="hidden h-8 w-px bg-[#efedf4] sm:block" />
            <div className="flex items-center gap-2.5 rounded-xl p-1.5 text-left">
              <span className="grid h-9 w-9 place-items-center rounded-full bg-gradient-to-br from-violet to-rose text-xs font-bold text-white">{details.initials}</span>
              <span className="hidden sm:block"><span className="block text-xs font-bold">{user.email}</span><span className="mt-0.5 block text-[10px] text-muted">{details.label}</span></span>
            </div>
            <button aria-label="Sign out" className="rounded-xl p-2 text-[#77738a] transition hover:bg-lavender hover:text-violet" onClick={handleSignOut} title="Sign out"><LogOut size={17} /></button>
          </div>
        </header>

        <main className="mx-auto max-w-[1440px] px-5 py-8 sm:px-8 sm:py-10">
          <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
            <div>
              <p className="eyebrow">{details.label} dashboard</p>
              <h1 className="mt-2 text-3xl font-extrabold tracking-[-.055em] sm:text-[38px]">{selectedSection === "Overview" ? details.title : selectedSection}<span className="gradient-text">.</span></h1>
              <p className="mt-2 text-sm text-muted">{selectedSection === "Overview" ? details.subtitle : `${selectedSection} for your ${details.label.toLowerCase()} account.`}</p>
            </div>
            <div className="flex items-center gap-2">
              {role === "organizer" && <Link className="inline-flex h-10 items-center gap-2 rounded-xl bg-violet px-3.5 text-xs font-bold text-white transition hover:brightness-105 sm:px-4" to="/organizer/events"><CalendarDays size={15} /><span className="hidden sm:inline">Manage events</span><span className="sm:hidden">Events</span></Link>}
              <span className="inline-flex h-10 items-center gap-2 rounded-xl border border-[#eae7f1] bg-white px-3.5 text-xs font-semibold text-[#625d73] shadow-sm"><CalendarDays size={15} /> Reporting period</span>
              <button aria-label="Export report (available after API integration)" className="inline-flex h-10 cursor-not-allowed items-center gap-2 rounded-xl bg-ink/50 px-4 text-xs font-bold text-white" disabled title="Reports will be exportable after the analytics API is available"><ArrowDownToLine size={15} /> Export</button>
            </div>
          </div>

          <div className="mt-8 rounded-[22px] border border-[#e9e3ff] bg-gradient-to-r from-[#f0ecff] via-white to-[#fff4f8] p-5 sm:flex sm:items-center sm:justify-between sm:px-6">
            <div>
              <p className="flex items-center gap-2 text-sm font-bold"><span className="grid h-7 w-7 place-items-center rounded-lg bg-white text-violet"><SparkleIcon /></span> Your Ventixe workspace is ready to grow</p>
              <p className="mt-2 pl-9 text-xs leading-5 text-muted">This dashboard will show live {details.label.toLowerCase()} data when connected to the platform API.</p>
            </div>
            <Link className="mt-4 inline-flex items-center gap-2 pl-9 text-xs font-bold text-violet transition hover:gap-3 sm:mt-0 sm:pl-0" to="/">Explore the platform <ArrowUpRight size={14} /></Link>
          </div>

          <section aria-label="Summary metrics" className="mt-6 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
            {metricsByRole[role].map(({ title, icon: Icon }, index) => (
              <div className="rounded-[20px] border border-[#efedf4] bg-white p-5 shadow-sm" key={title}>
                <div className="flex items-center justify-between">
                  <span className={`grid h-10 w-10 place-items-center rounded-[14px] ${["bg-[#eee9ff] text-violet", "bg-[#fceaf3] text-[#cf5e99]", "bg-[#e4f4ee] text-[#38836d]", "bg-[#fff1df] text-[#be7a28]"][index]}`}><Icon size={18} /></span>
                  <span className="text-[10px] font-semibold text-[#aaa6b5]">Awaiting data</span>
                </div>
                <p className="mt-5 text-xs font-medium text-muted">{title}</p>
                <p className="mt-1 text-[28px] font-extrabold tracking-[-.05em] text-ink">—</p>
                <p className="mt-1 text-[10px] text-[#aaa6b5]">Available after API integration</p>
              </div>
            ))}
          </section>

          <section className="mt-6 grid gap-6 xl:grid-cols-[1.55fr_1fr]">
            <div className="rounded-[20px] border border-[#efedf4] bg-white p-5 sm:p-6">
              <div className="flex items-start justify-between gap-3">
                <div><h2 className="text-sm font-bold">Activity overview</h2><p className="mt-1 text-xs text-muted">A live view of activity across your workspace.</p></div>
                <button aria-label="Activity options (available after API integration)" className="cursor-not-allowed rounded-lg p-2 text-[#aaa6b5]" disabled title="Activity controls will be available after API integration"><ChevronDown size={15} /></button>
              </div>
              <div className="mt-5"><EmptyState title="Your activity will show here" description="Once connected, this space will show verified activity from your Ventixe account." /></div>
            </div>
            <div className="rounded-[20px] border border-[#efedf4] bg-white p-5 sm:p-6">
              <div className="flex items-start justify-between gap-3">
                <div><h2 className="text-sm font-bold">Recent updates</h2><p className="mt-1 text-xs text-muted">The latest from your account.</p></div>
                <button aria-label="Update options (available after API integration)" className="cursor-not-allowed rounded-lg p-2 text-[#aaa6b5]" disabled title="Update controls will be available after API integration"><ChevronDown size={15} /></button>
              </div>
              <div className="mt-5"><EmptyState title="All caught up" description="Updates and notifications will appear here when available." /></div>
            </div>
          </section>

          <p className="mt-8 text-center text-[11px] text-[#aaa6b5]">Dashboard preview · Live metrics and workspace actions will be wired in upcoming phases.</p>
        </main>
      </div>
    </div>
  );
}

function SparkleIcon() {
  return <span aria-hidden="true" className="text-sm">✦</span>;
}
