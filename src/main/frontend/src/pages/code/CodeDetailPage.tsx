import {Fragment, useState} from 'react'
import type {Code, CodeMaster, DetailCodeForm} from '../../types/code'
import {CODE_DETAIL_PREFIX, CODE_LIST_PATH} from '../../constants/routes'
import {formatDate} from '../../utils/code'
import {AuditInfoTable} from '../../components/AuditInfoTable'
import {useMenuPermission} from '../../contexts/useMenuPermission'

type CodeDetailPageProps = {
    selectedMaster: CodeMaster | null
    pageTitle: string
    masterEditForm: CodeMaster | null
    selectedDetailCode: string
    detailCodes: Code[]
    detailEditForms: DetailCodeForm[]
    detailForms: DetailCodeForm[]
    useeYsnoCodes: Code[]
    saving: boolean
    onMovePath: (path: string) => void
    onChangeMasterForm: (form: CodeMaster) => void
    onAddDetailInput: () => void
    onRemoveDetailInput: (index: number) => void
    onChangeDetailEditForm: (index: number, field: keyof DetailCodeForm, value: string) => void
    onChangeDetailForm: (index: number, field: keyof DetailCodeForm, value: string) => void
    onSelectDetail: (detail: Code) => void
    onSaveAll: () => void
}

/**
 * 공통코드 또는 선택한 세부코드의 상세와 직계 자식 관리 화면을 구성한다
 *
 * @author SeungHyeon.Kang
 * @param props 공통코드·세부코드 상세 데이터와 저장 및 이동 동작
 * @return 현재 계층의 상세와 직계 자식 편집 화면
 */
