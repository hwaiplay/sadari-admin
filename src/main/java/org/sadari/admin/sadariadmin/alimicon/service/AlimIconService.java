package org.sadari.admin.sadariadmin.alimicon.service;

import java.io.IOException;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconSearchVO;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : AlimIconService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 알림 상황별 아이콘 관리 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 상황 식별 구조로 전환
 */
public interface AlimIconService {

    /**
     * 검색 조건에 맞는 ALIM_SITU 공통코드와 아이콘 등록 상태를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param search 아이콘 검색 조건
     * @param admin 로그인 관리자
     * @return 알림 상황별 아이콘 관리 페이지
     */
    PageData<AlimIconVO> getAlimIconList(AlimIconSearchVO search, AdminSessionVO admin);

    /**
     * 알림 상황으로 공통코드와 아이콘 상세를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @param admin 로그인 관리자
     * @return 알림 상황과 아이콘 상세
     */
    AlimIconVO getAlimIconDtl(String alimSitu, AdminSessionVO admin);

    /**
     * 관리자 미리보기용 아이콘 원본을 알림 상황으로 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @param admin 로그인 관리자
     * @return MIME 유형과 이미지 바이너리
     */
    AlimIconVO getAlimIconImage(String alimSitu, AdminSessionVO admin);

    /**
     * ALIM_SITU 공통코드에 SVG 또는 PNG 아이콘을 등록하거나 교체한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @param file 등록하거나 교체할 SVG 또는 PNG 원본
     * @param admin 로그인 관리자
     * @return 저장된 알림 상황별 아이콘 상세
     * @throws IOException 업로드 원본을 읽지 못할 때 발생
     */
    AlimIconVO saveAlimIcon(String alimSitu, MultipartFile file, AdminSessionVO admin) throws IOException;
}
