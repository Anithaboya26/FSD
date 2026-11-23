import { ArrowLeft, LoaderCircle, LockKeyhole, Mail, Sparkles, UserRound } from "lucide-react";
import { useState } from "react";
import { useForm, useWatch } from "react-hook-form";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import Brand from "../components/Brand.jsx";
import { useAuth } from "../context/AuthContext.jsx";
import { getApiErrorMessage } from "../api/client.js";

export function LoginPage() {
  const { signIn, user, status } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState("");
  const { register, handleSubmit, formState: { errors, isSubmitting } } = useForm({
    defaultValues: { email: "", password: "" },
  });

  if (status === "ready" && user) {
    return <Navigate replace to={`/dashboard/${user.role.toLowerCase()}`} />;
  }

  async function onSubmit(values) {
    setServerError("");
    try {
      const currentUser = await signIn(values);
      const requestedPath = location.state?.from?.pathname;
      const ownDashboard = `/dashboard/${currentUser.role.toLowerCase()}`;
      navigate(requestedPath?.startsWith(ownDashboard) ? requestedPath : ownDashboard, { replace: true });
    } catch (error) {
      setServerError(getApiErrorMessage(error));
    }
  }

  return (
    <AuthLayout eyebrow="Welcome back" title="Good to see you again." description="Sign in to pick up right where the good memories begin.">
      <form className="mt-8 space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
        {serverError && <FormNotice>{serverError}</FormNotice>}
        <InputField
          autoComplete="email"
          error={errors.email?.message}
          icon={<Mail size={17} />}
          label="Email address"
          name="email"
          placeholder="you@example.com"
          register={register}
          rules={{
            required: "Enter your email address.",
            pattern: { value: /^\S+@\S+\.\S+$/, message: "Enter a valid email address." },
            maxLength: { value: 254, message: "Email must be 254 characters or fewer." },
          }}
          type="email"
        />
        <InputField
          autoComplete="current-password"
          error={errors.password?.message}
          icon={<LockKeyhole size={17} />}
          label="Password"
          name="password"
          placeholder="Your password"
          register={register}
          rules={{
            required: "Enter your password.",
            maxLength: { value: 72, message: "Password must be 72 characters or fewer." },
          }}
          type="password"
        />
        <SubmitButton busy={isSubmitting}>Sign in</SubmitButton>
      </form>
      <p className="mt-7 text-center text-sm text-muted">
        New to Ventixe? <Link className="font-bold text-violet hover:text-rose" to="/register">Create an account</Link>
      </p>
    </AuthLayout>
  );
}

export function RegisterPage() {
  const { signUp, user, status } = useAuth();
  const navigate = useNavigate();
  const [serverError, setServerError] = useState("");
  const { register, handleSubmit, control, formState: { errors, isSubmitting } } = useForm({
    defaultValues: {
      firstName: "",
      lastName: "",
      email: "",
      password: "",
      role: "CUSTOMER",
      businessName: "",
    },
  });
  const role = useWatch({ control, name: "role" });

  if (status === "ready" && user) {
    return <Navigate replace to={`/dashboard/${user.role.toLowerCase()}`} />;
  }

  async function onSubmit(values) {
    setServerError("");
    try {
      const currentUser = await signUp(values);
      navigate(`/dashboard/${currentUser.role.toLowerCase()}`, { replace: true });
    } catch (error) {
      setServerError(getApiErrorMessage(error));
    }
  }

  return (
    <AuthLayout eyebrow="Your next chapter" title="Make an account. Make a plan." description="Join Ventixe to keep your favorite experiences close.">
      <form className="mt-7 space-y-4" onSubmit={handleSubmit(onSubmit)} noValidate>
        {serverError && <FormNotice>{serverError}</FormNotice>}
        <div className="grid gap-4 sm:grid-cols-2">
          <InputField
            autoComplete="given-name"
            error={errors.firstName?.message}
            icon={<UserRound size={17} />}
            label="First name"
            name="firstName"
            placeholder="First name"
            register={register}
            rules={{ required: "Enter your first name.", maxLength: { value: 100, message: "Use 100 characters or fewer." } }}
          />
          <InputField
            autoComplete="family-name"
            error={errors.lastName?.message}
            icon={<UserRound size={17} />}
            label="Last name"
            name="lastName"
            placeholder="Last name"
            register={register}
            rules={{ required: "Enter your last name.", maxLength: { value: 100, message: "Use 100 characters or fewer." } }}
          />
        </div>
        <InputField
          autoComplete="email"
          error={errors.email?.message}
          icon={<Mail size={17} />}
          label="Email address"
          name="email"
          placeholder="you@example.com"
          register={register}
          rules={{
            required: "Enter your email address.",
            pattern: { value: /^\S+@\S+\.\S+$/, message: "Enter a valid email address." },
            maxLength: { value: 254, message: "Email must be 254 characters or fewer." },
          }}
          type="email"
        />
        <InputField
          autoComplete="new-password"
          error={errors.password?.message}
          icon={<LockKeyhole size={17} />}
          label="Password"
          name="password"
          placeholder="At least 12 characters"
          register={register}
          rules={{
            required: "Choose a password.",
            minLength: { value: 12, message: "Use at least 12 characters." },
            maxLength: { value: 72, message: "Password must be 72 characters or fewer." },
          }}
          type="password"
        />
        <div>
          <span className="mb-2 block text-xs font-bold text-ink">I’m here as a</span>
          <div className="grid grid-cols-2 gap-2">
            {[
              ["CUSTOMER", "Event-goer"],
              ["ORGANIZER", "Organizer"],
            ].map(([value, label]) => (
              <label className={`flex cursor-pointer items-center gap-2.5 rounded-xl border px-3 py-3 text-sm font-semibold transition ${role === value ? "border-violet/40 bg-[#f2efff] text-violet" : "border-[#efedf4] text-[#77738a] hover:border-violet/20"}`} key={value}>
                <input className="accent-violet" type="radio" value={value} {...register("role")} />
                {label}
              </label>
            ))}
          </div>
        </div>
        {role === "ORGANIZER" && (
          <InputField
            error={errors.businessName?.message}
            icon={<Sparkles size={17} />}
            label="Business or organization"
            name="businessName"
            placeholder="Your event brand"
            register={register}
            rules={{ required: "Enter your business or organization name.", maxLength: { value: 200, message: "Use 200 characters or fewer." } }}
          />
        )}
        <SubmitButton busy={isSubmitting}>Create account</SubmitButton>
      </form>
      <p className="mt-6 text-center text-sm text-muted">
        Already have an account? <Link className="font-bold text-violet hover:text-rose" to="/login">Sign in</Link>
      </p>
    </AuthLayout>
  );
}

