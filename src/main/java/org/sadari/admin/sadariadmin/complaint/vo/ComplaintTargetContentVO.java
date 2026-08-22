package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * fileName       : ComplaintTargetContentVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 자동 조치 진행 여부를 판정할 현재 신고 대상 원문과 파일 정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ComplaintTargetContentVO extends ComplaintTargetFileVO {

    // 현재 서비스에 노출 중인 신고 대상 원문
    private String tagtCntn;
}
