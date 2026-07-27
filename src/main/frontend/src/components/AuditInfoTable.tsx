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
    return (
        <section className="table-wrap audit-info-table">
            <table>
                <tbody>
                <tr>
                    <th>등록자</th>
                    <td className="readonly-cell">{regiAdmnName ?? regiAdmn ?? ''}</td>
                    <th>등록일</th>
                    <td className="readonly-cell">{formatDate(regiDate ?? null)}</td>
                    <th>수정자</th>
                    <td className="readonly-cell">{updtAdmnName ?? updtAdmn ?? ''}</td>
                    <th>수정일</th>
                    <td className="readonly-cell">{formatDate(updtDate ?? null)}</td>
                </tr>
                </tbody>
            </table>
        </section>
    )
}
