package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : CurrentUserWithdrawalHistoryVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자의 계정 비활성화와 영구 탈퇴 처리 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Data
public class CurrentUserWithdrawalHistoryVO {

    // 계정 처리 이력 번호
    private Long wthdNumb;

    // 계정 처리 유형 코드
    private String wthdType;

    // 계정 처리 유형명
    private String wthdTypeName;

    // 계정 처리 사유 코드
    private String wthdRson;

    // 계정 처리 사유명
    private String wthdRsonName;

    // 계정 처리 상태 코드
    private String wthdStat;

    // 계정 처리 상태명
    private String wthdStatName;

    // 계정 처리 요청일시
    private LocalDateTime requDate;

    // 영구 삭제 예정일시
    private LocalDateTime deltDate;

    // 처리 완료일시
    private LocalDateTime procDate;

    // 계정 복구일시
    private LocalDateTime rcovDate;
}
