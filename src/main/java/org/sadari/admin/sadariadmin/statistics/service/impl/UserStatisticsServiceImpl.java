package org.sadari.admin.sadariadmin.statistics.service.impl;

import lombok.RequiredArgsConstructor;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.statistics.mapper.UserStatisticsMapper;
import org.sadari.admin.sadariadmin.statistics.service.UserStatisticsService;
import org.sadari.admin.sadariadmin.statistics.vo.UserStatisticsSearchVO;
import org.sadari.admin.sadariadmin.statistics.vo.UserStatisticsVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * fileName       : UserStatisticsServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 조회 기간을 검증하고 사용자 현황과 정착 및 이탈 통계를 구성한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 * 2026-08-13        SeungHyeon.Kang    유지율과 전환 및 활동 구성과 이탈 추세 추가
 * 2026-08-13        SeungHyeon.Kang    사용자 통계 조회 기간을 1년까지 확장
 * 2026-08-13        SeungHyeon.Kang    미사용 전환과 활동 깊이 집계 제거
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserStatisticsServiceImpl implements UserStatisticsService {

    // 통계 화면에서 허용하는 조회 일수
    private static final Set<Integer> ALLOWED_DAYS = Set.of(30, 90, 180, 365);

    // 사용자 통계 조회 Mapper
    private final UserStatisticsMapper userStatisticsMapper;

    /**
     * 선택 일수의 사용자 현황과 가입 후 정착 및 계정 이탈 통계를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param days 오늘을 포함한 조회 일수
     * @param admin 로그인 관리자
     * @return 사용자 통계 대시보드 데이터
     */
    @Override
    public UserStatisticsVO getUserStatistics(int days, AdminSessionVO admin) {

        // 인증되지 않은 요청에는 운영 사용자 통계를 제공하지 않는다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }

        // 과도하거나 임의인 날짜 범위로 반복 집계 SQL이 실행되지 않도록 허용 범위를 제한한다
        if (!ALLOWED_DAYS.contains(days)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 오늘을 포함한 고정 일수의 조회 조건을 생성한다
        UserStatisticsSearchVO search = new UserStatisticsSearchVO();
        // 조회 종료일은 관리자 서버의 현재 일자로 설정한다
        search.setEndDate(LocalDate.now());
        // 시작일은 종료일을 포함해 선택 일수가 되도록 계산한다
        search.setStartDate(search.getEndDate().minusDays(days - 1L));

        // 네 통계 영역을 한 응답에 담을 객체를 생성한다
        UserStatisticsVO statistics = new UserStatisticsVO();
        // 화면에 표시할 조회 시작일을 설정한다
        statistics.setStartDate(search.getStartDate());
        // 화면에 표시할 조회 종료일을 설정한다
        statistics.setEndDate(search.getEndDate());
        // 최신 조회 결과의 생성 시각을 설정한다
        statistics.setGeneratedAt(LocalDateTime.now());
        // 현재 보관 중인 회원의 상태별 수를 설정한다
        statistics.setStatusList(userStatisticsMapper.getUserStatusList());
        // 날짜별 신규 가입과 활동 및 활성 회원 추세를 설정한다
        statistics.setTrendList(userStatisticsMapper.getUserTrendList(search));
        // 정상 회원의 미접속 기간별 수를 설정한다
        statistics.setInactivity(userStatisticsMapper.getUserInactivityDtl());
        // 가입 후 1일과 7일 및 30일 재방문 유지율을 설정한다
        statistics.setRetentionList(userStatisticsMapper.getUserRetentionList(search));
        // 일별 계정 비활성화와 영구 탈퇴 및 정지와 복구 수를 설정한다
        statistics.setChurnTrendList(userStatisticsMapper.getUserChurnTrendList(search));

        // 개인정보를 포함하지 않은 사용자 통계 응답을 반환한다
        return statistics;
    }
}
