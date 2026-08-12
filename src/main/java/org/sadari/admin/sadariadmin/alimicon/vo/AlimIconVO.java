package org.sadari.admin.sadariadmin.alimicon.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : AlimIconVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 알림 상황 공통코드와 아이콘 관리 데이터를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 상황 식별 구조로 전환
 */
@Data
public class AlimIconVO {

    // 아이콘을 식별하는 알림 상황 코드
    private String alimSitu;

    // ALIM_SITU 공통코드명
    private String alimSituName;

    // ALIM_SITU 공통코드 사용 여부
    private String useeYsno;

    // 해당 알림 상황의 아이콘 등록 여부
    private String iconRegiYsno;

    // 저장한 아이콘 MIME 유형
    private String mimeType;

    // 정규화한 알림 아이콘 바이너리
    private byte[] iconData;

    // 아이콘 파일 크기
    private Long fileSize;

    // 아이콘 픽셀 너비
    private Integer pixlWdth;

    // 아이콘 픽셀 높이
    private Integer pixlHght;

    // 해당 알림 상황을 사용하는 템플릿 수
    private Integer tempCnt;

    // 등록 관리자 번호
    private Long regiAdmn;

    // 등록 관리자명
    private String regiAdmnName;

    // 등록 일시
    private LocalDateTime regiDate;

    // 수정 관리자 번호
    private Long updtAdmn;

    // 수정 관리자명
    private String updtAdmnName;

    // 수정 일시
    private LocalDateTime updtDate;
}
