package org.sadari.admin.sadariadmin.popup.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentVO;

import java.util.List;

/**
 * fileName       : PopupContentMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 사용자 안내 팝업 콘텐츠의 목록과 상세 및 저장 쿼리를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface PopupContentMapper {

    /**
     * 팝업 콘텐츠 목록의 현재 페이지 데이터를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param startRow 조회 시작 행 번호
     * @param endRow 조회 종료 행 번호
     * @return 현재 페이지의 팝업 콘텐츠 목록
     */
    List<PopupContentVO> getPopupContentList(@Param("startRow") int startRow, @Param("endRow") int endRow);

    /**
     * 전체 팝업 콘텐츠 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @return 전체 팝업 콘텐츠 건수
     */
    int getPopupContentListCnt();

    /**
     * 화면 구분과 팝업 코드로 팝업 콘텐츠 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @return 복합키에 해당하는 팝업 콘텐츠
     */
    PopupContentVO getPopupContentDtl(@Param("popuSitu") String popuSitu, @Param("popuCode") String popuCode);

    /**
     * 화면 구분과 팝업 코드가 같은 콘텐츠 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @return 동일한 복합키의 콘텐츠 건수
     */
    int getPopupContentCnt(@Param("popuSitu") String popuSitu, @Param("popuCode") String popuCode);

    /**
     * 사용 가능한 팝업 화면 구분 코드 건수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @return 사용 가능한 화면 구분 코드 건수
     */
    int getPopupSituCnt(@Param("popuSitu") String popuSitu);

    /**
     * 신규 팝업 콘텐츠를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 등록할 팝업 콘텐츠
     */
    void setPopupContent(PopupContentVO popupContent);

    /**
     * 기존 팝업의 제목과 네 개 콘텐츠 영역을 수정한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 수정할 팝업 콘텐츠
     */
    void uptPopupContent(PopupContentVO popupContent);
}
