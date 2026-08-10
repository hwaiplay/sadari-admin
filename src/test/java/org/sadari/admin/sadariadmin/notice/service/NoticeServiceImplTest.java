package org.sadari.admin.sadariadmin.notice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.notice.mapper.NoticeMapper;
import org.sadari.admin.sadariadmin.notice.service.impl.NoticeServiceImpl;
import org.sadari.admin.sadariadmin.notice.vo.NoticeSearchVO;
import org.sadari.admin.sadariadmin.notice.vo.NoticeVO;

/**
 * fileName       : NoticeServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 공지사항 버전 증가와 HTML 정제 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    공지 전체 삭제 범위 검증 추가
 * 2026-08-08        SeungHyeon.Kang    현재 배포 상태별 버전 저장 검증 추가
 * 2026-08-08        SeungHyeon.Kang    관리자 목록의 배포 버전 선택 기준 검증 추가
 */
@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    // 공지사항 데이터 접근 Mock
    @Mock
    private NoticeMapper noticeMapper;
    // 공지사항 이미지 저장소 서비스 Mock
    @Mock
    private NoticeImageService noticeImageService;
    // 공지사항 서비스 단위 테스트 대상
    private NoticeServiceImpl noticeService;
    // 인증된 관리자 세션
    private AdminSessionVO admin;

    /** 각 테스트에 독립된 서비스와 관리자 세션을 구성한다. */
    @BeforeEach
    void setUp() {
        noticeService = new NoticeServiceImpl(noticeMapper, noticeImageService);
        admin = new AdminSessionVO();
        admin.setAdmnNumb(3L);
    }

    /** 관리자 목록 조회에 현재 배포 여부 기준값을 서버 상수로 설정한다. */
    @Test
    void getNoticeListSetsCurrentDeployVersionCriteria() {
        NoticeSearchVO search = new NoticeSearchVO();
        when(noticeMapper.getNoticeList(search)).thenReturn(List.of());
        when(noticeMapper.getNoticeListCnt(search)).thenReturn(0);

        noticeService.getNoticeList(search, admin);

        assertEquals("Y", search.getDplyYsno());
    }

    /** 현재 배포 중인 공지는 최신 버전의 MAX + 1로 저장하고 외부 이미지를 제거한다. */
    @Test
    void uptNoticeVersionCreatesNextVersionWhenCurrentlyDeployed() {
        NoticeVO request = new NoticeVO();
        request.setCateCode("GUIDE");
        request.setTopxYsno("Y");
        request.setNotiTitl(" 새 공지 ");
        request.setNotiCntn("<p>본문</p><script>alert(1)</script>"
                + "<img src=\"https://example.com/tracker.png\">"
                + "<img src=\"/uploads/notice/260807/123e4567-e89b-12d3-a456-426614174000.png\">");
        when(noticeMapper.getLatestVersionForUpdate(9L)).thenReturn(4);
        when(noticeMapper.getNoticeCategoryCnt("NOTI_CATE", "GUIDE", "Y")).thenReturn(1);
        NoticeVO deployed = new NoticeVO();
        deployed.setDplyYsno("Y");
        when(noticeMapper.getNoticeDtl(9L, 4)).thenReturn(deployed);
        NoticeVO originalAudit = new NoticeVO();
        originalAudit.setRegiAdmn(2L);
        when(noticeMapper.getNoticeOriginalAudit(9L)).thenReturn(originalAudit);
        when(noticeMapper.setNotice(any(NoticeVO.class))).thenReturn(1);
        when(noticeMapper.getNoticeDtl(9L, 5)).thenAnswer(invocation -> request);

        NoticeVO saved = noticeService.uptNoticeVersion(9L, 4, request, admin);

        assertEquals(5, saved.getVersNumb());
        assertEquals("새 공지", saved.getNotiTitl());
        assertEquals(2L, saved.getRegiAdmn());
        assertEquals(3L, saved.getUpdtAdmn());
        assertFalse(saved.getNotiCntn().contains("script"));
        assertFalse(saved.getNotiCntn().contains("example.com"));
        assertTrue(saved.getNotiCntn().contains("/uploads/notice/260807/"));
    }

    /** 과거 배포일이 있어도 현재 미배포 상태이면 선택한 버전 번호를 유지하여 수정한다. */
    @Test
    void uptNoticeVersionUpdatesSameVersionWhenNotCurrentlyDeployed() {
        NoticeVO request = new NoticeVO();
        request.setCateCode("GUIDE");
        request.setTopxYsno("N");
        request.setNotiTitl("수정 공지");
        request.setNotiCntn("<p>수정 본문</p>");
        NoticeVO notDeployed = new NoticeVO();
        notDeployed.setDplyYsno("N");
        notDeployed.setDplyDate(LocalDateTime.now());
        when(noticeMapper.getLatestVersionForUpdate(9L)).thenReturn(4);
        when(noticeMapper.getNoticeCategoryCnt("NOTI_CATE", "GUIDE", "Y")).thenReturn(1);
        when(noticeMapper.getNoticeDtl(9L, 4)).thenReturn(notDeployed, request);
        when(noticeMapper.uptNotice(any(NoticeVO.class))).thenReturn(1);

        NoticeVO saved = noticeService.uptNoticeVersion(9L, 4, request, admin);

        assertEquals(4, saved.getVersNumb());
        assertEquals(3L, saved.getUpdtAdmn());
        verify(noticeMapper).uptNotice(request);
        verify(noticeMapper, never()).setNotice(any(NoticeVO.class));
    }

    /** 공지 삭제 시 모든 버전과 읽음 이력 및 중복 제거한 실제 이미지를 함께 삭제한다. */
    @Test
    void delNoticeDeletesVersionsViewsAndDistinctImages() throws IOException {
        String imagePath = "/uploads/notice/260807/123e4567-e89b-12d3-a456-426614174000.png";
        when(noticeMapper.getNoticeContentList(9L)).thenReturn(List.of(
                "<p>첫 버전</p><img src=\"" + imagePath + "\">",
                "<p>둘째 버전</p><img src=\"" + imagePath + "\">"));
        when(noticeMapper.delNotice(9L)).thenReturn(2);

        noticeService.delNotice(9L, admin);

        verify(noticeMapper).delNoticeView("NOTICE", 9L);
        verify(noticeMapper).delNotice(9L);
        verify(noticeImageService).delNoticeImages(java.util.Set.of(imagePath));
    }
}
