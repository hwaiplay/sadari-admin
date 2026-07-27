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
import type {UserMenu, UserMenuForm} from '../../types/userMenu'
import {formatDate, getUseeYsnoCodeName} from '../../utils/code'
import {AuditInfoTable} from '../../components/AuditInfoTable'
import {useMenuPermission} from '../../contexts/MenuPermissionContext'

type UserMenuManagePageProps = {
    currentPath: string
    onMovePath: (path: string) => void
    onError: (message: string | null) => void
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
    const [detail, setDetail] = useState<UserMenu | null>(null)
    const [subMenus, setSubMenus] = useState<UserMenu[]>([])
    const [form, setForm] = useState<UserMenuForm>(emptyUserMenuForm())
    const [childForms, setChildForms] = useState<UserMenuForm[]>([])
    const [ysnoCodes, setYsnoCodes] = useState<Code[]>([])
    const [saving, setSaving] = useState(false)

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
                if (isList) {
                    setRows(await getUserMenus())
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
                }
            } catch (error: unknown) {
                onError(error instanceof Error ? error.message : '사용자 메뉴 조회 중 오류가 발생했습니다.')
            }
        }
        void load()
    }, [currentPath])

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

    /** 사용자 메뉴 저장 */
    const saveMenu = async (event?: FormEvent<HTMLFormElement>) => {
        event?.preventDefault()
        if (!form.menuName.trim() || !form.menuUrlx.trim()) {
            alert('메뉴명과 URL을 입력해 주세요.')
            return
        }
        if (childForms.some((child) => !child.menuName.trim() || !child.menuUrlx.trim())) {
            alert('하위메뉴명과 URL을 입력해 주세요.')
            return
        }
        setSaving(true)
        onError(null)
        try {
            const result = await saveUserMenuApi(form, Boolean(detailKey))
            await Promise.all(childForms.map((child) => saveUserMenuApi(child, false)))
            alert(result.message)
            if (detailKey) {
                setChildForms([])
                setSubMenus(await getUserSubMenus(form.menuNumb))
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
            if (detailKey) onMovePath(USER_MENU_LIST_PATH)
            else setRows(await getUserMenus())
        } catch (error: unknown) {
            onError(error instanceof Error ? error.message : '사용자 메뉴 삭제 중 오류가 발생했습니다.')
        }
    }

    if (isList) {
        return (
            <section className="menu-manage">
                <section className="content-header"><h1>사용자 메뉴관리</h1>
                    <div className="status">총 {rows.length}건</div>
                </section>
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
                            <div><h2>하위메뉴</h2><p>등록된 사용자 하위메뉴 목록입니다.</p></div>
                        </div>
                        <UserSubMenuTable menus={subMenus} ysnoCodes={ysnoCodes} canDelete={permission.deltYsno === 'Y'}
                                          onMovePath={onMovePath} onDelete={deleteMenu}/>
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
                                        </tr>
                                        </thead>
                                        <tbody>{childForms.map((child, index) => <UserMenuInputRow key={index}
                                                                                                   form={child}
                                                                                                   ysnoCodes={ysnoCodes}
                                                                                                   onChange={(field, value) => changeChildForm(index, field, value)}/>)}</tbody>
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

type FormProps = { form: UserMenuForm; ysnoCodes: Code[]; onChange: (field: keyof UserMenuForm, value: string) => void }

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
function UserMenuInputRow({form, ysnoCodes, onChange}: FormProps) {
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
    </tr>
}

/** 사용자 하위 메뉴 목록 표 */
function UserSubMenuTable({menus, ysnoCodes, canDelete, onMovePath, onDelete}: {
    menus: UserMenu[];
    ysnoCodes: Code[];
    canDelete: boolean;
    onMovePath: (path: string) => void;
    onDelete: (menu: Pick<UserMenu, 'menuNumb' | 'subxNumb'>) => Promise<void>
}) {
    return <section className="table-wrap menu-list-table">
        <table>
            <thead>
            <tr>
                <th>메뉴명</th>
                <th>URL</th>
                <th className="col-usee">햄버거 메뉴 노출</th>
                <th className="col-usee">사용여부</th>
                <th className="col-sort">정렬</th>
                <th className="col-action">삭제</th></tr>
            </thead>
            <tbody>{menus.length === 0 ? <tr className="empty-row">
                <td colSpan={6}>하위메뉴가 없습니다.</td>
            </tr> : menus.map((menu) => <tr key={`${menu.menuNumb}-${menu.subxNumb}`}
                                            onClick={() => onMovePath(`${USER_MENU_DETAIL_PREFIX}/${menu.menuNumb}/${menu.subxNumb}`)}>
                <td>{menu.menuName}</td>
                <td>{menu.menuUrlx}</td>
                <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.showYsno, menu.showYsnoName)}</td>
                <td className="col-usee">{getUseeYsnoCodeName(ysnoCodes, menu.useeYsno, menu.useeYsnoName)}</td>
                <td className="col-sort">{menu.showYsno === DEFAULT_USEE_YSNO ? menu.sortOrdr : ''}</td>
                <td className="col-action">
                    {canDelete && <button type="button" className="delete-button" onClick={(event) => {
                        event.stopPropagation();
                        void onDelete(menu)
                    }}>삭제
                    </button>}
                </td></tr>)}</tbody>
        </table>
    </section>
}

/** 여부 코드 셀렉트박스 */
function YsnoSelect({value, codes, onChange}: { value: string; codes: Code[]; onChange: (value: string) => void }) {
    return <select value={value} onChange={(event) => onChange(event.target.value)}>{codes.map((code) => <option
        key={code.comdCode} value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}</select>
}
