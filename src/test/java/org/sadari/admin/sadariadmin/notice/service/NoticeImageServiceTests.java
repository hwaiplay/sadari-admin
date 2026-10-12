package org.sadari.admin.sadariadmin.notice.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.sadari.admin.sadariadmin.notice.vo.NoticeImageVO;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * fileName       : NoticeImageServiceTests
 * author         : OpenAI.Codex
 * date           : 2026-08-28
 * description    : 관리 콘텐츠별 이미지 저장 루트 분리를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-28        OpenAI.Codex       최초 생성
 * 2026-10-12        SeungHyeon.Kang    본문 정제와 커밋 후 파일 정리 검증 추가
 */
class NoticeImageServiceTests {

    /** 웰컴 이미지를 전용 경로와 제한 너비로 저장하는지 검증한다. */
    @Test
    void storeWelcomeUnderRoot() throws Exception {

        FileStorage fileStorage = mock(FileStorage.class);
        NoticeImageService service = new NoticeImageService(fileStorage);
        ReflectionTestUtils.setField(service, "maxImageBytes", 10_485_760L);
        ReflectionTestUtils.setField(service, "maxImagePixels", 20_000_000L);
        BufferedImage source = new BufferedImage(900, 100, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        assertThat(ImageIO.write(source, "png", output)).isTrue();
        MockMultipartFile image = new MockMultipartFile(
                "file", "welcome.png", "image/png", output.toByteArray());

        NoticeImageVO uploaded = service.setWelcomeImage(image);

        ArgumentCaptor<String> objectKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<byte[]> storedBytes = ArgumentCaptor.forClass(byte[].class);
        verify(fileStorage).setFile(objectKey.capture(), storedBytes.capture(), eq("image/webp"));
        assertThat(objectKey.getValue())
                .matches("welcome/[0-9]{6}/[0-9a-f-]{36}\\.webp");
        assertThat(uploaded.url()).isEqualTo("/uploads/" + objectKey.getValue());
        BufferedImage storedImage = ImageIO.read(new ByteArrayInputStream(storedBytes.getValue()));
        assertThat(storedImage.getWidth()).isEqualTo(860);
        assertThat(storedImage.getColorModel().hasAlpha()).isTrue();
    }

    /** 공용 본문 정책이 실행 요소와 외부 이미지를 제거하는지 검증한다. */
    @Test
    void sanitizeContentPolicy() {
        // 저장소 접근 없이 본문 정책을 검증할 이미지 서비스를 생성한다
        NoticeImageService service = new NoticeImageService(mock(FileStorage.class));
        String internalImage = "/uploads/notice/261012/123e4567-e89b-12d3-a456-426614174000.png";

        // 위험 요소와 내부 이미지를 함께 포함한 본문을 정제한다
        String sanitized = service.sanitizeContentHtml(
                "<script>alert(1)</script><img src=\"https://example.com/a.png\">"
                        + "<img src=\"" + internalImage + "\"><p style=\"background:url(x)\">본문</p>");

        // 실행 요소와 외부 이미지는 제거하고 검증된 내부 이미지는 유지하는지 확인한다
        assertThat(sanitized).doesNotContain("script", "example.com", "background").contains(internalImage, "본문");
    }

    /** 저장소 정리 실패가 이미 확정된 DB 삭제 결과에 예외를 전파하지 않는지 검증한다. */
    @Test
    void cleanupFailureIsIsolated() throws IOException {
        // 이미지 삭제 실패를 재현할 저장소 대역을 생성한다
        FileStorage fileStorage = mock(FileStorage.class);
        // 커밋 이후 저장소 장애가 발생하도록 삭제 호출에 예외를 설정한다
        doThrow(new IOException("storage unavailable")).when(fileStorage)
                .delFile("notice/261012/123e4567-e89b-12d3-a456-426614174000.png");
        // 실패 격리 정책을 실행할 이미지 서비스를 생성한다
        NoticeImageService service = new NoticeImageService(fileStorage);
        String imagePath = "/uploads/notice/261012/123e4567-e89b-12d3-a456-426614174000.png";

        // 트랜잭션 밖의 정리 실패도 업무 처리 예외로 전파되지 않는지 확인한다
        assertThatNoException().isThrownBy(() -> service.setImageCleanupOnCommit(List.of(imagePath)));
        // 허용된 이미지 경로가 실제 저장소 객체 키로 변환되어 삭제를 시도했는지 확인한다
        verify(fileStorage).delFile("notice/261012/123e4567-e89b-12d3-a456-426614174000.png");
    }
}
