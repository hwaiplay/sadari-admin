package org.sadari.admin.sadariadmin.popup.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentVO;

/**
 * fileName       : PopupContentService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠의 조회와 등록 및 수정을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
public interface PopupContentService {

    /**
     * 로그인 관리자가 조회할 팝업 콘텐츠 목록 페이지를 반환한다
     *
     * @author SeungHyeon.Kang
     * @param pageNumber 요청 페이지 번호
     * @param admin 로그인 관리자 세션
     * @return 팝업 콘텐츠 목록과 페이지 정보
     */
    PageData<PopupContentVO> getPopupContentList(int pageNumber, AdminSessionVO admin);

    /**
     * 복합키에 해당하는 팝업 콘텐츠 상세를 반환한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @param admin 로그인 관리자 세션
     * @return 팝업 콘텐츠 상세
     */
    PopupContentVO getPopupContentDtl(String popuSitu, String popuCode, AdminSessionVO admin);

    /**
     * 신규 팝업 콘텐츠를 검증하여 등록한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 등록할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @return 등록된 팝업 콘텐츠 상세
     */
    PopupContentVO setPopupContent(PopupContentVO popupContent, AdminSessionVO admin);

    /**
     * 복합키를 유지하면서 팝업 제목과 목록 콘텐츠를 수정한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @param popupContent 수정할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @return 수정된 팝업 콘텐츠 상세
     */
    PopupContentVO uptPopupContent(String popuSitu, String popuCode, PopupContentVO popupContent
                                , AdminSessionVO admin);
}
