/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        medical: {
          bg: '#070D1E',
          surface: '#0F1A34',
          card: '#152244',
          border: '#223565',
          cyan: '#00E5FF',
          blue: '#2563EB',
          indigo: '#6366F1',
          normal: '#10B981',
          stone: '#F59E0B',
          cyst: '#06B6D4',
          tumor: '#EF4444',
          disagree: '#8B5CF6'
        }
      }
    },
  },
  plugins: [],
}
