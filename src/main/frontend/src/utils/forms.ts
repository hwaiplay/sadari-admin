import { DEFAULT_AUTH_CODE, DEFAULT_USEE_YSNO } from '../constants/codes'
import type { DetailCodeForm } from '../types/code'
import type { Menu, MenuForm } from '../types/menu'

export const emptyMenuForm = (authCode = DEFAULT_AUTH_CODE, parentMenuNumb = ''): MenuForm => ({
  menuNumb: parentMenuNumb,
  subxNumb: '',
  menuName: '',
  menuUrlx: '/sadari/adm/',
  readAuth: authCode,
  writAuth: authCode,
  deltAuth: authCode,
  sortOrdr: '10',
  useeYsno: DEFAULT_USEE_YSNO,
})

export const emptyDetailForm = (): DetailCodeForm => ({
  comdCode: '',
  comdName: '',
  codeExpl: '',
  opt1Code: '',
  opt1Name: '',
  opt2Code: '',
  opt2Name: '',
  opt3Code: '',
  opt3Name: '',
  opt4Code: '',
  opt4Name: '',
  sortOrder: '10',
  useeYsno: DEFAULT_USEE_YSNO,
})

export const toDetailCodeForm = (code: {
  comdCode: string
  comdName: string
  codeExpl: string | null
  opt1Code?: string | null
  opt1Name?: string | null
  opt2Code?: string | null
  opt2Name?: string | null
  opt3Code?: string | null
  opt3Name?: string | null
  opt4Code?: string | null
  opt4Name?: string | null
  sortOrder: number | null
  useeYsno: string | null
}): DetailCodeForm => ({
  comdCode: code.comdCode,
  comdName: code.comdName,
  codeExpl: code.codeExpl ?? '',
  opt1Code: code.opt1Code ?? '',
  opt1Name: code.opt1Name ?? '',
  opt2Code: code.opt2Code ?? '',
  opt2Name: code.opt2Name ?? '',
  opt3Code: code.opt3Code ?? '',
  opt3Name: code.opt3Name ?? '',
  opt4Code: code.opt4Code ?? '',
  opt4Name: code.opt4Name ?? '',
  sortOrder: String(code.sortOrder ?? 10),
  useeYsno: code.useeYsno ?? DEFAULT_USEE_YSNO,
})

export const toMenuForm = (menu: Menu): MenuForm => ({
  menuNumb: menu.menuNumb,
  subxNumb: menu.subxNumb,
  menuName: menu.menuName,
  menuUrlx: menu.menuUrlx,
  readAuth: menu.readAuth ?? DEFAULT_AUTH_CODE,
  writAuth: menu.writAuth ?? menu.readAuth ?? DEFAULT_AUTH_CODE,
  deltAuth: menu.deltAuth ?? menu.readAuth ?? DEFAULT_AUTH_CODE,
  sortOrdr: String(menu.sortOrdr ?? 10),
  useeYsno: menu.useeYsno ?? DEFAULT_USEE_YSNO,
})