function AuthLayout({ eyebrow, title, description, children }) {
  return (
    <main className="grid min-h-screen bg-white lg:grid-cols-[1fr_.95fr]">
      <section className="relative hidden overflow-hidden bg-ink px-12 py-10 text-white lg:flex lg:flex-col lg:justify-between xl:px-20">
        <div className="absolute -left-20 top-1/4 h-80 w-80 rounded-full bg-violet/40 blur-[100px]" />
        <div className="absolute -bottom-28 right-[-5%] h-96 w-96 rounded-full bg-rose/25 blur-[100px]" />
        <Brand light />
        <div className="relative z-[1] max-w-lg py-16">
          <p className="text-xs font-bold uppercase tracking-[.2em] text-[#d3c4ff]">A world of moments</p>
          <h1 className="mt-5 text-5xl font-extrabold leading-[1.05] tracking-[-.065em] xl:text-6xl">Find the thing you’ll be talking about <span className="text-[#d6b1f5]">next week.</span></h1>
          <p className="mt-6 max-w-md text-sm leading-7 text-white/60">Discover live music, bright ideas, new places, and the people you want to share them with.</p>
        </div>
        <p className="text-xs text-white/40">Ventixe · Find your next feeling.</p>
      </section>
      <section className="flex items-center justify-center px-5 py-8 sm:px-10 lg:px-14">
        <div className="w-full max-w-[440px]">
          <div className="mb-10 flex items-center justify-between lg:hidden"><Brand /><Link className="text-xs font-semibold text-muted" to="/">Home</Link></div>
          <Link className="mb-9 hidden items-center gap-2 text-xs font-semibold text-muted transition hover:text-violet lg:inline-flex" to="/"><ArrowLeft size={14} /> Back to home</Link>
          <p className="eyebrow">{eyebrow}</p>
          <h2 className="mt-3 text-3xl font-extrabold leading-tight tracking-[-.06em] sm:text-[38px]">{title}</h2>
          <p className="mt-3 text-sm leading-6 text-muted">{description}</p>
          {children}
        </div>
      </section>
    </main>
  );
}

function InputField({ autoComplete, error, icon, label, name, placeholder, register, rules, type = "text" }) {
  const inputId = `auth-${name}`;
  const errorId = `${inputId}-error`;
  return (
    <div>
      <label className="mb-2 block text-xs font-bold text-ink" htmlFor={inputId}>{label}</label>
      <div className={`flex h-12 items-center gap-3 rounded-xl border bg-white px-3 transition focus-within:ring-2 ${error ? "border-rose/50 focus-within:border-rose focus-within:ring-rose/10" : "border-[#eae7f1] focus-within:border-violet/40 focus-within:ring-violet/10"}`}>
        <span className="shrink-0 text-[#aaa6b5]">{icon}</span>
        <input
          autoComplete={autoComplete}
          aria-describedby={error ? errorId : undefined}
          aria-invalid={Boolean(error)}
          className="h-full min-w-0 flex-1 border-0 bg-transparent p-0 text-sm text-ink outline-none placeholder:text-[#aaa6b5] focus:ring-0"
          id={inputId}
          placeholder={placeholder}
          type={type}
          {...register(name, rules)}
        />
      </div>
      {error && <p className="mt-1.5 text-xs text-[#bd416b]" id={errorId}>{error}</p>}
    </div>
  );
}

function SubmitButton({ busy, children }) {
  return (
    <button className="flex h-12 w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-violet to-[#9860d7] text-sm font-bold text-white shadow-lg shadow-violet/20 transition hover:brightness-105 disabled:cursor-not-allowed disabled:opacity-70" disabled={busy} type="submit">
      {busy && <LoaderCircle className="animate-spin" size={16} />}
      {children}
    </button>
  );
}

function FormNotice({ children }) {
  return <p className="rounded-xl border border-rose/20 bg-[#fff4f8] px-4 py-3 text-sm text-[#9b365e]" role="alert">{children}</p>;
}
