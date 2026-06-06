import { useAuthStore } from '../stores/auth'

/**
 * Composable for permission checks in templates and scripts.
 */
export function usePermission() {
  const auth = useAuthStore()

  function can(code: string): boolean {
    return auth.hasPermission(code)
  }

  function canAny(...codes: string[]): boolean {
    return auth.hasAnyPermission(...codes)
  }

  function canAll(...codes: string[]): boolean {
    return auth.hasAllPermissions(...codes)
  }

  return { can, canAny, canAll }
}