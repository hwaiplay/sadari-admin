package org.sadari.admin.sadariadmin.usermenu.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;

/**
 * fileName       : UserMenuMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 관리 데이터 접근 기능을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 * 2026-08-10        SeungHyeon.Kang    3단계 인접 목록 메뉴 구조 적용
 * 2026-08-10        SeungHyeon.Kang    사용자 메뉴 직계 하위 목록 조회 추가
 * 2026-08-22        SeungHyeon.Kang    메뉴 트리 단위 페이징 건수 조회 추가
 */
@Mapper
public interface UserMenuMapper {

    /** 검색 조건에 맞는 사용자 메뉴 목록을 조회한다. */
    List<UserMenuVO> getUserMenuList(UserMenuSearchVO search);

    /** 검색 조건에 맞는 사용자 메뉴 전체 건수를 조회한다. */
    int getUserMenuCount(UserMenuSearchVO search);

    /** 사용자 메뉴의 최상위 메뉴 전체 건수를 조회한다. */
    int getUserMenuRootCount();

    /** 메뉴 번호로 사용자 메뉴 상세를 조회한다. */
    UserMenuVO getUserMenuDtl(@Param("menuNumb") Long menuNumb);

    /** 사용자 메뉴의 직계 하위 메뉴 목록을 조회한다. */
    List<UserMenuVO> getUserMenuChildList(@Param("menuNumb") Long menuNumb);

    /** 사용자 메뉴의 상위 메뉴 후보 목록을 조회한다. */
    List<UserMenuVO> getUserMenuParentList();

    /** 사용자 메뉴의 직계 하위 메뉴 건수를 조회한다. */
    int getUserMenuChildCount(@Param("menuNumb") Long menuNumb);

    /** 사용자 메뉴의 가장 깊은 하위 단계 차이를 조회한다. */
    int getMenuDescendantDepth(@Param("menuNumb") Long menuNumb);

    /** 지정한 메뉴가 하위 메뉴 트리에 포함되는지 조회한다. */
    int getMenuDescendantCnt(@Param("menuNumb") Long menuNumb,
                                   @Param("candidateNumb") Long candidateNumb);

    /** 사용자 메뉴를 등록한다. */
    void setUserMenu(UserMenuVO menu);

    /** 사용자 메뉴를 수정한다. */
    void uptUserMenu(UserMenuVO menu);

    /** 사용자 메뉴를 삭제한다. */
    void delUserMenu(@Param("menuNumb") Long menuNumb);
}
