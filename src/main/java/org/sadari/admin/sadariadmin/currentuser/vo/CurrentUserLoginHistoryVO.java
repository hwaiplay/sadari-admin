package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : CurrentUserLoginHistoryVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자의 마스킹된 로그인 접속 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    로그인 제공자 표시명 추가
 */
@Data
public class CurrentUserLoginHistoryVO {

    // 로그인 이력 번호
    private Long lognNumb;

    // 로그인 일시
    private LocalDateTime lognDate;

    // 마스킹된 로그인 IP
    private String lognIpxx;

    // 로그인 클라이언트 User-Agent
    private String userAgnt;

    // 로그인 제공자 코드
    private String provCode;

    // 로그인 제공자 표시명
    private String provCodeName;
}
