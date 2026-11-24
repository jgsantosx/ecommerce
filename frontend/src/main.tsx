import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
// Fonte servida pelo próprio app (pacote @fontsource), sem chamar o Google Fonts.
import '@fontsource-variable/inter'
import App from './App'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
