import { ALIM_TEMP_DETAIL_PREFIX, ALIM_TEMP_NEW_PATH } from '../../constants/routes'
import type { AlimTemp } from '../../types/alim'
import type { Code } from '../../types/code'
import type { Permission } from '../../types/common'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'

type AlimTempListPageProps = {
  alimTemps: AlimTemp[]
  permission: Permission
  useeYsnoCodes: Code[]
  onMovePath: (path: string) => void
}

/**
 * 알림 템플릿 목록 화면
 * @Author SeungHyeon.Kang
 * @param alimTemps
 * @param permission
 * @param useeYsnoCodes
 * @param onMovePath
 * @return
 */
export function AlimTempListPage({ alimTemps, permission, useeYsnoCodes, onMovePath }: AlimTempListPageProps) {
  return (
    <section className="alim-temp-manage">
      <section className="content-header">
        <h1>알림 템플릿 관리</h1>
        <div className="status">총 {alimTemps.length}건</div>
      </section>
      <section className="table-wrap alim-temp-list-table">
        <table>
          <thead>
            <tr>
              <th>알림상황</th>
              <th>템플릿코드</th>
              <th>관리용 제목</th>
              <th className="col-usee">사용여부</th>
              <th>등록자</th>
              <th>등록일</th>
            </tr>
          </thead>
          <tbody>
            {alimTemps.length === 0 ? (
              <tr className="empty-row">
                <td colSpan={6}>알림 템플릿이 없습니다.</td>
              </tr>
            ) : (
              alimTemps.map((alimTemp) => (
                <tr key={`${alimTemp.alimSitu}-${alimTemp.tempCode}`} onClick={() => onMovePath(`${ALIM_TEMP_DETAIL_PREFIX}/${encodeURIComponent(alimTemp.alimSitu)}/${encodeURIComponent(alimTemp.tempCode)}`)}>
                  <td>{alimTemp.alimSituName ?? alimTemp.alimSitu}</td>
                  <td>{alimTemp.tempCode}</td>
                  <td>{alimTemp.tempTitl}</td>
                  <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, alimTemp.useeYsno, alimTemp.useeYsnoName)}</td>
                  <td>{alimTemp.regiAdmnName ?? alimTemp.regiAdmn}</td>
                  <td>{formatDate(alimTemp.regiDate)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </section>
      {permission.writYn && <button type="button" className="floating-button" onClick={() => onMovePath(ALIM_TEMP_NEW_PATH)}>등록</button>}
    </section>
  )
}
