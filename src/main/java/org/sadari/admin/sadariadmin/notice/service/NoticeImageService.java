package org.sadari.admin.sadariadmin.notice.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.sadari.admin.sadariadmin.notice.vo.NoticeImageVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * fileName       : NoticeImageService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : Summernote 이미지를 검증하고 공지 전용 저장 경로에 기록한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    공지 삭제용 실제 이미지 삭제 기능 추가
 */
@Service
public class NoticeImageService {

    // 공지 이미지 저장 디렉터리
    private static final String NOTICE_DIRECTORY = "notice";
    // 공지 전용 공개 이미지 경로 형식
    private static final String NOTICE_IMAGE_PATH = "^/uploads/notice/[0-9]{6}/[0-9a-fA-F-]{36}\\.(jpg|png)$";
    // 업로드 일자 디렉터리 형식
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    // 실행 환경에 연결된 로컬 또는 S3 저장소
    private final FileStorage fileStorage;

    // 업로드 가능한 이미지 최대 바이트
    @Value("${app.upload.max-image-bytes:10485760}")
    private long maxImageBytes;

    // 업로드 가능한 이미지 최대 픽셀 수
    @Value("${app.upload.max-image-pixels:20000000}")
    private long maxImagePixels;

    /** 공지 이미지 서비스에 저장소를 주입한다. */
    public NoticeImageService(FileStorage fileStorage) {
        this.fileStorage = fileStorage;
    }

    /** JPG 또는 PNG 이미지를 재인코딩하여 공지 전용 경로에 저장한다. */
    public NoticeImageVO setNoticeImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty() || image.getSize() > maxImageBytes) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_IMAGE_INVALID);
        }
        byte[] source = image.getBytes();
        String format = detectFormat(source);
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(source));
        if (decoded == null || (long) decoded.getWidth() * decoded.getHeight() > maxImagePixels) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_IMAGE_INVALID);
        }
        byte[] normalized = encode(decoded, format);
        String uploadDate = LocalDate.now().format(DATE_FORMATTER);
        String storedName = UUID.randomUUID() + "." + format;
        String objectKey = NOTICE_DIRECTORY + "/" + uploadDate + "/" + storedName;
        fileStorage.setFile(objectKey, normalized, "png".equals(format) ? "image/png" : "image/jpeg");
        return new NoticeImageVO("/uploads/" + objectKey);
    }

    /**
     * 공지사항 본문에서 수집한 공지 전용 파일을 저장소에서 삭제한다.
     *
     * @author SeungHyeon.Kang
     * @param imagePaths 삭제할 공개 이미지 경로
     * @throws IOException 저장소 삭제 실패 시 발생
     */
    public void delNoticeImages(Collection<String> imagePaths) throws IOException {
        // 공지 본문 여러 버전에서 수집한 파일을 한 번씩 저장소에서 제거한다.
        for (String imagePath : imagePaths) {
            // 다른 파일 유형과 경로 순회 입력은 공지 삭제 범위에서 제외한다.
            if (imagePath != null && imagePath.matches(NOTICE_IMAGE_PATH)) {
                // 공개 경로에서 저장소 객체 키만 추출하여 실제 파일을 제거한다.
                fileStorage.delFile(imagePath.substring("/uploads/".length()));
            }

        }
    }

    /** 파일 시그니처로 지원 이미지 형식을 판정한다. */
    private String detectFormat(byte[] bytes) {
        boolean jpeg = bytes.length >= 3 && (bytes[0] & 0xff) == 0xff
                && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
        boolean png = bytes.length >= 8 && (bytes[0] & 0xff) == 0x89
                && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47;
        if (jpeg) {
            return "jpg";
        }
        if (png) {
            return "png";
        }
        throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_IMAGE_INVALID);
    }

    /** 디코딩한 픽셀만 새 이미지로 재인코딩한다. */
    private byte[] encode(BufferedImage source, String format) throws IOException {
        BufferedImage target = source;
        if ("jpg".equals(format) && source.getType() != BufferedImage.TYPE_INT_RGB) {
            target = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = target.createGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, target.getWidth(), target.getHeight());
            graphics.drawImage(source, 0, 0, null);
            graphics.dispose();
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        if (!ImageIO.write(target, format, output)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_IMAGE_INVALID);
        }
        return output.toByteArray();
    }
}
