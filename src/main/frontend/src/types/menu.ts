export type Menu = {
  menuNumb: string
  subxNumb: string
  menuName: string
  menuUrlx: string
  readAuth: string | null
  writAuth: string | null
  deltAuth: string | null
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
  readAuth: string
  writAuth: string
  deltAuth: string
  sortOrdr: string
  useeYsno: string
}
