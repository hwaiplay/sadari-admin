package org.sadari.admin.sadariadmin.popup.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * fileName       : PopupContentVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업의 식별값과 콘텐츠 및 관리 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Data
public class PopupContentVO {

    // 팝업 사용 화면 구분 코드
    private String popuSitu;

    // 팝업 사용 화면 구분 코드명
    private String popuSituName;

    // 화면 안에서 팝업을 식별하는 코드
    private String popuCode;

    // 관리자 화면에서 사용하는 팝업 제목
    private String mngmTitl;

    // 첫 번째 목록 영역의 JSON 문자열 배열
    private String contFirs;

    // 두 번째 목록 영역의 JSON 문자열 배열
    private String contSeco;

    // 세 번째 목록 영역의 JSON 문자열 배열
    private String contThir;

    // 네 번째 목록 영역의 JSON 문자열 배열
    private String contFour;

    // 등록 관리자 식별값
    private String regiAdmn;

    // 등록 관리자명 또는 시스템 등록자 표시값
    private String regiAdmnName;

    // 등록일시
    private LocalDateTime regiDate;

    // 수정 관리자 식별값
    private String updtAdmn;

    // 수정 관리자명 또는 시스템 수정자 표시값
    private String updtAdmnName;

    // 수정일시
    private LocalDateTime updtDate;
}
