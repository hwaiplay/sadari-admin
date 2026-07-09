export type ApiResult<T> = {
  code: number
  message: string
  data: T
}

export type Permission = {
  readYn: boolean
  writYn: boolean
  deltYn: boolean
}

export const emptyPermission: Permission = {
  readYn: false,
  writYn: false,
  deltYn: false,
}

