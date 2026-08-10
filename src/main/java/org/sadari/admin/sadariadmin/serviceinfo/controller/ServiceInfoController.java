package org.sadari.admin.sadariadmin.serviceinfo.controller;

import java.io.IOException;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.notice.service.NoticeImageService;
import org.sadari.admin.sadariadmin.serviceinfo.service.ServiceInfoService;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoSearchVO;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : ServiceInfoController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 관리자 서비스 정보 버전과 배포 및 Summernote 이미지 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
@RestController
@RequestMapping(Constant.API_SERVICE_INFO_PREFIX)
public class ServiceInfoController {

    // 서비스 정보 관리 서비스
    private final ServiceInfoService serviceInfoService;
    // Summernote 공용 이미지 저장 서비스
    private final NoticeImageService noticeImageService;

    /**
     * 서비스 정보 API에 업무와 이미지 저장 서비스를 주입한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfoService 서비스 정보 버전 관리 서비스
     * @param noticeImageService Summernote 이미지 저장 서비스
     */
    public ServiceInfoController(ServiceInfoService serviceInfoService, NoticeImageService noticeImageService) {
        this.serviceInfoService = serviceInfoService;
        this.noticeImageService = noticeImageService;
    }

    /**
     * 카테고리별 대표 서비스 정보 버전 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 서비스 정보 검색과 페이징 조건
     * @param admin 접근하는 인증 관리자 정보
     * @return 카테고리별 대표 서비스 정보 버전 목록
     */
    @GetMapping
    public ResultData getServiceInfoList(@ModelAttribute ServiceInfoSearchVO search
                                       , @AuthenticationPrincipal AdminSessionVO admin) {
        // 관리자 목록 화면에 페이징된 서비스 정보를 반환한다.
        return ResultData.success(serviceInfoService.getServiceInfoList(search, admin));
    }

    /**
     * 카테고리와 버전으로 서비스 정보 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 조회할 버전 번호
     * @param admin 접근하는 인증 관리자 정보
     * @return 지정 버전의 서비스 정보 상세
     */
    @GetMapping("/{cateCode}/{versNumb}")
    public ResultData getServiceInfoDtl(@PathVariable String cateCode, @PathVariable Integer versNumb
                                      , @AuthenticationPrincipal AdminSessionVO admin) {
        // 요청한 서비스 정보 버전 상세를 반환한다.
        return ResultData.success(serviceInfoService.getServiceInfoDtl(cateCode, versNumb, admin));
    }

    /**
     * 같은 카테고리의 서비스 정보 버전 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param admin 접근하는 인증 관리자 정보
     * @return 카테고리에 속한 전체 서비스 정보 버전
     */
    @GetMapping("/{cateCode}/versions")
    public ResultData getServiceInfoVersionList(@PathVariable String cateCode
                                              , @AuthenticationPrincipal AdminSessionVO admin) {
        // 카테고리에 연결된 전체 버전을 최신순으로 반환한다.
        return ResultData.success(serviceInfoService.getServiceInfoVersionList(cateCode, admin));
    }

    /**
     * 글이 없는 카테고리에 최초 서비스 정보 버전을 등록한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfo 최초 버전으로 저장할 서비스 정보
     * @param admin 등록하는 인증 관리자 정보
     * @return 등록된 최초 서비스 정보 버전
     */
    @PostMapping
    public ResultData setServiceInfo(@RequestBody ServiceInfoVO serviceInfo
                                   , @AuthenticationPrincipal AdminSessionVO admin) {
        // "저장했습니다."
        return ResultData.success(ResultEnum.SAVE_SUCCESS, serviceInfoService.setServiceInfo(serviceInfo, admin));
    }

    /**
     * 배포 상태에 따라 같은 버전을 수정하거나 다음 버전을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 수정 기준 버전 번호
     * @param serviceInfo 저장할 제목과 HTML 본문
     * @param admin 수정하는 인증 관리자 정보
     * @return 저장된 초안 버전
     */
    @PutMapping("/{cateCode}/{versNumb}")
    public ResultData uptServiceInfoVersion(@PathVariable String cateCode, @PathVariable Integer versNumb
                                          , @RequestBody ServiceInfoVO serviceInfo
                                          , @AuthenticationPrincipal AdminSessionVO admin) {
        ServiceInfoVO saved = serviceInfoService.uptServiceInfoVersion(cateCode, versNumb, serviceInfo, admin);
        // "저장했습니다."
        return ResultData.success(ResultEnum.SAVE_SUCCESS, saved);
    }

    /**
     * 선택한 서비스 정보 버전을 현재 사용자 배포본으로 전환한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 배포할 버전 번호
     * @param admin 배포하는 인증 관리자 정보
     * @return 현재 배포본으로 전환된 서비스 정보 버전
     */
    @PostMapping("/{cateCode}/{versNumb}/deploy")
    public ResultData uptServiceInfoDeploy(@PathVariable String cateCode, @PathVariable Integer versNumb
                                         , @AuthenticationPrincipal AdminSessionVO admin) {
        // "수정했습니다."
        return ResultData.success(ResultEnum.UPDATE_SUCCESS
                                , serviceInfoService.uptServiceInfoDeploy(cateCode, versNumb, admin));
    }

    /**
     * 카테고리에 연결된 서비스 정보 전체 버전을 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 삭제할 서비스 정보 카테고리 코드
     * @param admin 삭제하는 인증 관리자 정보
     * @return 서비스 정보 전체 버전 삭제 결과
     */
    @DeleteMapping("/{cateCode}")
    public ResultData delServiceInfo(@PathVariable String cateCode
                                   , @AuthenticationPrincipal AdminSessionVO admin) {
        serviceInfoService.delServiceInfo(cateCode, admin);
        // "삭제했습니다."
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }

    /**
     * 서비스 정보 Summernote 본문 이미지를 공용 콘텐츠 경로에 저장한다
     *
     * @author SeungHyeon.Kang
     * @param image 저장할 Summernote 이미지 파일
     * @return 저장된 이미지의 사용자 접근 경로
     * @throws IOException 이미지 파일 저장에 실패할 때 발생한다
     */
    @PostMapping("/images")
    public ResultData setServiceInfoImage(@RequestParam("file") MultipartFile image) throws IOException {
        // "저장했습니다."
        return ResultData.success(ResultEnum.SAVE_SUCCESS, noticeImageService.setNoticeImage(image));
    }
}
