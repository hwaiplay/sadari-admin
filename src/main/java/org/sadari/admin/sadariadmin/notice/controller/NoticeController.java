package org.sadari.admin.sadariadmin.notice.controller;

import java.io.IOException;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.notice.service.NoticeImageService;
import org.sadari.admin.sadariadmin.notice.service.NoticeService;
import org.sadari.admin.sadariadmin.notice.vo.NoticeSearchVO;
import org.sadari.admin.sadariadmin.notice.vo.NoticeVO;
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
 * fileName       : NoticeController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 관리자 공지사항 버전과 배포 및 삭제와 Summernote 이미지 API를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    버전 목록 조회와 공지 전체 삭제 API 추가
 * 2026-08-08        SeungHyeon.Kang    현재 배포 상태 기준 공지 수정 API 적용
 */
@RestController
@RequestMapping(Constant.API_NOTICE_PREFIX)
public class NoticeController {

    // 공지사항 관리 서비스
    private final NoticeService noticeService;
    // 공지사항 이미지 저장 서비스
    private final NoticeImageService noticeImageService;

    /** 공지사항 API에 업무 서비스를 주입한다. */
    public NoticeController(NoticeService noticeService, NoticeImageService noticeImageService) {
        this.noticeService = noticeService;
        this.noticeImageService = noticeImageService;
    }

    @GetMapping
    public ResultData getNoticeList(@ModelAttribute NoticeSearchVO search
                                  , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(noticeService.getNoticeList(search, admin));
    }

    @GetMapping("/{notiNumb}/{versNumb}")
    public ResultData getNoticeDtl(@PathVariable Long notiNumb, @PathVariable Integer versNumb
                                 , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(noticeService.getNoticeDtl(notiNumb, versNumb, admin));
    }

    /** 같은 공지사항의 버전 이력을 최신 버전부터 조회한다. */
    @GetMapping("/{notiNumb}/versions")
    public ResultData getNoticeVersionList(@PathVariable Long notiNumb
                                         , @AuthenticationPrincipal AdminSessionVO admin) {
        // 같은 주키의 버전 선택 표에 사용할 관리 정보를 반환한다.
        return ResultData.success(noticeService.getNoticeVersionList(notiNumb, admin));
    }

    @PostMapping
    public ResultData setNotice(@RequestBody NoticeVO notice
                              , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, noticeService.setNotice(notice, admin));
    }

    /**
     * 현재 배포 상태를 기준으로 공지 버전을 수정하거나 다음 버전을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param notiNumb 수정할 공지사항 번호
     * @param versNumb 수정 기준 버전 번호
     * @param notice 수정할 공지사항 내용
     * @param admin 수정 요청 관리자 세션
     * @return 서버가 결정한 버전의 저장 결과
     */
    @PutMapping("/{notiNumb}/{versNumb}")
    public ResultData uptNoticeVersion(@PathVariable Long notiNumb, @PathVariable Integer versNumb
                                     , @RequestBody NoticeVO notice
                                     , @AuthenticationPrincipal AdminSessionVO admin) {
        // 서버가 대상 버전의 현재 배포 상태를 확인한 뒤 결정한 저장 결과를 반환한다.
        NoticeVO savedNotice = noticeService.uptNoticeVersion(notiNumb, versNumb, notice, admin);
        // "저장했습니다."
        return ResultData.success(ResultEnum.SAVE_SUCCESS, savedNotice);
    }

    @PostMapping("/{notiNumb}/{versNumb}/deploy")
    public ResultData uptNoticeDeploy(@PathVariable Long notiNumb, @PathVariable Integer versNumb
                                    , @AuthenticationPrincipal AdminSessionVO admin) {
        return ResultData.success(ResultEnum.UPDATE_SUCCESS
                                , noticeService.uptNoticeDeploy(notiNumb, versNumb, admin));
    }

    /** 공지사항의 전체 버전과 읽음 이력 및 저장 파일을 삭제한다. */
    @DeleteMapping("/{notiNumb}")
    public ResultData delNotice(@PathVariable Long notiNumb
                              , @AuthenticationPrincipal AdminSessionVO admin) {
        // 저장소 파일까지 삭제된 경우에만 삭제 성공 응답을 생성한다.
        noticeService.delNotice(notiNumb, admin);
        // 공지사항 전체 삭제 완료 결과를 반환한다.
        return ResultData.success(ResultEnum.DELETE_SUCCESS);
    }

    @PostMapping("/images")
    public ResultData setNoticeImage(@RequestParam("file") MultipartFile image) throws IOException {
        return ResultData.success(ResultEnum.SAVE_SUCCESS, noticeImageService.setNoticeImage(image));
    }
}
