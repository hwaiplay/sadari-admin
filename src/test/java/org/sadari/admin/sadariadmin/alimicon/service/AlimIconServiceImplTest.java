package org.sadari.admin.sadariadmin.alimicon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alimicon.mapper.AlimIconMapper;
import org.sadari.admin.sadariadmin.alimicon.service.impl.AlimIconServiceImpl;
import org.sadari.admin.sadariadmin.alimicon.vo.AlimIconVO;
import org.springframework.mock.web.MockMultipartFile;

/**
 * fileName       : AlimIconServiceImplTest
 * author         : SeungHyeon.Kang
 * date           : 2026-08-12
 * description    : 알림 상황별 아이콘 SVG·PNG 검증과 직접 저장을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-12        SeungHyeon.Kang    최초 생성
 * 2026-08-12        SeungHyeon.Kang    알림 상황 식별 구조로 전환
 */
class AlimIconServiceImplTest {

    @Mock
    private AlimIconMapper alimIconMapper;

    private AlimIconService alimIconService;

    /** 테스트마다 서비스와 Mock 의존 객체를 준비한다. */
    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
        alimIconService = new AlimIconServiceImpl(alimIconMapper);
    }

    /** 정사각형 PNG가 알림 상황 PK와 이미지 메타데이터로 저장되는지 검증한다. */
    @Test
    void saveAlimIconNormalizesPng() throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(34, 34, BufferedImage.TYPE_INT_ARGB), "png", output);
        MockMultipartFile file = new MockMultipartFile("file", "icon.png", "image/png", output.toByteArray());
        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(1L);
        when(alimIconMapper.getAlimSituCodeCount("LIKE")).thenReturn(1);
        when(alimIconMapper.getAlimIconDtl("LIKE")).thenAnswer(invocation -> {
            AlimIconVO result = new AlimIconVO();
            result.setAlimSitu("LIKE");
            return result;
        });

        AlimIconVO result = alimIconService.saveAlimIcon("LIKE", file, admin);
        ArgumentCaptor<AlimIconVO> captor = ArgumentCaptor.forClass(AlimIconVO.class);
        verify(alimIconMapper).saveAlimIcon(captor.capture());
        AlimIconVO saved = captor.getValue();

        assertEquals("LIKE", result.getAlimSitu());
        assertEquals("LIKE", saved.getAlimSitu());
        assertEquals("image/png", saved.getMimeType());
        assertEquals(34, saved.getPixlWdth());
        assertEquals(34, saved.getPixlHght());
    }

    /** 안전한 정사각형 SVG 원본을 알림 상황 아이콘으로 저장할 수 있는지 검증한다. */
    @Test
    void saveAlimIconAcceptsSafeSvg() throws Exception {

        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 34 34\">"
                + "<path fill=\"#222\" d=\"M4 4h26v26H4z\"/></svg>";
        MockMultipartFile file = new MockMultipartFile(
                "file", "icon.svg", "image/svg+xml", svg.getBytes(StandardCharsets.UTF_8));
        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(1L);
        when(alimIconMapper.getAlimSituCodeCount("REPORT")).thenReturn(1);
        when(alimIconMapper.getAlimIconDtl("REPORT")).thenAnswer(invocation -> {
            AlimIconVO result = new AlimIconVO();
            result.setAlimSitu("REPORT");
            return result;
        });

        alimIconService.saveAlimIcon("REPORT", file, admin);
        ArgumentCaptor<AlimIconVO> captor = ArgumentCaptor.forClass(AlimIconVO.class);
        verify(alimIconMapper).saveAlimIcon(captor.capture());
        AlimIconVO saved = captor.getValue();

        assertEquals("image/svg+xml", saved.getMimeType());
        assertEquals(svg, new String(saved.getIconData(), StandardCharsets.UTF_8));
        assertEquals(34, saved.getPixlWdth());
        assertEquals(34, saved.getPixlHght());
    }

    /** 실행 요소나 외부 참조가 포함된 SVG 등록을 거부하는지 검증한다. */
    @Test
    void saveAlimIconRejectsUnsafeSvg() {

        String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 34 34\">"
                + "<script>alert(1)</script></svg>";
        MockMultipartFile file = new MockMultipartFile(
                "file", "icon.svg", "image/svg+xml", svg.getBytes(StandardCharsets.UTF_8));
        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(1L);
        when(alimIconMapper.getAlimSituCodeCount("REPLY")).thenReturn(1);

        assertThrows(RuntimeException.class,
                () -> alimIconService.saveAlimIcon("REPLY", file, admin));
    }

    /** ALIM_SITU 공통코드에 없는 식별값의 아이콘 저장을 거부하는지 검증한다. */
    @Test
    void saveAlimIconRejectsUnknownSitu() {

        MockMultipartFile file = new MockMultipartFile(
                "file", "icon.svg", "image/svg+xml",
                "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 34 34\"/>"
                        .getBytes(StandardCharsets.UTF_8));
        AdminSessionVO admin = new AdminSessionVO();
        admin.setAdmnNumb(1L);

        assertThrows(RuntimeException.class,
                () -> alimIconService.saveAlimIcon("UNKNOWN", file, admin));
    }
}
