export type UserStatusStatistics = {
  userStat: string
  userStatName: string
  userCntt: number
}

export type UserTrendStatistics = {
  statDate: string
  joinCntt: number
  reportCntt: number
  replyCntt: number
  likeCntt: number
  followCntt: number
  dauCntt: number
  wauCntt: number
  mauCntt: number
}

export type UserInactivityStatistics = {
  inactive30Cntt: number
  inactive90Cntt: number
}

export type UserRetentionStatistics = {
  periodDays: number
  cohortCntt: number
  retainedCntt: number
  retentionRate: number
}

export type UserChurnTrendStatistics = {
  statDate: string
  withdrawnCntt: number
  deleteCntt: number
  suspendedCntt: number
  restoredCntt: number
}

export type UserStatistics = {
  startDate: string
  endDate: string
  generatedAt: string
  statusList: UserStatusStatistics[]
  trendList: UserTrendStatistics[]
  inactivity: UserInactivityStatistics
  retentionList: UserRetentionStatistics[]
  churnTrendList: UserChurnTrendStatistics[]
}

export type InactivityChartItem = {
  name: string
  userCntt: number
}

export type RetentionChartItem = UserRetentionStatistics & {
  name: string
  fill: string
}
