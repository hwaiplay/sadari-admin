import { createContext } from 'react'
import { emptyMenuPermission } from '../types/permission'
import type { MenuPermission } from '../types/permission'

export const MenuPermissionContext = createContext<MenuPermission>(emptyMenuPermission)
