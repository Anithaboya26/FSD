import { ArrowLeft, CirclePlus, LoaderCircle, Minus, Save } from "lucide-react";
import { useEffect, useState } from "react";
import { useFieldArray, useForm } from "react-hook-form";
import { Link, useNavigate, useParams } from "react-router-dom";
import Brand from "../components/Brand.jsx";
import { createEvent, getApiErrorMessage, getEventCategories, getMyEvent, updateEvent } from "../api/client.js";

const localTimezone = Intl.DateTimeFormat().resolvedOptions().timeZone || "UTC";

function emptyTicket() {
  return {
    name: "General Admission",
    description: "",
    price: "0.00",
    currency: "INR",
    quantityTotal: 100,
    perOrderMin: 1,
    perOrderMax: 10,
    salesStartAt: "",
    salesEndAt: "",
  };
}

function emptyForm() {
  return {
    title: "",
    description: "",
    categoryId: "",
    startsAt: "",
    endsAt: "",
    timezone: localTimezone,
    bookingOpensAt: "",
    bookingClosesAt: "",
    ageRestriction: "",
    venue: {
      name: "",
      addressLine1: "",
      addressLine2: "",
      city: "",
      region: "",
      postalCode: "",
      countryCode: "IN",
      latitude: "",
      longitude: "",
      mapUrl: "",
    },
    ticketTypes: [emptyTicket()],
  };
}

