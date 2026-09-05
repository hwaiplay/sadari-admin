package org.sadari.admin.sadariadmin.welcome.vo;

import java.time.LocalDateTime;
import lombok.Data;

/** 웰컴페이지 버전별 문구와 이미지 및 관리 정보를 전달한다. */
@Data
public class WelcomePageVO {
    // 웰컴페이지 주키
    private Long wlcmNumb;
    // 웰컴페이지 버전 부키
    private Integer versNumb;
    // 웰컴페이지 소제목
    private String subxTitl;
    private String subxEntl;
    // 웰컴페이지 제목
    private String mainTitl;
    private String mainEntl;
    // 웰컴페이지 설명
    private String pageDesc;
    private String pageEnct;
    // 웰컴페이지 이미지 공개 경로
    private String imgeUrlx;
    private String imgeEnur;
    // 사용자 노출 순서
    private Integer sortOrdr;
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
