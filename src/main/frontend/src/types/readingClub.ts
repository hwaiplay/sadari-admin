export type ReadingClubSearch = {
  keyword: string
  clubStat: string
  clubVisb: string
  joinType: string
  rcrtYsno: string
  regiDateFrom: string
  regiDateTo: string
}

export type ReadingClub = {
  clubNumb: number
  ownrNumb: number | null
  ownrNick: string | null
  clubName: string
  clubCntn: string | null
  clubVisb: string
  clubVisbName: string | null
  joinType: string
  joinTypeName: string | null
  clubStat: string
  clubStatName: string | null
  rcrtYsno: 'Y' | 'N'
  maxxMemb: number
  memberCnt: number
  invitedCnt: number
  categoryNames: string | null
  regiDate: string | null
  updtDate: string | null
  closDate: string | null
}

export type ReadingClubAction = {
  clubNumb: number
  histNumb: number
  admnNumb: number
  admnName: string | null
  actnType: string
  actnTypeName: string | null
  befrStat: string | null
  befrStatName: string | null
  aftrStat: string
  aftrStatName: string | null
  actnRson: string
  regiDate: string | null
}

export type ReadingClubActionRequest = {
  actnType: 'RECRUIT_STOP' | 'SUSPEND' | 'RESTORE' | 'CLOSE'
  actnRson: string
}
