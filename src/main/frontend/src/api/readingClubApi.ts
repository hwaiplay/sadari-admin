import { fetchJson } from './client'
import type { PageData } from '../types/common'
import type {
  ReadingClub,
  ReadingClubAction,
  ReadingClubActionRequest,
  ReadingClubSearch,
} from '../types/readingClub'

/** 독서 모임 검색 조건을 API 쿼리 문자열로 변환한다. */
const createSearchParams = (pageNumber: number, search: ReadingClubSearch): URLSearchParams => {
  // 모든 목록 요청에 페이지 번호를 포함한다.
  const params = new URLSearchParams({ page: String(pageNumber) })
  // 실제 값이 있는 검색 조건만 서버에 전달한다.
  Object.entries(search).forEach(([key, value]) => {
    // 공백뿐인 입력은 검색 조건에서 제외한다.
    if (value.trim()) {
      // 앞뒤 공백을 제거한 검색값을 쿼리에 설정한다.
      params.set(key, value.trim())
    }
  })
  // 완성된 검색 쿼리 문자열을 반환한다.
  return params
}

/** 관리자 독서 모임 목록을 검색한다. */
export const getReadingClubs = (
  pageNumber: number,
  search: ReadingClubSearch,
): Promise<PageData<ReadingClub>> => {
  // 검색 조건을 포함한 관리자 독서 모임 목록 API를 호출한다.
  return fetchJson<PageData<ReadingClub>>(
    `/api/reading-clubs?${createSearchParams(pageNumber, search).toString()}`,
    undefined,
    '독서 모임 목록을 불러오지 못했습니다.',
  )
}

/** 관리자용 독서 모임 상세를 조회한다. */
export const getReadingClub = (clubNumb: number): Promise<ReadingClub> => {
  // 모임 번호에 해당하는 운영 정보와 회원 작성 소개 조회값을 요청한다.
  return fetchJson<ReadingClub>(
    `/api/reading-clubs/${clubNumb}`,
    undefined,
    '독서 모임 상세를 불러오지 못했습니다.',
  )
}

/** 독서 모임 관리자 조치 이력을 조회한다. */
export const getReadingClubActions = (
  clubNumb: number,
  pageNumber: number,
): Promise<PageData<ReadingClubAction>> => {
  // 모임 번호와 페이지에 해당하는 감사 이력을 요청한다.
  return fetchJson<PageData<ReadingClubAction>>(
    `/api/reading-clubs/${clubNumb}/actions?page=${pageNumber}`,
    undefined,
    '독서 모임 조치 이력을 불러오지 못했습니다.',
  )
}

/** 독서 모임에 관리자 상태 조치를 적용한다. */
export const setReadingClubAction = (
  clubNumb: number,
  request: ReadingClubActionRequest,
): Promise<ReadingClub> => {
  // 조치 유형과 필수 근거를 상태 변경 API에 전달한다.
  return fetchJson<ReadingClub>(
    `/api/reading-clubs/${clubNumb}/actions`,
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(request),
    },
    '독서 모임 조치를 적용하지 못했습니다.',
  )
}
