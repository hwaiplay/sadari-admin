import { MENU_DETAIL_PREFIX, MENU_NEW_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { Menu } from '../../types/menu'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'
import { useMenuPermission } from '../../contexts/MenuPermissionContext'
import { Pagination } from '../../components/Pagination'
import type { PageData } from '../../types/common'

type MenuListPageProps = {
  menuRows: Menu[]
  pageData: PageData<Menu>
  useeYsnoCodes: Code[]
  onPageChange: (pageNumber: number) => void
  onMovePath: (path: string) => void
  onDelete: (menu: Menu) => void
}

/**
 * 메뉴관리 목록 화면
 * @Author SeungHyeon.Kang
 * @param menuRows
 * @param useeYsnoCodes
 * @param onMovePath
 * @param onDelete
 * @return
 */
export function MenuListPage({ menuRows, pageData, useeYsnoCodes, onPageChange, onMovePath, onDelete }: MenuListPageProps) {
  const permission = useMenuPermission()
  return (
    <section className="menu-manage">
      <section className="content-header">
        <h1>메뉴관리</h1>
        <div className="status">총 {pageData.totalCount}건</div>
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
              <th className="col-action">삭제</th>
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
                <td className="col-action">
                  {permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={(event) => { event.stopPropagation(); onDelete(menu) }}>삭제</button>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
      <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages} onPageChange={onPageChange} />
      {permission.writYsno === 'Y' && <button type="button" className="floating-button" onClick={() => onMovePath(MENU_NEW_PATH)}>등록</button>}
    </section>
  )
}
