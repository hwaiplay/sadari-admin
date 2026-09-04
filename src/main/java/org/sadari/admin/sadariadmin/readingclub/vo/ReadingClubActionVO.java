package org.sadari.admin.sadariadmin.readingclub.vo;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * fileName       : ReadingClubActionVO
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 조치 요청과 감사 이력 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@Data
public class ReadingClubActionVO {

    // 모임 번호
    private Long clubNumb;

    // 모임별 조치 이력 번호
    private Long histNumb;

    // 조치 관리자 번호
    private Long admnNumb;

    // 조치 관리자 이름
    private String admnName;

    // 관리자 조치 유형
    private String actnType;

    // 관리자 조치 유형명
    private String actnTypeName;

    // 조치 전 모임 상태
    private String befrStat;

    // 조치 전 모임 상태명
    private String befrStatName;

    // 조치 후 모임 상태
    private String aftrStat;

    // 조치 후 모임 상태명
    private String aftrStatName;

    // 관리자 조치 사유
    private String actnRson;

    // 조치 일시
    private LocalDateTime regiDate;
}
