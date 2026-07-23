export type Code = {
  commCode: string
  comdCode: string
  comdName: string
  codeExpl: string | null
  opt1Code?: string | null
  opt1Name?: string | null
  opt2Code?: string | null
  opt2Name?: string | null
  opt3Code?: string | null
  opt3Name?: string | null
  opt4Code?: string | null
  opt4Name?: string | null
  useeYsno: string | null
  useeYsnoName?: string | null
  regiAdmn: string | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: string | null
  updtAdmnName?: string | null
  updtDate: string | null
  sortOrdr: number | null
}

export type CodeMaster = {
  commCode: string
  codeName: string
  codeExpl: string | null
  useeYsno: string | null
  useeYsnoName?: string | null
  regiAdmn: string | null
  regiAdmnName?: string | null
  regiDate: string | null
  updtAdmn: string | null
  updtAdmnName?: string | null
  updtDate: string | null
}

export type DetailCodeForm = {
  comdCode: string
  comdName: string
  codeExpl: string
  opt1Code: string
  opt1Name: string
  opt2Code: string
  opt2Name: string
  opt3Code: string
  opt3Name: string
  opt4Code: string
  opt4Name: string
  sortOrdr: string
  useeYsno: string
}

export type DetailCodePayload = {
  comdCode: string
  comdName: string
  codeExpl: string
  opt1Code: string
  opt1Name: string
  opt2Code: string
  opt2Name: string
  opt3Code: string
  opt3Name: string
  opt4Code: string
  opt4Name: string
  sortOrdr: number
  useeYsno: string
}
