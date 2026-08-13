package org.sadari.admin.sadariadmin.statistics.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.statistics.service.UserStatisticsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : UserStatisticsController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 사용자 통계 대시보드 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    사용자 정착과 이탈 통계 응답 추가
 */
@RestController
@RequestMapping(Constant.API_USER_STATISTICS_PREFIX)
public class UserStatisticsController {

    // 사용자 통계 조회 서비스
    private final UserStatisticsService userStatisticsService;

    /**
     * 사용자 통계 API를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param userStatisticsService 사용자 통계 조회 서비스
     */
    public UserStatisticsController(UserStatisticsService userStatisticsService) {

        this.userStatisticsService = userStatisticsService;
    }

    /**
     * 사용자 현황과 가입 후 정착 및 계정 이탈 통계를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param days 오늘을 포함한 조회 일수
     * @param admin 로그인 관리자
     * @return 사용자 통계 대시보드 데이터
     */
    @GetMapping
    public ResultData getUserStatistics(@RequestParam(defaultValue = "30") int days
                                       , @AuthenticationPrincipal AdminSessionVO admin) {
        // 검증된 기간의 비식별 사용자 통계를 반환한다
        return ResultData.success(userStatisticsService.getUserStatistics(days, admin));
    }
}
