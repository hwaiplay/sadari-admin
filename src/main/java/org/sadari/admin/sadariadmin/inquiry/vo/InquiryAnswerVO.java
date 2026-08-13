package org.sadari.admin.sadariadmin.inquiry.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : InquiryAnswerVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 고객문의 관리자 답변 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquiryAnswerVO {

    // 답변 번호
    private Long answNumb;
    // 관리자 답변 내용
    private String answCntn;
    // 사용자 읽음 여부
    private String readYsno;
    // 답변 등록 관리자 번호
    private Long regiAdmn;
    // 답변 등록 관리자명
    private String regiAdmnName;
    // 답변 등록 일시
    private LocalDateTime regiDate;
}
