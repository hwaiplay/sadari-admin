package org.sadari.admin.sadariadmin.usermenu.service.impl;

import java.util.List;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.usermenu.mapper.UserMenuMapper;
import org.sadari.admin.sadariadmin.usermenu.service.UserMenuService;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuSearchVO;
import org.sadari.admin.sadariadmin.usermenu.vo.UserMenuVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : UserMenuServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 사용자 메뉴 관리와 최대 3단계 계층 검증을 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    사용자 메뉴 목록 검색 조건 추가
 * 2026-08-10        SeungHyeon.Kang    3단계 인접 목록 메뉴 구조 적용
 * 2026-08-10        SeungHyeon.Kang    사용자 메뉴 직계 하위 목록 조회 추가
 */
@Service
@Transactional(readOnly = true)
public class UserMenuServiceImpl implements UserMenuService {

    /** 사용자 메뉴 Mapper */
    private final UserMenuMapper userMenuMapper;

    /** 사용자 메뉴 관리 서비스를 생성한다. */
    public UserMenuServiceImpl(UserMenuMapper userMenuMapper) {
        // 사용자 메뉴 데이터 접근 의존성을 설정한다
        this.userMenuMapper = userMenuMapper;
    }

    /** 사용자 메뉴 목록을 조회한다. */
    @Override
    public PageData<UserMenuVO> getUserMenuList(UserMenuSearchVO search, AdminSessionVO admin) {
        // 인증되지 않은 요청이 사용자 메뉴 정보를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 요청 페이지에 해당하는 조회 행 범위를 계산한다
        PageRequest pageRequest = new PageRequest(search.getPage());
        // 검색 조건에 페이지 시작 행을 설정한다
        search.setStartRow(pageRequest.getStartRow());
        // 검색 조건에 페이지 마지막 행을 설정한다
        search.setEndRow(pageRequest.getEndRow());
        // 검색 조건에 맞는 사용자 메뉴 목록과 전체 건수로 페이지 응답을 반환한다
        return PageData.of(userMenuMapper.getUserMenuList(search), userMenuMapper.getUserMenuCount(search),
                           pageRequest);
    }

    /** 사용자 메뉴 상세를 조회한다. */
    @Override
    public UserMenuVO getUserMenuDtl(Long menuNumb, AdminSessionVO admin) {
        // 사용자 메뉴 상세 조회 전에 로그인 상태를 확인한다
        checkLogin(admin);
        // 메뉴 번호로 사용자 메뉴 상세를 조회한다
        return getMenu(menuNumb);
    }

    /** 사용자 메뉴의 직계 하위 메뉴 목록을 조회한다. */
    @Override
    public List<UserMenuVO> getUserMenuChildList(Long menuNumb, AdminSessionVO admin) {
        // 사용자 메뉴 하위 목록 조회 전에 로그인 상태를 확인한다
        checkLogin(admin);
        // 조회 기준 사용자 메뉴가 존재하는지 확인한다
        getMenu(menuNumb);
        // 같은 부모 아래의 정렬 순서대로 직계 하위 메뉴 목록을 반환한다
        return userMenuMapper.getUserMenuChildList(menuNumb);
    }

    /** 사용자 메뉴의 상위 메뉴 후보 목록을 조회한다. */
    @Override
    public List<UserMenuVO> getUserMenuParentList(AdminSessionVO admin) {
        // 사용자 메뉴 상위 후보 조회 전에 로그인 상태를 확인한다
        checkLogin(admin);
        // 1단계와 2단계 사용자 메뉴를 상위 메뉴 후보로 반환한다
        return userMenuMapper.getUserMenuParentList();
    }

    /** 사용자 메뉴를 등록한다. */
    @Override
    @Transactional
    public UserMenuVO setUserMenu(UserMenuVO menu, AdminSessionVO admin) {
        // 사용자 메뉴 등록 전에 로그인 상태를 확인한다
        checkLogin(admin);
        // 사용자 메뉴 필수 입력값을 확인한다
        checkRequired(menu);
        // 선택한 상위 메뉴를 기준으로 신규 메뉴 단계를 계산한다
        setMenuHierarchy(menu, null);
        // 노출과 사용 여부 및 관리자 감사 정보를 설정한다
        setDefaults(menu, admin);
        // 검증된 사용자 메뉴를 등록한다
        userMenuMapper.setUserMenu(menu);
        // 생성된 메뉴 번호로 사용자 메뉴 상세를 반환한다
        return getMenu(menu.getMenuNumb());
    }

