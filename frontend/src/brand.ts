import { useEffect } from 'react'

/**
 * ============================================================
 *  PERSONALIZAÇÃO DA LOJA
 *  Troque os textos abaixo para adaptar a loja a cada cliente.
 *  As cores ficam no topo de src/index.css (variáveis --brand*).
 *  O ícone da aba fica em public/favicon.svg.
 * ============================================================
 */
export const store = {
  name: 'Darc',
  fullName: 'Darc E-commerce',
  tagline: 'E-commerce',

  heroTitle: 'Tudo o que você procura, em um só lugar.',
  heroSubtitle: 'Escolha, pague como preferir e acompanhe seu pedido do pagamento à entrega.',

  /** Destaques exibidos abaixo do banner. Mantenha só o que a loja realmente oferece. */
  highlights: [
    { icon: 'payment', title: 'Pix, cartão ou boleto', text: 'Escolha a forma de pagamento no fechamento.' },
    { icon: 'tracking', title: 'Acompanhe seu pedido', text: 'Veja cada etapa, do pagamento à entrega.' },
    { icon: 'cancel', title: 'Cancelamento simples', text: 'Cancele antes do pagamento, sem burocracia.' },
  ] as const,

  /** Aviso no topo do site. Deixe vazio ('') para esconder. */
  notice: 'Ambiente de demonstração — os pagamentos são simulados.',
}

/** Atualiza o título da aba: "Carrinho — Darc E-commerce". */
export function usePageTitle(title?: string) {
  useEffect(() => {
    document.title = title ? `${title} — ${store.fullName}` : store.fullName
  }, [title])
}
