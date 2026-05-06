/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#1B4F8A",
          light:   "#2563EB",
          dark:    "#1e3a5f",
        },
      },
      fontFamily: {
        sans: ["Arial", "Helvetica", "sans-serif"],
      },
      spacing: {
        "grid": "8px",
      },
      minHeight: {
        "touch": "44px",
      },
    },
  },
  plugins: [],
};
