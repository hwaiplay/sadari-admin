export type ServiceInfo = {
  cateCgrp: string
  cateCode: string
  cateName: string
  versNumb: number
  svciTitl: string
  svciEntl: string
  svciCntn: string
  svciEnct: string
  dplyYsno: 'Y' | 'N'
  regiAdmn: number
  regiAdmnName: string | null
  regiDate: string
  updtAdmn: number | null
  updtAdmnName: string | null
  updtDate: string | null
  dplyAdmn: number | null
  dplyAdmnName: string | null
  dplyDate: string | null
}

export type ServiceInfoForm = {
  cateCode: string
  svciTitl: string
  svciEntl: string
  svciCntn: string
  svciEnct: string
}
