import type { Code, CodeMaster } from '../../types/code'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/useMenuPermission'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'

type CodeListPageProps = {
  codeMasters: CodeMaster[]
  pageData: PageData<CodeMaster>
  useeYsnoCodes: Code[]
  onPageChange: (pageNumber: number) => void
  onSelect: (master: CodeMaster) => void
  onOpenRegister: () => void
}

/**
 * 코드관리 목록 화면
 * @Author SeungHyeon.Kang
 * @param codeMasters
 * @param useeYsnoCodes
 * @param onSelect
 * @param onOpenRegister
 * @return
 */
export function CodeListPage({ codeMasters, pageData, useeYsnoCodes, onPageChange, onSelect, onOpenRegister }: CodeListPageProps) {
  const permission = useMenuPermission()
  return (
    <section className="code-manage">
      <section className="content-header">
        <h1>코드관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
      </section>
      <section className="table-wrap code-list-table">
        <table>
          <thead>
            <tr>
              <th>공통코드</th>
              <th>공통코드명</th>
              <th className="col-usee">사용여부</th>
              <th>등록자</th>
              <th>등록일</th>
              <th>수정자</th>
              <th>수정일</th>
            </tr>
          </thead>
          <tbody>
            {codeMasters.map((master) => (
              <tr key={master.commCode} onClick={() => onSelect(master)}>
                <td>{master.commCode}</td>
                <td>{master.codeName}</td>
                <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, master.useeYsno, master.useeYsnoName)}</td>
                <td>{master.regiAdmnName ?? master.regiAdmn}</td>
                <td>{formatDate(master.regiDate)}</td>
                <td>{master.updtAdmnName ?? master.updtAdmn}</td>
                <td>{formatDate(master.updtDate)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={onPageChange} />
      {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={onOpenRegister}>등록</button>}
    </section>
  )
}
