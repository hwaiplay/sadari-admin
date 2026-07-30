import { useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { getMenuPermission } from '../api/menuPermissionApi'
import { emptyMenuPermission } from '../types/permission'
import type { MenuPermission } from '../types/permission'
import { MenuPermissionContext } from './menuPermissionContextValue'

type MenuPermissionProviderProps = {
  menuUrlx: string
  children: ReactNode
}

/** 현재 경로의 관리자 메뉴 권한 조회 */
function MenuPermissionLoader({ menuUrlx, children }: MenuPermissionProviderProps) {
  const [permission, setPermission] = useState<MenuPermission>(emptyMenuPermission)

  useEffect(() => {
    if (!menuUrlx || menuUrlx === '/sadari/adm') return
    let active = true
    getMenuPermission(menuUrlx)
      .then((loadedPermission) => {
        if (active) setPermission(loadedPermission)
      })
      .catch(() => {
        if (active) setPermission(emptyMenuPermission)
      })
    return () => {
      active = false
    }
  }, [menuUrlx])

  return <MenuPermissionContext.Provider value={permission}>{children}</MenuPermissionContext.Provider>
}

/** 현재 관리자 메뉴 권한 제공 */
export function MenuPermissionProvider({ menuUrlx, children }: MenuPermissionProviderProps) {
  return <MenuPermissionLoader menuUrlx={menuUrlx}>{children}</MenuPermissionLoader>
}