export function CodeDetailPage({
                                   selectedMaster,
                                   pageTitle,
                                   masterEditForm,
                                   selectedDetailCode,
                                   detailCodes,
                                   detailEditForms,
                                   detailForms,
                                   useeYsnoCodes,
                                   saving,
                                   onMovePath,
                                   onChangeMasterForm,
                                   onAddDetailInput,
                                   onRemoveDetailInput,
                                   onChangeDetailEditForm,
                                   onChangeDetailForm,
                                   onSelectDetail,
                                   onSaveAll
                               }: CodeDetailPageProps) {
    const permission = useMenuPermission()
    const [openedEditRows, setOpenedEditRows] = useState<Set<string>>(new Set())
    const [openedNewRows, setOpenedNewRows] = useState<Set<number>>(new Set())

    if (!selectedMaster || !masterEditForm) return null

    const selectedDetailIndex = detailCodes.findIndex((detail) => detail.comdCode === selectedDetailCode)
    const selectedDetail = selectedDetailIndex >= 0 ? detailCodes[selectedDetailIndex] : null
    const selectedDetailForm = selectedDetailIndex >= 0 ? detailEditForms[selectedDetailIndex] : null
    const childRows = detailCodes
        .map((detail, index) => ({detail, form: detailEditForms[index], index}))
        .filter((row) => (row.detail.upprCode ?? '') === selectedDetailCode && row.form)
    const parentPath = selectedDetail
        ? selectedDetail.upprCode
            ? `${CODE_DETAIL_PREFIX}/${encodeURIComponent(selectedMaster.commCode)}/${encodeURIComponent(selectedDetail.upprCode)}`
            : `${CODE_DETAIL_PREFIX}/${encodeURIComponent(selectedMaster.commCode)}`
        : CODE_LIST_PATH

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

    /**
     * 저장 전 세부코드 입력 행 삭제
     * @Author SeungHyeon.Kang
     * @param index
     * @return
     */
    const removeNewRow = (index: number) => {
        onRemoveDetailInput(index)
        // 삭제한 행 이후의 펼침 상태 인덱스를 한 칸씩 당긴다
        setOpenedNewRows(new Set(
            [...openedNewRows]
                .filter((rowIndex) => rowIndex !== index)
                .map((rowIndex) => rowIndex > index ? rowIndex - 1 : rowIndex)
        ))
    }

    return (
        <section className="code-detail">
            <section className="content-header">
                <div>
                    <h1>{pageTitle}</h1>
                </div>
            </section>

            {/* 현재 계층 기준 코드의 상세 편집 영역 */}
            <section className="detail-panel">
                {selectedDetail && selectedDetailForm ? (
                    <>
                        <div className="detail-title">
                            <div>
                                <h2>세부코드 상세</h2>
                                <p>선택한 세부코드를 기준으로 바로 아래 자식 코드를 관리합니다.</p>
                            </div>
                        </div>
                        <section className="table-wrap code-edit-table">
                            <table>
                                <thead>
                                <tr>
                                    <th className="col-code">세부코드</th>
                                    <th className="col-code-name">세부코드명</th>
                                    <th className="col-code-name">영문 세부코드명</th>
                                    <th>설명</th>
                                    <th className="col-parent-code">상위 세부코드</th>
                                    <th className="col-sort">정렬</th>
                                    <th className="col-usee">사용여부</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr className="editable-row">
                                    <td className="col-code"><input value={selectedDetailForm.comdCode} readOnly/></td>
                                    <td className="col-code-name"><input value={selectedDetailForm.comdName}
                                               onChange={(event) => onChangeDetailEditForm(selectedDetailIndex, 'comdName', event.target.value)}/></td>
                                    <td className="col-code-name"><input value={selectedDetailForm.comdEnnm}
                                               onChange={(event) => onChangeDetailEditForm(selectedDetailIndex, 'comdEnnm', event.target.value)}/></td>
                                    <td><input value={selectedDetailForm.codeExpl}
                                               onChange={(event) => onChangeDetailEditForm(selectedDetailIndex, 'codeExpl', event.target.value)}/></td>
                                    <td className="col-parent-code"><input value={selectedDetailForm.upprCode}
                                               onChange={(event) => onChangeDetailEditForm(selectedDetailIndex, 'upprCode', event.target.value)}/></td>
                                    <td className="col-sort"><input type="number" min="1" value={selectedDetailForm.sortOrdr}
                                               onChange={(event) => onChangeDetailEditForm(selectedDetailIndex, 'sortOrdr', event.target.value)}/></td>
                                    <td className="col-usee">
                                        <select value={selectedDetailForm.useeYsno}
                                                onChange={(event) => onChangeDetailEditForm(selectedDetailIndex, 'useeYsno', event.target.value)}>
                                            {useeYsnoCodes.map((code) => <option key={code.comdCode}
                                                                                 value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                                        </select>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </section>
                    </>
                ) : (
                    <>
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
                                    <th className="col-code">공통코드</th>
                                    <th className="col-code-name">공통코드명</th>
                                    <th>설명</th>
                                    <th className="col-usee">사용여부</th>
                                </tr>
                                </thead>
                                <tbody>
                                <tr className="editable-row">
                                    <td className="col-code"><input value={masterEditForm.commCode} readOnly/></td>
                                    <td className="col-code-name"><input value={masterEditForm.codeName} onChange={(event) => onChangeMasterForm({
                                        ...masterEditForm,
                                        codeName: event.target.value
                                    })}/></td>
                                    <td><input value={masterEditForm.codeExpl ?? ''} onChange={(event) => onChangeMasterForm({
                                        ...masterEditForm,
                                        codeExpl: event.target.value
                                    })}/></td>
                                    <td className="col-usee">
                                        <select value={masterEditForm.useeYsno ?? 'Y'} onChange={(event) => onChangeMasterForm({
                                            ...masterEditForm,
                                            useeYsno: event.target.value
                                        })}>
                                            {useeYsnoCodes.map((code) => <option key={code.comdCode}
                                                                                 value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                                        </select>
                                    </td>
                                </tr>
                                </tbody>
                            </table>
                        </section>
                    </>
                )}
            </section>

            <section className="detail-panel">
                <div className="detail-title">
                    <div>
                        <h2>{selectedDetail ? '하위 세부코드' : '세부코드'}</h2>
                        <p>현재 계층 바로 아래의 세부코드를 수정하거나 코드값을 눌러 더 깊은 자식을 관리합니다.</p>
                    </div>
                </div>
                <section className="table-wrap code-edit-table">
                    <table>
                        <thead>
                        <tr>
                            <th className="col-code">세부코드</th>
                            <th className="col-code-name">세부코드명</th>
                            <th className="col-code-name">영문 세부코드명</th>
                            <th>설명</th>
                            <th className="col-sort">정렬</th>
                            <th className="col-usee">사용여부</th>
                            <th>수정자</th>
                            <th className="col-datetime">수정일</th>
                            <th className="col-action">확장</th>
                        </tr>
                        </thead>
                        <tbody>
                        {childRows.map(({detail, form, index}) => {
                            if (!form) return null
                            const expanded = openedEditRows.has(detail.comdCode)
                            return (
                                <Fragment key={detail.comdCode}>
                                    <tr className="editable-row">
                                        <td className="col-code">
                                            <button type="button" className="code-drill-button"
                                                    onClick={() => onSelectDetail(detail)}>{form.comdCode}</button>
                                        </td>
                                        <td className="col-code-name"><input value={form.comdName}
                                                   onChange={(event) => onChangeDetailEditForm(index, 'comdName', event.target.value)}
                                                   /></td>
                                        <td className="col-code-name"><input value={form.comdEnnm}
                                                   onChange={(event) => onChangeDetailEditForm(index, 'comdEnnm', event.target.value)}
                                                   /></td>
                                        <td><input value={form.codeExpl}
                                                   onChange={(event) => onChangeDetailEditForm(index, 'codeExpl', event.target.value)}
                                                   /></td>
                                        <td className="col-sort"><input type="number" min="1" value={form.sortOrdr}
                                                                        onChange={(event) => onChangeDetailEditForm(index, 'sortOrdr', event.target.value)}
                                                                        /></td>
                                        <td className="col-usee">
                                            <select value={form.useeYsno}
                                                    onChange={(event) => onChangeDetailEditForm(index, 'useeYsno', event.target.value)}
                                                    >
                                                {useeYsnoCodes.map((code) => <option key={code.comdCode}
                                                                                     value={code.comdCode}>{code.opt1Name ?? code.comdName}</option>)}
                                            </select>
                                        </td>
                                        <td>{detail.updtAdmnName ?? detail.updtAdmn}</td>
                                        <td className="col-datetime">{formatDate(detail.updtDate)}</td>
                                        <td className="col-action">
                                            <button type="button" className="icon-toggle-button"
                                                    aria-label={expanded ? '접기' : '펼치기'} title={expanded ? '접기' : '펼치기'}
                                                    onClick={() => toggleEditRow(detail.comdCode)}>
                                                <ExpandToggleIcon expanded={expanded}/>
                                            </button>
                                        </td>
                                    </tr>
                                    {expanded && <ExtensionRow form={form} index={index} disabled={false}
                                                               colSpan={9} onChange={onChangeDetailEditForm}/>}
                                </Fragment>
                            )
                        })}
                        </tbody>
                    </table>
                </section>
            </section>

            <section className="detail-panel">
                <div className="detail-title">
                    <div>
                        <h2>세부코드 추가</h2>
                        <p>추가할 세부코드를 여러 개 입력한 뒤 한번에 저장합니다.</p>
                    </div>
                    {permission.writYsno === 'Y' && <button type="button" className="subtle-button" onClick={onAddDetailInput}>세부코드 추가</button>}
                </div>
                {detailForms.length > 0 ? (
                    <>
                        <section className="table-wrap code-edit-table new-code-table">
                            <table>
                                <thead>
                                <tr>
                                    <th className="col-code">세부코드</th>
                                    <th className="col-code-name">세부코드명</th>
                                    <th className="col-code-name">영문 세부코드명</th>
                                    <th>설명</th>
                                    <th className="col-sort">정렬</th>
                                    <th className="col-usee">사용여부</th>
                                    <th className="col-action">확장</th>
                                    <th className="col-action">삭제</th>
                                </tr>
                                </thead>
                                <tbody>
                                {detailForms.map((form, index) => {
                                    const expanded = openedNewRows.has(index)
                                    return (
                                        <Fragment key={index}>
                                            <tr className="editable-row">
                                                <td className="col-code"><input value={form.comdCode}
                                                           onChange={(event) => onChangeDetailForm(index, 'comdCode', event.target.value)}/>
                                                </td>
                                                <td className="col-code-name"><input value={form.comdName}
                                                           onChange={(event) => onChangeDetailForm(index, 'comdName', event.target.value)}/>
                                                </td>
                                                <td className="col-code-name"><input value={form.comdEnnm}
                                                           onChange={(event) => onChangeDetailForm(index, 'comdEnnm', event.target.value)}/>
                                                </td>
                                                <td><input value={form.codeExpl}
                                                           onChange={(event) => onChangeDetailForm(index, 'codeExpl', event.target.value)}/>
                                                </td>
                                                <td className="col-sort"><input type="number" min="1" value={form.sortOrdr}
                                                                                onChange={(event) => onChangeDetailForm(index, 'sortOrdr', event.target.value)}/>
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
                                                        <ExpandToggleIcon expanded={expanded}/>
                                                    </button>
                                                </td>
                                                <td className="col-action">
                                                    <button type="button" className="delete-button"
                                                            onClick={() => removeNewRow(index)}>삭제
                                                    </button>
                                                </td>
                                            </tr>
                                            {expanded &&
                                                <ExtensionRow form={form} index={index} disabled={false} colSpan={8}
                                                              onChange={onChangeDetailForm}/>}
                                        </Fragment>
                                    )
                                })}
                                </tbody>
                            </table>
                        </section>
                    </>
                ) : (
                    <div className="empty small">추가할 세부코드가 없습니다.</div>
                )}
            </section>
            <AuditInfoTable
                regiAdmn={selectedMaster.regiAdmn}
                regiAdmnName={selectedMaster.regiAdmnName}
                regiDate={selectedMaster.regiDate}
                updtAdmn={selectedMaster.updtAdmn}
                updtAdmnName={selectedMaster.updtAdmnName}
                updtDate={selectedMaster.updtDate}
            />
            <div className="detail-footer">
                <div className="detail-footer-left">
                    <button type="button" className="subtle-button" onClick={() => onMovePath(parentPath)}>{selectedDetail ? '상위' : '목록'}</button>
                </div>
                <div className="detail-footer-right">
                    {permission.writYsno === 'Y' && <button type="button" disabled={saving} onClick={onSaveAll}>{saving ? '저장 중' : '수정'}</button>}
                </div>
            </div>
        </section>
    )
}

/**
 * 확장 영역의 펼침 또는 접힘 상태 아이콘을 표시한다
 *
 * @author SeungHyeon.Kang
 * @param expanded 현재 확장 영역이 펼쳐졌는지 여부
 * @return 펼침 상태에 맞는 화살표 아이콘
 */
function ExpandToggleIcon({expanded}: { expanded: boolean }) {
    return (
        <svg viewBox="0 0 24 24" fill="none" aria-hidden="true">
            <path
                d={expanded
                    ? 'M19.92 15.05L13.4 8.53C12.63 7.76 11.37 7.76 10.6 8.53L4.08 15.05'
                    : 'M19.92 8.95L13.4 15.47C12.63 16.24 11.37 16.24 10.6 15.47L4.08 8.95'}
                stroke="currentColor"
                strokeWidth="1.7"
                strokeMiterlimit="10"
                strokeLinecap="round"
                strokeLinejoin="round"
            />
        </svg>
    )
}

type ExtensionRowProps = {
    form: DetailCodeForm
    index: number
    disabled: boolean
    colSpan: number
    onChange: (index: number, field: keyof DetailCodeForm, value: string) => void
}

/**
 * 세부코드의 네 개 확장 속성 편집 행을 구성한다
 *
 * @author SeungHyeon.Kang
 * @param props 세부코드 확장 폼과 변경 동작
 * @return 세부코드 확장 속성 편집 행
 */
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
                        <th>확장1 영문명</th>
                        <td><input value={form.opt1Ennm} onChange={(event) => onChange(index, 'opt1Ennm', event.target.value)} readOnly={disabled}/></td>
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
                        <th>확장2 영문명</th>
                        <td><input value={form.opt2Ennm} onChange={(event) => onChange(index, 'opt2Ennm', event.target.value)} readOnly={disabled}/></td>
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
                        <th>확장3 영문명</th>
                        <td><input value={form.opt3Ennm} onChange={(event) => onChange(index, 'opt3Ennm', event.target.value)} readOnly={disabled}/></td>
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
                        <th>확장4 영문명</th>
                        <td><input value={form.opt4Ennm} onChange={(event) => onChange(index, 'opt4Ennm', event.target.value)} readOnly={disabled}/></td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>
    )
}
