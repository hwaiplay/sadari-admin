export type WelcomePage = {
  wlcmNumb: number
  versNumb: number
  subxTitl: string
  mainTitl: string
  pageDesc: string
  imgeUrlx: string | null
  sortOrdr: number
  dplyYsno: 'Y' | 'N'
  regiAdmn: number
  regiAdmnName: string | null
  regiDate: string
  updtAdmn: number | null
  updtAdmnName: string | null
  updtDate: string | null
  dplyAdmn: number | null
  dplyAdmnName: string | null
  dplyDate: string | null
}

export type WelcomePageForm = {
  subxTitl: string
  mainTitl: string
  pageDesc: string
  imgeUrlx: string | null
  sortOrdr: number
}
