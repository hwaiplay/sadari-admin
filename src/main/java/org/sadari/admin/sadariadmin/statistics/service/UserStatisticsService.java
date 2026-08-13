package org.sadari.admin.sadariadmin.statistics.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.statistics.vo.UserStatisticsVO;

/**
 * fileName       : UserStatisticsService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 사용자 현황과 정착 및 이탈 통계 조회 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    사용자 정착과 이탈 통계 응답 추가
 */
public interface UserStatisticsService {

    /**
     * 선택 일수의 사용자 현황과 정착 및 이탈 통계를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param days 오늘을 포함한 조회 일수
     * @param admin 로그인 관리자
     * @return 사용자 통계 대시보드 데이터
     */
    UserStatisticsVO getUserStatistics(int days, AdminSessionVO admin);
}
