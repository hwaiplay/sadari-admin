export type MenuPermission = {
  readYsno: string
  writYsno: string
  deltYsno: string
}

export const emptyMenuPermission: MenuPermission = {
  readYsno: 'N',
  writYsno: 'N',
  deltYsno: 'N',
}
