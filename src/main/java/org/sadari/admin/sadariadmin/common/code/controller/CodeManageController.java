package org.sadari.admin.sadariadmin.common.code.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.service.CodeManageService;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterSearchVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
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
 * fileName       : CodeManageController
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 코드관리 API 컨트롤러 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    공통코드 목록 검색 조건 추가
 */
@RestController
@RequestMapping(Constant.API_CODE_MANAGE_PREFIX)
public class CodeManageController {

    /** 코드관리 서비스 */
    private final CodeManageService codeManageService;

    /**
     * 코드관리 API 컨트롤러 생성
     * @author SeungHyeon.Kang
     * @param codeManageService
     * @return
     */
    public CodeManageController(CodeManageService codeManageService) {
        this.codeManageService = codeManageService;
    }

    /**
     * 공통코드 목록 조회
     * @author SeungHyeon.Kang
     * @return
     */
    @GetMapping("/masters")
    public ResultData getCommCodeList(@ModelAttribute CodeMasterSearchVO search) {
        // 검색 조건에 맞는 공통코드 목록을 반환한다
        return ResultData.success(codeManageService.getCommCodeList(search));
    }

    /**
     * 공통코드 상세 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    @GetMapping("/masters/{commCode}")
    public ResultData getCommCodeDtl(@PathVariable String commCode) {
        return ResultData.success(codeManageService.getCommCodeDtl(commCode));
    }

    /**
     * 공통코드 중복 여부 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    @GetMapping("/masters/{commCode}/duplicate")
    public ResultData isCommCodeDuplicate(@PathVariable String commCode) {
        return ResultData.success(codeManageService.isCommCodeDuplicate(commCode));
    }

    /**
     * 공통코드 등록
     * @author SeungHyeon.Kang
     * @param codeMaster
     * @param admin
     * @return
     */
    @PostMapping("/masters")
    public ResultData setCommCode(
            @RequestBody CodeMasterVO codeMaster,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, codeManageService.setCommCode(codeMaster, admin));
    }

    /**
     * 공통코드 수정
     * @author SeungHyeon.Kang
     * @param commCode
     * @param codeMaster
     * @return
     */
    @PutMapping("/masters/{commCode}")
    public ResultData uptCommCode(
            @PathVariable String commCode,
            @RequestBody CodeMasterVO codeMaster,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, codeManageService.uptCommCode(commCode, codeMaster, admin));
    }

    /**
     * 세부코드 목록 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    @GetMapping("/masters/{commCode}/details")
    public ResultData getComdCodeList(@PathVariable String commCode) {
        return ResultData.success(codeManageService.getComdCodeList(commCode));
    }

    /**
     * 세부코드 등록
     * @author SeungHyeon.Kang
     * @param commCode
     * @param code
     * @param admin
     * @return
     */
    @PostMapping("/masters/{commCode}/details")
    public ResultData setComdCode(
            @PathVariable String commCode,
            @RequestBody CodeVO code,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, codeManageService.setComdCode(commCode, code, admin));
    }

    /**
     * 세부코드 수정
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @param code
     * @return
     */
    @PutMapping("/masters/{commCode}/details/{comdCode}")
    public ResultData uptComdCode(
            @PathVariable String commCode,
            @PathVariable String comdCode,
            @RequestBody CodeVO code,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, codeManageService.uptComdCode(commCode, comdCode, code, admin));
    }

    /**
     * 세부코드 삭제
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    @DeleteMapping("/masters/{commCode}/details/{comdCode}")
    public ResultData delComdCode(@PathVariable String commCode, @PathVariable String comdCode) {
        codeManageService.delComdCode(commCode, comdCode);
        return ResultData.success();
    }
}
