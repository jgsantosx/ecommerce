import { useCallback, useEffect, useState } from 'react'

/**
 * Hook para carregar dados da API: devolve { data, error, loading, reload }.
 * Evita repetir o mesmo trio de useState/useEffect em cada página.
 */
export function useAsync<T>(load: () => Promise<T>, deps: unknown[] = []) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)

  // As dependências vêm de quem chama o hook (como no próprio useEffect).
  // oxlint-disable-next-line react/use-memo, react-hooks/exhaustive-deps
  const run = useCallback(load, deps)

  const reload = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setData(await run())
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Erro inesperado')
    } finally {
      setLoading(false)
    }
  }, [run])

  useEffect(() => {
    // oxlint-disable-next-line react/set-state-in-effect -- buscar dados da API é sincronizar com um sistema externo
    void reload()
  }, [reload])

  return { data, setData, error, loading, reload }
}

export function errorMessage(e: unknown): string {
  return e instanceof Error ? e.message : 'Erro inesperado'
}
