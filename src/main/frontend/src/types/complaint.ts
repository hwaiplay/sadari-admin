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
  tagtType: string
  tagtTypeName: string | null
  tagtNumb: number
  tagtHash: string
  tagtUser: number | null
  tagtCntn: string | null
  evidenceAvailable: boolean
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

export type ComplaintAction = {
  actnNumb: number
  tagtType: string
  tagtNumb: number
  tagtHash: string
  tagtUser: number | null
  actnType: string
  actnTypeName: string | null
  rsltCode: string
  rsltCodeName: string | null
  thrsCntt: number
  cmplCntt: number
  actnOrdr: number
  trigCmpl: number | null
  rsltCntn: string | null
  regiDate: string
}

export type ComplaintAutoAction = {
  autoActionTarget: boolean
  progressStatus: 'PENDING' | 'AUTO_ACTIONED' | 'MANUAL_ACTIONED' | 'VERSION_CHANGED' | 'TARGET_MISSING' | null
  actnType: string | null
  actnTypeName: string | null
  threshold: number
  complaintCount: number
  nextActionCount: number
  remainingCount: number
  actionHistories: ComplaintAction[]
}

export type ComplaintDetail = {
  complaint: Complaint
  targetUser: CurrentUser | null
  targetContentExists: boolean
  autoAction: ComplaintAutoAction
  relatedComplaints: Complaint[]
  relatedComplaintCount: number
}

export type ComplaintUpdate = {
  cmplStat: string
  procCntn: string
  updtDate: string
}
