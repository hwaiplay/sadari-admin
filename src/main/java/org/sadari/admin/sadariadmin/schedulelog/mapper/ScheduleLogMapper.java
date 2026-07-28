package org.sadari.admin.sadariadmin.schedulelog.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleFailVO;
import org.sadari.admin.sadariadmin.schedulelog.vo.ScheduleLogVO;

import java.util.List;

/**
 * fileName       : ScheduleLogMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-28
 * description    : 스케줄러 실행 로그와 실패 상세 로그를 조회한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-28        SeungHyeon.Kang    최초 생성
 * 2026-07-28        SeungHyeon.Kang    실행 로그 단건 조회 추가
 */
@Mapper
public interface ScheduleLogMapper {

    /**
     * 스케줄러 실행 결과 목록을 최신 실행 순서로 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 스케줄러 실행 결과 목록
     */
    List<ScheduleLogVO> getScheduleLogList(@org.apache.ibatis.annotations.Param("startRow") int startRow
                                         , @org.apache.ibatis.annotations.Param("endRow") int endRow);

    int getScheduleLogListCount();

    /**
     * 스케줄러 실행 번호로 실행 결과 단건을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @return 스케줄러 실행 결과
     */
    ScheduleLogVO getScheduleLogDtl(Long runxNumb);

    /**
     * 스케줄러 실행 번호에 연결된 실패 상세 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param runxNumb 스케줄러 실행 번호
     * @return 스케줄러 실패 상세 목록
     */
    List<ScheduleFailVO> getScheduleFailList(Long runxNumb);
}
