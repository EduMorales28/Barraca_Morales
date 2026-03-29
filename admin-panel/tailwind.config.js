/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#ef6b3e',
        secondary: '#2d3748',
        success: '#48bb78',
        warning: '#ed8936',
        danger: '#f56565',
      }
    },
  },
  plugins: [],
}
