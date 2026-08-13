import { useEffect, useRef, useState } from 'react'
import './AdminDatePicker.css'

type AdminDatePickerProps = {
  value: string
  onChange: (value: string) => void
  ariaLabel: string
  min?: string
  max?: string
  disabled?: boolean
}

const WEEK_DAYS = ['일', '월', '화', '수', '목', '금', '토']

/**
 * 날짜 문자열을 달력에서 사용할 로컬 날짜 객체로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param value yyyy-MM-dd 형식의 날짜 문자열
 * @return 변환된 로컬 날짜 객체
 */
const parseDateValue = (value: string): Date => {
  // 날짜가 선택되지 않았으면 현재 월을 달력 기준으로 사용한다
  if (!value) {
    return new Date()
  }

  const [year, month, day] = value.split('-').map(Number)

  // 브라우저 시간대의 영향을 받지 않는 로컬 날짜 객체를 반환한다
  return new Date(year, month - 1, day)
}

/**
 * 로컬 날짜 객체를 yyyy-MM-dd 형식으로 변환한다
 *
 * @author SeungHyeon.Kang
 * @param date 변환할 로컬 날짜 객체
 * @return yyyy-MM-dd 형식의 날짜 문자열
 */
const formatDateValue = (date: Date): string => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')

  // 검색 조건과 API에서 사용하는 날짜 형식을 반환한다
  return `${year}-${month}-${day}`
}

/**
 * 표시 중인 월의 앞쪽 빈칸과 날짜 목록을 생성한다
 *
 * @author SeungHyeon.Kang
 * @param year 표시할 연도
 * @param month 표시할 월의 0부터 시작하는 번호
 * @return 달력 격자에 표시할 날짜 목록
 */
const getCalendarDays = (year: number, month: number): Array<number | null> => {
  const firstDay = new Date(year, month, 1).getDay()
  const lastDate = new Date(year, month + 1, 0).getDate()

  // 요일 위치에 맞춘 빈칸과 해당 월의 날짜를 함께 반환한다
  return [
    ...Array.from({ length: firstDay }, () => null),
    ...Array.from({ length: lastDate }, (_, index) => index + 1),
  ]
}

/**
 * 관리자 검색 화면에 사용자단 스타일의 날짜 선택 달력을 제공한다
 *
 * @author SeungHyeon.Kang
 * @param value 현재 선택된 yyyy-MM-dd 형식의 날짜
 * @param onChange 선택 날짜 변경 함수
 * @param ariaLabel 날짜 선택 버튼의 접근성 이름
 * @param min 선택 가능한 최소 날짜
 * @param max 선택 가능한 최대 날짜
 * @param disabled 날짜 선택 비활성화 여부
 * @return 날짜 입력 버튼과 달력 팝오버
 */
