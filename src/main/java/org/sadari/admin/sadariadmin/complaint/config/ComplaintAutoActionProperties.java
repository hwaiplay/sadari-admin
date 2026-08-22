package org.sadari.admin.sadariadmin.complaint.config;

import lombok.Getter;
import lombok.Setter;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * fileName       : ComplaintAutoActionProperties
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 관리자 신고 상세에 표시할 대상별 자동 조치 임계치를 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "complaint.auto-action")
public class ComplaintAutoActionProperties {

    // 독후감 자동 비공개 전환 신고 임계치
    private int bookReportThreshold = 5;

    // 댓글 자동 삭제 신고 임계치
    private int replyThreshold = 5;

    // 프로필 사진 자동 초기화 신고 임계치
    private int profileImageThreshold = 5;

    // 한줄소개 자동 초기화 신고 임계치
    private int introductionThreshold = 5;

    /**
     * 신고 대상 유형에 대응하는 자동 조치 임계치를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param tagtType 신고 대상 유형
     * @return 대상별 임계치, 자동 조치 대상이 아니면 0
     */
    public int getThreshold(String tagtType) {
        // 자동 조치 대상 유형은 사용자 서버와 같은 설정 항목에 고정 매핑한다
        return switch (tagtType) {
            // 독후감은 비공개 전환 기준을 반환한다
            case Constant.CMPL_TARGET_BOOK_REPORT -> bookReportThreshold;
            // 댓글은 논리 삭제 기준을 반환한다
            case Constant.CMPL_TARGET_REPLY -> replyThreshold;
            // 프로필 사진은 기본 이미지 초기화 기준을 반환한다
            case Constant.CMPL_TARGET_PROFILE_IMAGE -> profileImageThreshold;
            // 한줄소개는 NULL 초기화 기준을 반환한다
            case Constant.CMPL_TARGET_INTRODUCTION -> introductionThreshold;
            // 사용자 전체와 모임은 관리자 수동 검토 대상으로 구분한다
            default -> 0;
        };
    }
}
