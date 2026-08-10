export type UserMenu = {
  menuNumb: number
  parnNumb: number | null
  parnName?: string | null
  menuLevl: number
  menuName: string
  menuUrlx: string
  sortOrdr: number | null
  showYsno: string | null
  showYsnoName?: string | null
  useeYsno: string | null
  useeYsnoName?: string | null
  regiAdmn: number | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: number | null
  updtAdmnName?: string | null
  updtDate: string | null
}

export type UserMenuForm = {
  menuNumb: string
  parnNumb: string
  menuName: string
  menuUrlx: string
  sortOrdr: string
  showYsno: string
  useeYsno: string
}
export type UserMenuSearch = {
  keyword: string
  showYsno: string
  useeYsno: string
  menuLevl: string
}
