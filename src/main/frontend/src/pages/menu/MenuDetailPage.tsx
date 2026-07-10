import type { FormEvent } from 'react'
import { MENU_DETAIL_PREFIX, MENU_LIST_PATH } from '../../constants/routes'
import type { Code } from '../../types/code'
import type { Permission } from '../../types/common'
import type { Menu, MenuForm } from '../../types/menu'
import { formatDate, getUseeYsnoCodeName } from '../../utils/code'

type MenuDetailPageProps = {
  isNewPage: boolean
  pageTitle: string
  saving: boolean
  menuForm: MenuForm
  menuDetail: Menu | null
  childForms: MenuForm[]
  subMenus: Menu[]
  authCodes: Code[]
  useeYsnoCodes: Code[]
  permission: Permission
  onMovePath: (path: string) => void
  onChangeMenuForm: (field: keyof MenuForm, value: string) => void
  onChangeChildForm: (index: number, field: keyof MenuForm, value: string) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
  onAddChildForm: () => void
  onSaveAllChildMenus: () => void
  onDelete: (menu: Menu) => void
}

/**
 * 메뉴관리 상세 화면
 * @Author SeungHyeon.Kang
 * @param isNewPage
 * @param pageTitle
 * @param saving
 * @param menuForm
 * @param menuDetail
 * @param childForms
 * @param subMenus
 * @param authCodes
 * @param useeYsnoCodes
 * @param permission
 * @param onMovePath
 * @param onChangeMenuForm
 * @param onChangeChildForm
 * @param onSubmit
 * @param onAddChildForm
 * @param onSaveAllChildMenus
 * @param onDelete
 * @return
 */
