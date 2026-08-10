package org.sadari.admin.sadariadmin.serviceinfo.vo;

import java.time.LocalDateTime;
import lombok.Data;

/**
 * fileName       : ServiceInfoVO
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 카테고리별 서비스 정보 버전과 배포 및 감사정보를 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
@Data
public class ServiceInfoVO {

    // 서비스 정보 카테고리 공통코드
    private String cateCgrp;
    // 서비스 정보 카테고리 상세코드
    private String cateCode;
    // 서비스 정보 카테고리명
    private String cateName;
    // 서비스 정보 버전 번호
    private Integer versNumb;
    // 서비스 정보 제목
    private String svciTitl;
    // 정제된 서비스 정보 HTML 본문
    private String svciCntn;
    // 현재 배포 여부
    private String dplyYsno;
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
    // 배포 관리자 번호
    private Long dplyAdmn;
    // 배포 관리자명
    private String dplyAdmnName;
    // 배포 일시
    private LocalDateTime dplyDate;
}
