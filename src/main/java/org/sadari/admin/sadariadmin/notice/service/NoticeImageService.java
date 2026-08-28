package org.sadari.admin.sadariadmin.notice.service;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import com.luciad.imageio.webp.WebPWriteParam;
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
 * description    : 관리 콘텐츠 이미지를 검증하고 콘텐츠별 저장 경로에 기록한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    공지 삭제용 실제 이미지 삭제 기능 추가
 * 2026-08-28        OpenAI.Codex       웰컴 이미지 크기 축소와 WebP 변환 추가
 */
@Service
public class NoticeImageService {

    // 공지 이미지 저장 디렉터리
    private static final String NOTICE_DIRECTORY = "notice";
    // 웰컴페이지 이미지 저장 디렉터리
    private static final String WELCOME_DIRECTORY = "welcome";
    // 공지 전용 공개 이미지 경로 형식
    private static final String NOTICE_IMAGE_PATH = "^/uploads/notice/[0-9]{6}/[0-9a-fA-F-]{36}\\.(jpg|png)$";
    // 업로드 일자 디렉터리 형식
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    // 웰컴 화면의 고밀도 모바일 표시를 지원하는 최대 이미지 너비
    private static final int WELCOME_IMAGE_MAX_WIDTH = 860;
    // 웰컴 이미지 WebP 손실 압축 품질
    private static final float WELCOME_IMAGE_QUALITY = 0.82F;

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

    /**
     * JPG 또는 PNG 이미지를 재인코딩하여 공지 전용 경로에 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param image 저장할 원본 이미지
     * @return 저장된 이미지의 공개 경로
     * @throws IOException 이미지 읽기 또는 저장 실패 시 발생
     */
    public NoticeImageVO setNoticeImage(MultipartFile image) throws IOException {
        // 공지 전용 루트에 검증된 이미지를 저장한 결과를 반환한다
        return setImage(image, NOTICE_DIRECTORY);
    }

    /**
     * JPG 또는 PNG 이미지를 재인코딩하여 웰컴페이지 전용 경로에 저장한다.
     *
     * @author SeungHyeon.Kang
     * @param image 저장할 원본 이미지
     * @return 저장된 이미지의 공개 경로
     * @throws IOException 이미지 읽기 또는 저장 실패 시 발생
     */
    public NoticeImageVO setWelcomeImage(MultipartFile image) throws IOException {
        // 웰컴페이지 전용 루트에 검증된 이미지를 저장한 결과를 반환한다
        return setImage(image, WELCOME_DIRECTORY);
    }

    /** 검증된 이미지를 지정된 관리 콘텐츠 루트에 저장한다. */
    private NoticeImageVO setImage(MultipartFile image, String directory) throws IOException {
        if (image == null || image.isEmpty() || image.getSize() > maxImageBytes) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_IMAGE_INVALID);
        }
        byte[] source = image.getBytes();
        String format = detectFormat(source);
        BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(source));
        if (decoded == null || (long) decoded.getWidth() * decoded.getHeight() > maxImagePixels) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_IMAGE_INVALID);
        }
        boolean isWelcomeImage = WELCOME_DIRECTORY.equals(directory);
        String storedFormat = isWelcomeImage ? "webp" : format;
        // 웰컴 이미지는 실제 모바일 표시 밀도에 맞춰 축소하고 투명 WebP로 압축한다
        BufferedImage normalizedImage = isWelcomeImage ? resizeWelcomeImage(decoded) : decoded;
        // 콘텐츠 종류에 맞는 형식으로 메타데이터가 제거된 이미지 바이트를 생성한다
        byte[] normalized = isWelcomeImage ? encodeWelcomeImage(normalizedImage) : encode(normalizedImage, format);
        String uploadDate = LocalDate.now().format(DATE_FORMATTER);
        String storedName = UUID.randomUUID() + "." + storedFormat;
        String objectKey = directory + "/" + uploadDate + "/" + storedName;
        String contentType = isWelcomeImage ? "image/webp" : "png".equals(format) ? "image/png" : "image/jpeg";
        // 콘텐츠별 전용 경로에 정규화한 이미지와 정확한 미디어 형식을 저장한다
        fileStorage.setFile(objectKey, normalized, contentType);
        // 저장소 객체 키를 공개 이미지 경로로 변환하여 반환한다
        return new NoticeImageVO("/uploads/" + objectKey);
    }

    /** 웰컴 이미지를 모바일 고밀도 화면에 필요한 최대 너비로 축소한다. */
    private BufferedImage resizeWelcomeImage(BufferedImage source) {
        // 이미 목표 너비 이하면 불필요한 확대 없이 원본 픽셀을 사용한다
        if (source.getWidth() <= WELCOME_IMAGE_MAX_WIDTH) {
            // 검증이 끝난 원본 이미지를 WebP 인코딩 대상으로 반환한다
            return source;
        }

        int targetHeight = (int) Math.round(
                (double) source.getHeight() * WELCOME_IMAGE_MAX_WIDTH / source.getWidth());
        // 투명 배경을 유지할 축소 이미지를 생성한다
        BufferedImage target = new BufferedImage(
                WELCOME_IMAGE_MAX_WIDTH, targetHeight, BufferedImage.TYPE_INT_ARGB);
        // 축소 과정에서 윤곽선 품질과 알파 채널을 유지하도록 렌더링한다
        Graphics2D graphics = target.createGraphics();
        // 모바일 표시에서 계단 현상을 줄이도록 고품질 보간을 적용한다
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        // 승인된 이미지의 투명 배경을 포함해 목표 크기로 그린다
        graphics.drawImage(source, 0, 0, WELCOME_IMAGE_MAX_WIDTH, targetHeight, null);
        // 네이티브 그래픽 자원을 즉시 해제한다
        graphics.dispose();
        // 축소된 투명 이미지를 WebP 인코딩 대상으로 반환한다
        return target;
    }

    /** 투명 웰컴 이미지를 지정 품질의 WebP 바이트로 인코딩한다. */
    private byte[] encodeWelcomeImage(BufferedImage source) throws IOException {
        // 실행 환경에 등록된 WebP 작성기를 조회한다
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        // WebP 작성기가 없으면 손상된 원본을 저장하지 않고 요청을 실패시킨다
        if (!writers.hasNext()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }

        // WebP 출력 바이트를 메모리에 구성한다
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        // 첫 번째 지원 작성기로 투명 이미지를 압축한다
        ImageWriter writer = writers.next();
        // 손실 압축 품질을 명시할 WebP 출력 설정을 생성한다
        WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());
        // 웰컴 일러스트의 전송량을 줄이도록 압축 설정을 직접 지정한다
        writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        // 알파 채널을 유지하면서 색상 데이터는 손실 압축한다
        writeParam.setCompressionType("Lossy");
        // 텍스트 없는 일러스트에 맞는 품질로 압축한다
        writeParam.setCompressionQuality(WELCOME_IMAGE_QUALITY);

        // ImageIO 작성기가 사용할 출력 스트림을 열고 항상 닫는다
        try (ImageOutputStream imageOutput = ImageIO.createImageOutputStream(output)) {
            // 메모리 출력 스트림을 WebP 작성 대상에 연결한다
            writer.setOutput(imageOutput);
            // 정규화된 픽셀만 WebP로 기록한다
            writer.write(null, new IIOImage(source, null, null), writeParam);
        }

        // 네이티브 WebP 작성기 자원을 즉시 해제한다
        writer.dispose();
        // 모바일 전송용 WebP 바이트를 반환한다
        return output.toByteArray();
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