export function MenuDetailPage({ isNewPage, pageTitle, saving, menuForm, menuDetail, childForms, subMenus, authCodes, useeYsnoCodes, permission, onMovePath, onChangeMenuForm, onChangeChildForm, onSubmit, onAddChildForm, onSaveAllChildMenus, onDelete }: MenuDetailPageProps) {
  return (
    <section className="menu-detail-page">
      <section className="content-header">
        <h1>{pageTitle}</h1>
        <div className="header-actions">
          {!isNewPage && permission.deltYn && <button type="button" className="delete-button" onClick={() => onDelete({ ...menuForm, sortOrdr: Number(menuForm.sortOrdr), regiAdmn: null, regiAdmnName: null, regiDate: null, updtAdmn: null, updtAdmnName: null, updtDate: null })}>삭제</button>}
          <button type="button" className="subtle-button" onClick={() => onMovePath(MENU_LIST_PATH)}>목록</button>
        </div>
      </section>

      <form className="detail-panel" onSubmit={onSubmit}>
        <div className="detail-title">
          <div>
            <h2>{isNewPage ? '메뉴 등록' : '메뉴 정보'}</h2>
            <p>메뉴 기본 정보와 권한을 설정합니다.</p>
          </div>
        </div>
        <MenuFormTable form={menuForm} menuDetail={menuDetail} authCodes={authCodes} useeYsnoCodes={useeYsnoCodes} onChange={onChangeMenuForm} />
        {permission.writYn && <div className="section-actions"><button type="submit" disabled={saving}>{saving ? '저장 중' : isNewPage ? '저장' : '수정'}</button></div>}
      </form>

      {!isNewPage && menuForm.subxNumb === '0' && (
        <>
          <section className="detail-panel">
            <div className="detail-title">
              <div>
                <h2>하위메뉴</h2>
                <p>등록된 하위메뉴 목록입니다.</p>
              </div>
            </div>
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
                  {subMenus.length === 0 ? (
                    <tr className="empty-row">
                      <td colSpan={permission.deltYn ? 7 : 6}>하위메뉴가 없습니다.</td>
                    </tr>
                  ) : (
                    subMenus.map((menu) => (
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
                    ))
                  )}
                </tbody>
              </table>
            </section>
          </section>

          <section className="detail-panel">
            <div className="detail-title">
              <div>
                <h2>하위메뉴 등록</h2>
                <p>추가할 하위메뉴를 여러 개 입력한 뒤 한번에 저장합니다.</p>
              </div>
              {permission.writYn && <button type="button" className="subtle-button" onClick={onAddChildForm}>하위메뉴 추가</button>}
            </div>
            {childForms.length > 0 ? (
              <>
                <section className="table-wrap menu-edit-table">
                  <table>
                    <thead>
                      <tr>
                        <th>메뉴명</th>
                        <th>URL</th>
                        <th className="col-usee">사용여부</th>
                        <th>조회권한</th>
                        <th>쓰기권한</th>
                        <th>삭제권한</th>
                        <th className="col-sort">정렬</th>
                      </tr>
                    </thead>
                    <tbody>
                      {childForms.map((form, index) => (
                        <tr key={index} className="editable-row">
                          <MenuTableCells form={form} authCodes={authCodes} useeYsnoCodes={useeYsnoCodes} onChange={(field, value) => onChangeChildForm(index, field, value)} />
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </section>
                <div className="section-actions"><button type="button" disabled={saving} onClick={onSaveAllChildMenus}>저장</button></div>
              </>
            ) : (
              <div className="empty small">추가할 하위메뉴가 없습니다.</div>
            )}
          </section>
        </>
      )}
    </section>
  )
}

type MenuFormTableProps = {
  form: MenuForm
  menuDetail?: Menu | null
  authCodes: Code[]
  useeYsnoCodes: Code[]
  onChange: (field: keyof MenuForm, value: string) => void
}

/**
 * 메뉴 기본정보 입력 표
 * @Author SeungHyeon.Kang
 * @param form
 * @param menuDetail
 * @param authCodes
 * @param useeYsnoCodes
 * @param onChange
 * @return
 */
function MenuFormTable({ form, menuDetail, authCodes, useeYsnoCodes, onChange }: MenuFormTableProps) {
  return (
    <section className="table-wrap menu-info-table">
      <table>
        <tbody>
          <tr>
            <th>메뉴명</th>
            <td><input value={form.menuName} onChange={(event) => onChange('menuName', event.target.value)} required /></td>
            <th>URL</th>
            <td><input value={form.menuUrlx} onChange={(event) => onChange('menuUrlx', event.target.value)} required /></td>
            <th>사용여부</th>
            <td>
              <select value={form.useeYsno} onChange={(event) => onChange('useeYsno', event.target.value)}>
                {useeYsnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
              </select>
            </td>
          </tr>
          <tr>
            <th>조회권한</th>
            <td><select value={form.readAuth} onChange={(event) => onChange('readAuth', event.target.value)}>{authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td>
            <th>쓰기권한</th>
            <td><select value={form.writAuth} onChange={(event) => onChange('writAuth', event.target.value)}>{authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td>
            <th>삭제권한</th>
            <td><select value={form.deltAuth} onChange={(event) => onChange('deltAuth', event.target.value)}>{authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td>
          </tr>
          <tr>
            <th>정렬</th>
            <td><input type="number" value={form.sortOrdr} onChange={(event) => onChange('sortOrdr', event.target.value)} required /></td>
            <th>수정자</th>
            <td className="readonly-cell">{menuDetail?.updtAdmnName ?? menuDetail?.updtAdmn ?? ''}</td>
            <th>수정일</th>
            <td className="readonly-cell">{formatDate(menuDetail?.updtDate ?? null)}</td>
          </tr>
        </tbody>
      </table>
    </section>
  )
}

/**
 * 메뉴 입력 표 셀 목록
 * @Author SeungHyeon.Kang
 * @param form
 * @param authCodes
 * @param useeYsnoCodes
 * @param onChange
 * @return
 */
function MenuTableCells({ form, authCodes, useeYsnoCodes, onChange }: MenuFormTableProps) {
  return (
    <>
      <td><input value={form.menuName} onChange={(event) => onChange('menuName', event.target.value)} required /></td>
      <td><input value={form.menuUrlx} onChange={(event) => onChange('menuUrlx', event.target.value)} required /></td>
      <td className="col-usee">
        <select value={form.useeYsno} onChange={(event) => onChange('useeYsno', event.target.value)}>
          {useeYsnoCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
        </select>
      </td>
      <td><select value={form.readAuth} onChange={(event) => onChange('readAuth', event.target.value)}>{authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td>
      <td><select value={form.writAuth} onChange={(event) => onChange('writAuth', event.target.value)}>{authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td>
      <td><select value={form.deltAuth} onChange={(event) => onChange('deltAuth', event.target.value)}>{authCodes.map((code) => <option key={code.comdCode} value={code.comdCode}>{code.comdName}</option>)}</select></td>
      <td className="col-sort"><input type="number" value={form.sortOrdr} onChange={(event) => onChange('sortOrdr', event.target.value)} required /></td>
    </>
  )
}
