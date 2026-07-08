package org.sadari.admin.sadariadmin.common.code.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.service.CodeManageService;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코드관리 API 컨트롤러.
 */
@RestController
@RequestMapping("/api/code-manage")
public class CodeManageController {

    /** 코드관리 서비스. */
    private final CodeManageService codeManageService;

    public CodeManageController(CodeManageService codeManageService) {
        this.codeManageService = codeManageService;
    }

    /**
     * 공통코드 목록을 조회한다.
     */
    @GetMapping("/masters")
    public ResultData getCommCodeList() {
        return ResultData.success(codeManageService.getCommCodeList());
    }

    /**
     * 공통코드 상세를 조회한다.
     */
    @GetMapping("/masters/{commCode}")
    public ResultData getCommCodeDtl(@PathVariable String commCode) {
        return ResultData.success(codeManageService.getCommCodeDtl(commCode));
    }

    /**
     * 공통코드 중복 여부를 조회한다.
     */
    @GetMapping("/masters/{commCode}/duplicate")
    public ResultData isCommCodeDuplicate(@PathVariable String commCode) {
        return ResultData.success(codeManageService.isCommCodeDuplicate(commCode));
    }

    /**
     * 공통코드를 등록한다.
     */
    @PostMapping("/masters")
    public ResultData setCommCode(
            @RequestBody CodeMasterVO codeMaster,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(codeManageService.setCommCode(codeMaster, admin));
    }

    /**
     * 세부코드 목록을 조회한다.
     */
    @GetMapping("/masters/{commCode}/details")
    public ResultData getComdCodeList(@PathVariable String commCode) {
        return ResultData.success(codeManageService.getComdCodeList(commCode));
    }

    /**
     * 세부코드를 등록한다.
     */
    @PostMapping("/masters/{commCode}/details")
    public ResultData setComdCode(
            @PathVariable String commCode,
            @RequestBody CodeVO code,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(codeManageService.setComdCode(commCode, code, admin));
    }

    /**
     * 세부코드를 삭제한다.
     */
    @DeleteMapping("/masters/{commCode}/details/{comdCode}")
    public ResultData delComdCode(@PathVariable String commCode, @PathVariable String comdCode) {
        codeManageService.delComdCode(commCode, comdCode);
        return ResultData.success();
    }
}
