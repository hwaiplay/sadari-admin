export type WelcomePage = {
  wlcmNumb: number
  versNumb: number
  subxTitl: string
  subxEntl: string
  mainTitl: string
  mainEntl: string
  pageDesc: string
  pageEnct: string
  imgeUrlx: string | null
  imgeEnur: string | null
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
  subxEntl: string
  mainTitl: string
  mainEntl: string
  pageDesc: string
  pageEnct: string
  imgeUrlx: string | null
  imgeEnur: string | null
  sortOrdr: number
}
