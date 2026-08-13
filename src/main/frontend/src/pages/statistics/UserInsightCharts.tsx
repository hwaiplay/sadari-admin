import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Funnel,
  FunnelChart,
  LabelList,
  Legend,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type {
  InsightChartItem,
  RetentionChartItem,
  UserStatistics,
} from '../../types/userStatistics'

type UserInsightChartsProps = {
  statistics: UserStatistics
}

const CHART_MARGIN = { top: 12, right: 24, bottom: 4, left: 0 }
const AXIS_TICK = { fill: '#8a94a6', fontSize: 11 }
const TOOLTIP_STYLE = {
  border: '1px solid #e6eaf2',
  borderRadius: 12,
  boxShadow: '0 12px 30px rgba(35, 45, 70, 0.12)',
  color: '#273044',
  fontSize: 12,
}

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
 * 큰 회원 수를 차트 축 너비에 맞는 단위로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param value 차트 축에 표시할 회원 수
 * @return 한글 단위를 적용한 축 문구
 */
const formatCountTick = (value: number): string => {
  // 만 단위 이상은 짧은 한글 단위로 표시한다.
  if (value >= 10000) return `${Math.round(value / 1000) / 10}만`
  // 천 단위 이상은 짧은 한글 단위로 표시한다.
  if (value >= 1000) return `${Math.round(value / 100) / 10}천`
  // 작은 값은 원래 숫자로 표시한다.
  return String(value)
}

/**
 * 유지율 응답을 기준 일수별 가로 막대 데이터로 변환한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 1일과 7일 및 30일 유지율 차트 데이터
 */
const createRetentionData = (statistics: UserStatistics): RetentionChartItem[] => {
  const chartData: RetentionChartItem[] = []
  // 기준 일수에 맞는 한글 이름과 강조 색상을 유지율 항목에 설정한다.
  for (const retention of statistics.retentionList) {
    let fill = '#566bea'
    // 장기 유지율은 단계가 깊어질수록 진한 보라색으로 구분한다.
    if (retention.periodDays === 7) fill = '#7b61d9'
    // 30일 유지율은 가장 장기적인 정착 지표로 별도 색상을 적용한다.
    if (retention.periodDays === 30) fill = '#a45ec5'
    // 화면에 사용할 기준 일수 이름과 서버 집계값을 추가한다.
    chartData.push({ ...retention, name: `${retention.periodDays}일 유지`, fill })
  }

  // 유지율 카드가 사용할 세 기준 일수의 데이터를 반환한다.
  return chartData
}

/**
 * 가입자의 핵심 행동 전환 수를 단계형 차트 데이터로 변환한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 가입부터 소셜 활동까지의 전환 단계 데이터
 */
const createConversionData = (statistics: UserStatistics): InsightChartItem[] => {
  // 순차 전환 조건이 적용된 네 단계와 회원 수를 반환한다.
  return [
    { name: '가입', userCntt: statistics.conversion.joinCntt, fill: '#4e63e6' },
    { name: '온보딩 완료', userCntt: statistics.conversion.onboardingCntt, fill: '#6779eb' },
    { name: '첫 독후감', userCntt: statistics.conversion.reportCntt, fill: '#7e8cef' },
    { name: '소셜 활동', userCntt: statistics.conversion.socialCntt, fill: '#99a5f4' },
  ]
}

/**
 * 현재 정상 회원의 활동 깊이 수를 도넛 차트 데이터로 변환한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 가장 깊은 활동 단계별 회원 수
 */
const createActivityData = (statistics: UserStatistics): InsightChartItem[] => {
  // 각 회원이 도달한 가장 깊은 행동 단계와 회원 수를 반환한다.
  return [
    { name: '방문만', userCntt: statistics.activityComposition.visitCntt, fill: '#7ecdc1' },
    { name: '독후감', userCntt: statistics.activityComposition.reportCntt, fill: '#5d73e8' },
    { name: '댓글·좋아요', userCntt: statistics.activityComposition.communityCntt, fill: '#e96eaa' },
    { name: '팔로우', userCntt: statistics.activityComposition.relationCntt, fill: '#9a68dc' },
  ]
}

/**
 * 관리자 사용자 정착과 이탈 통계를 두 번째 2열 2행 차트로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 유지율과 전환 및 활동 깊이와 이탈 추세 영역
 */
