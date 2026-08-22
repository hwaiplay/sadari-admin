package org.sadari.admin.sadariadmin.currentuser.vo;

import lombok.Data;

/**
 * fileName       : CurrentUserFileVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 현 사용자 관리에서 삭제할 사용자 이미지 파일 메타정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@Data
public class CurrentUserFileVO {

    // 파일 번호
    private Long fileNumb;

    // 서버 저장 파일명
    private String storName;

    // 파일 접근 경로
    private String filePath;
}
