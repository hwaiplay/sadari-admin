package org.sadari.admin.sadariadmin.inquiry.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

/**
 * fileName       : InquiryVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 고객문의 목록과 상세 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Data
public class InquiryVO {

    // 고객문의 번호
    private Long inqrNumb;
    // 작성 사용자 번호
    private Long userNumb;
    // 작성 사용자 닉네임
    private String userNick;
    // 작성 사용자 상태
    private String userStat;
    // 문의 카테고리 코드
    private String inqrCatg;
    // 문의 카테고리명
    private String inqrCatgName;
    // 문의 제목
    private String inqrTitl;
    // 문의 내용
    private String inqrCntn;
    // 문의 처리 상태 코드
    private String inqrStat;
    // 문의 처리 상태명
    private String inqrStatName;
    // 연결 이용정지 이력 번호
    private Long spndNumb;
    // 연결 이용정지 상태
    private String spndStat;
    // 연결 이용정지 사유명
    private String spndRsonName;
    // 연결 이용정지 시작 일시
    private LocalDateTime spndStrtDate;
    // 연결 이용정지 종료 일시
    private LocalDateTime spndEndxDate;
    // 담당 관리자 번호
    private Long asgnAdmn;
    // 담당 관리자명
    private String asgnAdmnName;
    // 최종 답변 일시
    private LocalDateTime answDate;
    // 문의 접수 일시
    private LocalDateTime regiDate;
    // 문의 수정 일시
    private LocalDateTime updtDate;
    // 관리자 답변 목록
    private List<InquiryAnswerVO> answers;
}
