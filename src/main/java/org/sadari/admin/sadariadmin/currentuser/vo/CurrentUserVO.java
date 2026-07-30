package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : CurrentUserVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 관리자 화면에 노출할 현재 사용자 기본정보와 활동 요약을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    사용자 서버 상태 반영 결과 추가
 * 2026-07-30        SeungHyeon.Kang    로그인 제공자 표시명 추가
 */
@Data
public class CurrentUserVO {

    // 내부 회원번호
    private Long userNumb;

    // 서비스 닉네임
    private String userNick;

    // 로그인 제공자 코드
    private String userProv;

    // 로그인 제공자 표시명
    private String userProvName;

    // 사용자 권한 코드
    private String userRole;

    // 회원 상태 코드
    private String userStat;

    // 회원 상태명
    private String userStatName;

    // 사용자 서버 회원 상태 반영 결과
    private String userStatusSyncStat;

    // 온보딩 완료 여부
    private String onbdYsno;

    // 온보딩 완료 여부명
    private String onbdYsnoName;

    // 한줄소개
    private String intrCntn;

    // 프로필 이미지 경로
    private String profPath;

    // 배경 이미지 경로
    private String bgimPath;

    // 가입일시
    private LocalDateTime joinDate;

    // 비활성화 또는 영구 탈퇴 요청일시
    private LocalDateTime wthdDate;

    // 영구 삭제 예정일시
    private LocalDateTime deltDate;

    // 최근 로그인 일시
    private LocalDateTime lastLognDate;

    // 작성 독후감 건수
    private long reportCntt;

    // 작성 댓글 건수
    private long replyCntt;

    // 좋아요 건수
    private long likeCntt;

    // 팔로잉 건수
    private long followingCntt;

    // 팔로워 건수
    private long followerCntt;

    // 독서 목표 건수
    private long goalCntt;

    // 활성 푸시 구독 건수
    private long pushCntt;
}
