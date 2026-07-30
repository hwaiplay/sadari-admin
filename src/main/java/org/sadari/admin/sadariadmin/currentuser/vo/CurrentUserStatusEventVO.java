package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : CurrentUserStatusEventVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 서버가 처리할 회원 상태 변경 Outbox 이벤트를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    전달 대상 정지 이력 번호 추가
 */
@Data
public class CurrentUserStatusEventVO {

    // 이벤트 번호
    private Long evntNumb;

    // 이벤트 유형
    private String evntType;

    // 회원 번호
    private Long userNumb;

    // 전달 대상 정지 이력 번호
    private Long spndNumb;

    // 이벤트 등록 일시
    private LocalDateTime regiDate;
}
