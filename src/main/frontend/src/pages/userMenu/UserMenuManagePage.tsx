import {useEffect, useMemo, useState} from 'react'
import type {FormEvent} from 'react'
import {getCodeList} from '../../api/codeApi'
import {
    deleteUserMenuApi,
    getUserMenuDetail,
    getUserMenus,
    getUserSubMenus,
    saveUserMenuApi
} from '../../api/userMenuApi'
import {COMM_YSNO, DEFAULT_USEE_YSNO} from '../../constants/codes'
import {USER_MENU_DETAIL_PREFIX, USER_MENU_LIST_PATH, USER_MENU_NEW_PATH} from '../../constants/routes'
import type {Code} from '../../types/code'
import type {UserMenu, UserMenuForm, UserMenuSearch} from '../../types/userMenu'
import {formatDate, getUseeYsnoCodeName} from '../../utils/code'
import {AuditInfoTable} from '../../components/AuditInfoTable'
import {useMenuPermission} from '../../contexts/useMenuPermission'
import {Pagination} from '../../components/Pagination'
import type {PageData} from '../../types/common'

type UserMenuManagePageProps = {
    currentPath: string
    onMovePath: (path: string) => void
    onError: (message: string | null) => void
}

const DEFAULT_SEARCH: UserMenuSearch = {
    keyword: '',
    showYsno: '',
    useeYsno: '',
}

/** 빈 사용자 메뉴 입력 폼 생성 */
const emptyUserMenuForm = (menuNumb = ''): UserMenuForm => ({
    menuNumb,
    subxNumb: '',
    menuName: '',
    menuUrlx: '/',
    sortOrdr: '1',
    showYsno: DEFAULT_USEE_YSNO,
    useeYsno: DEFAULT_USEE_YSNO,
})

/** 조회 사용자 메뉴를 입력 폼으로 변환 */
const toUserMenuForm = (menu: UserMenu): UserMenuForm => ({
    menuNumb: menu.menuNumb,
    subxNumb: menu.subxNumb,
    menuName: menu.menuName,
    menuUrlx: menu.menuUrlx,
    sortOrdr: menu.showYsno === DEFAULT_USEE_YSNO ? String(menu.sortOrdr ?? 1) : '',
    showYsno: menu.showYsno ?? DEFAULT_USEE_YSNO,
    useeYsno: menu.useeYsno ?? DEFAULT_USEE_YSNO,
})