    /** 사용자 메뉴를 수정한다. */
    @Override
    @Transactional
    public UserMenuVO uptUserMenu(UserMenuVO menu, AdminSessionVO admin) {
        // 사용자 메뉴 수정 전에 로그인 상태를 확인한다
        checkLogin(admin);
        // 수정 대상 사용자 메뉴가 존재하는지 확인한다
        UserMenuVO savedMenu = getMenu(menu.getMenuNumb());
        // 사용자 메뉴 필수 입력값을 확인한다
        checkRequired(menu);
        // 부모 변경 시 순환과 최대 3단계 제한을 확인하고 메뉴 단계를 다시 계산한다
        setMenuHierarchy(menu, savedMenu);
        // 노출과 사용 여부 및 관리자 감사 정보를 설정한다
        setDefaults(menu, admin);
        // 검증된 사용자 메뉴를 수정한다
        userMenuMapper.uptUserMenu(menu);
        // 수정된 사용자 메뉴 상세를 반환한다
        return getMenu(menu.getMenuNumb());
    }

    /** 사용자 메뉴를 삭제한다. */
    @Override
    @Transactional
    public void delUserMenu(Long menuNumb, AdminSessionVO admin) {
        // 사용자 메뉴 삭제 전에 로그인 상태를 확인한다
        checkLogin(admin);
        // 삭제 대상 사용자 메뉴가 존재하는지 확인한다
        getMenu(menuNumb);
        // 하위 메뉴가 있으면 트리 단절을 막기 위해 삭제를 거부한다
        if (userMenuMapper.getUserMenuChildCount(menuNumb) > 0) {
            // "하위 메뉴가 있는 사용자 메뉴는 삭제할 수 없습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.USER_MENU_HAS_CHILDREN);
        }
        // 하위 메뉴가 없는 사용자 메뉴를 삭제한다
        userMenuMapper.delUserMenu(menuNumb);
    }

    /** 사용자 메뉴 필수 입력값을 확인한다. */
    private void checkRequired(UserMenuVO menu) {
        // 메뉴 객체 또는 메뉴명이 없으면 저장할 수 없는 요청으로 분기한다
        if (StringUtil.isEmpty(menu) || StringUtil.isEmpty(menu.getMenuName())) {
            // "필수값을 입력해 주세요."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
    }

    /** 사용자 메뉴의 상위 번호와 단계를 검증하고 설정한다. */
    private void setMenuHierarchy(UserMenuVO menu, UserMenuVO savedMenu) {
        // 최상위 메뉴의 기본 단계를 설정한다
        int menuLevl = 1;
        Long parnNumb = menu.getParnNumb();

        // 상위 메뉴를 선택한 경우 부모 존재 여부와 최대 단계를 검증한다
        if (parnNumb != null) {
            // 자기 자신을 상위 메뉴로 선택하면 순환 참조로 거부한다
            if (menu.getMenuNumb() != null && menu.getMenuNumb().equals(parnNumb)) {
                // "자기 자신이나 하위 메뉴를 상위 메뉴로 선택할 수 없습니다."
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.USER_MENU_CYCLE);
            }

            // 선택한 상위 사용자 메뉴를 조회한다
            UserMenuVO parentMenu = userMenuMapper.getUserMenuDtl(parnNumb);
            // 선택한 상위 메뉴가 없으면 유효하지 않은 계층 요청으로 거부한다
            if (StringUtil.isEmpty(parentMenu)) {
                // "유효한 상위 메뉴를 선택해 주세요."
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.USER_MENU_PARENT_INVALID);
            }
            // 3단계 메뉴 아래에 메뉴를 추가하지 못하도록 제한한다
            if (parentMenu.getMenuLevl() >= 3) {
                // "사용자 메뉴는 최대 3단계까지 등록할 수 있습니다."
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.USER_MENU_MAX_DEPTH);
            }

