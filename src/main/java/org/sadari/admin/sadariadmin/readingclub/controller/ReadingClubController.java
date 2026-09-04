package org.sadari.admin.sadariadmin.readingclub.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.readingclub.service.ReadingClubService;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubActionVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubSearchVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : ReadingClubController
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 조회와 상태 조치 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@RestController
@RequestMapping(Constant.API_READING_CLUBS_PREFIX)
public class ReadingClubController {

    // 관리자 독서 모임 서비스
    private final ReadingClubService readingClubService;

    /** 관리자 독서 모임 API를 생성한다. */
    public ReadingClubController(ReadingClubService readingClubService) {
        this.readingClubService = readingClubService;
    }

    /** 관리자 검색 조건에 맞는 독서 모임 목록을 조회한다. */
    @GetMapping
    public ResultData getClubList(@ModelAttribute ReadingClubSearchVO search
                                 , @AuthenticationPrincipal AdminSessionVO admin) {
        // 검색 조건에 맞는 관리자용 독서 모임 페이지를 반환한다.
        return ResultData.success(readingClubService.getClubList(search, admin));
    }

    /** 모임 번호로 관리자용 독서 모임 상세를 조회한다. */
    @GetMapping("/{clubNumb}")
    public ResultData getClubDtl(@PathVariable Long clubNumb
                                , @AuthenticationPrincipal AdminSessionVO admin) {
        // 회원 작성 소개는 조회만 가능한 관리자용 모임 상세를 반환한다.
        return ResultData.success(readingClubService.getClubDtl(clubNumb, admin));
    }

    /** 모임 번호로 관리자 조치 감사 이력을 조회한다. */
    @GetMapping("/{clubNumb}/actions")
    public ResultData getActionList(@PathVariable Long clubNumb
                                   , @RequestParam(name = "page", defaultValue = "1") int pageNumber
                                   , @AuthenticationPrincipal AdminSessionVO admin) {
        // 모임별 관리자 조치 감사 이력 페이지를 반환한다.
        return ResultData.success(readingClubService.getActionList(clubNumb, pageNumber, admin));
    }

    /** 모집 중지, 이용 정지, 해제 또는 종료 조치를 적용한다. */
    @PostMapping("/{clubNumb}/actions")
    public ResultData setClubAction(@PathVariable Long clubNumb
                                   , @RequestBody ReadingClubActionVO action
                                   , @AuthenticationPrincipal AdminSessionVO admin) {
        // 상태 변경과 감사 이력 및 영향 회원 알림이 완료된 최신 상세를 반환한다.
        return ResultData.success(readingClubService.setClubAction(clubNumb, action, admin));
    }
}
