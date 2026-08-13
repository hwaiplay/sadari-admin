export type InquirySearch = {
  inqrNumb: string
  inqrCatg: string
  inqrStat: string
  userKeyword: string
}

export type InquiryAnswer = {
  answNumb: number
  answCntn: string
  readYsno: 'Y' | 'N'
  regiAdmn: number
  regiAdmnName: string
  regiDate: string
}

export type Inquiry = {
  inqrNumb: number
  userNumb: number | null
  userNick: string | null
  userStat: string | null
  inqrCatg: string
  inqrCatgName: string
  inqrTitl: string
  inqrCntn: string
  inqrStat: string
  inqrStatName: string
  spndNumb: number | null
  spndStat: string | null
  spndRsonName: string | null
  spndStrtDate: string | null
  spndEndxDate: string | null
  asgnAdmn: number | null
  asgnAdmnName: string | null
  answDate: string | null
  regiDate: string
  updtDate: string
  answers: InquiryAnswer[]
}
