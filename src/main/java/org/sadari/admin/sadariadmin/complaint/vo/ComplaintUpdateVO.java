package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : ComplaintUpdateVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 신고 검토 시작과 최종 처리 요청값을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintUpdateVO {

    // 변경할 신고 처리 상태 세부코드
    private String cmplStat;

    // 관리자 처리 내용
    private String procCntn;

    // 화면이 조회한 신고 수정 일시
    private LocalDateTime updtDate;
}
