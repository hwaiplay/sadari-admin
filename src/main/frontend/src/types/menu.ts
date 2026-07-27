export type Menu = {
  menuNumb: string
  subxNumb: string
  menuName: string
  menuUrlx: string
  sortOrdr: number | null
  useeYsno: string | null
  useeYsnoName?: string | null
  regiAdmn: number | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: number | null
  updtAdmnName?: string | null
  updtDate: string | null
}

export type MenuForm = {
  menuNumb: string
  subxNumb: string
  menuName: string
  menuUrlx: string
  sortOrdr: string
  useeYsno: string
}