/** 사용자 메뉴관리 목록 상세 등록 화면 */
export function UserMenuManagePage({currentPath, onMovePath, onError}: UserMenuManagePageProps) {
    const permission = useMenuPermission()
    const [rows, setRows] = useState<UserMenu[]>([])
    const [pageData, setPageData] = useState<PageData<UserMenu>>({items: [], totalCount: 0, pageNumber: 1, pageSize: 20, totalPages: 0})
    const [detail, setDetail] = useState<UserMenu | null>(null)
    const [subMenus, setSubMenus] = useState<UserMenu[]>([])
    const [form, setForm] = useState<UserMenuForm>(emptyUserMenuForm())
    const [subMenuEditForms, setSubMenuEditForms] = useState<UserMenuForm[]>([])
    const [childForms, setChildForms] = useState<UserMenuForm[]>([])
    const [ysnoCodes, setYsnoCodes] = useState<Code[]>([])
    const [saving, setSaving] = useState(false)
    const [search, setSearch] = useState<UserMenuSearch>(DEFAULT_SEARCH)
    const [appliedSearch, setAppliedSearch] = useState<UserMenuSearch>(DEFAULT_SEARCH)

    const detailKey = useMemo(() => {
        if (!currentPath.startsWith(USER_MENU_DETAIL_PREFIX)) return null
        const [, , , , , menuNumb, subxNumb] = currentPath.split('/')
        return menuNumb && subxNumb ? {menuNumb, subxNumb} : null
    }, [currentPath])
    const isList = currentPath === USER_MENU_LIST_PATH
    const isNew = currentPath === USER_MENU_NEW_PATH

    /** 현재 경로에 필요한 사용자 메뉴 데이터를 조회 */
    useEffect(() => {
        const load = async () => {
            onError(null)
            try {
                const codeResult = await getCodeList(COMM_YSNO)
                setYsnoCodes(codeResult)
                setChildForms([])
                setSubMenuEditForms([])
                if (isList) {
                    const result = await getUserMenus(1, DEFAULT_SEARCH)
                    setPageData(result)
                    setRows(result.items)
                    setSearch(DEFAULT_SEARCH)
                    setAppliedSearch(DEFAULT_SEARCH)
                    setDetail(null)
                    return
                }
                if (isNew) {
                    setDetail(null)
                    setSubMenus([])
                    setForm(emptyUserMenuForm())
                    return
                }
                if (detailKey) {
                    const [menu, children] = await Promise.all([getUserMenuDetail(detailKey.menuNumb, detailKey.subxNumb), getUserSubMenus(detailKey.menuNumb)])
                    setDetail(menu)
                    setForm(toUserMenuForm(menu))
                    setSubMenus(children)
                    setSubMenuEditForms(children.map(toUserMenuForm))
                }
            } catch (error: unknown) {
                onError(error instanceof Error ? error.message : '사용자 메뉴 조회 중 오류가 발생했습니다.')
            }
        }
        void load()
    }, [currentPath, detailKey, isList, isNew, onError])

    /**
     * 지정한 검색 조건과 페이지로 사용자 메뉴 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param pageNumber 조회할 페이지 번호
     * @param targetSearch 적용할 사용자 메뉴 검색 조건
     * @return 반환값이 없다
     */
    const loadListPage = async (pageNumber: number, targetSearch: UserMenuSearch): Promise<void> => {
        const result = await getUserMenus(pageNumber, targetSearch)
        setPageData(result)
        setRows(result.items)
        setAppliedSearch(targetSearch)
    }

    /**
     * 입력한 사용자 메뉴 조건으로 첫 페이지를 검색한다
     *
     * @author SeungHyeon.Kang
     * @param event 사용자 메뉴 검색 폼 제출 이벤트
     * @return 반환값이 없다
     */
    const handleSearch = async (event: FormEvent<HTMLFormElement>): Promise<void> => {
        event.preventDefault()
        await loadListPage(1, search)
    }

    /**
     * 사용자 메뉴 검색 조건과 결과를 전체 목록으로 초기화한다
     *
     * @author SeungHyeon.Kang
     * @return 반환값이 없다
     */
    const handleSearchReset = async (): Promise<void> => {
        setSearch(DEFAULT_SEARCH)
        await loadListPage(1, DEFAULT_SEARCH)
    }

    /** 사용자 메뉴 입력값 변경 */
    const changeForm = (field: keyof UserMenuForm, value: string) => {
        // 햄버거 메뉴 노출 여부에 따라 정렬 입력값을 사용하거나 제거한다
        if (field === 'showYsno') {
            setForm({...form, showYsno: value, sortOrdr: value === DEFAULT_USEE_YSNO ? form.sortOrdr || '1' : ''})
            return
        }
        setForm({...form, [field]: value})
    }

    /** 하위 메뉴 입력값 변경 */
    const changeChildForm = (index: number, field: keyof UserMenuForm, value: string) => {
        setChildForms(childForms.map((child, childIndex) => {
            if (childIndex !== index) return child
            // 햄버거 메뉴 노출 여부에 따라 하위 메뉴 정렬 입력값을 사용하거나 제거한다
            if (field === 'showYsno') {
                return {...child, showYsno: value, sortOrdr: value === DEFAULT_USEE_YSNO ? child.sortOrdr || '1' : ''}
            }
            return {...child, [field]: value}
        }))
    }

    /** 등록된 하위 메뉴 입력값 변경 */
    const changeSubMenuEditForm = (index: number, field: keyof UserMenuForm, value: string) => {
        setSubMenuEditForms(subMenuEditForms.map((child, childIndex) => {
            if (childIndex !== index) return child
            // 햄버거 메뉴 노출 여부에 따라 등록된 하위 메뉴 정렬 입력값을 사용하거나 제거한다
            if (field === 'showYsno') {
                return {...child, showYsno: value, sortOrdr: value === DEFAULT_USEE_YSNO ? child.sortOrdr || '1' : ''}
            }
            return {...child, [field]: value}
        }))
    }

    /** 사용자 메뉴 저장 */
    const saveMenu = async (event?: FormEvent<HTMLFormElement>) => {
        event?.preventDefault()
        if (!form.menuName.trim() || !form.menuUrlx.trim()) {
            alert('메뉴명과 URL을 입력해 주세요.')
            return
        }
        const menuChildForms = form.subxNumb === '0' ? [...subMenuEditForms, ...childForms] : []
        if (menuChildForms.some((child) => !child.menuName.trim() || !child.menuUrlx.trim())) {
            alert('하위메뉴명과 URL을 입력해 주세요.')
            return
        }
        setSaving(true)
        onError(null)
        try {
            const result = await saveUserMenuApi(form, Boolean(detailKey))
            if (form.subxNumb === '0') {
                await Promise.all(subMenuEditForms.map((child) => saveUserMenuApi(child, true)))
            }
            await Promise.all(childForms.map((child) => saveUserMenuApi(child, false)))
            alert(result.message)
            if (detailKey) {
                const children = await getUserSubMenus(form.menuNumb)
                setChildForms([])
                setSubMenus(children)
                setSubMenuEditForms(children.map(toUserMenuForm))
            } else {
                onMovePath(`${USER_MENU_DETAIL_PREFIX}/${result.data.menuNumb}/${result.data.subxNumb}`)
            }
        } catch (error: unknown) {
            onError(error instanceof Error ? error.message : '사용자 메뉴 저장 중 오류가 발생했습니다.')
        } finally {
            setSaving(false)
        }
    }

    /** 사용자 메뉴 삭제 */
    const deleteMenu = async (menu: Pick<UserMenu, 'menuNumb' | 'subxNumb'>) => {
        try {
            const result = await deleteUserMenuApi(menu)
            alert(result.message)
            const isCurrentMenu = detailKey?.menuNumb === menu.menuNumb && detailKey.subxNumb === menu.subxNumb
            if (isCurrentMenu) {
                onMovePath(USER_MENU_LIST_PATH)
                return
            }
            if (detailKey && form.subxNumb === '0') {
                const children = await getUserSubMenus(form.menuNumb)
                setSubMenus(children)
                setSubMenuEditForms(children.map(toUserMenuForm))
                return
            }
            await loadListPage(pageData.pageNumber, appliedSearch)
        } catch (error: unknown) {
            onError(error instanceof Error ? error.message : '사용자 메뉴 삭제 중 오류가 발생했습니다.')
        }
    }

    if (isList) {
        return (
            <section className="menu-manage">
                <section className="content-header"><h1>사용자 메뉴관리</h1>
                    <div className="status">총 {pageData.totalCount}건</div>
                </section>
                <form className="list-search" onSubmit={(event) => void handleSearch(event)}>
                    <label>
                        <span>검색어</span>
                        <input value={search.keyword} placeholder="메뉴명 또는 URL"
                               onChange={(event) => setSearch({...search, keyword: event.target.value})}/>
                    </label>
                    <label>
                        <span>햄버거 메뉴 노출</span>
                        <select value={search.showYsno}
                                onChange={(event) => setSearch({...search, showYsno: event.target.value})}>
                            <option value="">전체</option>
                            {ysnoCodes.map((code) => <option key={code.comdCode}
                                                            value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                        </select>
                    </label>
                    <label>
                        <span>사용여부</span>
                        <select value={search.useeYsno}
                                onChange={(event) => setSearch({...search, useeYsno: event.target.value})}>
                            <option value="">전체</option>
                            {ysnoCodes.map((code) => <option key={code.comdCode}
                                                            value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                        </select>
                    </label>
                    <div className="list-search-actions">
                        <button type="button" className="subtle-button"
                                onClick={() => void handleSearchReset()}>초기화</button>
                        <button type="submit">검색</button>
                    </div>
                </form>
                <section className="table-wrap menu-list-table">
                    <table>
                        <thead>
                        <tr>
                            <th>메뉴명</th>
                            <th>URL</th>
                            <th className="col-usee">햄버거 메뉴 노출</th>
                            <th className="col-usee">사용여부</th>
                            <th className="col-sort">정렬</th>
                            <th>수정자</th>
                            <th>수정일</th>
                            <th className="col-action">삭제</th></tr>
                        </thead>
                        <tbody>
                        {rows.length === 0 ? <tr className="empty-row">
                            <td colSpan={8}>등록된 사용자 메뉴가 없습니다.</td>
                        </tr> : rows.map((menu) => (
                            <tr key={`${menu.menuNumb}-${menu.subxNumb}`}
                                onClick={() => onMovePath(`${USER_MENU_DETAIL_PREFIX}/${menu.menuNumb}/${menu.subxNumb}`)}>
                                <td>{menu.menuName}</td>
                                <td>{menu.menuUrlx}</td>
                                <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.showYsno, menu.showYsnoName)}</td>
                                <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.useeYsno, menu.useeYsnoName)}</td>
                                <td className="col-sort">{menu.showYsno === DEFAULT_USEE_YSNO ? menu.sortOrdr : ''}</td>
                                <td>{menu.updtAdmnName ?? menu.updtAdmn}</td>
                                <td>{formatDate(menu.updtDate)}</td>
                                <td className="col-action">
                                    {permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={(event) => {
                                        event.stopPropagation();
                                        void deleteMenu(menu)
                                    }}>삭제
                                    </button>}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </section>
                <Pagination pageNumber={pageData.pageNumber} totalPages={pageData.totalPages}
                            onPageChange={(pageNumber) => void loadListPage(pageNumber, appliedSearch)}/>
                {permission.writYsno === 'Y' && <button type="button" className="floating-button"
                        onClick={() => onMovePath(USER_MENU_NEW_PATH)}>등록</button>}
            </section>
        )
    }

    return (
        <section className="menu-detail-page">
            <section className="content-header">
                <h1>{isNew ? '사용자 메뉴 등록' : '사용자 메뉴관리 상세'}</h1>
            </section>
            <form className="detail-panel" onSubmit={saveMenu}>
                <div className="detail-title">
                    <div><h2>{isNew ? '사용자 메뉴 등록' : '사용자 메뉴 정보'}</h2><p>사용자 화면에 제공할 메뉴 정보를 설정합니다.</p></div>
                </div>
                <UserMenuFormTable form={form} ysnoCodes={ysnoCodes}
                                   onChange={changeForm}/>
            </form>
            {!isNew && form.subxNumb === '0' && (
                <>
                    <section className="detail-panel">
                        <div className="detail-title">
                            <div><h2>하위메뉴</h2><p>등록된 사용자 하위메뉴를 상세 이동 없이 바로 수정합니다.</p></div>
                        </div>
                        <UserSubMenuEditTable menus={subMenus} forms={subMenuEditForms}
                                              ysnoCodes={ysnoCodes} canDelete={permission.deltYsno === 'Y'}
                                              onChange={changeSubMenuEditForm} onDelete={deleteMenu}/>
                    </section>
                    <section className="detail-panel">
                        <div className="detail-title">
                            <div><h2>하위메뉴 등록</h2><p>추가할 하위메뉴를 입력한 뒤 한번에 저장합니다.</p></div>
                            {permission.writYsno === 'Y' && <button type="button" className="subtle-button"
                                    onClick={() => setChildForms([...childForms, emptyUserMenuForm(form.menuNumb)])}>하위메뉴
                                추가</button>}</div>
                        {childForms.length === 0 ? <div className="empty small">추가할 하위메뉴가 없습니다.</div> : (
                            <>
                                <section className="table-wrap menu-edit-table">
                                    <table>
                                        <thead>
                                        <tr>
                                            <th>메뉴명</th>
                                            <th>URL</th>
                                            <th className="col-usee">햄버거 메뉴 노출</th>
                                            <th className="col-usee">사용여부</th>
                                            <th className="col-sort">정렬</th>
                                            <th className="col-action">삭제</th>
                                        </tr>
                                        </thead>
                                        <tbody>{childForms.map((child, index) => <UserMenuInputRow key={index}
                                                                                                   form={child}
                                                                                                   ysnoCodes={ysnoCodes}
                                                                                                   onChange={(field, value) => changeChildForm(index, field, value)}
                                                                                                   onRemove={() => setChildForms(childForms.filter((_, childIndex) => childIndex !== index))}/>)}</tbody>
                                    </table>
                                </section>
                            </>
                        )}
                    </section>
                </>
            )}
            {!isNew && detail && <AuditInfoTable regiAdmn={detail.regiAdmn} regiAdmnName={detail.regiAdmnName} regiDate={detail.regiDate} updtAdmn={detail.updtAdmn} updtAdmnName={detail.updtAdmnName} updtDate={detail.updtDate}/>}
            <div className="detail-footer">
                <div className="detail-footer-left">
                    <button type="button" className="subtle-button" onClick={() => onMovePath(USER_MENU_LIST_PATH)}>목록</button>
                    {!isNew && detail && permission.deltYsno === 'Y' && <button type="button" className="delete-button" onClick={() => void deleteMenu(detail)}>삭제</button>}
                </div>
                <div className="detail-footer-right">
                    {permission.writYsno === 'Y' && <button type="button" disabled={saving} onClick={() => void saveMenu()}>{saving ? '저장 중' : isNew ? '저장' : '수정'}</button>}
                </div>
            </div>
        </section>
    )
}

type FormProps = {
    form: UserMenuForm
    ysnoCodes: Code[]
    onChange: (field: keyof UserMenuForm, value: string) => void
    onRemove?: () => void
}

/** 사용자 메뉴 기본정보 입력 표 */
function UserMenuFormTable({form, ysnoCodes, onChange}: FormProps) {
    return <section className="table-wrap menu-info-table">
        <table>
            <tbody>
            <tr>
                <th>메뉴명</th>
                <td><input value={form.menuName}
                                       onChange={(event) => onChange('menuName', event.target.value)} required/></td>
                <th>URL</th>
                <td><input value={form.menuUrlx}
                                       onChange={(event) => onChange('menuUrlx', event.target.value)} required/></td>
                <th>사용여부</th>
                <td><YsnoSelect value={form.useeYsno} codes={ysnoCodes}
                                onChange={(value) => onChange('useeYsno', value)}/></td>
            </tr>
            <tr>
                <th>햄버거 메뉴 노출</th>
                <td><YsnoSelect value={form.showYsno} codes={ysnoCodes}
                                onChange={(value) => onChange('showYsno', value)}/></td>
                {form.showYsno === DEFAULT_USEE_YSNO ? (
                    <>
                        <th>정렬</th>
                        <td><input type="number" min="1" value={form.sortOrdr}
                                   onChange={(event) => onChange('sortOrdr', event.target.value)} required/></td>
                        <td colSpan={2}/>
                    </>
                ) : <td colSpan={4}/>}
            </tr>
            </tbody>
        </table>
    </section>
}

/** 사용자 하위 메뉴 입력 행 */
function UserMenuInputRow({form, ysnoCodes, onChange, onRemove}: FormProps) {
    return <tr className="editable-row">
        <td><input value={form.menuName} onChange={(event) => onChange('menuName', event.target.value)} required/></td>
        <td><input value={form.menuUrlx} onChange={(event) => onChange('menuUrlx', event.target.value)} required/></td>
        <td className="col-usee"><YsnoSelect value={form.showYsno} codes={ysnoCodes}
                                             onChange={(value) => onChange('showYsno', value)}/></td>
        <td className="col-usee"><YsnoSelect value={form.useeYsno} codes={ysnoCodes}
                                             onChange={(value) => onChange('useeYsno', value)}/></td>
        <td className="col-sort">{form.showYsno === DEFAULT_USEE_YSNO &&
            <input type="number" min="1" value={form.sortOrdr}
                   onChange={(event) => onChange('sortOrdr', event.target.value)} required/>}</td>
        {onRemove && <td className="col-action">
            <button type="button" className="delete-button" onClick={onRemove}>삭제</button>
        </td>}
    </tr>
}

/** 사용자 하위 메뉴 인라인 수정 표 */
function UserSubMenuEditTable({menus, forms, ysnoCodes, canDelete, onChange, onDelete}: {
    menus: UserMenu[];
    forms: UserMenuForm[];
    ysnoCodes: Code[];
    canDelete: boolean;
    onChange: (index: number, field: keyof UserMenuForm, value: string) => void;
    onDelete: (menu: Pick<UserMenu, 'menuNumb' | 'subxNumb'>) => Promise<void>
}) {
    return <section className="table-wrap menu-edit-table">
        <table>
            <thead>
            <tr>
                <th>메뉴명</th>
                <th>URL</th>
                <th className="col-usee">햄버거 메뉴 노출</th>
                <th className="col-usee">사용여부</th>
                <th className="col-sort">정렬</th>
                <th>수정자</th>
                <th className="col-datetime">수정일</th>
                <th className="col-action">삭제</th></tr>
            </thead>
            <tbody>{menus.length === 0 ? <tr className="empty-row">
                <td colSpan={8}>하위메뉴가 없습니다.</td>
            </tr> : menus.map((menu, index) => {
                const editForm = forms[index]
                if (!editForm) return null
                return <tr className="editable-row" key={`${menu.menuNumb}-${menu.subxNumb}`}>
                    <td><input value={editForm.menuName}
                               onChange={(event) => onChange(index, 'menuName', event.target.value)} required/></td>
                    <td><input value={editForm.menuUrlx}
                               onChange={(event) => onChange(index, 'menuUrlx', event.target.value)} required/></td>
                    <td className="col-usee"><YsnoSelect value={editForm.showYsno} codes={ysnoCodes}
                                                         onChange={(value) => onChange(index, 'showYsno', value)}/></td>
                    <td className="col-usee"><YsnoSelect value={editForm.useeYsno} codes={ysnoCodes}
                                                         onChange={(value) => onChange(index, 'useeYsno', value)}/></td>
                    <td className="col-sort">{editForm.showYsno === DEFAULT_USEE_YSNO &&
                        <input type="number" min="1" value={editForm.sortOrdr}
                               onChange={(event) => onChange(index, 'sortOrdr', event.target.value)} required/>}</td>
                    <td>{menu.updtAdmnName ?? menu.updtAdmn}</td>
                    <td className="col-datetime">{formatDate(menu.updtDate)}</td>
                    <td className="col-action">
                        {canDelete && <button type="button" className="delete-button"
                                              onClick={() => void onDelete(menu)}>삭제</button>}
                    </td>
                </tr>
            })}</tbody>
        </table>
    </section>
}

/** 여부 코드 셀렉트박스 */
function YsnoSelect({value, codes, onChange}: { value: string; codes: Code[]; onChange: (value: string) => void }) {
    return <select value={value} onChange={(event) => onChange(event.target.value)}>{codes.map((code) => <option
        key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}</select>
}
