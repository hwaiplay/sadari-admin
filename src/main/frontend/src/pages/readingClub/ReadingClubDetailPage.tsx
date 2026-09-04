import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  getReadingClub,
  getReadingClubActions,
  setReadingClubAction,
} from '../../api/readingClubApi'
import { Pagination } from '../../components/Pagination'
import { READING_CLUB_LIST_PATH } from '../../constants/routes'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import type { PageData } from '../../types/common'
import type {
  ReadingClub,
  ReadingClubAction,
  ReadingClubActionRequest,
} from '../../types/readingClub'
import { formatDate } from '../../utils/code'

type ReadingClubDetailPageProps = {
  clubNumb: number
  onMovePath: (path: string) => void
  onError: (message: string | null) => void
}

const EMPTY_ACTION_PAGE: PageData<ReadingClubAction> = {
  items: [],
  totalCount: 0,
  pageNumber: 1,
  pageSize: 20,
  totalPages: 0,
}

/**
 * 관리자 독서 모임 상세와 상태 조치 및 감사 이력을 제공한다.
 *
 * @author HanWon.Jang
 * @param clubNumb 조회하고 조치할 모임 번호
 * @param onMovePath 화면 경로 이동 함수
 * @param onError 공통 오류 메시지 변경 함수
 * @return 독서 모임 관리 상세 화면
 */
