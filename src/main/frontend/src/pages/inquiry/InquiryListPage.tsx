import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import { getInquiries } from '../../api/inquiryApi'
import { Pagination } from '../../components/Pagination'
import { INQUIRY_DETAIL_PREFIX } from '../../constants/routes'
import type { PageData } from '../../types/common'
import type { Inquiry, InquirySearch } from '../../types/inquiry'
import { formatDate } from '../../utils/code'

type Props = { onMovePath: (path: string) => void; onError: (message: string | null) => void }
const DEFAULT_SEARCH: InquirySearch = { inqrNumb: '', inqrCatg: '', inqrStat: '', userKeyword: '' }
const EMPTY_PAGE: PageData<Inquiry> = { items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0 }

/** 관리자 고객문의 검색과 처리 상태 목록을 제공한다 */
export function InquiryListPage({ onMovePath, onError }: Props) {
  const [search, setSearch] = useState({ ...DEFAULT_SEARCH })
  const [applied, setApplied] = useState({ ...DEFAULT_SEARCH })
  const [pageData, setPageData] = useState(EMPTY_PAGE)
  const [loading, setLoading] = useState(true)

  const load = async (page: number, condition: InquirySearch): Promise<void> => {
    setLoading(true)
    try { setPageData(await getInquiries(page, condition)); onError(null) }
    catch (error: unknown) { onError(error instanceof Error ? error.message : '고객문의 목록을 불러오지 못했습니다.') }
    finally { setLoading(false) }
  }

  useEffect(() => { void load(1, DEFAULT_SEARCH) }, [])
  const submit = (event: FormEvent) => { event.preventDefault(); const next = { ...search }; setApplied(next); void load(1, next) }

  return <section className="complaint-page">
    <section className="content-header"><h2>고객문의 관리</h2><div className="status">총 {pageData.totalCount.toLocaleString()}건</div></section>
    <form className="complaint-search" onSubmit={submit}>
      <label>문의번호<input value={search.inqrNumb} onChange={event => setSearch({ ...search, inqrNumb: event.target.value })} /></label>
      <label>사용자<input value={search.userKeyword} placeholder="회원번호 또는 닉네임" onChange={event => setSearch({ ...search, userKeyword: event.target.value })} /></label>
      <label>카테고리<select value={search.inqrCatg} onChange={event => setSearch({ ...search, inqrCatg: event.target.value })}><option value="">전체</option><option value="GENERAL">일반 문의</option><option value="ACCOUNT">계정 문의</option><option value="SUSPENSION_APPEAL">이용정지 이의제기</option><option value="BUG">오류 신고</option><option value="SUGGESTION">제안</option></select></label>
      <label>처리상태<select value={search.inqrStat} onChange={event => setSearch({ ...search, inqrStat: event.target.value })}><option value="">전체</option><option value="INQR_RECEIVED">접수</option><option value="INQR_REVIEWING">검토 중</option><option value="INQR_ANSWERED">답변 완료</option></select></label>
      <div className="complaint-search-actions"><button type="button" className="subtle-button" onClick={() => { setSearch({ ...DEFAULT_SEARCH }); setApplied({ ...DEFAULT_SEARCH }); void load(1, DEFAULT_SEARCH) }}>초기화</button><button type="submit">검색</button></div>
    </form>
    <section className="table-wrap complaint-list-table"><table><thead><tr><th>문의번호</th><th>상태</th><th>카테고리</th><th>제목</th><th>사용자</th><th>담당자</th><th>접수일시</th></tr></thead><tbody>
      {loading ? <tr className="empty-row"><td colSpan={7}>고객문의 목록을 불러오고 있습니다.</td></tr> : pageData.items.length === 0 ? <tr className="empty-row"><td colSpan={7}>검색 조건에 맞는 고객문의가 없습니다.</td></tr> : pageData.items.map(item => <tr className="complaint-row" tabIndex={0} key={item.inqrNumb} onClick={() => onMovePath(`${INQUIRY_DETAIL_PREFIX}/${item.inqrNumb}`)}><td><span className="table-link-button">{item.inqrNumb}</span></td><td><span className="complaint-status">{item.inqrStatName}</span></td><td>{item.inqrCatgName}</td><td>{item.inqrTitl}</td><td>{item.userNick ?? '-'} ({item.userNumb ?? '-'})</td><td>{item.asgnAdmnName ?? '-'}</td><td>{formatDate(item.regiDate)}</td></tr>)}
    </tbody></table></section>
    <Pagination
      pageNumber={pageData.pageNumber}
      totalPages={pageData.totalPages}
      onPageChange={(pageNumber) => void load(pageNumber, applied)}
    />
  </section>
}
