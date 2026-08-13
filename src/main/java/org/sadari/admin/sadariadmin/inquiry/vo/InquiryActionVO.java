package org.sadari.admin.sadariadmin.inquiry.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : InquiryActionVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 고객문의 검토와 답변 및 이용정지 해제 요청값을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquiryActionVO {

    // 관리자 답변 내용
    private String answCntn;
    // 화면이 조회한 고객문의 수정 일시
    private LocalDateTime updtDate;
    // 이용정지 해제 관리자 메모
    private String rlesCntn;
}
