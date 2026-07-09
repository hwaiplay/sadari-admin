import {Fragment, useState} from 'react'
import type {Code, CodeMaster, DetailCodeForm} from '../../types/code'
import type {Permission} from '../../types/common'
import {CODE_LIST_PATH} from '../../constants/routes'
import {formatDate} from '../../utils/code'

type CodeDetailPageProps = {
    selectedMaster: CodeMaster | null
    pageTitle: string
    masterEditForm: CodeMaster | null
    detailCodes: Code[]
    detailEditForms: DetailCodeForm[]
    detailForms: DetailCodeForm[]
    permission: Permission
    useeYsnoCodes: Code[]
    saving: boolean
    onMovePath: (path: string) => void
    onChangeMasterForm: (form: CodeMaster) => void
    onSaveMasterForm: () => void
    onAddDetailInput: () => void
    onChangeDetailEditForm: (index: number, field: keyof DetailCodeForm, value: string) => void
    onChangeDetailForm: (index: number, field: keyof DetailCodeForm, value: string) => void
    onSaveAllDetailEditCodes: () => void
    onSaveAllDetailCodes: () => void
}

export function CodeDetailPage({
                                   selectedMaster,
                                   pageTitle,
                                   masterEditForm,
                                   detailCodes,
                                   detailEditForms,
                                   detailForms,
                                   permission,
                                   useeYsnoCodes,
                                   saving,
                                   onMovePath,
                                   onChangeMasterForm,
                                   onSaveMasterForm,
                                   onAddDetailInput,
                                   onChangeDetailEditForm,
                                   onChangeDetailForm,
                                   onSaveAllDetailEditCodes,
                                   onSaveAllDetailCodes
                               }: CodeDetailPageProps) {
    const [openedEditRows, setOpenedEditRows] = useState<Set<string>>(new Set())
    const [openedNewRows, setOpenedNewRows] = useState<Set<number>>(new Set())

    if (!selectedMaster || !masterEditForm) return null

    const toggleEditRow = (comdCode: string) => {
        const nextRows = new Set(openedEditRows)
        if (nextRows.has(comdCode)) nextRows.delete(comdCode)
        else nextRows.add(comdCode)
        setOpenedEditRows(nextRows)
    }

    const toggleNewRow = (index: number) => {
        const nextRows = new Set(openedNewRows)
        if (nextRows.has(index)) nextRows.delete(index)
        else nextRows.add(index)
        setOpenedNewRows(nextRows)
    }

    return (
        <section className="code-detail">
            <section className="content-header">
                <div>
                    <h1>{pageTitle}</h1>
                </div>
                <div className="header-actions">
                    <button type="button" className="subtle-button" onClick={() => onMovePath(CODE_LIST_PATH)}>목록
                    </button>
                </div>
            </section>

            <section className="detail-panel">
                <div className="detail-title">
                    <div>
                        <h2>공통코드</h2>
                        <p>공통코드는 코드값을 제외한 항목만 수정할 수 있습니다.</p>
                    </div>
                </div>
                <section className="table-wrap code-edit-table">
                    <table>
                        <thead>
                        <tr>
                            <th>공통코드</th>
                            <th>공통코드명</th>
                            <th>설명</th>
                            <th className="col-usee">사용여부</th>
                            <th>수정자</th>
                            <th>수정일</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr className="editable-row">
                            <td><input value={masterEditForm.commCode} readOnly/></td>
                            <td><input value={masterEditForm.codeName} onChange={(event) => onChangeMasterForm({
                                ...masterEditForm,
                                codeName: event.target.value
                            })} readOnly={!permission.writYn}/></td>
                            <td><input value={masterEditForm.codeExpl ?? ''} onChange={(event) => onChangeMasterForm({
                                ...masterEditForm,
                                codeExpl: event.target.value
                            })} readOnly={!permission.writYn}/></td>
                            <td className="col-usee">
                                <select value={masterEditForm.useeYsno ?? 'Y'} onChange={(event) => onChangeMasterForm({
                                    ...masterEditForm,
                                    useeYsno: event.target.value
                                })} disabled={!permission.writYn}>
                                    {useeYsnoCodes.map((code) => <option key={code.comdCode}
                                                                         value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                                </select>
                            </td>
                            <td>{selectedMaster.updtAdmnName ?? selectedMaster.updtAdmn}</td>
                            <td>{formatDate(selectedMaster.updtDate)}</td>
                        </tr>
                        </tbody>
                    </table>
                </section>
                {permission.writYn && <div className="section-actions">
                    <button type="button" disabled={saving} onClick={onSaveMasterForm}>수정</button>
                </div>}
            </section>

            <section className="detail-panel">
                <div className="detail-title">
                    <div>
                        <h2>세부코드</h2>
                        <p>기존 세부코드는 코드값을 제외한 항목을 수정할 수 있습니다.</p>
                    </div>
                </div>
                <section className="table-wrap code-edit-table">
                    <table>
                        <thead>
                        <tr>
                            <th>세부코드</th>
                            <th>세부코드명</th>
                            <th>설명</th>
                            <th className="col-sort">정렬</th>
                            <th className="col-usee">사용여부</th>
                            <th>수정자</th>
                            <th>수정일</th>
                            <th className="col-action">확장</th>
                        </tr>
                        </thead>
                        <tbody>
                        {detailCodes.map((detail, index) => {
                            const form = detailEditForms[index]
                            if (!form) return null
                            const expanded = openedEditRows.has(detail.comdCode)
                            return (
                                <Fragment key={detail.comdCode}>
                                    <tr className="editable-row">
                                        <td><input value={form.comdCode} readOnly/></td>
                                        <td><input value={form.comdName}
                                                   onChange={(event) => onChangeDetailEditForm(index, 'comdName', event.target.value)}
                                                   readOnly={!permission.writYn}/></td>
                                        <td><input value={form.codeExpl}
                                                   onChange={(event) => onChangeDetailEditForm(index, 'codeExpl', event.target.value)}
                                                   readOnly={!permission.writYn}/></td>
                                        <td className="col-sort"><input type="number" value={form.sortOrder}
                                                                        onChange={(event) => onChangeDetailEditForm(index, 'sortOrder', event.target.value)}
                                                                        readOnly={!permission.writYn}/></td>
                                        <td className="col-usee">
                                            <select value={form.useeYsno}
                                                    onChange={(event) => onChangeDetailEditForm(index, 'useeYsno', event.target.value)}
                                                    disabled={!permission.writYn}>
                                                {useeYsnoCodes.map((code) => <option key={code.comdCode}
                                                                                     value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                                            </select>
                                        </td>
                                        <td>{detail.updtAdmnName ?? detail.updtAdmn}</td>
                                        <td>{formatDate(detail.updtDate)}</td>
                                        <td className="col-action">
                                            <button type="button" className="icon-toggle-button"
                                                    aria-label={expanded ? '접기' : '펼치기'} title={expanded ? '접기' : '펼치기'}
                                                    onClick={() => toggleEditRow(detail.comdCode)}>
                                                {expanded ? <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
                                                                 xmlns="http://www.w3.org/2000/svg">
                                                        <path
                                                            d="M19.9201 15.0499L13.4001 8.52989C12.6301 7.75989 11.3701 7.75989 10.6001 8.52989L4.08008 15.0499"
                                                            stroke="#292D32" stroke-width="1.5" stroke-miterlimit="10"
                                                            stroke-linecap="round" stroke-linejoin="round"/>
                                                    </svg>
                                                    : <svg width="24" height="24" viewBox="0 0 24 24" fill="none"
                                                           xmlns="http://www.w3.org/2000/svg">
                                                        <path
                                                            d="M19.9201 8.94995L13.4001 15.47C12.6301 16.24 11.3701 16.24 10.6001 15.47L4.08008 8.94995"
                                                            stroke="#292D32" stroke-width="1.5" stroke-miterlimit="10"
                                                            stroke-linecap="round" stroke-linejoin="round"/>
                                                    </svg>
                                                }
                                            </button>
                                        </td>
                                    </tr>
                                    {expanded && <ExtensionRow form={form} index={index} disabled={!permission.writYn}
                                                               colSpan={8} onChange={onChangeDetailEditForm}/>}
                                </Fragment>
                            )
                        })}
                        </tbody>
                    </table>
                </section>
                {permission.writYn && <div className="section-actions">
                    <button type="button" disabled={saving || detailEditForms.length === 0}
                            onClick={onSaveAllDetailEditCodes}>수정
                    </button>
                </div>}
            </section>

            <section className="detail-panel">
                <div className="detail-title">
                    <div>
                        <h2>세부코드 추가</h2>
                        <p>추가할 세부코드를 여러 개 입력한 뒤 한번에 저장합니다.</p>
                    </div>
                    {permission.writYn &&
                        <button type="button" className="subtle-button" onClick={onAddDetailInput}>세부코드 추가</button>}
                </div>
                {detailForms.length > 0 ? (
                    <>
                        <section className="table-wrap code-edit-table new-code-table">
                            <table>
                                <thead>
                                <tr>
                                    <th>세부코드</th>
                                    <th>세부코드명</th>
                                    <th>설명</th>
                                    <th className="col-sort">정렬</th>
                                    <th className="col-usee">사용여부</th>
                                    <th className="col-action">확장</th>
                                </tr>
                                </thead>
                                <tbody>
                                {detailForms.map((form, index) => {
                                    const expanded = openedNewRows.has(index)
                                    return (
                                        <Fragment key={index}>
                                            <tr className="editable-row">
                                                <td><input value={form.comdCode}
                                                           onChange={(event) => onChangeDetailForm(index, 'comdCode', event.target.value)}/>
                                                </td>
                                                <td><input value={form.comdName}
                                                           onChange={(event) => onChangeDetailForm(index, 'comdName', event.target.value)}/>
                                                </td>
                                                <td><input value={form.codeExpl}
                                                           onChange={(event) => onChangeDetailForm(index, 'codeExpl', event.target.value)}/>
                                                </td>
                                                <td className="col-sort"><input type="number" value={form.sortOrder}
                                                                                onChange={(event) => onChangeDetailForm(index, 'sortOrder', event.target.value)}/>
                                                </td>
                                                <td className="col-usee">
                                                    <select value={form.useeYsno}
                                                            onChange={(event) => onChangeDetailForm(index, 'useeYsno', event.target.value)}>
                                                        {useeYsnoCodes.map((code) => <option key={code.comdCode}
                                                                                             value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                                                    </select>
                                                </td>
                                                <td className="col-action">
                                                    <button type="button" className="icon-toggle-button"
                                                            aria-label={expanded ? '접기' : '펼치기'}
                                                            title={expanded ? '접기' : '펼치기'}
                                                            onClick={() => toggleNewRow(index)}>
                                                        {expanded ? '∧' : '∨'}
                                                    </button>
                                                </td>
                                            </tr>
                                            {expanded &&
                                                <ExtensionRow form={form} index={index} disabled={false} colSpan={6}
                                                              onChange={onChangeDetailForm}/>}
                                        </Fragment>
                                    )
                                })}
                                </tbody>
                            </table>
                        </section>
                        <div className="section-actions">
                            <button type="button" disabled={saving} onClick={onSaveAllDetailCodes}>저장</button>
                        </div>
                    </>
                ) : (
                    <div className="empty small">추가할 세부코드가 없습니다.</div>
                )}
            </section>
        </section>
    )
}

type ExtensionRowProps = {
    form: DetailCodeForm
    index: number
    disabled: boolean
    colSpan: number
    onChange: (index: number, field: keyof DetailCodeForm, value: string) => void
}

function ExtensionRow({form, index, disabled, colSpan, onChange}: ExtensionRowProps) {
    return (
        <tr className="extension-row">
            <td colSpan={colSpan}>
                <table className="extension-info-table">
                    <tbody>
                    <tr>
                        <th>확장1 코드</th>
                        <td><input value={form.opt1Code}
                                   onChange={(event) => onChange(index, 'opt1Code', event.target.value)}
                                   readOnly={disabled}/></td>
                        <th>확장1 명</th>
                        <td><input value={form.opt1Name}
                                   onChange={(event) => onChange(index, 'opt1Name', event.target.value)}
                                   readOnly={disabled}/></td>
                    </tr>
                    <tr>
                        <th>확장2 코드</th>
                        <td><input value={form.opt2Code}
                                   onChange={(event) => onChange(index, 'opt2Code', event.target.value)}
                                   readOnly={disabled}/></td>
                        <th>확장2 명</th>
                        <td><input value={form.opt2Name}
                                   onChange={(event) => onChange(index, 'opt2Name', event.target.value)}
                                   readOnly={disabled}/></td>
                    </tr>
                    <tr>
                        <th>확장3 코드</th>
                        <td><input value={form.opt3Code}
                                   onChange={(event) => onChange(index, 'opt3Code', event.target.value)}
                                   readOnly={disabled}/></td>
                        <th>확장3 명</th>
                        <td><input value={form.opt3Name}
                                   onChange={(event) => onChange(index, 'opt3Name', event.target.value)}
                                   readOnly={disabled}/></td>
                    </tr>
                    <tr>
                        <th>확장4 코드</th>
                        <td><input value={form.opt4Code}
                                   onChange={(event) => onChange(index, 'opt4Code', event.target.value)}
                                   readOnly={disabled}/></td>
                        <th>확장4 명</th>
                        <td><input value={form.opt4Name}
                                   onChange={(event) => onChange(index, 'opt4Name', event.target.value)}
                                   readOnly={disabled}/></td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
    )
}
