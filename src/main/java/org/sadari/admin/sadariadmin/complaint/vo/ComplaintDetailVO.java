package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;

import java.util.List;

/**
 * fileName       : ComplaintDetailVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 신고 상세와 피신고자 및 동일 대상 신고 요약을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintDetailVO {

    // 신고 접수와 처리 상세
    private ComplaintVO complaint;

    // 영구 삭제되지 않은 피신고자의 현재 회원 정보
    private CurrentUserVO targetUser;

    // 신고 대상 콘텐츠 원본이 현재 조치 가능한 상태로 존재하는지 여부
    private boolean targetContentExists;

    // 동일 대상의 최근 다른 신고 목록
    private List<ComplaintVO> relatedComplaints;

    // 동일 대상의 전체 다른 신고 건수
    private int relatedComplaintCount;
}
