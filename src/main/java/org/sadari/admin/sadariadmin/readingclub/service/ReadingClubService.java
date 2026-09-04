package org.sadari.admin.sadariadmin.readingclub.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubActionVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubSearchVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubVO;

/**
 * fileName       : ReadingClubService
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 조회와 상태 조치 업무 계약을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
public interface ReadingClubService {

    /** 관리자 검색 조건에 맞는 독서 모임 페이지를 조회한다. */
    PageData<ReadingClubVO> getClubList(ReadingClubSearchVO search, AdminSessionVO admin);

    /** 모임 번호로 관리자용 독서 모임 상세를 조회한다. */
    ReadingClubVO getClubDtl(Long clubNumb, AdminSessionVO admin);

    /** 모임 번호로 관리자 조치 이력 페이지를 조회한다. */
    PageData<ReadingClubActionVO> getActionList(Long clubNumb, int pageNumber, AdminSessionVO admin);

    /** 모임 상태 조치와 감사 이력 및 영향 회원 알림을 함께 처리한다. */
    ReadingClubVO setClubAction(Long clubNumb, ReadingClubActionVO action, AdminSessionVO admin);
}
