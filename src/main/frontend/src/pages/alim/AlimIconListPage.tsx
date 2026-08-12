import { useState } from 'react'
import type { FormEvent } from 'react'
import { Pagination } from '../../components/Pagination'
import { ALIM_ICON_DETAIL_PREFIX, ALIM_ICON_LIST_PATH } from '../../constants/routes'
import type { AlimIcon, AlimIconSearch } from '../../types/alim'
import type { Code } from '../../types/code'
import type { PageData } from '../../types/common'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { getListPageSnapshot } from '../../utils/search'

type Props = {
  icons: AlimIcon[]
  pageData: PageData<AlimIcon>
  useeYsnoCodes: Code[]
  onSearch: (pageNumber: number, search: AlimIconSearch) => void
  onMovePath: (path: string) => void
}

const DEFAULT_SEARCH: AlimIconSearch = { keyword: '', useeYsno: '' }

/** ALIM_SITU 공통코드 전체와 아이콘 등록 상태를 표시한다. */
export function AlimIconListPage({ icons, pageData, useeYsnoCodes, onSearch, onMovePath }: Props) {
  const initialSnapshot = getListPageSnapshot(ALIM_ICON_LIST_PATH, DEFAULT_SEARCH)
  const [search, setSearch] = useState<AlimIconSearch>(initialSnapshot.search)
  const [appliedSearch, setAppliedSearch] = useState<AlimIconSearch>(initialSnapshot.search)

  /** 입력한 알림 상황 검색 조건을 적용한다. */
  const handleSearch = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const next = { ...search }
    setAppliedSearch(next)
    onSearch(1, next)
  }

  /** 알림 상황 검색 조건을 초기화한다. */
  const handleReset = () => {
    const next = { ...DEFAULT_SEARCH }
    setSearch(next)
    setAppliedSearch(next)
    onSearch(1, next)
  }

  return (
    <section className="alim-icon-manage">
      <section className="content-header"><h1>알림 아이콘 관리</h1><div className="status">총 {pageData.totalCount}건</div></section>
      <form className="list-search" onSubmit={handleSearch}>
        <label><span>코드·코드명</span><input value={search.keyword} maxLength={100} placeholder="알림상황 코드 또는 코드명" onChange={(event) => setSearch({ ...search, keyword: event.target.value })} /></label>
        <label><span>공통코드 사용여부</span><select value={search.useeYsno} onChange={(event) => setSearch({ ...search, useeYsno: event.target.value })}><option value="">전체</option>{useeYsnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></label>
        <div className="list-search-actions"><button type="button" className="subtle-button" onClick={handleReset}>초기화</button><button type="submit">검색</button></div>
      </form>
      <section className="table-wrap alim-icon-list-table">
        <table><thead><tr><th className="col-icon-preview">미리보기</th><th>알림상황 코드</th><th>공통코드명</th><th>아이콘 등록</th><th className="col-usee">사용여부</th><th className="col-icon-count">템플릿</th><th>등록자</th><th>등록일</th></tr></thead>
          <tbody>{icons.length === 0 ? <tr className="empty-row"><td colSpan={8}>알림상황 공통코드가 없습니다.</td></tr> : icons.map((icon) => (
            <tr key={icon.alimSitu} onClick={() => onMovePath(`${ALIM_ICON_DETAIL_PREFIX}/${encodeURIComponent(icon.alimSitu)}`)}>
              <td className="col-icon-preview">{icon.iconRegiYsno === 'Y' ? <img className="alim-icon-preview-small" src={`/api/alim-icons/${encodeURIComponent(icon.alimSitu)}/image`} alt="" /> : <span>미등록</span>}</td><td>{icon.alimSitu}</td><td>{icon.alimSituName}</td><td>{icon.iconRegiYsno === 'Y' ? '등록' : '미등록'}</td><td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, icon.useeYsno)}</td><td className="col-icon-count">{icon.tempCnt}개</td><td>{icon.regiAdmnName ?? icon.regiAdmn}</td><td>{formatDate(icon.regiDate)}</td>
            </tr>
          ))}</tbody></table>
      </section>
      <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={(page) => onSearch(page, appliedSearch)} />
    </section>
  )
}
