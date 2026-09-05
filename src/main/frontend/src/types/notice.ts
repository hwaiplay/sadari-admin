export type Notice = {
  notiNumb: number
  versNumb: number
  cateCgrp: string
  cateCode: string
  cateName: string
  notiTitl: string
  notiEntl: string
  notiCntn: string
  notiEnct: string
  topxYsno: 'Y' | 'N'
  dplyYsno: 'Y' | 'N'
  dplyDate: string | null
  dplyAdmn: number | null
  dplyAdmnName: string | null
  regiAdmn: number
  regiAdmnName: string | null
  regiDate: string
  updtAdmn: number | null
  updtAdmnName: string | null
  updtDate: string | null
}

export type NoticeForm = {
  cateCode: string
  notiTitl: string
  notiEntl: string
  notiCntn: string
  notiEnct: string
  topxYsno: 'Y' | 'N'
}
