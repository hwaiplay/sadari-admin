package org.sadari.admin.sadariadmin.readingclub.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * fileName       : ReadingClubVO
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 화면에 노출할 독서 모임 운영 정보와 인원 요약을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@Data
public class ReadingClubVO {

    // 모임 번호
    private Long clubNumb;

    // 모임장 사용자 번호
    private Long ownrNumb;

    // 모임장 닉네임
    private String ownrNick;

    // 모임명
    private String clubName;

    // 회원 작성 모임 소개 조회값
    private String clubCntn;

    // 모임 공개 범위 코드
    private String clubVisb;

    // 모임 공개 범위명
    private String clubVisbName;

    // 모임 가입 방식 코드
    private String joinType;

    // 모임 가입 방식명
    private String joinTypeName;

    // 모임 운영 상태 코드
    private String clubStat;

    // 모임 운영 상태명
    private String clubStatName;

    // 신규 회원 모집 가능 여부
    private String rcrtYsno;

    // 모임 정원
    private Integer maxxMemb;

    // 활성 모임원 수
    private Integer memberCnt;

    // 유효 초대 예약석 수
    private Integer invitedCnt;

    // 모임 카테고리 표시명 목록
    private String categoryNames;

    // 모임 생성 일시
    private LocalDateTime regiDate;

    // 모임 수정 일시
    private LocalDateTime updtDate;

    // 모임 종료 일시
    private LocalDateTime closDate;
}
