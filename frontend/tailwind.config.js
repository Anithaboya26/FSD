/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ink: "#201a35",
        muted: "#77738a",
        lavender: "#f7f5fc",
        violet: "#7455dc",
        rose: "#e76ca7",
      },
      boxShadow: {
        panel: "0 18px 55px rgba(48, 34, 91, 0.08)",
      },
      fontFamily: {
        sans: ["Inter", "ui-sans-serif", "system-ui", "sans-serif"],
      },
    },
  },
  plugins: [],
};