export function ReadingClubDetailPage({ clubNumb, onMovePath, onError }: ReadingClubDetailPageProps) {
  const permission = useMenuPermission()
  const [club, setClub] = useState<ReadingClub | null>(null)
  const [actionPage, setActionPage] = useState<PageData<ReadingClubAction>>(EMPTY_ACTION_PAGE)
  const [actnType, setActnType] = useState<ReadingClubActionRequest['actnType']>('RECRUIT_STOP')
  const [actnRson, setActnRson] = useState('')
  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)

  /** 지정한 페이지의 관리자 조치 이력을 조회한다. */
  const loadActions = async (pageNumber: number): Promise<void> => {
    // 모임 번호와 페이지에 해당하는 감사 이력을 조회한다.
    const result = await getReadingClubActions(clubNumb, pageNumber)
    // 조회된 감사 이력 페이지를 화면에 설정한다.
    setActionPage(result)
  }

  // 첫 진입 시 모임 상세와 첫 감사 이력 페이지를 함께 조회한다.
  useEffect(() => {
    let active = true
    // 모임 운영 정보와 감사 이력을 병렬로 조회한다.
    Promise.all([getReadingClub(clubNumb), getReadingClubActions(clubNumb, 1)])
      .then(([clubResult, actionResult]) => {
        // 화면이 유지되는 동안에만 상세와 감사 이력을 반영한다.
        if (active) {
          // 관리자용 모임 상세를 화면에 설정한다.
          setClub(clubResult)
          // 첫 관리자 조치 이력 페이지를 화면에 설정한다.
          setActionPage(actionResult)
          // 이전 상세 조회 오류를 제거한다.
          onError(null)
          // 상세 로딩을 종료한다.
          setLoading(false)
        }
      })
      .catch((error: unknown) => {
        // 화면이 유지되는 동안에만 상세 조회 오류를 표시한다.
        if (active) {
          // "독서 모임 상세를 불러오지 못했습니다."
          onError(error instanceof Error ? error.message : '독서 모임 상세를 불러오지 못했습니다.')
          // 오류 상태에서도 상세 로딩을 종료한다.
          setLoading(false)
        }
      })
    // 화면 해제 뒤 도착하는 응답이 상태를 변경하지 않도록 차단한다.
    return () => {
      active = false
    }
  }, [clubNumb, onError])

  /** 선택한 관리자 조치를 필수 근거와 함께 적용한다. */
  const handleAction = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
    // 브라우저 기본 폼 전송을 막는다.
    event.preventDefault()
    // 공백 사유는 서버 요청 전에 관리자에게 입력을 안내한다.
    if (!actnRson.trim()) {
      // "조치 사유를 입력해 주세요."
      onError('조치 사유를 입력해 주세요.')
      // 조치 근거가 입력될 때까지 상태 변경을 중단한다.
      return
    }

    // 선택한 상태 조치의 비가역 가능성을 관리자에게 다시 확인한다.
    if (!window.confirm('선택한 독서 모임 조치를 적용하시겠습니까?')) {
      // 확인하지 않은 상태 변경 요청은 전송하지 않는다.
      return
    }

    // 중복 제출을 막기 위해 조치 처리 상태를 시작한다.
    setSubmitting(true)
    // 상태 조치 실패 시 최신 화면을 유지하고 공통 오류를 표시한다.
    try {
      // 선택한 조치 유형과 근거를 서버에 전달한다.
      const updatedClub = await setReadingClubAction(clubNumb, {
        actnType,
        actnRson: actnRson.trim(),
      })
      // 상태 변경이 반영된 최신 모임 상세를 화면에 설정한다.
      setClub(updatedClub)
      // 저장된 관리자 조치 사유 입력값을 초기화한다.
      setActnRson('')
      // 최신 조치가 첫 행에 표시되도록 감사 이력 첫 페이지를 다시 조회한다.
      await loadActions(1)
      // 이전 조치 오류를 제거한다.
      onError(null)
    } catch (error: unknown) {
      // "독서 모임 조치를 적용하지 못했습니다."
      onError(error instanceof Error ? error.message : '독서 모임 조치를 적용하지 못했습니다.')
    } finally {
      // 성공 여부와 관계없이 조치 처리 상태를 종료한다.
      setSubmitting(false)
    }
  }

  // 상세 조회 중에는 관리자에게 로딩 상태를 반환한다.
  if (loading) {
    // 독서 모임 상세 로딩 안내를 반환한다.
    return <section className="complaint-page"><p>독서 모임 상세를 불러오고 있습니다.</p></section>
  }

  // 상세가 없으면 목록으로 돌아갈 수 있는 안내를 반환한다.
  if (!club) {
    // 조회할 수 없는 독서 모임 안내를 반환한다.
    return (
      <section className="complaint-page">
        <p>독서 모임 정보를 확인할 수 없습니다.</p>
        <button type="button" className="subtle-button" onClick={() => onMovePath(READING_CLUB_LIST_PATH)}>목록</button>
      </section>
    )
  }

  // 독서 모임 운영 상세와 관리자 조치 및 감사 이력을 반환한다.
  return (
    <section className="complaint-page">
      <section className="content-header">
        <div>
          <h1>독서 모임 상세</h1>
          <div className="status">모임번호 {club.clubNumb}</div>
        </div>
        <button type="button" className="subtle-button" onClick={() => onMovePath(READING_CLUB_LIST_PATH)}>목록</button>
      </section>

      <section className="table-wrap">
        <table>
          <tbody>
            <tr><th>모임명</th><td>{club.clubName}</td><th>모임장</th><td>{club.ownrNumb ? `${club.ownrNick ?? '닉네임 없음'} (${club.ownrNumb})` : '모임장 없음'}</td></tr>
            <tr><th>상태</th><td>{club.clubStatName ?? club.clubStat}</td><th>모집</th><td>{club.rcrtYsno === 'Y' ? '모집 중' : '모집 중지'}</td></tr>
            <tr><th>공개 범위</th><td>{club.clubVisbName ?? club.clubVisb}</td><th>가입 방식</th><td>{club.joinTypeName ?? club.joinType}</td></tr>
            <tr><th>카테고리</th><td>{club.categoryNames ?? '-'}</td><th>인원</th><td>{club.memberCnt}/{club.maxxMemb} (예약 {club.invitedCnt})</td></tr>
            <tr><th>생성일시</th><td>{formatDate(club.regiDate)}</td><th>수정일시</th><td>{formatDate(club.updtDate)}</td></tr>
            <tr><th>종료일시</th><td colSpan={3}>{formatDate(club.closDate) || '-'}</td></tr>
            <tr><th>회원 작성 소개</th><td colSpan={3} className="readonly-cell">{club.clubCntn || '-'}</td></tr>
          </tbody>
        </table>
      </section>

      {permission.writYsno === 'Y' && (
        <form className="complaint-search" onSubmit={(event) => void handleAction(event)}>
          <label>
            <span>관리자 조치</span>
            <select value={actnType} onChange={(event) => setActnType(event.target.value as ReadingClubActionRequest['actnType'])}>
              <option value="RECRUIT_STOP">모집 중지</option>
              <option value="SUSPEND">이용 정지</option>
              <option value="RESTORE">해제</option>
              <option value="CLOSE">종료</option>
            </select>
          </label>
          <label>
            <span>조치 사유</span>
            <textarea
              value={actnRson}
              maxLength={666}
              placeholder="감사 이력에 남길 운영 판단 근거를 입력해 주세요."
              onChange={(event) => setActnRson(event.target.value)}
            />
          </label>
          <div className="complaint-search-actions">
            <button type="submit" disabled={submitting}>{submitting ? '처리 중' : '조치 적용'}</button>
          </div>
        </form>
      )}

      <section className="content-header">
        <h2>관리자 조치 이력</h2>
        <div className="status">총 {actionPage.totalCount.toLocaleString()}건</div>
      </section>
      <section className="table-wrap complaint-list-table">
        <table>
          <thead>
            <tr><th>번호</th><th>조치</th><th>처리 전</th><th>처리 후</th><th>사유</th><th>관리자</th><th>처리일시</th></tr>
          </thead>
          <tbody>
            {actionPage.items.length === 0 ? (
              <tr className="empty-row"><td colSpan={7}>관리자 조치 이력이 없습니다.</td></tr>
            ) : actionPage.items.map((action) => (
              <tr key={action.histNumb}>
                <td>{action.histNumb}</td>
                <td>{action.actnTypeName ?? action.actnType}</td>
                <td>{action.befrStatName ?? action.befrStat ?? '-'}</td>
                <td>{action.aftrStatName ?? action.aftrStat}</td>
                <td>{action.actnRson}</td>
                <td>{action.admnName ?? action.admnNumb}</td>
                <td className="col-date-time">{formatDate(action.regiDate)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <Pagination
        pageNumber={actionPage.pageNumber}
        totalPages={actionPage.totalPages}
        onPageChange={(pageNumber) => void loadActions(pageNumber).catch((error: unknown) => {
          // "독서 모임 조치 이력을 불러오지 못했습니다."
          onError(error instanceof Error ? error.message : '독서 모임 조치 이력을 불러오지 못했습니다.')
        })}
      />
    </section>
  )
}
