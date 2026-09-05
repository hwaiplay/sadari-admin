package org.sadari.admin.sadariadmin.serviceinfo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.notice.service.NoticeImageService;
import org.sadari.admin.sadariadmin.serviceinfo.mapper.ServiceInfoMapper;
import org.sadari.admin.sadariadmin.serviceinfo.service.impl.ServiceInfoServiceImpl;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoVO;

/**
 * fileName       : ServiceInfoServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 카테고리 단일 글 제한과 서비스 정보 버전 생성 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 * 2026-09-05        Codex               영문 서비스 정보 필수값 반영
 */
@ExtendWith(MockitoExtension.class)
class ServiceInfoServiceImplTest {

    // 서비스 정보 데이터 접근 Mock
    @Mock
    private ServiceInfoMapper serviceInfoMapper;
    // 서비스 정보 이미지 저장소 서비스 Mock
    @Mock
    private NoticeImageService noticeImageService;
    // 서비스 정보 서비스 단위 테스트 대상
    private ServiceInfoServiceImpl serviceInfoService;
    // 인증된 관리자 세션
    private AdminSessionVO admin;

    /** 각 테스트에 독립된 서비스와 관리자 세션을 구성한다. */
    @BeforeEach
    void setUp() {
        serviceInfoService = new ServiceInfoServiceImpl(serviceInfoMapper, noticeImageService);
        admin = new AdminSessionVO();
        admin.setAdmnNumb(3L);
    }

    /** 이미 버전이 있는 카테고리에는 두 번째 논리 글 등록을 차단한다. */
    @Test
    void setServiceInfoRejectsDup() {
        ServiceInfoVO request = createRequest();
        when(serviceInfoMapper.getServiceInfoCategoryCnt("SVIF_CATE", "PRIVACY", "Y")).thenReturn(1);
        when(serviceInfoMapper.getServiceInfoCnt("SVIF_CATE", "PRIVACY")).thenReturn(2);

        assertThrows(BusinessException.class, () -> serviceInfoService.setServiceInfo(request, admin));

        verify(serviceInfoMapper, never()).setServiceInfo(any(ServiceInfoVO.class));
    }

    /** 배포된 서비스 정보 수정은 최신 버전 다음 번호의 미배포 초안을 생성한다. */
    @Test
    void uptServiceCreatesDraft() {
        ServiceInfoVO request = createRequest();
        ServiceInfoVO deployed = new ServiceInfoVO();
        deployed.setDplyYsno("Y");
        ServiceInfoVO originalAudit = new ServiceInfoVO();
        originalAudit.setRegiAdmn(2L);
        originalAudit.setRegiDate(LocalDateTime.now());
        when(serviceInfoMapper.getServiceInfoCategoryCnt("SVIF_CATE", "PRIVACY", "Y")).thenReturn(1);
        when(serviceInfoMapper.getLatestVersionForUpdate("SVIF_CATE", "PRIVACY")).thenReturn(4);
        when(serviceInfoMapper.getServiceInfoDtl("SVIF_CATE", "PRIVACY", 4)).thenReturn(deployed);
        when(serviceInfoMapper.getServiceInfoOrigAudit("SVIF_CATE", "PRIVACY")).thenReturn(originalAudit);
        when(serviceInfoMapper.setServiceInfo(request)).thenReturn(1);
        when(serviceInfoMapper.getServiceInfoDtl("SVIF_CATE", "PRIVACY", 5)).thenReturn(request);

        ServiceInfoVO saved = serviceInfoService.uptServiceInfoVersion("PRIVACY", 4, request, admin);

        assertEquals(5, saved.getVersNumb());
        assertEquals("N", saved.getDplyYsno());
        assertEquals(2L, saved.getRegiAdmn());
        assertEquals(3L, saved.getUpdtAdmn());
    }

    /** 미배포 초안 수정은 새 버전을 만들지 않고 선택 버전을 반복 저장한다. */
    @Test
    void uptServiceInfoKeepsDraft() {
        ServiceInfoVO request = createRequest();
        ServiceInfoVO draft = new ServiceInfoVO();
        draft.setDplyYsno("N");
        when(serviceInfoMapper.getServiceInfoCategoryCnt("SVIF_CATE", "PRIVACY", "Y")).thenReturn(1);
        when(serviceInfoMapper.getLatestVersionForUpdate("SVIF_CATE", "PRIVACY")).thenReturn(4);
        when(serviceInfoMapper.getServiceInfoDtl("SVIF_CATE", "PRIVACY", 4)).thenReturn(draft, request);
        when(serviceInfoMapper.uptServiceInfo(request)).thenReturn(1);

        ServiceInfoVO saved = serviceInfoService.uptServiceInfoVersion("PRIVACY", 4, request, admin);

        assertEquals(4, saved.getVersNumb());
        assertEquals(3L, saved.getUpdtAdmn());
        verify(serviceInfoMapper).uptServiceInfo(request);
        verify(serviceInfoMapper, never()).setServiceInfo(any(ServiceInfoVO.class));
    }

    /** 서비스 정보 저장 테스트에 사용할 정상 개인정보처리방침 요청을 만든다. */
    private ServiceInfoVO createRequest() {
        ServiceInfoVO request = new ServiceInfoVO();
        request.setCateCode("PRIVACY");
        request.setSvciTitl("개인정보처리방침");
        request.setSvciEntl("Privacy Policy");
        request.setSvciCntn("<p>정책 본문</p>");
        request.setSvciEnct("<p>Policy content</p>");
        // 필수값을 갖춘 서비스 정보 저장 요청을 반환한다.
        return request;
    }
}
