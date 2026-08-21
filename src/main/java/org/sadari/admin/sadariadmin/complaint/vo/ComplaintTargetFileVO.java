package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;

/**
 * fileName       : ComplaintTargetFileVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 관리에서 삭제할 피신고자 이미지 파일 메타정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintTargetFileVO {

    // 파일 번호
    private Long fileNumb;

    // 서버 저장 파일명
    private String storName;

    // 파일 접근 경로
    private String filePath;
}
