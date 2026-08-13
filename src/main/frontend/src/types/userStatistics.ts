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

export type UserConversionStatistics = {
  joinCntt: number
  onboardingCntt: number
  reportCntt: number
  socialCntt: number
}

export type UserActivityComposition = {
  visitCntt: number
  reportCntt: number
  communityCntt: number
  relationCntt: number
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
  conversion: UserConversionStatistics
  activityComposition: UserActivityComposition
  churnTrendList: UserChurnTrendStatistics[]
}

export type InactivityChartItem = {
  name: string
  userCntt: number
}

export type InsightChartItem = {
  name: string
  userCntt: number
  fill: string
}

export type RetentionChartItem = UserRetentionStatistics & {
  name: string
  fill: string
}
