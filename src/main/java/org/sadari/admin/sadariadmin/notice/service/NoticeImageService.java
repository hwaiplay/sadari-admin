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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import com.luciad.imageio.webp.WebPWriteParam;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.sadari.admin.sadariadmin.notice.vo.NoticeImageVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
 * 2026-10-12        SeungHyeon.Kang    본문 정제 통합과 커밋 후 이미지 정리 추가
 */
@Service
public class NoticeImageService {

    // 커밋 후 이미지 정리 실패를 운영 로그에 남기는 로거
    private static final Logger log = LoggerFactory.getLogger(NoticeImageService.class);

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

    /**
     * 본문 목록에서 공용 Summernote 이미지 경로를 중복 없이 수집한다
     *
     * @author SeungHyeon.Kang
     * @param contents 이미지 경로를 찾을 HTML 본문 목록
     * @return 공지 업로드 규격과 일치하는 이미지 경로
     */
    public Set<String> getNoticeImagePaths(Collection<String> contents) {
        // 여러 버전에서 같은 이미지를 참조해도 한 번만 정리하도록 순서를 유지한 집합을 생성한다
        Set<String> imagePaths = new LinkedHashSet<>();
        // 과거 버전에만 남은 이미지도 삭제 대상에 포함한다
        for (String content : contents) {
            // 허용된 공용 업로드 경로만 실제 파일 삭제 대상으로 수집한다
            for (Element image : Jsoup.parseBodyFragment(content == null ? "" : content).select("img[src]")) {
                String imagePath = image.attr("src");
                // 공지 업로드 규격으로 발급한 상대 경로만 수집한다
                if (imagePath.matches(NOTICE_IMAGE_PATH)) {
                    // 검증된 이미지 공개 경로를 정리 대상에 추가한다
                    imagePaths.add(imagePath);
                }

            }

        }

        // 중복이 제거된 공용 Summernote 이미지 경로를 반환한다
        return imagePaths;
    }

    /**
     * 실행 요소와 외부 이미지를 제거한 안전한 관리 콘텐츠 HTML을 만든다
     *
     * @author SeungHyeon.Kang
     * @param html 정제할 관리 콘텐츠 HTML
     * @return 허용된 태그와 내부 이미지만 포함한 HTML
     */
    public String sanitizeContentHtml(String html) {
        // 관리자 편집기에 필요한 서식만 허용하는 HTML 정책을 구성한다
        Safelist safelist = Safelist.relaxed()
                .addTags("div", "span", "hr", "table", "thead", "tbody", "tr", "th", "td")
                .addAttributes(":all", "class", "style")
                .addAttributes("a", "target", "rel")
                .addAttributes("img", "width", "height")
                .removeProtocols("img", "src", "http", "https");
        // 비어 있는 입력도 같은 정제 정책을 거치도록 HTML 문서로 변환한다
        Document dirty = Jsoup.parseBodyFragment(html == null ? "" : html);
        // 허용 목록에 없는 실행 요소와 속성을 제거한다
        Document clean = new Cleaner(safelist).clean(dirty);
        // 외부 파일과 다른 업무 디렉터리 이미지를 본문에서 제거한다
        for (Element image : clean.select("img")) {
            // 공용 Summernote 이미지 경로가 아니면 요소 전체를 제거한다
            if (!image.attr("src").matches(NOTICE_IMAGE_PATH)) {
                // 허용되지 않은 이미지 요소를 정제 결과에서 제거한다
                image.remove();
            }

        }

        // CSS 실행이나 외부 요청을 만들 수 있는 위험한 스타일을 제거한다
        for (Element styled : clean.select("[style]")) {
            String style = styled.attr("style").toLowerCase();
            // 외부 리소스와 표현식이 포함된 style 속성을 제거한다
            if (style.contains("url(") || style.contains("expression") || style.contains("@import")) {
                // 위험한 style 속성만 제거하고 본문 요소는 유지한다
                styled.removeAttr("style");
            }

        }

        // 정제된 body 내부 HTML을 반환한다
        return clean.body().html().trim();
    }

    /**
     * DB 삭제가 커밋된 뒤 본문 이미지 파일을 정리한다
     *
     * @author SeungHyeon.Kang
     * @param imagePaths 커밋 후 삭제할 이미지 공개 경로
     */
    public void setImageCleanupOnCommit(Collection<String> imagePaths) {
        // 정리 대상이 없으면 트랜잭션 콜백을 등록하지 않는다
        if (imagePaths == null || imagePaths.isEmpty()) {
            // 이미지가 없는 본문 삭제 처리를 종료한다
            return;
        }

        // 호출 이후 원본 컬렉션 변경이 정리 범위에 영향을 주지 않도록 복사한다
        Set<String> cleanupPaths = new LinkedHashSet<>(imagePaths);
        // 트랜잭션 밖의 독립 호출에서는 확정된 DB 상태에 맞춰 즉시 정리한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 독립 호출의 이미지 파일을 즉시 정리한다
            delImagesSafely(cleanupPaths);
            // 즉시 이미지 정리를 마친다
            return;
        }

        // DB 롤백 시 기존 본문이 계속 참조할 파일을 유지하도록 종료 상태 확인 작업을 등록한다
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * DB 커밋이 완료된 경우에만 본문 이미지 파일을 삭제한다
             *
             * @author SeungHyeon.Kang
             * @param status 트랜잭션 종료 상태
             */
            @Override
            public void afterCompletion(int status) {
                // 롤백된 본문이 기존 파일을 계속 가리킬 수 있으므로 커밋 외 상태는 유지한다
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    // 기존 이미지 파일을 유지하고 정리 콜백을 종료한다
                    return;
                }

                // DB에서 본문이 삭제된 이미지 파일을 저장소에서 정리한다
                delImagesSafely(cleanupPaths);
            }
        });
    }

    /**
     * 이미지 파일 정리 실패가 커밋된 DB 상태에 영향을 주지 않도록 예외를 격리한다
     *
     * @author SeungHyeon.Kang
     * @param imagePaths 삭제할 이미지 공개 경로
     */
    private void delImagesSafely(Collection<String> imagePaths) {
        // 커밋 이후 파일 저장소 장애를 업무 트랜잭션과 분리한다
        try {
            // 검증된 이미지 경로에 해당하는 저장소 파일을 삭제한다
            delNoticeImages(imagePaths);
        }

        // 정리 실패 경로를 운영 로그에 남겨 수동 재처리가 가능하게 한다
        catch (IOException exception) {
            log.error("Committed content image cleanup failed. imageCount={}", imagePaths.size(), exception);
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
