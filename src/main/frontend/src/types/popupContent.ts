export type PopupContent = {
  popuSitu: string
  popuSituName?: string | null
  popuCode: string
  mngmTitl: string
  contFirs: string
  englFirs: string
  contSeco: string | null
  englSeco: string | null
  contThir: string | null
  englThir: string | null
  contFour: string | null
  englFour: string | null
  regiAdmn: string | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: string | null
  updtAdmnName?: string | null
  updtDate: string | null
}

export type PopupContentForm = {
  popuSitu: string
  popuCode: string
  mngmTitl: string
  contFirs: string
  englFirs: string
  contSeco: string
  englSeco: string
  contThir: string
  englThir: string
  contFour: string
  englFour: string
}

export type PopupContentKey = {
  popuSitu: string
  popuCode: string
}
export type PopupContentSearch = {
  keyword: string
  popuSitu: string
}