export function UserInsightCharts({ statistics }: UserInsightChartsProps) {
  // 가입 후 기준 일수별 재방문율을 차트 데이터로 구성한다.
  const retentionData = createRetentionData(statistics)
  // 가입부터 소셜 활동까지 순차 전환 데이터를 구성한다.
  const conversionData = createConversionData(statistics)
  // 현재 정상 회원의 가장 깊은 활동 단계 데이터를 구성한다.
  const activityData = createActivityData(statistics)

  // 사용자 정착과 이탈을 설명하는 네 개의 차트 카드를 반환한다.
  return (
    <>
      {/* 사용자 정착과 이탈 통계 영역 제목 */}
      <section className="statistics-section-title">
        <div>
          {/* "사용자 정착과 이탈" */}
          <h2>사용자 정착과 이탈</h2>
          <p>가입 이후 핵심 행동 도달과 계정 이탈 흐름</p>
        </div>
      </section>

      {/* 유지율과 전환 및 활동 깊이와 이탈 추세 2열 2행 영역 */}
      <section className="statistics-grid statistics-insight-grid">
        <article className="statistics-card statistics-card-retention">
          <header><div>{/* "가입자 유지율" */}<h2>가입자 유지율</h2><p>관찰 기간이 지난 가입자의 기준일 내 재방문</p></div></header>
          <div className="statistics-chart">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={retentionData} layout="vertical" margin={CHART_MARGIN}>
                <CartesianGrid stroke="#edf0f6" strokeDasharray="4 5" horizontal={false} />
                <XAxis type="number" domain={[0, 100]} unit="%" tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <YAxis type="category" dataKey="name" width={76} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={TOOLTIP_STYLE} cursor={{ fill: '#f5f3ff' }} />
                <Bar dataKey="retentionRate" name="유지율" unit="%" fill="#566bea" radius={[0, 9, 9, 0]} barSize={28} background={{ fill: '#f5f6fa', radius: 9 }}>
                  <Cell fill="#566bea" />
                  <Cell fill="#7b61d9" />
                  <Cell fill="#a45ec5" />
                  <LabelList dataKey="retentionRate" position="right" fill="#596176" fontSize={12} />
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="statistics-card statistics-card-conversion">
          <header><div>{/* "핵심 행동 전환" */}<h2>핵심 행동 전환</h2><p>선택 기간 가입자의 순차 행동 도달</p></div></header>
          <div className="statistics-chart statistics-funnel-chart">
            <ResponsiveContainer width="100%" height="100%">
              <FunnelChart>
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Funnel dataKey="userCntt" nameKey="name" data={conversionData} isAnimationActive>
                  <LabelList position="right" dataKey="name" fill="#4e566a" fontSize={12} />
                  <LabelList position="center" dataKey="userCntt" fill="#ffffff" fontSize={13} />
                </Funnel>
              </FunnelChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="statistics-card statistics-card-depth">
          <header><div>{/* "활성 회원의 활동 깊이" */}<h2>활성 회원의 활동 깊이</h2><p>현재 정상 회원이 선택 기간에 도달한 가장 깊은 행동</p></div></header>
          <div className="statistics-chart">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, color: '#657086' }} />
                <Pie data={activityData} dataKey="userCntt" nameKey="name" innerRadius="48%" outerRadius="76%" paddingAngle={3} stroke="none">
                  <Cell fill="#7ecdc1" />
                  <Cell fill="#5d73e8" />
                  <Cell fill="#e96eaa" />
                  <Cell fill="#9a68dc" />
                </Pie>
              </PieChart>
            </ResponsiveContainer>
          </div>
        </article>

        <article className="statistics-card statistics-card-churn">
          <header><div>{/* "계정 이탈과 복구 추세" */}<h2>계정 이탈과 복구 추세</h2><p>비활성화·영구 탈퇴·이용 정지·복구 처리</p></div></header>
          <div className="statistics-chart">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={statistics.churnTrendList} margin={CHART_MARGIN}>
                <CartesianGrid stroke="#edf0f6" strokeDasharray="4 5" vertical={false} />
                <XAxis dataKey="statDate" tickFormatter={formatDateTick} minTickGap={24} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <YAxis tickFormatter={formatCountTick} allowDecimals={false} width={42} tick={AXIS_TICK} axisLine={false} tickLine={false} />
                <Tooltip contentStyle={TOOLTIP_STYLE} />
                <Legend iconType="circle" iconSize={8} wrapperStyle={{ fontSize: 12, color: '#657086' }} />
                <Line type="monotone" dataKey="withdrawnCntt" name="계정 비활성화" stroke="#f09b63" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                <Line type="monotone" dataKey="deleteCntt" name="영구 탈퇴" stroke="#e15f79" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                <Line type="monotone" dataKey="suspendedCntt" name="이용 정지" stroke="#8a6bdf" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
                <Line type="monotone" dataKey="restoredCntt" name="계정 복구" stroke="#20af91" dot={false} activeDot={{ r: 5 }} strokeWidth={2.5} />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </article>
      </section>
    </>
  )
}
