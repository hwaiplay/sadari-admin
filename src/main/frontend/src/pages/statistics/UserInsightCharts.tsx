import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  LabelList,
  Legend,
  Line,
  LineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import type {
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
  if (value >= 10000) {
    // 만 단위로 축약한 회원 수를 반환한다.
    return `${Math.round(value / 1000) / 10}만`
  }

  // 천 단위 이상은 짧은 한글 단위로 표시한다.
  if (value >= 1000) {
    // 천 단위로 축약한 회원 수를 반환한다.
    return `${Math.round(value / 100) / 10}천`
  }

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
 * 가입자 유지율과 계정 이탈 추세를 사용자 통계 그리드의 카드로 표시한다.
 *
 * @author SeungHyeon.Kang
 * @param statistics 사용자 통계 API 응답
 * @return 유지율과 계정 이탈 추세 카드
 */
export function UserInsightCharts({ statistics }: UserInsightChartsProps) {
  // 가입 후 기준 일수별 재방문율을 차트 데이터로 구성한다.
  const retentionData = createRetentionData(statistics)

  // 3행 2열 사용자 통계 그리드의 마지막 두 카드를 반환한다.
  return (
    <>
      {/* 가입자 유지율 통계 카드 */}
      <article className="statistics-card statistics-card-retention">
        <header>
          <div>
            {/* "가입자 유지율" */}
            <h2>가입자 유지율</h2>
            {/* "관찰 기간이 지난 가입자의 기준일 내 재방문" */}
            <p>관찰 기간이 지난 가입자의 기준일 내 재방문</p>
          </div>
        </header>
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

      {/* 계정 이탈과 복구 추세 통계 카드 */}
      <article className="statistics-card statistics-card-churn">
        <header>
          <div>
            {/* "계정 이탈과 복구 추세" */}
            <h2>계정 이탈과 복구 추세</h2>
            {/* "비활성화·영구 탈퇴·이용 정지·복구 처리" */}
            <p>비활성화·영구 탈퇴·이용 정지·복구 처리</p>
          </div>
        </header>
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
    </>
  )
}
