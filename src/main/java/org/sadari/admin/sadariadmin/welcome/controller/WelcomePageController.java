package org.sadari.admin.sadariadmin.welcome.controller;

import java.io.IOException;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.notice.service.NoticeImageService;
import org.sadari.admin.sadariadmin.welcome.service.WelcomePageService;
import org.sadari.admin.sadariadmin.welcome.vo.WelcomePageVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/** 관리자 웰컴페이지 버전과 배포 API를 제공한다. */
@RestController
@RequestMapping(Constant.API_WELCOME_PAGE_PREFIX)
public class WelcomePageController {

    // 웰컴페이지 관리 서비스
    private final WelcomePageService welcomePageService;
    // 검증 완료 콘텐츠 이미지 저장 서비스
    private final NoticeImageService noticeImageService;

    /** 웰컴페이지 API에 관리 및 이미지 저장 서비스를 주입한다. */
    public WelcomePageController(WelcomePageService welcomePageService, NoticeImageService noticeImageService) {
        this.welcomePageService = welcomePageService;
        this.noticeImageService = noticeImageService;
    }

    /** 배포본 우선 웰컴페이지 목록을 조회한다. */
    @GetMapping
    public ResultData getWelcomePageList(@AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(welcomePageService.getWelcomePageList(admin));
    }

    /** 웰컴페이지 버전 상세를 조회한다. */
    @GetMapping("/{wlcmNumb}/{versNumb}")
    public ResultData getWelcomePageDtl(@PathVariable Long wlcmNumb, @PathVariable Integer versNumb
                                      , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(welcomePageService.getWelcomePageDtl(wlcmNumb, versNumb, admin));
    }

    /** 같은 웰컴페이지의 모든 버전을 조회한다. */
    @GetMapping("/{wlcmNumb}/versions")
    public ResultData getWelcomePageVersionList(@PathVariable Long wlcmNumb
                                               , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(welcomePageService.getWelcomePageVersionList(wlcmNumb, admin));
    }

    /** 신규 웰컴페이지 최초 버전을 등록한다. */
    @PostMapping
    public ResultData setWelcomePage(@RequestBody WelcomePageVO welcomePage
                                   , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS
                                , welcomePageService.setWelcomePage(welcomePage, admin));
    }

    /** 웰컴페이지 버전을 저장하거나 다음 초안을 생성한다. */
    @PutMapping("/{wlcmNumb}/{versNumb}")
    public ResultData uptWelcomePageVersion(@PathVariable Long wlcmNumb, @PathVariable Integer versNumb
                                          , @RequestBody WelcomePageVO welcomePage
                                          , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS
                , welcomePageService.uptWelcomePageVersion(wlcmNumb, versNumb, welcomePage, admin));
    }

    /** 선택한 웰컴페이지 버전을 사용자 화면에 배포한다. */
    @PostMapping("/{wlcmNumb}/{versNumb}/deploy")
    public ResultData uptWelcomePageDeploy(@PathVariable Long wlcmNumb, @PathVariable Integer versNumb
                                         , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.UPDATE_SUCCESS
                , welcomePageService.uptWelcomePageDeploy(wlcmNumb, versNumb, admin));
    }

    /** 웰컴페이지의 모든 버전을 삭제한다. */
    @DeleteMapping("/{wlcmNumb}")
    public ResultData delWelcomePage(@PathVariable Long wlcmNumb
                                   , @AuthenticationPrincipal AdminSessionVO admin) {
        welcomePageService.delWelcomePage(wlcmNumb, admin);
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }

    /** 웰컴페이지에 표시할 JPG 또는 PNG 이미지를 검증해 저장한다. */
    @PostMapping("/images")
    public ResultData setWelcomePageImage(@RequestParam("file") MultipartFile image) throws IOException {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, noticeImageService.setWelcomeImage(image));
    }
}
