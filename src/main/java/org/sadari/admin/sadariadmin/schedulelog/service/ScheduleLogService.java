package org.sadari.admin.sadariadmin.schedulelog.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleFailVO;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleLogSearchVO;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleLogVO;

import java.util.List;

/**
 * fileName       : ScheduleLogService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 인증된 관리자의 스케줄러 실행 로그 조회를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    실행 로그 단건 조회 계약 추가
 * 2026-07-31        SeungHyeon.Kang    스케줄러 로그 검색 조건 추가
 */
public interface ScheduleLogService {

    /**
     * 스케줄러 실행 결과 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 스케줄러 로그 검색 조건
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실행 결과 목록
     */
    PageData<ScheduleLogVO> getScheduleLogList(ScheduleLogSearchVO search, AdminSessionVO admin);

    /**
     * 선택한 스케줄러 실행 결과를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실행 결과
     */
    ScheduleLogVO getScheduleLogDtl(Long runxNumb, AdminSessionVO admin);

    /**
     * 선택한 스케줄러 실행의 실패 상세 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실패 상세 목록
     */
    List<ScheduleFailVO> getScheduleFailList(Long runxNumb, AdminSessionVO admin);
}
