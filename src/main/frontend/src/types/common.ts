export type ApiResult<T> = {
  code: number
  message: string
  data: T
}

export type PageData<T> = {
  items: T[]
  totalCount: number
  pageNumber: number
  pageSize: number
  totalPages: number
}
