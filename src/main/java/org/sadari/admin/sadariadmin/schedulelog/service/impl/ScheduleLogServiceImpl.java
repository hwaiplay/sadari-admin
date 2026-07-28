package org.sadari.admin.sadariadmin.schedulelog.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.schedulelog.mapper.ScheduleLogMapper;
import org.sadari.admin.sadariadmin.schedulelog.service.ScheduleLogService;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleFailVO;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleLogVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * fileName       : ScheduleLogServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 스케줄러 실행 로그 조회 업무를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    실행 로그 단건 조회 처리 추가
 */
@Service
@Transactional(readOnly = true)
public class ScheduleLogServiceImpl implements ScheduleLogService {

    // 스케줄러 로그 Mapper
    private final ScheduleLogMapper scheduleLogMapper;

    /**
     * 스케줄러 로그 조회 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param scheduleLogMapper 스케줄러 로그 Mapper
     */
    public ScheduleLogServiceImpl(ScheduleLogMapper scheduleLogMapper) {
        this.scheduleLogMapper = scheduleLogMapper;
    }

    /**
     * 스케줄러 실행 결과 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실행 결과 목록
     */
    @Override
    public List<ScheduleLogVO> getScheduleLogList(AdminSessionVO admin) {
        // 인증되지 않은 요청이 스케줄러 실행 정보를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 최신 스케줄러 실행 결과 목록을 조회한다
        return scheduleLogMapper.getScheduleLogList();
    }

    /**
     * 선택한 스케줄러 실행 결과를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실행 결과
     */
    @Override
    public ScheduleLogVO getScheduleLogDtl(Long runxNumb, AdminSessionVO admin) {
        // 인증되지 않은 요청이 스케줄러 실행 정보를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 부모 실행 로그를 특정할 수 있도록 실행 번호를 검증한다
        validateRunxNumb(runxNumb);
        // 실행 번호에 해당하는 부모 스케줄러 로그를 조회한다
        ScheduleLogVO scheduleLog = scheduleLogMapper.getScheduleLogDtl(runxNumb);

        // 실행 번호에 해당하는 부모 로그가 없으면 상세 화면을 구성할 수 없어 조회 결과 없음으로 분기한다
        if (StringUtil.isEmpty(scheduleLog)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMMON_NO_DATA);
        }

        // 실행 번호에 해당하는 스케줄러 실행 결과를 반환한다
        return scheduleLog;
    }

    /**
     * 선택한 스케줄러 실행의 실패 상세 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @param admin 로그인한 관리자 정보
     * @return 스케줄러 실패 상세 목록
     */
    @Override
    public List<ScheduleFailVO> getScheduleFailList(Long runxNumb, AdminSessionVO admin) {
        // 인증되지 않은 요청이 실패 상세 정보를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 부모 실행 로그를 특정할 수 있도록 실행 번호를 검증한다
        validateRunxNumb(runxNumb);

        // 선택한 실행 번호에 연결된 실패 상세 목록을 조회한다
        return scheduleLogMapper.getScheduleFailList(runxNumb);
    }

    /**
     * 스케줄러 실행 번호의 유효성을 확인한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     */
    private void validateRunxNumb(Long runxNumb) {
        // 실행 번호가 없거나 유효 범위가 아니면 부모 로그를 특정할 수 없어 요청 오류로 분기한다
        if (StringUtil.isEmpty(runxNumb) || runxNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 스케줄러 로그 조회 요청의 로그인 상태를 확인한다
     *
     * @author SeungHyeon.Kang
     * @param admin 로그인한 관리자 정보
     */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체가 없으면 로그인하지 않은 요청으로 판단한다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
