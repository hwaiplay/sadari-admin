package org.sadari.admin.sadariadmin.adminauth.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.adminauth.service.AdminAuthManageService;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthSearchVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * fileName       : AdminAuthManageController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 관리자 권한 부여 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    관리자 권한 목록 검색 조건 추가
 */
@RestController
@RequestMapping(Constant.API_ADMIN_AUTHS_PREFIX)
public class AdminAuthManageController {

    /** 관리자 권한 부여 서비스 */
    private final AdminAuthManageService adminAuthManageService;

    /** 관리자 권한 부여 API 생성 */
    public AdminAuthManageController(AdminAuthManageService adminAuthManageService) {
        this.adminAuthManageService = adminAuthManageService;
    }

    /** 관리자 권한 부여 화면 데이터 조회 */
    @GetMapping
    public ResultData getAdminAuthManage(@ModelAttribute AdminAuthSearchVO search
                                       , @AuthenticationPrincipal AdminSessionVO admin) {
        // 검색 조건에 맞는 관리자 권한 화면 데이터를 반환한다
        return ResultData.success(adminAuthManageService.getAdminAuthManage(search, admin));
    }

    /** 관리자 권한 일괄 수정 */
    @PutMapping
    public ResultData uptAdminAuthList(@RequestBody List<AdminAuthVO> admins
                                    , @ModelAttribute AdminAuthSearchVO search
                                    , @AuthenticationPrincipal AdminSessionVO admin) {
        // 수정 후에도 현재 검색 조건을 유지한 관리자 권한 목록을 반환한다
        return ResultData.success(ResultEnum.UPDATE_SUCCESS
                , adminAuthManageService.uptAdminAuthList(admins, search, admin));
    }
}
