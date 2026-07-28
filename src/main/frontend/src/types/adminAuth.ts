export type AdminAuth = {
  admnNumb: number
  admnIdxx: string
  admnName: string
  authCode: string
  authName: string | null
  deptCode: string | null
}

export type AdminAuthGroup = {
  authCode: string
  authName: string
  useeYsno: string
}

export type AdminAuthManage = {
  admins: PageData<AdminAuth>
  authGroups: AdminAuthGroup[]
}
import type { PageData } from './common'
