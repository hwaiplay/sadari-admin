import {formatDate} from '../utils/code'

type AuditInfoTableProps = {
    regiAdmn: string | number | null | undefined
    regiAdmnName?: string | null
    regiDate: string | null | undefined
    updtAdmn: string | number | null | undefined
    updtAdmnName?: string | null
    updtDate: string | null | undefined
    dplyAdmn?: string | number | null
    dplyAdmnName?: string | null
    dplyDate?: string | null
    showDeployInfo?: boolean
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
 * @param dplyAdmn
 * @param dplyAdmnName
 * @param dplyDate
 * @param showDeployInfo
 * @return
 */
export function AuditInfoTable({
                                   regiAdmn,
                                   regiAdmnName,
                                   regiDate,
                                   updtAdmn,
                                   updtAdmnName,
                                   updtDate,
                                   dplyAdmn,
                                   dplyAdmnName,
                                   dplyDate,
                                   showDeployInfo = false
                               }: AuditInfoTableProps) {
    const regiName = regiAdmnName ?? regiAdmn ?? ''
    const updtName = updtAdmnName ?? updtAdmn ?? ''
    const regiText = regiName ? `${regiName}(${formatDate(regiDate ?? null)})` : ''
    const updtText = updtName ? `${updtName}(${formatDate(updtDate ?? null)})` : ''
    const dplyName = dplyAdmnName ?? dplyAdmn ?? ''
    const dplyText = dplyName ? `${dplyName}(${formatDate(dplyDate ?? null)})` : ''

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
                {showDeployInfo && (
                    <tr>
                        <th>배포</th>
                        <td className="readonly-cell">{dplyText}</td>
                    </tr>
                )}
                </tbody>
            </table>
        </section>
    )
}
