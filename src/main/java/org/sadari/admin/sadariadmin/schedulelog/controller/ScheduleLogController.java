package org.sadari.admin.sadariadmin.schedulelog.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.schedulelog.service.ScheduleLogService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * fileName       : ScheduleLogController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 스케줄러 실행 결과와 실패 상세 조회 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    실행 로그 단건 조회 API 추가
 */
@RestController
@RequestMapping(Constant.API_SCHEDULE_LOGS_PREFIX)
public class ScheduleLogController {

    // 스케줄러 로그 조회 서비스
    private final ScheduleLogService scheduleLogService;

    /**
     * 스케줄러 로그 조회 API를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param scheduleLogService 스케줄러 로그 조회 서비스
     */
    public ScheduleLogController(ScheduleLogService scheduleLogService) {
        this.scheduleLogService = scheduleLogService;
    }

    /**
     * 스케줄러 실행 결과 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실행 결과 목록 응답
     */
    @GetMapping
    public ResultData getScheduleLogList(@RequestParam(defaultValue = "1") int page
                                       , @AuthenticationPrincipal AdminSessionVO admin) {
        // 로그인한 관리자가 조회할 수 있는 스케줄러 실행 결과를 반환한다
        return ResultData.success(scheduleLogService.getScheduleLogList(page, admin));
    }

    /**
     * 선택한 스케줄러 실행 결과를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실행 결과 응답
     */
    @GetMapping("/{runxNumb}")
    public ResultData getScheduleLogDtl(@PathVariable Long runxNumb, @AuthenticationPrincipal AdminSessionVO admin) {
        // 실행 번호에 해당하는 스케줄러 실행 결과를 반환한다
        return ResultData.success(scheduleLogService.getScheduleLogDtl(runxNumb, admin));
    }

    /**
     * 선택한 스케줄러 실행의 실패 상세 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실패 상세 목록 응답
     */
    @GetMapping("/{runxNumb}/failures")
    public ResultData getScheduleFailList(@PathVariable Long runxNumb, @AuthenticationPrincipal AdminSessionVO admin) {
        // 선택한 실행 번호에 연결된 실패 상세 목록을 반환한다
        return ResultData.success(scheduleLogService.getScheduleFailList(runxNumb, admin));
    }
}
