package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;

import java.util.List;

/**
 * fileName       : ComplaintAutoActionVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 대상의 자동 조치 진행 상태와 실행 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintAutoActionVO {

    // 현재 신고 대상 유형의 자동 조치 적용 여부
    private boolean autoActionTarget;

    // 예정된 자동 조치 유형 코드
    private String actnType;

    // 예정된 자동 조치 유형 명칭
    private String actnTypeName;

    // 한 번의 자동 조치를 실행하는 신고 임계치
    private int threshold;

    // 반려를 제외한 현재 유효 신고 누적 건수
    private int complaintCount;

    // 다음 자동 조치를 실행할 누적 신고 건수
    private int nextActionCount;

    // 다음 자동 조치까지 남은 유효 신고 건수
    private int remainingCount;

    // 동일 대상에 실제 실행된 자동 조치 이력
    private List<ComplaintActionVO> actionHistories;
}
