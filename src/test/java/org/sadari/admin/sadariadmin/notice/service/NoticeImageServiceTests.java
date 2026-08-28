package org.sadari.admin.sadariadmin.notice.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.sadari.admin.sadariadmin.notice.vo.NoticeImageVO;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
 */
class NoticeImageServiceTests {

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
}
