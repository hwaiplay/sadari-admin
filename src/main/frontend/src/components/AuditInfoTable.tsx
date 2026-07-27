import {formatDate} from '../utils/code'

type AuditInfoTableProps = {
    regiAdmn: string | number | null | undefined
    regiAdmnName?: string | null
    regiDate: string | null | undefined
    updtAdmn: string | number | null | undefined
    updtAdmnName?: string | null
    updtDate: string | null | undefined
}

/**
 * 단건 상세 등록 수정 이력 표
 * @Author SeungHyeon.Kang
 * @param regiAdmn
 * @param regiAdmnName
 * @param regiDate
 * @param updtAdmn
 * @param updtAdmnName
 * @param updtDate
 * @return
 */
export function AuditInfoTable({
                                   regiAdmn,
                                   regiAdmnName,
                                   regiDate,
                                   updtAdmn,
                                   updtAdmnName,
                                   updtDate
                               }: AuditInfoTableProps) {
    const regiName = regiAdmnName ?? regiAdmn ?? ''
    const updtName = updtAdmnName ?? updtAdmn ?? ''
    const regiText = regiName ? `${regiName}(${formatDate(regiDate ?? null)})` : ''
    const updtText = updtName ? `${updtName}(${formatDate(updtDate ?? null)})` : ''

    return (
        <section className="table-wrap audit-info-table">
            <table>
                <tbody>
                <tr>
                    <th>등록</th>
                    <td className="readonly-cell">{regiText}</td>
                </tr>
                <tr>
                    <th>수정</th>
                    <td className="readonly-cell">{updtText}</td>
                </tr>
                </tbody>
            </table>
        </section>
    )
}