export default function CreateEventPage() {
  const { eventId } = useParams();
  const editing = Boolean(eventId);
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [pageError, setPageError] = useState("");
  const [saveNotice, setSaveNotice] = useState("");
  const [loadFailed, setLoadFailed] = useState(false);
  const [loading, setLoading] = useState(editing);
  const {
    register,
    control,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm({ defaultValues: emptyForm() });
  const { fields, append, remove } = useFieldArray({ control, name: "ticketTypes" });

  useEffect(() => {
    let active = true;
    setPageError("");
    setLoadFailed(false);
    Promise.all([
      getEventCategories(),
      editing ? getMyEvent(eventId) : Promise.resolve(null),
    ])
      .then(([availableCategories, event]) => {
        if (!active) return;
        setCategories(availableCategories);
        if (event) {
          reset({
            title: event.title,
            description: event.description,
            categoryId: String(event.categoryId),
            startsAt: toLocalInput(event.startsAt),
            endsAt: toLocalInput(event.endsAt),
            timezone: event.timezone,
            bookingOpensAt: toLocalInput(event.bookingOpensAt),
            bookingClosesAt: toLocalInput(event.bookingClosesAt),
            ageRestriction: event.ageRestriction ?? "",
            venue: {
              name: event.venue.name,
              addressLine1: event.venue.addressLine1,
              addressLine2: event.venue.addressLine2 || "",
              city: event.venue.city,
              region: event.venue.region || "",
              postalCode: event.venue.postalCode || "",
              countryCode: event.venue.countryCode,
              latitude: event.venue.latitude ?? "",
              longitude: event.venue.longitude ?? "",
              mapUrl: event.venue.mapUrl || "",
            },
            ticketTypes: event.ticketTypes.map((ticket) => ({
              name: ticket.name,
              description: ticket.description || "",
              price: String(ticket.price),
              currency: ticket.currency,
              quantityTotal: ticket.availableQuantity,
              perOrderMin: ticket.perOrderMin,
              perOrderMax: ticket.perOrderMax,
              salesStartAt: toLocalInput(ticket.salesStartAt),
              salesEndAt: toLocalInput(ticket.salesEndAt),
            })),
          });
        } else if (availableCategories.length) {
          reset({ ...emptyForm(), categoryId: String(availableCategories[0].id) });
        }
      })
      .catch((requestError) => {
        if (active) {
          setPageError(getApiErrorMessage(requestError));
          setLoadFailed(editing);
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [editing, eventId, reset]);

  async function onSubmit(values) {
    setPageError("");
    setSaveNotice("");
    const body = {
      title: values.title.trim(),
      description: values.description.trim(),
      categoryId: Number(values.categoryId),
      startsAt: values.startsAt,
      endsAt: values.endsAt,
      timezone: values.timezone.trim(),
      bookingOpensAt: values.bookingOpensAt || null,
      bookingClosesAt: values.bookingClosesAt || null,
      ageRestriction: values.ageRestriction === "" ? null : Number(values.ageRestriction),
      venue: {
        ...values.venue,
        latitude: values.venue.latitude === "" ? null : Number(values.venue.latitude),
        longitude: values.venue.longitude === "" ? null : Number(values.venue.longitude),
        mapUrl: values.venue.mapUrl.trim() || null,
      },
      ticketTypes: values.ticketTypes.map((ticket) => ({
        ...ticket,
        name: ticket.name.trim(),
        description: ticket.description.trim() || null,
        price: Number(ticket.price),
        currency: ticket.currency.trim().toUpperCase(),
        quantityTotal: Number(ticket.quantityTotal),
        perOrderMin: Number(ticket.perOrderMin),
        perOrderMax: Number(ticket.perOrderMax),
        salesStartAt: ticket.salesStartAt || null,
        salesEndAt: ticket.salesEndAt || null,
      })),
    };
    try {
      const event = editing ? await updateEvent(eventId, body) : await createEvent(body);
      setSaveNotice("Draft saved.");
      navigate(`/organizer/events/${event.id}/edit`, {
        replace: !editing,
        state: { saved: true },
      });
    } catch (requestError) {
      setPageError(getApiErrorMessage(requestError));
    }
  }

  if (loading) {
    return <div className="grid min-h-screen place-items-center bg-lavender text-violet" role="status"><LoaderCircle className="animate-spin" size={28} /></div>;
  }

  if (loadFailed) {
    return (
      <main className="grid min-h-screen place-items-center bg-lavender px-5">
        <div className="max-w-md text-center">
          <p className="text-sm text-[#9b365e]" role="alert">{pageError}</p>
          <Link className="mt-5 inline-flex items-center gap-2 text-sm font-bold text-violet" to="/organizer/events"><ArrowLeft size={14} /> Your events</Link>
        </div>
      </main>
    );
  }

  return (
    <div className="min-h-screen bg-lavender text-ink">
      <header className="border-b border-[#efedf4] bg-white">
        <div className="mx-auto flex max-w-[1020px] items-center justify-between px-5 py-4 sm:px-8">
          <Brand />
          <Link className="inline-flex items-center gap-2 text-xs font-semibold text-muted transition hover:text-violet" to="/organizer/events"><ArrowLeft size={14} /> Your events</Link>
        </div>
      </header>
      <main className="mx-auto max-w-[1020px] px-5 py-8 sm:px-8 sm:py-11">
        <div>
          <p className="eyebrow">Organizer workspace</p>
          <h1 className="section-title mt-3">{editing ? "Edit your " : "Create an "}<span className="gradient-text">event.</span></h1>
          <p className="mt-3 text-sm text-muted">Save a draft, check the details, then submit it for review.</p>
        </div>
        {pageError && <p className="mt-6 rounded-xl border border-rose/20 bg-[#fff4f8] px-4 py-3 text-sm text-[#9b365e]" role="alert">{pageError}</p>}
        {saveNotice && <p className="mt-6 rounded-xl border border-[#d8eee4] bg-[#f0faf5] px-4 py-3 text-sm text-[#34765f]" role="status">{saveNotice}</p>}
        <form className="mt-7 space-y-5" onSubmit={handleSubmit(onSubmit)} noValidate>
          <FormSection title="The event">
            <div className="grid gap-4 sm:grid-cols-2">
              <Field error={errors.title?.message} label="Event name">
                <input className="form-input" placeholder="An evening worth remembering" {...register("title", { required: "Enter an event name.", maxLength: { value: 240, message: "Use 240 characters or fewer." } })} />
              </Field>
              <Field error={errors.categoryId?.message} label="Category">
                <select className="form-input" {...register("categoryId", { required: "Choose an event category." })}>
                  <option value="">Select a category</option>
                  {categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}
                </select>
              </Field>
            </div>
            <Field error={errors.description?.message} label="Description">
              <textarea className="form-input min-h-32 resize-y py-3" placeholder="Tell guests what to expect..." {...register("description", { required: "Describe the event.", minLength: { value: 20, message: "Add at least 20 characters." }, maxLength: { value: 20000, message: "Use 20,000 characters or fewer." } })} />
            </Field>
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              <Field error={errors.startsAt?.message} label="Starts">
                <input className="form-input" type="datetime-local" {...register("startsAt", { required: "Choose a start date and time." })} />
              </Field>
              <Field error={errors.endsAt?.message} label="Ends">
                <input className="form-input" type="datetime-local" {...register("endsAt", { required: "Choose an end date and time." })} />
              </Field>
              <Field error={errors.timezone?.message} label="Event timezone">
                <input className="form-input" placeholder="Asia/Kolkata" {...register("timezone", { required: "Enter the event timezone.", maxLength: { value: 64, message: "Use 64 characters or fewer." } })} />
              </Field>
              <Field label="Booking opens"><input className="form-input" type="datetime-local" {...register("bookingOpensAt")} /></Field>
              <Field label="Booking closes"><input className="form-input" type="datetime-local" {...register("bookingClosesAt")} /></Field>
              <Field error={errors.ageRestriction?.message} label="Minimum age (optional)">
                <input className="form-input" max="100" min="0" placeholder="All ages" type="number" {...register("ageRestriction", { min: { value: 0, message: "Age cannot be negative." }, max: { value: 100, message: "Age cannot exceed 100." } })} />
              </Field>
            </div>
          </FormSection>

          <FormSection title="Where it happens">
            <div className="grid gap-4 sm:grid-cols-2">
              <Field error={errors.venue?.name?.message} label="Venue name"><input className="form-input" placeholder="Venue or space name" {...register("venue.name", { required: "Enter the venue name.", maxLength: { value: 200, message: "Use 200 characters or fewer." } })} /></Field>
              <Field error={errors.venue?.city?.message} label="City"><input className="form-input" placeholder="City" {...register("venue.city", { required: "Enter the city.", maxLength: { value: 120, message: "Use 120 characters or fewer." } })} /></Field>
              <Field error={errors.venue?.addressLine1?.message} label="Street address"><input className="form-input" placeholder="Street address" {...register("venue.addressLine1", { required: "Enter the street address.", maxLength: { value: 255, message: "Use 255 characters or fewer." } })} /></Field>
              <Field label="Address line 2"><input className="form-input" placeholder="Floor, suite, etc. (optional)" {...register("venue.addressLine2")} /></Field>
              <Field label="State / region"><input className="form-input" placeholder="State or region" {...register("venue.region")} /></Field>
              <div className="grid grid-cols-2 gap-3">
                <Field label="Postal code"><input className="form-input" placeholder="Postal code" {...register("venue.postalCode")} /></Field>
                <Field error={errors.venue?.countryCode?.message} label="Country code"><input className="form-input uppercase" maxLength="2" placeholder="IN" {...register("venue.countryCode", { required: "Enter a two-letter country code.", pattern: { value: /^[A-Za-z]{2}$/, message: "Use a two-letter country code." } })} /></Field>
              </div>
              <Field error={errors.venue?.mapUrl?.message} label="Map link (optional)"><input className="form-input" placeholder="https://…" {...register("venue.mapUrl", { pattern: { value: /^https:\/\/\S+$/i, message: "Use an HTTPS map link." }, maxLength: { value: 1024, message: "Use 1024 characters or fewer." } })} /></Field>
            </div>
          </FormSection>

          <FormSection title="Ticket types">
            <div className="space-y-4">
              {fields.map((field, index) => (
                <fieldset className="rounded-2xl border border-[#efedf4] bg-[#fdfcff] p-4 sm:p-5" key={field.id}>
                  <div className="mb-4 flex items-center justify-between">
                    <legend className="text-sm font-bold">Ticket {index + 1}</legend>
                    {fields.length > 1 && <button aria-label={`Remove ticket type ${index + 1}`} className="rounded-lg p-2 text-muted transition hover:bg-[#fff0f2] hover:text-[#bd416b]" onClick={() => remove(index)} type="button"><Minus size={15} /></button>}
                  </div>
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <Field error={errors.ticketTypes?.[index]?.name?.message} label="Ticket name">
                      <input className="form-input" placeholder="General Admission" {...register(`ticketTypes.${index}.name`, { required: "Enter a ticket name.", maxLength: { value: 100, message: "Use 100 characters or fewer." } })} />
                    </Field>
                    <Field error={errors.ticketTypes?.[index]?.price?.message} label="Price">
                      <input className="form-input" min="0" placeholder="0.00" step="0.01" type="number" {...register(`ticketTypes.${index}.price`, { required: "Enter the ticket price.", min: { value: 0, message: "Price cannot be negative." }, max: { value: 9999999999.99, message: "Price is too high." } })} />
                    </Field>
                    <Field error={errors.ticketTypes?.[index]?.currency?.message} label="Currency">
                      <input className="form-input uppercase" maxLength="3" {...register(`ticketTypes.${index}.currency`, { required: "Enter a currency code.", pattern: { value: /^[A-Za-z]{3}$/, message: "Use a three-letter currency code." } })} />
                    </Field>
                    <Field error={errors.ticketTypes?.[index]?.quantityTotal?.message} label="Tickets available">
                      <input className="form-input" min="1" type="number" {...register(`ticketTypes.${index}.quantityTotal`, { required: "Enter the ticket quantity.", min: { value: 1, message: "Add at least one ticket." }, max: { value: 1000000, message: "Quantity cannot exceed 1,000,000." } })} />
                    </Field>
                    <Field error={errors.ticketTypes?.[index]?.perOrderMin?.message} label="Minimum per order">
                      <input className="form-input" min="1" type="number" {...register(`ticketTypes.${index}.perOrderMin`, { required: "Enter the minimum.", min: { value: 1, message: "Minimum must be at least one." } })} />
                    </Field>
                    <Field error={errors.ticketTypes?.[index]?.perOrderMax?.message} label="Maximum per order">
                      <input className="form-input" min="1" type="number" {...register(`ticketTypes.${index}.perOrderMax`, { required: "Enter the maximum.", min: { value: 1, message: "Maximum must be at least one." } })} />
                    </Field>
                    <Field label="Sales open (optional)"><input className="form-input" type="datetime-local" {...register(`ticketTypes.${index}.salesStartAt`)} /></Field>
                    <Field label="Sales close (optional)"><input className="form-input" type="datetime-local" {...register(`ticketTypes.${index}.salesEndAt`)} /></Field>
                    <div className="sm:col-span-2 lg:col-span-4">
                      <Field label="Ticket description (optional)"><input className="form-input" placeholder="What does this ticket include?" {...register(`ticketTypes.${index}.description`, { maxLength: { value: 500, message: "Use 500 characters or fewer." } })} /></Field>
                    </div>
                  </div>
                </fieldset>
              ))}
            </div>
            <button className="mt-4 inline-flex items-center gap-2 rounded-lg border border-[#eae7f1] bg-white px-3 py-2.5 text-xs font-bold text-violet transition hover:border-violet/30" onClick={() => append(emptyTicket())} type="button"><CirclePlus size={15} /> Add ticket type</button>
          </FormSection>

          <div className="flex flex-col-reverse justify-between gap-3 sm:flex-row sm:items-center">
            <Link className="inline-flex items-center justify-center gap-2 py-2 text-xs font-semibold text-muted hover:text-violet" to="/organizer/events"><ArrowLeft size={14} /> Cancel</Link>
            <button className="inline-flex items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-violet to-[#9860d7] px-5 py-3 text-sm font-bold text-white shadow-lg shadow-violet/20 transition hover:brightness-105 disabled:opacity-60" disabled={isSubmitting} type="submit">{isSubmitting ? <LoaderCircle className="animate-spin" size={16} /> : <Save size={16} />}{editing ? "Save changes" : "Save draft"}</button>
          </div>
          <p className="text-center text-[11px] leading-5 text-muted">Events are saved as drafts and are not public until they pass review.</p>
        </form>
      </main>
    </div>
  );
}

function Field({ label, error, children }) {
  return (
    <label className="block min-w-0 text-xs font-bold text-ink">
      <span className="mb-2 block">{label}</span>
      {children}
      {error && <span className="mt-1.5 block text-[11px] font-medium text-[#bd416b]" role="alert">{error}</span>}
    </label>
  );
}

function FormSection({ title, children }) {
  return <section className="rounded-[22px] border border-[#efedf4] bg-white p-5 shadow-sm sm:p-6"><h2 className="mb-5 text-base font-extrabold tracking-[-.03em]">{title}</h2><div className="space-y-4">{children}</div></section>;
}

function toLocalInput(value) {
  return value ? value.slice(0, 16) : "";
}
