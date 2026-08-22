package org.sadari.admin.sadariadmin.usermenu.mapper;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * fileName       : UserMenuMapperTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 사용자 메뉴 트리 단위 페이징 조회를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
@SpringBootTest
@ActiveProfiles("loc")
class UserMenuMapperTests {

    // 사용자 메뉴 데이터 접근 검증 대상
    @Autowired
    private UserMenuMapper userMenuMapper;

    /** 각 사용자 메뉴 분기가 한 페이지에만 포함되는지 확인한다. */
    @Test
    void getTreePagesWithoutDup() {
        // 실제 최상위 메뉴 건수로 조회할 전체 페이지 수를 계산한다
        int totalPages = (int) Math.ceil((double) userMenuMapper.getUserMenuRootCount()
                / PageRequest.PAGE_SIZE);
        // 서로 다른 페이지에 같은 메뉴가 포함되는지 확인할 전체 메뉴 번호를 생성한다
        Set<Long> allMenuNumbs = new HashSet<>();
        // 모든 최상위 메뉴 분기 페이지를 순서대로 조회한다
        for (int page = 1; page <= totalPages; page++) {
            // 현재 최상위 메뉴 분기 페이지의 조회 범위를 생성한다
            PageRequest pageRequest = new PageRequest(page);
            // 검색 조건이 없는 트리 단위 페이지 요청을 생성한다
            UserMenuSearchVO search = new UserMenuSearchVO();
            // 최상위 메뉴 분기 단위 조회 모드를 설정한다
            search.setTreeMode(true);
            // 현재 페이지의 최상위 메뉴 시작 순번을 설정한다
            search.setStartRow(pageRequest.getStartRow());
            // 현재 페이지의 최상위 메뉴 종료 순번을 설정한다
            search.setEndRow(pageRequest.getEndRow());
            // 실제 사용자 메뉴 분기 페이지를 조회한다
            List<UserMenuVO> menuList = userMenuMapper.getUserMenuList(search);
            // 현재 페이지에서 부모 메뉴 포함 여부를 확인할 메뉴 번호를 생성한다
            Set<Long> pageMenuNumbs = new HashSet<>();
            // 현재 페이지의 모든 메뉴 번호를 부모 확인 기준에 추가한다
            for (UserMenuVO menu : menuList) {
                // 현재 사용자 메뉴 번호를 페이지 기준에 추가한다
                pageMenuNumbs.add(menu.getMenuNumb());
            }

            // 현재 페이지 메뉴의 중복과 부모 포함 여부를 검증한다
            for (UserMenuVO menu : menuList) {
                // 앞선 페이지에 같은 메뉴가 없었는지 검증한다
                assertTrue(allMenuNumbs.add(menu.getMenuNumb()), "서로 다른 페이지에 같은 메뉴가 중복됐다");
                // 최상위 메뉴가 아니면 부모가 같은 페이지에 포함됐는지 검증한다
                if (!StringUtil.isEmpty(menu.getParnNumb())) {
                    // 하위 메뉴의 부모가 같은 최상위 메뉴 분기 페이지에 있는지 검증한다
                    assertTrue(pageMenuNumbs.contains(menu.getParnNumb()), "부모와 하위 메뉴의 페이지가 분리됐다");
                }

            }

        }
    }
}
