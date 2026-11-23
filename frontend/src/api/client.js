import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api/v1",
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

let csrfToken;

export async function getCsrfToken() {
  const { data } = await api.get("/auth/csrf");
  csrfToken = data.token;
  return csrfToken;
}

export async function postWithCsrf(path, body) {
  if (!csrfToken) {
    await getCsrfToken();
  }
  return api.post(path, body, {
    headers: {
      "X-XSRF-TOKEN": csrfToken,
    },
  });
}

export function getCurrentUser() {
  return api.get("/auth/me").then(({ data }) => data);
}

export function getEventCategories() {
  return api.get("/categories").then(({ data }) => data);
}

export function searchEvents(params) {
  return api.get("/events", { params }).then(({ data }) => data);
}

export function getEvent(slug) {
  return api.get(`/events/${encodeURIComponent(slug)}`).then(({ data }) => data);
}

export function getMyEvents() {
  return api.get("/organizer/events").then(({ data }) => data);
}

export function getMyEvent(id) {
  return api.get(`/organizer/events/${id}`).then(({ data }) => data);
}

export function createEvent(details) {
  return postWithCsrf("/organizer/events", details).then(({ data }) => data);
}

export function updateEvent(id, details) {
  return putWithCsrf(`/organizer/events/${id}`, details).then(({ data }) => data);
}

export function submitEvent(id) {
  return postWithCsrf(`/organizer/events/${id}/submit`);
}

export async function archiveEvent(id) {
  if (!csrfToken) await getCsrfToken();
  await api.delete(`/organizer/events/${id}`, {
    headers: { "X-XSRF-TOKEN": csrfToken },
  });
}

export async function putWithCsrf(path, body) {
  if (!csrfToken) {
    await getCsrfToken();
  }
  return api.put(path, body, {
    headers: { "X-XSRF-TOKEN": csrfToken },
  });
}

export async function login(credentials) {
  const { data } = await postWithCsrf("/auth/login", credentials);
  return data;
}

export async function register(details) {
  const { data } = await postWithCsrf("/auth/register", details);
  return data;
}

export async function logout() {
  await postWithCsrf("/auth/logout");
  csrfToken = undefined;
}

export function getApiErrorMessage(error) {
  return error.response?.data?.message || "We couldn't complete your request. Please try again.";
}
