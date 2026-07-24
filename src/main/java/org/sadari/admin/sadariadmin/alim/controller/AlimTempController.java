package org.sadari.admin.sadariadmin.alim.controller;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alim.service.AlimTempService;
import org.sadari.admin.sadariadmin.alim.vo.AlimTempVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 알림 템플릿 관리 API 컨트롤러
 */
@RestController
@RequestMapping(Constant.API_ALIM_TEMP_PREFIX)
public class AlimTempController {

    /** 알림 템플릿 관리 서비스 */
    private final AlimTempService alimTempService;

    /**
     * 알림 템플릿 관리 API 컨트롤러 생성
     * @Author SeungHyeon.Kang
     * @param alimTempService
     * @return
     */
    public AlimTempController(AlimTempService alimTempService) {
        this.alimTempService = alimTempService;
    }

    /**
     * 알림 템플릿 목록 조회
     * @Author SeungHyeon.Kang
     * @param admin
     * @return
     */
    @GetMapping
    public ResultData getAlimTempList(@AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(alimTempService.getAlimTempList(admin));
    }

    /**
     * 알림 템플릿 상세 조회
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param admin
     * @return
     */
    @GetMapping("/{alimSitu}/{tempCode}")
    public ResultData getAlimTempDtl(
            @PathVariable String alimSitu,
            @PathVariable String tempCode,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(alimTempService.getAlimTempDtl(alimSitu, tempCode, admin));
    }

    /**
     * 알림 템플릿 중복 여부 조회
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param admin
     * @return
     */
    @GetMapping("/{alimSitu}/{tempCode}/duplicate")
    public ResultData getAlimTempDuplicate(
            @PathVariable String alimSitu,
            @PathVariable String tempCode,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(alimTempService.getAlimTempDuplicate(alimSitu, tempCode, admin));
    }

    /**
     * 알림 템플릿 등록
     * @Author SeungHyeon.Kang
     * @param alimTemp
     * @param admin
     * @return
     */
    @PostMapping
    public ResultData setAlimTemp(@RequestBody AlimTempVO alimTemp, @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, alimTempService.setAlimTemp(alimTemp, admin));
    }

    /**
     * 알림 템플릿 수정
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param alimTemp
     * @param admin
     * @return
     */
    @PutMapping("/{alimSitu}/{tempCode}")
    public ResultData uptAlimTemp(
            @PathVariable String alimSitu,
            @PathVariable String tempCode,
            @RequestBody AlimTempVO alimTemp,
            @AuthenticationPrincipal AdminSessionVO admin
    ) {
        return ResultData.success(ResultEnum.UPDATE_SUCCESS, alimTempService.uptAlimTemp(alimSitu, tempCode, alimTemp, admin));
    }
}
