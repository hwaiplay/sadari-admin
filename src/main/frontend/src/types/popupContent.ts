export type PopupContent = {
  popuSitu: string
  popuSituName?: string | null
  popuCode: string
  mngmTitl: string
  contFirs: string
  contSeco: string | null
  contThir: string | null
  contFour: string | null
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
  contSeco: string
  contThir: string
  contFour: string
}

export type PopupContentKey = {
  popuSitu: string
  popuCode: string
}
