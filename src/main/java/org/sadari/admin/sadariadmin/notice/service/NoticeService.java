package org.sadari.admin.sadariadmin.notice.service;

import java.util.List;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.notice.vo.NoticeSearchVO;
import org.sadari.admin.sadariadmin.notice.vo.NoticeVO;

/**
 * fileName       : NoticeService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 공지사항 버전 생성과 조회 및 배포와 삭제 기능을 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    버전 목록 조회와 공지 전체 삭제 기능 추가
 * 2026-08-08        SeungHyeon.Kang    현재 배포 상태 기준 공지 수정 계약 적용
 */
public interface NoticeService {

    PageData<NoticeVO> getNoticeList(NoticeSearchVO search, AdminSessionVO admin);

    NoticeVO getNoticeDtl(Long notiNumb, Integer versNumb, AdminSessionVO admin);

    /** 같은 공지사항의 전체 버전 이력을 조회한다. */
    List<NoticeVO> getNoticeVersionList(Long notiNumb, AdminSessionVO admin);

    NoticeVO setNotice(NoticeVO notice, AdminSessionVO admin);

    /**
     * 미배포 버전은 같은 버전으로 수정하고 현재 배포 중인 버전은 다음 버전으로 저장한다
     *
     * @author SeungHyeon.Kang
     * @param notiNumb 수정할 공지사항 번호
     * @param versNumb 수정 기준 버전 번호
     * @param notice 수정할 공지사항 내용
     * @param admin 수정 요청 관리자 세션
     * @return 수정되거나 새로 생성된 공지사항 버전
     */
    NoticeVO uptNoticeVersion(Long notiNumb, Integer versNumb, NoticeVO notice, AdminSessionVO admin);

    NoticeVO uptNoticeDeploy(Long notiNumb, Integer versNumb, AdminSessionVO admin);

    /** 공지사항의 모든 버전과 종속 데이터 및 실제 파일을 삭제한다. */
    void delNotice(Long notiNumb, AdminSessionVO admin);
}
