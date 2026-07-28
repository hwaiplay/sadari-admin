export type ScheduleLog = {
  runxNumb: number
  schdCode: string
  methName: string
  execStat: string
  strtDate: string
  fnshDate: string | null
  trgtCntt: number
  succCntt: number
  failCntt: number
  execMsec: number | null
}

export type ScheduleFail = {
  runxNumb: number
  failNumb: number
  failType: string
  rsltCode: number | null
  rsltMesg: string | null
  erroType: string | null
  erroCntn: string | null
  failDate: string
}