            // 상위 메뉴의 다음 단계를 현재 메뉴 단계로 계산한다
            menuLevl = parentMenu.getMenuLevl() + 1;
        }

        // 기존 메뉴를 수정할 때 선택한 부모가 자신의 하위 메뉴인지 확인한다
        if (savedMenu != null && parnNumb != null
                && userMenuMapper.getMenuDescendantCnt(savedMenu.getMenuNumb(), parnNumb) > 0) {
            // "자기 자신이나 하위 메뉴를 상위 메뉴로 선택할 수 없습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.USER_MENU_CYCLE);
        }

        // 기존 하위 트리를 새 단계로 이동해도 3단계를 넘지 않는지 확인한다
        if (savedMenu != null
                && menuLevl + userMenuMapper.getMenuDescendantDepth(savedMenu.getMenuNumb()) > 3) {
            // "사용자 메뉴는 최대 3단계까지 등록할 수 있습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.USER_MENU_MAX_DEPTH);
        }

        // 검증을 마친 메뉴 단계를 저장 대상에 설정한다
        menu.setMenuLevl(menuLevl);
    }

    /** 사용자 메뉴 기본값과 감사 정보를 설정한다. */
    private void setDefaults(UserMenuVO menu, AdminSessionVO admin) {
        // 노출 여부가 없으면 기본 노출 상태를 설정한다
        if (StringUtil.isEmpty(menu.getShowYsno())) {
            // 햄버거 메뉴 노출 여부 기본값을 Y로 설정한다
            menu.setShowYsno(Constant.YES);
        }
        // 노출 메뉴의 정렬 순서가 없거나 1보다 작으면 기본 정렬 순서를 설정한다
        if (Constant.YES.equals(menu.getShowYsno())
                && (StringUtil.isEmpty(menu.getSortOrdr())
                    || menu.getSortOrdr() < Constant.DEFAULT_MENU_SORT_ORDR)) {
            // 노출 메뉴 정렬 순서 기본값을 설정한다
            menu.setSortOrdr(Constant.DEFAULT_MENU_SORT_ORDR);
        // 숨김 메뉴는 정렬 대상이 아니므로 정렬 순서를 제거한다
        } else if (Constant.NO.equals(menu.getShowYsno())) {
            // 숨김 메뉴 정렬 순서를 비운다
            menu.setSortOrdr(null);
        }
        // 사용 여부가 없으면 기본 사용 상태를 설정한다
        if (StringUtil.isEmpty(menu.getUseeYsno())) {
            // 사용자 메뉴 사용 여부 기본값을 Y로 설정한다
            menu.setUseeYsno(Constant.YES);
        }
        // 신규 등록 감사 관리자 번호를 현재 관리자로 설정한다
        menu.setRegiAdmn(admin.getAdmnNumb());
        // 최종 수정 감사 관리자 번호를 현재 관리자로 설정한다
        menu.setUpdtAdmn(admin.getAdmnNumb());
    }

    /** 메뉴 번호로 사용자 메뉴를 조회하고 존재 여부를 확인한다. */
    private UserMenuVO getMenu(Long menuNumb) {
        // 메뉴 번호로 사용자 메뉴 상세를 조회한다
        UserMenuVO menu = userMenuMapper.getUserMenuDtl(menuNumb);
        // 메뉴 번호에 해당하는 사용자 메뉴가 없으면 조회 결과 없음으로 분기한다
        if (StringUtil.isEmpty(menu)) {
            // "메뉴를 찾을 수 없습니다."
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.MENU_NOT_FOUND);
        }
        // 존재하는 사용자 메뉴 상세를 반환한다
        return menu;
    }

    /** 로그인 상태를 확인한다. */
    private void checkLogin(AdminSessionVO admin) {
        // 관리자 세션이 없으면 인증이 필요한 요청으로 거부한다
        if (StringUtil.isEmpty(admin)) {
            // "로그인이 필요합니다."
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
