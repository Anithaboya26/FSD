import { Ticket } from "lucide-react";
import { Link } from "react-router-dom";

export default function Brand({ light = false }) {
  return (
    <Link
      className={`inline-flex items-center gap-2.5 font-extrabold tracking-[-0.06em] ${
        light ? "text-white" : "text-ink"
      }`}
      to="/"
      aria-label="Ventixe home"
    >
      <span className="grid h-10 w-10 place-items-center rounded-[14px] bg-gradient-to-br from-violet to-rose text-white shadow-lg shadow-violet/20">
        <Ticket size={21} strokeWidth={2.2} />
      </span>
      <span className="text-[23px]">ventixe</span>
    </Link>
  );
}