export function AdminDatePicker({ value, onChange, ariaLabel, min, max, disabled = false }: AdminDatePickerProps) {
  const rootRef = useRef<HTMLDivElement>(null)
  const [isOpen, setIsOpen] = useState(false)
  const [viewDate, setViewDate] = useState(() => parseDateValue(value))
  const [moveDirection, setMoveDirection] = useState<'prev' | 'next'>('next')
  const viewYear = viewDate.getFullYear()
  const viewMonth = viewDate.getMonth()
  const days = getCalendarDays(viewYear, viewMonth)
  const todayValue = formatDateValue(new Date())

  useEffect(() => {
    /**
     * 달력 바깥쪽 포인터 입력과 Escape 키로 팝오버를 닫는다
     *
     * @author SeungHyeon.Kang
     * @param event 문서에서 발생한 포인터 또는 키보드 이벤트
     * @return 반환값이 없다
     */
    const handleDocumentInput = (event: PointerEvent | KeyboardEvent): void => {
      // Escape 키를 누르면 현재 열린 달력을 닫는다
      if (event instanceof KeyboardEvent && event.key === 'Escape') {
        setIsOpen(false)
        return
      }

      // 달력 외부를 누른 경우에만 팝오버를 닫는다
      if (event instanceof PointerEvent && !rootRef.current?.contains(event.target as Node)) {
        setIsOpen(false)
      }
    }

    // 달력 외부 입력을 감지하도록 문서 이벤트를 연결한다
    document.addEventListener('pointerdown', handleDocumentInput)
    document.addEventListener('keydown', handleDocumentInput)

    // 컴포넌트가 사라질 때 문서 이벤트 연결을 해제한다
    return () => {
      document.removeEventListener('pointerdown', handleDocumentInput)
      document.removeEventListener('keydown', handleDocumentInput)
    }
  }, [])

  /**
   * 날짜 입력 버튼을 눌러 현재 선택 월의 달력을 여닫는다
   *
   * @author SeungHyeon.Kang
   * @return 반환값이 없다
   */
  const handleToggle = (): void => {
    // 팝오버를 열 때 현재 선택 날짜가 포함된 월부터 표시한다
    if (!isOpen) {
      setViewDate(parseDateValue(value))
    }

    // 현재 달력 팝오버의 열림 상태를 반전한다
    setIsOpen((current) => !current)
  }

  /**
   * 달력에서 이전 또는 다음 월로 이동한다
   *
   * @author SeungHyeon.Kang
   * @param amount 이동할 월 수
   * @return 반환값이 없다
   */
  const handleMonthMove = (amount: number): void => {
    // 월 전환 방향에 맞는 슬라이드 효과를 선택한다
    setMoveDirection(amount < 0 ? 'prev' : 'next')
    // 현재 연도와 월을 기준으로 이동한 월을 표시한다
    setViewDate(new Date(viewYear, viewMonth + amount, 1))
  }

  /**
   * 달력의 날짜를 선택하고 팝오버를 닫는다
   *
   * @author SeungHyeon.Kang
   * @param nextValue 선택할 yyyy-MM-dd 형식의 날짜
   * @return 반환값이 없다
   */
  const handleDateSelect = (nextValue: string): void => {
    // 선택한 날짜를 부모 검색 조건에 반영한다
    onChange(nextValue)
    // 날짜 선택을 마친 뒤 달력 팝오버를 닫는다
    setIsOpen(false)
  }

  return (
    <div className="admin-date-picker" ref={rootRef}>
      <button
        type="button"
        className={`admin-date-picker__trigger${isOpen ? ' opened' : ''}`}
        aria-label={ariaLabel}
        aria-haspopup="dialog"
        aria-expanded={isOpen}
        disabled={disabled}
        onClick={handleToggle}
      >
        <span className={value ? '' : 'admin-date-picker__placeholder'}>{value ? value.replaceAll('-', '.') : '날짜 선택'}</span>
        <span className="admin-date-picker__icon" aria-hidden="true" />
      </button>

      {isOpen && (
        <div className="admin-date-picker__popover" role="dialog" aria-label={ariaLabel}>
          <div className="admin-date-picker__header">
            <button type="button" className="admin-date-picker__nav" aria-label="이전 달" onClick={() => handleMonthMove(-1)}>
              <span className="admin-date-picker__nav-icon prev" aria-hidden="true" />
            </button>
            <strong>{viewYear}년 {viewMonth + 1}월</strong>
            <button type="button" className="admin-date-picker__nav" aria-label="다음 달" onClick={() => handleMonthMove(1)}>
              <span className="admin-date-picker__nav-icon next" aria-hidden="true" />
            </button>
          </div>

          <div className="admin-date-picker__weekdays">
            {WEEK_DAYS.map((weekDay) => <span key={weekDay}>{weekDay}</span>)}
          </div>

          <div className={`admin-date-picker__days slide-${moveDirection}`} key={`${viewYear}-${viewMonth}`}>
            {days.map((day, index) => {
              // 해당 월의 첫 요일 전까지 비어 있는 달력 칸을 반환한다
              if (day === null) {
                return <span className="admin-date-picker__empty" key={`empty-${index}`} />
              }

              const dateValue = formatDateValue(new Date(viewYear, viewMonth, day))
              const isSelected = dateValue === value
              const isToday = dateValue === todayValue
              const isDisabled = Boolean((min && dateValue < min) || (max && dateValue > max))
              const dayClassName = [
                'admin-date-picker__day',
                isSelected ? 'selected' : '',
                isToday ? 'today' : '',
              ].filter(Boolean).join(' ')

              // 선택 가능 여부와 현재 상태가 반영된 날짜 버튼을 반환한다
              return (
                <button
                  type="button"
                  className={dayClassName}
                  key={dateValue}
                  disabled={isDisabled}
                  aria-pressed={isSelected}
                  onClick={() => handleDateSelect(dateValue)}
                >
                  {day}
                </button>
              )
            })}
          </div>

          <div className="admin-date-picker__footer">
            <button type="button" className="admin-date-picker__footer-button" onClick={() => handleDateSelect(todayValue)}>오늘</button>
            <button type="button" className="admin-date-picker__footer-button" onClick={() => setIsOpen(false)}>닫기</button>
          </div>
        </div>
      )}
    </div>
  )
}
