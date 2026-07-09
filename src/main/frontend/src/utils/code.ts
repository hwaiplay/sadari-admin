import type { Code } from '../types/code'

export const getUseeYsnoCodeName = (codes: Code[], useeYsno: string | null | undefined, useeYsnoName?: string | null) =>
  useeYsnoName ?? codes.find((code) => code.comdCode === useeYsno)?.opt1Name ?? codes.find((code) => code.comdCode === useeYsno)?.comdName ?? useeYsno ?? ''

export const formatDate = (value: string | null) => {
  if (!value) return ''
  return value.replace('T', ' ').slice(0, 19)
}

