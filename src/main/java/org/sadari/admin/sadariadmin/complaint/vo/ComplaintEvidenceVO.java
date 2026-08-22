package org.sadari.admin.sadariadmin.complaint.vo;

import lombok.Data;

/**
 * fileName       : ComplaintEvidenceVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 관리자 전용 프로필 사진 신고 증거 원본을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class ComplaintEvidenceVO {

    // 증거 원본 파일명
    private String origName;

    // 증거 MIME 유형
    private String mimeType;

    // 비공개 증거 원본 바이트
    private byte[] evdcData;
}
