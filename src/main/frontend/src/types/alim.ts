export type AlimTemp = {
  alimSitu: string
  alimSituName?: string | null
  tempCode: string
  tempTitl: string
  alimTitl: string | null
  tempCont: string
  linkUrlx: string
  useeYsno: string | null
  useeYsnoName?: string | null
  regiAdmn: number | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: number | null
  updtAdmnName?: string | null
  updtDate: string | null
}

export type AlimTempForm = {
  alimSitu: string
  tempCode: string
  tempTitl: string
  alimTitl: string
  tempCont: string
  linkUrlx: string
  useeYsno: string
}
