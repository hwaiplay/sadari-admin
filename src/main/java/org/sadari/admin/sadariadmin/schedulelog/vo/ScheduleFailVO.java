package org.sadari.admin.sadariadmin.schedulelog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : ScheduleFailVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 스케줄러 실행 중 발생한 실패 상세 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 */
@Data
public class ScheduleFailVO {

    // 스케줄러 실행 번호
    private Long runxNumb;

    // 동일 실행 내 실패 순번
    private Integer failNumb;

    // 실패 유형 코드
    private String failType;

    // 업무 처리 결과 코드
    private Integer rsltCode;

    // 업무 처리 결과 메시지
    private String rsltMesg;

    // 발생 예외 클래스명
    private String erroType;

    // 발생 예외 또는 오류 내용
    private String erroCntn;

    // 실패 발생 일시
    private LocalDateTime failDate;
}
