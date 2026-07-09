import type { Permission } from '../../types/common'
import type { Code, CodeMaster } from '../../types/code'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'

type CodeListPageProps = {
  codeMasters: CodeMaster[]
  permission: Permission
  useeYsnoCodes: Code[]
  onSelect: (master: CodeMaster) => void
  onOpenRegister: () => void
}

export function CodeListPage({ codeMasters, permission, useeYsnoCodes, onSelect, onOpenRegister }: CodeListPageProps) {
  return (
    <section className="code-manage">
      <section className="content-header">
        <h1>코드관리</h1>
        <div className="status">총 {codeMasters.length}건</div>
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
      {permission.writYn && <button type="button" className="floating-button" onClick={onOpenRegister}>등록</button>}
    </section>
  )
}
