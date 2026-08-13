export type CurrentUserSearch = {
  keyword: string
  userStat: string
  userProv: string
  onbdYsno: string
  joinDateFrom: string
  joinDateTo: string
}

export type CurrentUser = {
  userNumb: number
  userNick: string
  userProv: string | null
  userRole: string | null
  userStat: string
  userStatName: string | null
  userProvName: string | null
  userStatusSyncStat: 'PENDING' | 'COMPLETED'
  onbdYsno: string
  onbdYsnoName: string | null
  intrCntn: string | null
  profPath: string | null
  bgimPath: string | null
  joinDate: string | null
  wthdDate: string | null
  deltDate: string | null
  lastLognDate: string | null
  reportCntt: number
  replyCntt: number
  likeCntt: number
  followingCntt: number
  followerCntt: number
  goalCntt: number
  pushCntt: number
}

export type CurrentUserLoginHistory = {
  lognNumb: number
  lognDate: string | null
  lognIpxx: string | null
  userAgnt: string | null
  provCode: string | null
  provCodeName: string | null
}

export type CurrentUserWithdrawalHistory = {
  wthdNumb: number
  wthdType: string
  wthdTypeName: string | null
  wthdRson: string | null
  wthdRsonName: string | null
  wthdStat: string
  wthdStatName: string | null
  requDate: string | null
  deltDate: string | null
  procDate: string | null
  rcovDate: string | null
}

export type CurrentUserSuspension = {
  spndNumb: number
  userNumb: number
  prevStat: string
  spndType: string
  spndTypeName: string | null
  spndRson: string
  spndRsonName: string | null
  spndCntn: string | null
  spndStat: string
  spndStatName: string | null
  strtDate: string | null
  endxDate: string | null
  rlesDate: string | null
  rlesCntn: string | null
  regiAdmn: number
  regiAdmnName: string | null
  rlesAdmn: number | null
  rlesAdmnName: string | null
  regiDate: string | null
  updtAdmn: number | null
  updtDate: string | null
}

export type CurrentUserSuspensionRequest = {
  spndType: string
  spndRson: string
  spndCntn: string
  endxDate: string | null
}

export type DeletedSuspensionSearch = {
  userNumb: string
}
