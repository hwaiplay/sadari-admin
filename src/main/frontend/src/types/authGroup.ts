export type AuthMenu = {
  authCode: string
  menuNumb: string
  subxNumb: string
  menuName: string
  menuUrlx: string
  sortOrdr: number | null
  readYsno: string
  writYsno: string
  deltYsno: string
}

export type AuthGroup = {
  authCode: string
  authName: string
  useeYsno: string
  useeYsnoName?: string | null
  regiAdmn: number | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: number | null
  updtAdmnName?: string | null
  updtDate: string | null
  menus: AuthMenu[]
}
export type AuthGroupSearch = {
  keyword: string
  useeYsno: string
}
