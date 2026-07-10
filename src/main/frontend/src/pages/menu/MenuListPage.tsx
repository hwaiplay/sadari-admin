import { MENU_DETAIL_PREFIX, MENU_NEW_PATH } from '../../constants/routes'
import type { Permission } from '../../types/common'
import type { Code } from '../../types/code'
import type { Menu } from '../../types/menu'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'

type MenuListPageProps = {
  menuRows: Menu[]
  permission: Permission
  useeYsnoCodes: Code[]
  onMovePath: (path: string) => void
  onDelete: (menu: Menu) => void
}

/**
 * 메뉴관리 목록 화면
 * @Author SeungHyeon.Kang
 * @param menuRows
 * @param permission
 * @param useeYsnoCodes
 * @param onMovePath
 * @param onDelete
 * @return
 */
export function MenuListPage({ menuRows, permission, useeYsnoCodes, onMovePath, onDelete }: MenuListPageProps) {
  return (
    <section className="menu-manage">
      <section className="content-header">
        <h1>메뉴관리</h1>
        <div className="status">총 {menuRows.length}건</div>
      </section>
      <section className="table-wrap menu-list-table">
        <table>
          <thead>
            <tr>
              <th>메뉴명</th>
              <th>URL</th>
              <th className="col-usee">사용여부</th>
              <th className="col-sort">정렬</th>
              <th>수정자</th>
              <th>수정일</th>
              {permission.deltYn && <th className="col-action">삭제</th>}
            </tr>
          </thead>
          <tbody>
            {menuRows.map((menu) => (
              <tr key={`${menu.menuNumb}-${menu.subxNumb}`} onClick={() => onMovePath(`${MENU_DETAIL_PREFIX}/${menu.menuNumb}/${menu.subxNumb}`)}>
                <td>{menu.menuName}</td>
                <td>{menu.menuUrlx}</td>
                <td className="col-usee">{getUseeYsnoCodeName(useeYsnoCodes, menu.useeYsno, menu.useeYsnoName)}</td>
                <td className="col-sort">{menu.sortOrdr}</td>
                <td>{menu.updtAdmnName ?? menu.updtAdmn}</td>
                <td>{formatDate(menu.updtDate)}</td>
                {permission.deltYn && (
                  <td className="col-action">
                    <button type="button" className="delete-button" onClick={(event) => { event.stopPropagation(); onDelete(menu) }}>삭제</button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      {permission.writYn && <button type="button" className="floating-button" onClick={() => onMovePath(MENU_NEW_PATH)}>등록</button>}
    </section>
  )
}
