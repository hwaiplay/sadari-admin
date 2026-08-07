import type { CurrentUser } from './currentUser'

export type ComplaintSearch = {
  cmplNumb: string
  cmplStat: string
  tagtType: string
  tagtNumb: string
  cmplRson: string
  reporterKeyword: string
  regiDateFrom: string
  regiDateTo: string
}

export type Complaint = {
  cmplNumb: number
  userNumb: number | null
  reporterNick: string | null
  reporterProfPath: string | null
  reporterBgimPath: string | null
  tagtType: string
  tagtTypeName: string | null
  tagtNumb: number
  targetUserNick: string | null
  cmplRson: string
  cmplRsonName: string | null
  cmplCntn: string | null
  cmplStat: string
  cmplStatName: string | null
  procCntn: string | null
  procAdmn: number | null
  procAdmnName: string | null
  procDate: string | null
  regiDate: string
  updtDate: string
}

export type ComplaintDetail = {
  complaint: Complaint
  targetUser: CurrentUser | null
  relatedComplaints: Complaint[]
  relatedComplaintCount: number
}

export type ComplaintUpdate = {
  cmplStat: string
  procCntn: string
  updtDate: string
}
