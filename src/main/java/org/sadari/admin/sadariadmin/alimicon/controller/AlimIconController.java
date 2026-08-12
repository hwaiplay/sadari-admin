package org.sadari.admin.sadariadmin.alimicon.controller;

import java.io.IOException;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alimicon.service.AlimIconService;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconSearchVO;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : AlimIconController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 관리자 알림 상황별 아이콘 조회와 저장 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 상황 식별 구조로 전환
 */
@RestController
@RequestMapping(Constant.API_ALIM_ICON_PREFIX)
public class AlimIconController {

    // 알림 아이콘 관리 서비스
    private final AlimIconService alimIconService;

    /**
     * 관리자 알림 아이콘 API를 생성한다.
     *
     * @author SeungHyeon.Kang
     * @param alimIconService 알림 아이콘 관리 서비스
     */
    public AlimIconController(AlimIconService alimIconService) {
        this.alimIconService = alimIconService;
    }

    /**
     * 검색 조건에 맞는 ALIM_SITU 공통코드와 아이콘 상태를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param search 아이콘 검색 조건
     * @param admin 로그인 관리자
     * @return 알림 상황별 아이콘 페이지 응답
     */
    @GetMapping
    public ResultData getAlimIconList(@ModelAttribute AlimIconSearchVO search
                                    , @AuthenticationPrincipal AdminSessionVO admin) {

        // 알림 상황별 아이콘 관리 페이지를 공통 응답으로 반환한다
        return ResultData.success(alimIconService.getAlimIconList(search, admin));
    }

    /**
     * 알림 상황으로 공통코드와 아이콘 상세를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @param admin 로그인 관리자
     * @return 알림 상황과 아이콘 상세 응답
     */
    @GetMapping("/{alimSitu}")
    public ResultData getAlimIconDtl(@PathVariable String alimSitu
                                   , @AuthenticationPrincipal AdminSessionVO admin) {

        // 요청한 알림 상황의 공통코드와 아이콘 상세를 반환한다
        return ResultData.success(alimIconService.getAlimIconDtl(alimSitu, admin));
    }

    /**
     * 관리자 화면 미리보기용 알림 아이콘 이미지를 조회한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @param admin 로그인 관리자
     * @return 알림 아이콘 바이너리 응답
     */
    @GetMapping("/{alimSitu}/image")
    public ResponseEntity<byte[]> getAlimIconImage(@PathVariable String alimSitu
                                                  , @AuthenticationPrincipal AdminSessionVO admin) {

        // 이미지 바이너리와 캐시 정보를 조회한다
        AlimIconVO icon = alimIconService.getAlimIconImage(alimSitu, admin);
        // 같은 URL의 이미지가 수정 즉시 반영되도록 캐시하지 않고 원본을 반환한다
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(icon.getMimeType()))
                .cacheControl(CacheControl.noStore())
                .body(icon.getIconData());
    }

    /**
     * ALIM_SITU 공통코드의 SVG 또는 PNG 아이콘을 등록하거나 교체한다.
     *
     * @author SeungHyeon.Kang
     * @param alimSitu 알림 상황 코드
     * @param file 등록하거나 교체할 SVG 또는 PNG 원본
     * @param admin 로그인 관리자
     * @return 저장 결과 응답
     * @throws IOException 업로드 원본을 읽지 못할 때 발생
     */
    @PutMapping(value = "/{alimSitu}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultData saveAlimIcon(@PathVariable String alimSitu, @RequestParam MultipartFile file
                                 , @AuthenticationPrincipal AdminSessionVO admin) throws IOException {

        // 알림 상황별 아이콘을 직접 저장하고 저장 성공 응답을 반환한다
        return ResultData.success(ResultEnum.SAVE_SUCCESS,
                alimIconService.saveAlimIcon(alimSitu, file, admin));
    }
}
