export type UserMenu = {
  menuNumb: string
  subxNumb: string
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
  subxNumb: string
  menuName: string
  menuUrlx: string
  sortOrdr: string
  showYsno: string
  useeYsno: string
}
