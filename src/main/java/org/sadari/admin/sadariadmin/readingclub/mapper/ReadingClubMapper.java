package org.sadari.admin.sadariadmin.readingclub.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubActionVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubSearchVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubVO;

/**
 * fileName       : ReadingClubMapper
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 조회와 상태 조치 SQL 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@Mapper
public interface ReadingClubMapper {

    /** 관리자 검색 조건에 맞는 독서 모임 목록을 조회한다. */
    List<ReadingClubVO> getClubList(ReadingClubSearchVO search);

    /** 관리자 검색 조건에 맞는 독서 모임 건수를 조회한다. */
    int getClubCnt(ReadingClubSearchVO search);

    /** 모임 번호로 관리자용 독서 모임 상세를 조회한다. */
    ReadingClubVO getClubDtl(@Param("clubNumb") Long clubNumb);

    /** 상태 조치 직렬화를 위해 독서 모임 한 건을 잠금 조회한다. */
    ReadingClubVO getClubForUpdate(@Param("clubNumb") Long clubNumb);

    /** 모임의 최신 관리자 조치 이력을 조회한다. */
    ReadingClubActionVO getLatestAction(@Param("clubNumb") Long clubNumb);

    /** 모임의 관리자 조치 이력 목록을 조회한다. */
    List<ReadingClubActionVO> getActionList(@Param("clubNumb") Long clubNumb
                                           , @Param("startRow") int startRow
                                           , @Param("endRow") int endRow);

    /** 모임의 관리자 조치 이력 건수를 조회한다. */
    int getActionCnt(@Param("clubNumb") Long clubNumb);

    /** 신규 회원 모집을 중지한다. */
    int uptRecruitStopped(@Param("clubNumb") Long clubNumb);

    /** 관리자 조치로 모임 이용을 정지한다. */
    int uptClubSuspended(@Param("clubNumb") Long clubNumb);

    /** 관리자 조치로 제한된 모임을 정상 운영 상태로 복원한다. */
    int uptClubRestored(@Param("clubNumb") Long clubNumb);

    /** 관리자 조치로 모임 운영을 종료한다. */
    int uptClubClosed(@Param("clubNumb") Long clubNumb);

    /** 관리자 모임 조치 감사 이력을 등록한다. */
    int setClubAction(ReadingClubActionVO action);

    /** 모임장과 활성 모임원에게 관리자 조치 알림을 등록한다. */
    int setActionNotifications(ReadingClubActionVO action);
}
