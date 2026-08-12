package org.sadari.admin.sadariadmin.alimicon.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconSearchVO;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconVO;

/**
 * fileName       : AlimIconMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 알림 상황별 아이콘 데이터 접근 메서드를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 상황 식별 구조로 전환
 */
@Mapper
public interface AlimIconMapper {

    /**
     * 검색 조건과 페이지 범위에 맞는 ALIM_SITU 공통코드를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param search 아이콘 검색 조건과 페이지 범위
     * @return 아이콘 등록 여부가 포함된 알림 상황 목록
     */
    List<AlimIconVO> getAlimIconList(AlimIconSearchVO search);

    /**
     * 검색 조건에 맞는 ALIM_SITU 공통코드 수를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param search 아이콘 검색 조건
     * @return 검색 조건에 맞는 알림 상황 수
     */
    int getAlimIconListCount(AlimIconSearchVO search);

    /**
     * 알림 상황으로 공통코드와 선택적 아이콘 상세를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @return 알림 상황과 아이콘 상세
     */
    AlimIconVO getAlimIconDtl(@Param("alimSitu") String alimSitu);

    /**
     * 알림 상황으로 관리자 미리보기용 이미지 원본을 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @return MIME 유형과 이미지 원본
     */
    AlimIconVO getAlimIconImage(@Param("alimSitu") String alimSitu);

    /**
     * 아이콘 식별값이 ALIM_SITU 공통코드인지 확인한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @return 등록된 ALIM_SITU 공통코드 수
     */
    int getAlimSituCodeCount(@Param("alimSitu") String alimSitu);

    /**
     * 알림 상황별 아이콘 원본을 신규 등록하거나 현재 행에 직접 교체한다.
     *
     * @author SeungHyeon.Kang
     * @param icon 저장할 알림 상황별 아이콘
     * @return 저장된 행 수
     */
    int saveAlimIcon(AlimIconVO icon);
}
