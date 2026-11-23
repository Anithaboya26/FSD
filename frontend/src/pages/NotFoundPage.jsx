import { ArrowLeft } from "lucide-react";
import { Link } from "react-router-dom";
import Brand from "../components/Brand.jsx";

export default function NotFoundPage() {
  return (
    <main className="grid min-h-screen place-items-center bg-lavender px-5 text-center">
      <div>
        <div className="mb-10 flex justify-center"><Brand /></div>
        <p className="text-sm font-bold uppercase tracking-[.18em] text-violet">404 · Lost in the crowd</p>
        <h1 className="mt-4 text-5xl font-extrabold tracking-[-.06em]">This page isn’t on the guest list.</h1>
        <p className="mx-auto mt-4 max-w-md text-sm leading-6 text-muted">The link may have changed, or the page may have moved. Let’s get you back to the good stuff.</p>
        <Link className="mt-7 inline-flex items-center gap-2 rounded-full bg-ink px-5 py-3 text-sm font-bold text-white transition hover:bg-violet" to="/"><ArrowLeft size={16} /> Back to home</Link>
      </div>
    </main>
  );
}
