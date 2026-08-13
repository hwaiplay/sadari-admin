package org.sadari.admin.sadariadmin.statistics.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sadari.admin.sadariadmin.statistics.vo.UserStatisticsSearchVO;
import org.sadari.admin.sadariadmin.statistics.vo.UserStatisticsVO;

import java.util.List;

/**
 * fileName       : UserStatisticsMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 사용자 통계 조회 SQL 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    사용자 정착과 이탈 통계 조회 추가
 */
@Mapper
public interface UserStatisticsMapper {

    /**
     * 현재 보관 중인 회원을 상태별로 집계한다
     *
     * @author SeungHyeon.Kang
     * @return 상태별 현재 회원 수
     */
    List<UserStatisticsVO.UserStatusStatistics> getUserStatusList();

    /**
     * 선택 기간의 신규 가입과 활동 및 활성 회원 추세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 조회 시작일과 종료일
     * @return 일별 사용자 추세
     */
    List<UserStatisticsVO.UserTrendStatistics> getUserTrendList(UserStatisticsSearchVO search);

    /**
     * 정상 회원의 30일 이상 미접속 구간별 수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 미접속 기간별 회원 수
     */
    UserStatisticsVO.UserInactivityStatistics getUserInactivityDtl();

    /**
     * 선택 기간 가입자의 가입 후 1일과 7일 및 30일 재방문율을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 조회 시작일과 종료일
     * @return 기간별 가입자 재방문 유지율
     */
    List<UserStatisticsVO.UserRetentionStatistics> getUserRetentionList(UserStatisticsSearchVO search);

    /**
     * 선택 기간 가입자의 온보딩과 독후감 및 소셜 활동 전환 수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 조회 시작일과 종료일
     * @return 단계별 핵심 행동 전환 수
     */
    UserStatisticsVO.UserConversionStatistics getUserConversionDtl(UserStatisticsSearchVO search);

    /**
     * 현재 정상 회원의 선택 기간 활동을 가장 깊은 행동 단계로 분류한다
     *
     * @author SeungHyeon.Kang
     * @param search 조회 시작일과 종료일
     * @return 활동 깊이별 정상 회원 수
     */
    UserStatisticsVO.UserActivityCompositionStatistics getUserActivityDtl(UserStatisticsSearchVO search);

    /**
     * 선택 기간의 계정 비활성화와 영구 탈퇴 및 정지와 복구 추세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 조회 시작일과 종료일
     * @return 일별 계정 이탈과 복구 처리 수
     */
    List<UserStatisticsVO.UserChurnTrendStatistics> getUserChurnTrendList(UserStatisticsSearchVO search);
}
