package org.sadari.admin.sadariadmin.notice.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : NoticeVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 공지사항 복합키와 버전별 본문 및 배포 이력을 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@Data
public class NoticeVO {

    // 공지사항 주키
    private Long notiNumb;
    // 공지사항 버전 부키
    private Integer versNumb;
    // 공지사항 카테고리 공통코드
    private String cateCgrp;
    // 공지사항 카테고리 상세코드
    private String cateCode;
    // 공지사항 카테고리명
    private String cateName;
    // 공지사항 제목
    private String notiTitl;
    // 정제된 공지사항 HTML 본문
    private String notiCntn;
    // 상단 고정 여부
    private String topxYsno;
    // 현재 배포 여부
    private String dplyYsno;
    // 등록 관리자 번호
    private Long regiAdmn;
    // 등록 관리자명
    private String regiAdmnName;
    // 등록 일시
    private LocalDateTime regiDate;
    // 배포 상태 수정 관리자 번호
    private Long updtAdmn;

    // 버전 수정 관리자명
    private String updtAdmnName;
    // 배포 상태 수정 일시
    private LocalDateTime updtDate;
    // 배포 관리자 번호
    private Long dplyAdmn;
    // 배포 관리자명
    private String dplyAdmnName;
    // 배포 일시
    private LocalDateTime dplyDate;
}
