package org.sadari.admin.sadariadmin.authgroup.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.authgroup.service.AuthGroupService;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupSearchVO;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : AuthGroupController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 권한그룹 관리 API
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    권한그룹 목록 검색 조건 추가
 */
@RestController
@RequestMapping(Constant.API_AUTH_GROUP_PREFIX)
public class AuthGroupController {

    /** 권한그룹 관리 서비스 */
    private final AuthGroupService authGroupService;

    /** 권한그룹 관리 API 생성 */
    public AuthGroupController(AuthGroupService authGroupService) {
        this.authGroupService = authGroupService;
    }

    /** 권한그룹 목록 조회 */
    @GetMapping
    public ResultData getAuthGroupList(@ModelAttribute AuthGroupSearchVO search
                                     , @AuthenticationPrincipal AdminSessionVO admin) {
        // 검색 조건에 맞는 권한그룹 목록을 반환한다
        return ResultData.success(authGroupService.getAuthGroupList(search, admin));
    }

    /** 권한그룹 상세 조회 */
    @GetMapping("/{authCode}")
    public ResultData getAuthGroup(@PathVariable String authCode, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(authGroupService.getAuthGroup(authCode, admin));
    }

    /** 권한 코드 중복 확인 */
    @GetMapping("/{authCode}/duplicate")
    public ResultData isDuplicate(@PathVariable String authCode, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(authGroupService.isDuplicate(authCode, admin));
    }

    /** 권한그룹 등록 */
    @PostMapping
    public ResultData setAuthGroup(@RequestBody AuthGroupVO authGroup, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, authGroupService.setAuthGroup(authGroup, admin));
    }

    /** 권한그룹 수정 */
    @PutMapping("/{authCode}")
    public ResultData uptAuthGroup(@PathVariable String authCode, @RequestBody AuthGroupVO authGroup, @AuthenticationPrincipal AdminSessionVO admin) {
        authGroup.setAuthCode(authCode);
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, authGroupService.uptAuthGroup(authGroup, admin));
    }

    /** 권한그룹 삭제 */
    @DeleteMapping("/{authCode}")
    public ResultData delAuthGroup(@PathVariable String authCode, @AuthenticationPrincipal AdminSessionVO admin) {
        authGroupService.delAuthGroup(authCode, admin);
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }
}
