package org.sadari.admin.sadariadmin.common.code.controller;

import org.sadari.admin.sadariadmin.common.util.CodeUtil;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 공통코드 읽기 API 컨트롤러
 */
@RestController
@RequestMapping(Constant.API_CODES_PREFIX)
public class CodeController {

    /** 공통코드 조회 유틸 */
    private final CodeUtil codeUtil;

    /**
     * 공통코드 읽기 API 컨트롤러 생성
     * @Author SeungHyeon.Kang
     * @param codeUtil
     * @return
     */
    public CodeController(CodeUtil codeUtil) {
        this.codeUtil = codeUtil;
    }

    /**
     * 세부코드 목록 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    @GetMapping("/{commCode}")
    public ResultData getCodeList(@PathVariable String commCode) {
        return ResultData.success(codeUtil.getCodeList(commCode));
    }

    /**
     * 세부코드명 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    @GetMapping("/{commCode}/{comdCode}/name")
    public ResultData getCodeName(@PathVariable String commCode, @PathVariable String comdCode) {
        return ResultData.success(codeUtil.getCodeName(commCode, comdCode));
    }

    /**
     * 세부코드명 단건 조회
     * @Author SeungHyeon.Kang
     * @param comdCode
     * @return
     */
    @GetMapping("/detail/{comdCode}/name")
    public ResultData getCodeName(@PathVariable String comdCode) {
        return ResultData.success(codeUtil.getCodeName(comdCode));
    }
}
