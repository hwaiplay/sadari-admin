import { useEffect, useState } from 'react'
import type { MouseEvent, ReactElement } from 'react'
import {
  Bar,
  BarChart,
  CartesianGrid,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { getUserStatistics } from '../../api/userStatisticsApi'
import type { UserStatisticsDays } from '../../api/userStatisticsApi'
import type { InactivityChartItem, UserStatistics } from '../../types/userStatistics'
import { UserInsightCharts } from './UserInsightCharts'
import './UserStatisticsPage.css'

type UserStatisticsPageProps = {
  onError: (message: string | null) => void
}

type StatisticsPeriod = {
  days: UserStatisticsDays
  label: string
}

const CHART_MARGIN = { top: 12, right: 22, bottom: 4, left: 0 }
const AXIS_TICK = { fill: '#8a94a6', fontSize: 11 }
const TOOLTIP_STYLE = {
  border: '1px solid #e6eaf2',
  borderRadius: 12,
  boxShadow: '0 12px 30px rgba(35, 45, 70, 0.12)',
  color: '#273044',
  fontSize: 12,
}
const STATISTICS_PERIOD_LIST: ReadonlyArray<StatisticsPeriod> = [
  { days: 30, label: '1개월' },
  { days: 90, label: '3개월' },
  { days: 180, label: '6개월' },
  { days: 365, label: '1년' },
]

/**
 * 통계 날짜 축을 월과 일 형식으로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param value 서버가 반환한 통계 기준 일자
 * @return 월과 일만 표시한 축 문구
 */
const formatDateTick = (value: string): string => {
  // 연도를 제외한 월과 일만 차트 축에 반환한다.
  return value.slice(5).replace('-', '.')
}

/**
 * 큰 회원 수를 축 너비에 맞는 단위로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param value 차트 축에 표시할 회원 또는 활동 수
 * @return 한글 단위를 적용한 축 문구
 */
const formatCountTick = (value: number): string => {
  // 만 단위 이상은 짧은 한글 단위로 표시한다.
  if (value >= 10000) {
    // 만 단위로 축약한 회원 또는 활동 수를 반환한다.
    return `${Math.round(value / 1000) / 10}만`
  }

  // 천 단위 이상은 짧은 한글 단위로 표시한다.
  if (value >= 1000) {
    // 천 단위로 축약한 회원 또는 활동 수를 반환한다.
    return `${Math.round(value / 100) / 10}천`
  }

  // 작은 값은 원래 숫자로 표시한다.
  return String(value)
}

/**
 * 상태별 현재 회원 수의 합계를 계산한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 현재 보관 중인 전체 회원 수
 */
const getTotalUserCount = (statistics: UserStatistics): number => {
  let totalCount = 0
  // 공통코드에 정의된 모든 상태의 현재 회원 수를 더한다.
  for (const status of statistics.statusList) totalCount += status.userCntt
  // 계산된 전체 회원 수를 반환한다.
  return totalCount
}

/**
 * 선택 기간에 가입한 전체 회원 수를 계산한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 선택 기간의 신규 가입자 수
 */
const getJoinTotal = (statistics: UserStatistics): number => {
  let totalCount = 0
  // 날짜별 신규 가입 수를 선택 기간 전체로 합산한다.
  for (const trend of statistics.trendList) totalCount += trend.joinCntt
  // 기간 카드에 표시할 신규 가입자 합계를 반환한다.
  return totalCount
}

/**
 * 선택 기간의 독후감과 댓글 및 좋아요와 팔로우 수를 계산한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 선택 기간의 전체 활동 수
 */
const getActivityTotal = (statistics: UserStatistics): number => {
  let totalCount = 0
  // 네 종류의 날짜별 활동 수를 하나의 기간 합계로 계산한다.
  for (const trend of statistics.trendList) {
    totalCount += trend.reportCntt + trend.replyCntt + trend.likeCntt + trend.followCntt
  }

  // 활동 추세 카드에 표시할 기간 합계를 반환한다.
  return totalCount
}

/**
 * 미접속 회원 수를 서로 겹치지 않는 두 구간의 차트 데이터로 변환한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 30일부터 89일과 90일 이상 구간 데이터
 */
const createInactivityData = (statistics: UserStatistics): InactivityChartItem[] => {
  // 두 미접속 구간의 이름과 회원 수를 차트 배열로 반환한다.
  return [
    { name: '30~89일', userCntt: statistics.inactivity.inactive30Cntt },
    { name: '90일 이상', userCntt: statistics.inactivity.inactive90Cntt },
  ]
}

/**
 * 관리자 사용자 통계를 3행 2열 차트 대시보드로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param onError 공통 오류 메시지 변경 함수
 * @return 사용자 통계 화면
 */
export function UserStatisticsPage({ onError }: UserStatisticsPageProps) {
  const [days, setDays] = useState<UserStatisticsDays>(30)
  const [statistics, setStatistics] = useState<UserStatistics | null>(null)
  const [loading, setLoading] = useState(true)
  const [requestVersion, setRequestVersion] = useState(0)

  /**
   * 최초 진입과 조회 조건 변경 시 실시간 통계를 조회한다.
   *
   * @author SeungHyeon.Kang
   * @return 화면 해제 뒤 이전 응답을 차단하는 정리 함수
   */
  const loadStatistics = (): (() => void) => {
    let active = true

    /**
     * 현재 기간의 통계를 조회해 화면 상태에 반영한다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const loadData = async (): Promise<void> => {
      try {
        // 관리자 서버가 원천 테이블에서 실시간으로 집계한 결과를 요청한다.
        const result = await getUserStatistics(days)
        // 현재 화면에 해당하는 최신 요청 결과만 반영한다.
        if (active) {
          // 여섯 통계 차트가 공유할 조회 결과를 설정한다.
          setStatistics(result)
          // 이전 공통 오류 메시지를 제거한다.
          onError(null)
        }
      // 실시간 통계 API 실패를 공통 오류 영역에 표시한다.
      } catch (error: unknown) {
        // 현재 화면에 해당하는 요청의 실패 원인만 표시한다.
        if (active) onError(error instanceof Error ? error.message : '사용자 통계를 불러오지 못했습니다.')
      // 요청 성공 여부와 관계없이 로딩 상태를 정리한다.
      } finally {
        // 현재 화면에 해당하는 요청의 로딩 상태만 종료한다.
        if (active) setLoading(false)
      }
    }

    // 화면 진입 또는 기간 변경에 맞는 통계 조회를 시작한다.
    void loadData()

    /**
     * 화면 해제 뒤 도착하는 이전 응답을 무시한다.
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const cancelLoad = (): void => {
      active = false
    }

    // React가 조건 변경과 화면 해제 때 실행할 정리 함수를 반환한다.
    return cancelLoad
  }

  // 최초 진입과 조회 기간 변경 시 통계를 다시 조회한다.
  useEffect(loadStatistics, [days, requestVersion, onError])

  /**
   * 조회 기간 버튼에서 1개월과 3개월 및 6개월과 1년 값을 적용한다.
   *
   * @author SeungHyeon.Kang
   * @param selectedDays 조회 기간에 대응하는 고정 일수
   * @return 반환값이 없다
   */
  const handleDaysClick = (selectedDays: UserStatisticsDays): void => {
    // 기간 변경 중 기존 차트를 흐리게 표시한다.
    setLoading(true)
    // 새 요청 전 이전 공통 오류 메시지를 제거한다.
    onError(null)
    // 선택한 기간으로 조회 조건을 변경한다.
    setDays(selectedDays)
  }

  /**
   * 기간 버튼에 설정된 일수를 사용자 통계 조회 조건에 적용한다.
   *
   * @author SeungHyeon.Kang
   * @param event 선택한 기간 버튼의 클릭 이벤트
   * @return 반환값이 없다
   */
  const handlePeriodClick = (event: MouseEvent<HTMLButtonElement>): void => {
    // 버튼 값은 허용된 네 조회 기간 중 하나이므로 사용자 통계 기간 타입으로 변환한다.
    const selectedDays = Number(event.currentTarget.value) as UserStatisticsDays
    // 변환한 조회 기간으로 통계 재조회를 시작한다.
    handleDaysClick(selectedDays)
  }

  /**
   * 사용자 통계 조회 기간 한 항목을 선택 버튼으로 표시한다.
   *
   * @author SeungHyeon.Kang
   * @param period 버튼에 표시할 기간 이름과 고정 일수
   * @return 조회 기간 선택 버튼
   */
  const renderPeriodButton = (period: StatisticsPeriod): ReactElement => {
    // 현재 조회 기간을 활성 상태로 구분한 버튼을 반환한다.
    return (
      <button
        type="button"
        key={period.days}
        value={period.days}
        className={days === period.days ? 'active' : ''}
        onClick={handlePeriodClick}
      >
        {/* "1개월", "3개월", "6개월", "1년" 중 선택 가능한 조회 기간 */}
        {period.label}
      </button>
    )
  }

  /**
   * 현재 선택 기간의 통계를 다시 조회한다.
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleReload = (): void => {
    // 재조회 요청 버전을 변경해 동일 기간의 API를 다시 호출한다.
    setLoading(true)
    // 새 요청 전 이전 공통 오류 메시지를 제거한다.
    onError(null)
    // 같은 기간을 다시 조회하도록 요청 버전을 올린다.
    setRequestVersion(requestVersion + 1)
  }

  // 최초 조회 중에는 차트 대신 명확한 로딩 메시지를 표시한다.
  if (loading && !statistics) {
    // "사용자 통계를 집계하고 있습니다."
    return <section className="statistics-loading panel">사용자 통계를 집계하고 있습니다.</section>
  }

  // 조회 결과가 없으면 비어 있는 화면 대신 재조회 가능한 안내를 표시한다.
  if (!statistics) {
    return (
      <section className="statistics-loading panel">
        {/* "사용자 통계를 표시할 수 없습니다." */}
        <p>사용자 통계를 표시할 수 없습니다.</p>
        {/* "다시 조회" */}
        <button type="button" onClick={handleReload}>다시 조회</button>
      </section>
    )
  }

  // 미접속 회원을 두 독립 구간으로 나눈 차트 데이터를 생성한다.
  const inactivityData = createInactivityData(statistics)
  // 상태별 현재 회원 수를 화면 표시 형식으로 변환한다.
  const totalUserCount = getTotalUserCount(statistics).toLocaleString()
  // 선택 기간의 신규 가입자 합계를 화면 표시 형식으로 변환한다.
  const joinTotal = getJoinTotal(statistics).toLocaleString()
  // 선택 기간의 전체 활동 합계를 화면 표시 형식으로 변환한다.
  const activityTotal = getActivityTotal(statistics).toLocaleString()
  // 가장 최근 일자의 활성 회원 지표를 요약 영역에 사용한다.
  const latestTrend = statistics.trendList.at(-1)
  // 일간 활성 회원 수를 화면 표시 형식으로 변환한다.
  const dailyActive = (latestTrend?.dauCntt ?? 0).toLocaleString()
  // 주간 활성 회원 수를 화면 표시 형식으로 변환한다.
  const weeklyActive = (latestTrend?.wauCntt ?? 0).toLocaleString()
  // 월간 활성 회원 수를 화면 표시 형식으로 변환한다.
  const monthlyActive = (latestTrend?.mauCntt ?? 0).toLocaleString()
  // 서버의 최종 조회 시각을 관리자 로컬 표시 형식으로 변환한다.
  const generatedAt = new Date(statistics.generatedAt).toLocaleString('ko-KR')

  // 여섯 통계를 같은 크기의 3행 2열 차트로 표시한다.
  return (
    <section className="user-statistics-page">
      {/* 화면 제목, 집계 기준과 공통 기간 필터 */}
      <section className="content-header statistics-header">
        <div>
          {/* "사용자 통계" */}
          <h1>사용자 통계</h1>
          <p>{statistics.startDate} ~ {statistics.endDate} · 실시간 조회</p>
        </div>
        <div className="statistics-period" aria-label="통계 조회 기간">
          {/* "1개월", "3개월", "6개월", "1년" */}
          {STATISTICS_PERIOD_LIST.map(renderPeriodButton)}
        </div>
      </section>

      {/* 여섯 가지 사용자 통계를 3행 2열로 배치한다. */}
      <section className={`statistics-grid${loading ? ' loading' : ''}`} aria-busy={loading}>
        <article className="statistics-card statistics-card-status">
          <header>
            <div>
              {/* "상태별 현재 회원수" */}
              <h2>상태별 현재 회원수</h2>
              <p><strong>{totalUserCount}</strong>명 · 현재 보관 회원</p>
            </div>
          </header>
          <div className="statistics-chart">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={statistics.statusList} layout="vertical" margin={CHART_MARGIN}>
                <defs>
                  <linearGradient id="statusGradient" x1="0" y1="0" x2="1" y2="0">
                    <stop offset="0%" stopColor="#6c7cff" />
                    <stop offset="100%" stopColor="#4055df" />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke="#edf0f6" strokeDasharray="4 5" horizontal={false} />
                <XAxis type="number" tickFormatter={formatCountTick} allowDecimals={false} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="userStatName" width={76} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: '#f3f5ff' }} />
                <Bar dataKey="userCntt" name="회원수" fill="url(#statusGradient)" radius={[0, 8, 8, 0]} barSize={24} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="statistics-card statistics-card-join">
          <header><div>{/* "신규 가입자" */}<h2>신규 가입자</h2><p><strong>{joinTotal}</strong>명 · 선택 기간 누적</p></div></header>
          <div className="statistics-chart">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={statistics.trendList} margin={CHART_MARGIN}>
                <defs>
                  <linearGradient id="joinGradient" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#22c7a9" />
                    <stop offset="100%" stopColor="#0f9f87" />
                  </linearGradient>
                </defs>
                <CartesianGrid stroke="#edf0f6" strokeDasharray="4 5" vertical={false} />
                <XAxis dataKey="statDate" tickFormatter={formatDateTick} minTickGap={24} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <YAxis tickFormatter={formatCountTick} allowDecimals={false} width={42} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: '#eefbf8' }} />
                <Bar dataKey="joinCntt" name="신규 가입" fill="url(#joinGradient)" radius={[7, 7, 2, 2]} maxBarSize={20} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="statistics-card statistics-card-activity">
          <header><div>{/* "활동 추세" */}<h2>활동 추세</h2><p><strong>{activityTotal}</strong>건 · 선택 기간 전체 활동</p></div></header>
          <div className="statistics-chart">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={statistics.trendList} margin={CHART_MARGIN}>
                <CartesianGrid stroke="#edf0f6" strokeDasharray="4 5" vertical={false} />
                <XAxis dataKey="statDate" tickFormatter={formatDateTick} minTickGap={24} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <YAxis tickFormatter={formatCountTick} allowDecimals={false} width={42} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, color: '#657086' }} />
                <Line type="monotone" dataKey="reportCntt" name="독후감" stroke="#5267e8" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                <Line type="monotone" dataKey="replyCntt" name="댓글" stroke="#f08a5d" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                <Line type="monotone" dataKey="likeCntt" name="좋아요" stroke="#df5f9d" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                <Line type="monotone" dataKey="followCntt" name="팔로우" stroke="#9a6be8" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="statistics-card statistics-card-active">
          <header>
            <div>
              {/* "활성 회원과 미접속 회원" */}
              <h2>활성 회원과 미접속 회원</h2>
              <p>현재 정상 회원 기준 · 최근 일자 활성 현황</p>
            </div>
            {/* 최근 일자의 일간과 주간 및 월간 활성 회원 요약 */}
            <div className="statistics-active-summary">
              <span><i className="daily" />일간 <strong>{dailyActive}</strong></span>
              <span><i className="weekly" />주간 <strong>{weeklyActive}</strong></span>
              <span><i className="monthly" />월간 <strong>{monthlyActive}</strong></span>
            </div>
          </header>
          <div className="statistics-active-charts">
            <div className="statistics-active-line">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={statistics.trendList} margin={CHART_MARGIN}>
                  <CartesianGrid stroke="#edf0f6" strokeDasharray="4 5" vertical={false} />
                  <XAxis dataKey="statDate" tickFormatter={formatDateTick} minTickGap={24} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                  <YAxis tickFormatter={formatCountTick} allowDecimals={false} width={42} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={TOOLTIP_STYLE} />
                  <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, color: '#657086' }} />
                  <Line type="monotone" dataKey="dauCntt" name="일간 활성" stroke="#16ad92" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                  <Line type="monotone" dataKey="wauCntt" name="주간 활성" stroke="#5267e8" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                  <Line type="monotone" dataKey="mauCntt" name="월간 활성" stroke="#9565dc" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div className="statistics-inactive-chart" aria-label="미접속 회원 구간별 차트">
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={inactivityData} layout="vertical" margin={{ top: 0, right: 22, bottom: 0, left: 0 }}>
                  <defs>
                    <linearGradient id="inactiveGradient" x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0%" stopColor="#ff9c74" />
                      <stop offset="100%" stopColor="#ee704d" />
                    </linearGradient>
                  </defs>
                  <XAxis type="number" tickFormatter={formatCountTick} allowDecimals={false} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                  <YAxis type="category" dataKey="name" width={76} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                  <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: '#fff5f1' }} />
                  <Bar dataKey="userCntt" name="미접속 회원" fill="url(#inactiveGradient)" radius={[0, 8, 8, 0]} barSize={18} background={{ fill: '#f7f8fb', radius: 8 }} />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>
        </article>

        {/* 가입자 유지율과 계정 이탈 및 복구 추세 통계 카드 */}
        <UserInsightCharts statistics={statistics} />
      </section>

      {/* 마지막 실시간 조회 시각 */}
      <p className="statistics-generated">마지막 집계: {generatedAt}</p>
    </section>
  )
}
