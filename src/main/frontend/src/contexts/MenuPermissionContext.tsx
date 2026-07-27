import { createContext, useContext, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { getMenuPermission } from '../api/menuPermissionApi'
import { emptyMenuPermission } from '../types/permission'
import type { MenuPermission } from '../types/permission'

const MenuPermissionContext = createContext<MenuPermission>(emptyMenuPermission)

type MenuPermissionProviderProps = {
  menuUrlx: string
  children: ReactNode
}

/** 현재 관리자 메뉴 권한 제공 */
export function MenuPermissionProvider({ menuUrlx, children }: MenuPermissionProviderProps) {
  const [permission, setPermission] = useState<MenuPermission>(emptyMenuPermission)

  useEffect(() => {
    setPermission(emptyMenuPermission)
    if (!menuUrlx || menuUrlx === '/sadari/adm') return
    getMenuPermission(menuUrlx)
      .then(setPermission)
      .catch(() => setPermission(emptyMenuPermission))
  }, [menuUrlx])

  return <MenuPermissionContext.Provider value={permission}>{children}</MenuPermissionContext.Provider>
}

/** 현재 관리자 메뉴 권한 사용 */
export const useMenuPermission = () => useContext(MenuPermissionContext)
