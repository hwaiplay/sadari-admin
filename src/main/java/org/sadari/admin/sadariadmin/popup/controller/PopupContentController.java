package org.sadari.admin.sadariadmin.popup.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.popup.service.PopupContentService;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentSearchVO;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : PopupContentController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 관리자 팝업 콘텐츠 목록과 상세 및 저장 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    팝업 콘텐츠 목록 검색 조건 추가
 */
@RestController
@RequestMapping(Constant.API_POPUP_CONTENT_PREFIX)
public class PopupContentController {

    // 팝업 콘텐츠 관리 서비스
    private final PopupContentService popupContentService;

    /**
     * 팝업 콘텐츠 관리 API에 서비스 기능을 주입한다
     *
     * @author SeungHyeon.Kang
     * @param popupContentService 팝업 콘텐츠 관리 서비스
     */
    public PopupContentController(PopupContentService popupContentService) {
        this.popupContentService = popupContentService;
    }

    /**
     * 팝업 콘텐츠 목록과 페이지 정보를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 팝업 콘텐츠 검색 조건
     * @param admin 로그인 관리자 세션
     * @return 팝업 콘텐츠 목록 조회 성공 응답
     */
    @GetMapping
    public ResultData getPopupContentList(@ModelAttribute PopupContentSearchVO search
                                        , @AuthenticationPrincipal AdminSessionVO admin) {
        // 관리자 검색 조건으로 조회한 팝업 콘텐츠 목록 페이지를 반환한다
        return ResultData.success(popupContentService.getPopupContentList(search, admin));
    }

    /**
     * 화면 구분과 팝업 코드로 팝업 콘텐츠 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @param admin 로그인 관리자 세션
     * @return 팝업 콘텐츠 상세 조회 성공 응답
     */
    @GetMapping("/{popuSitu}/{popuCode}")
    public ResultData getPopupContentDtl(@PathVariable String popuSitu, @PathVariable String popuCode
                                       , @AuthenticationPrincipal AdminSessionVO admin) {
        // 복합키에 해당하는 팝업 콘텐츠 상세를 반환한다
        return ResultData.success(popupContentService.getPopupContentDtl(popuSitu, popuCode, admin));
    }

    /**
     * 신규 팝업 콘텐츠를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 등록할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @return 등록된 팝업 콘텐츠와 저장 성공 응답
     */
    @PostMapping
    public ResultData setPopupContent(@RequestBody PopupContentVO popupContent
                                    , @AuthenticationPrincipal AdminSessionVO admin) {
        // 등록된 팝업 콘텐츠 상세와 "저장했습니다." 메시지를 반환한다
        return ResultData.success(ResultEnum.SAVE_SUCCESS
                                , popupContentService.setPopupContent(popupContent, admin));
    }

    /**
     * 복합키를 유지하면서 팝업 콘텐츠 제목과 목록 문구를 수정한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @param popupContent 수정할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @return 수정된 팝업 콘텐츠와 수정 성공 응답
     */
    @PutMapping("/{popuSitu}/{popuCode}")
    public ResultData uptPopupContent(@PathVariable String popuSitu, @PathVariable String popuCode
                                    , @RequestBody PopupContentVO popupContent
                                    , @AuthenticationPrincipal AdminSessionVO admin) {
        // 수정된 팝업 콘텐츠 상세와 "수정했습니다." 메시지를 반환한다
        return ResultData.success(ResultEnum.UPDATE_SUCCESS
                                , popupContentService.uptPopupContent(popuSitu, popuCode, popupContent, admin));
    }
}
