package org.sadari.admin.sadariadmin.popup;

import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.popup.service.PopupContentService;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentSearchVO;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * fileName       : PopupContentServiceTests
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 실제 MySQL 스키마에서 팝업 콘텐츠 조회와 수정 및 JSON 검증을 확인한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    목록 검색 조건 반영
 * 2026-09-05        Codex               영문 팝업 필수값 반영
 */
@SpringBootTest
@ActiveProfiles("loc")
@Transactional
class PopupContentServiceTests {

    // 실제 Mapper와 JSON 검증이 연결된 팝업 콘텐츠 서비스
    @Autowired
    private PopupContentService popupContentService;

    /**
     * 팝업 콘텐츠 목록과 상세 및 수정 쿼리가 같은 복합키 데이터를 처리하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void getAndUpdatePopupContent() {
        // 실제 DB의 팝업 콘텐츠 목록 첫 페이지를 조회한다
        PopupContentSearchVO search = new PopupContentSearchVO();
        search.setPage(1);
        PageData<PopupContentVO> pageData = popupContentService.getPopupContentList(search, createAdminSession());
        // 초기 팝업 콘텐츠가 관리자 목록에 한 건 이상 표시되는지 확인한다
        assertFalse(pageData.getItems().isEmpty());

        // 회원 계정 처리 정책 팝업의 상세 JSON 목록을 조회한다
        PopupContentVO popupContent = popupContentService.getPopupContentDtl("ACCOUNT", "WITHDRAWAL_POLICY"
                                                                           , createAdminSession());
        // 기존 영문 데이터가 없는 로컬 DB에서도 새 필수값 검증을 통과하도록 대응값을 설정한다
        setEnglishContent(popupContent);
        // 조회된 관리 제목을 유지하면서 수정 Mapper와 감사정보 갱신을 실행한다
        PopupContentVO updatedContent = popupContentService.uptPopupContent("ACCOUNT", "WITHDRAWAL_POLICY"
                                                                          , popupContent, createAdminSession());
        // 수정 후에도 사용자 화면이 참조하는 복합키가 그대로 유지되는지 확인한다
        assertEquals("ACCOUNT", updatedContent.getPopuSitu());
        // 수정 후에도 계정 처리 정책 팝업 코드가 그대로 유지되는지 확인한다
        assertEquals("WITHDRAWAL_POLICY", updatedContent.getPopuCode());
    }

    /**
     * 공백 목록 문구가 포함된 JSON 콘텐츠를 저장 전에 차단하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setPopupRejectsBlank() {
        // 회원 계정 처리 정책 팝업의 현재 상세를 조회한다
        PopupContentVO popupContent = popupContentService.getPopupContentDtl("ACCOUNT", "WITHDRAWAL_POLICY"
                                                                           , createAdminSession());
        // 검증 대상인 한글 JSON 이외 영문 필수값은 정상 상태로 설정한다
        setEnglishContent(popupContent);
        // 사용자 화면에서 비어 있는 목록 항목이 생기는 잘못된 JSON을 설정한다
        popupContent.setContFirs("[\"\"]");
        // 잘못된 JSON 목록 문구가 DB 수정 전에 업무 예외로 차단되는지 확인한다
        assertThrows(BusinessException.class
                   , () -> popupContentService.uptPopupContent("ACCOUNT", "WITHDRAWAL_POLICY"
                                                            , popupContent, createAdminSession()));
    }

    /**
     * 테스트 요청에 사용할 최고관리자 세션을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 팝업 콘텐츠 관리 권한을 가진 관리자 세션
     */
    private AdminSessionVO createAdminSession() {
        // 실제 감사 컬럼과 같은 관리자 번호를 담을 테스트 세션을 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 테스트 수정 이력에 사용할 관리자 번호를 설정한다
        admin.setAdmnNumb(1L);
        // 관리자 서비스 인증 확인을 통과할 테스트 세션을 반환한다
        return admin;
    }

    /** 현재 팝업의 한글 JSON을 영문 필수 테스트값으로 복사한다. */
    private void setEnglishContent(PopupContentVO popupContent) {
        // 네 개 팝업 영역의 구조를 유지한 채 영문 필수값을 준비한다
        popupContent.setEnglFirs(popupContent.getContFirs());
        popupContent.setEnglSeco(popupContent.getContSeco());
        popupContent.setEnglThir(popupContent.getContThir());
        popupContent.setEnglFour(popupContent.getContFour());
    }
}
