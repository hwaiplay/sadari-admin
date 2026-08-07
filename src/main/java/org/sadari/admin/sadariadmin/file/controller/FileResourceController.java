package org.sadari.admin.sadariadmin.file.controller;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.sadari.admin.sadariadmin.file.storage.StoredFile;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * fileName       : FileResourceController
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 관리자 인증 화면에 로컬 또는 S3의 사용자 이미지를 제공한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@RestController
public class FileResourceController {

    // 관리자 화면에서 조회할 수 있는 사용자 이미지 유형
    private static final Set<String> ALLOWED_DIRECTORIES = Set.of("profile", "background", "notice");
    // 업로드 날짜 경로가 yyMMdd 숫자로만 구성되는지 검증하는 패턴
    private static final Pattern UPLOAD_DATE_PATTERN = Pattern.compile("[0-9]{6}");
    // 서버가 생성한 UUID 이미지 파일명만 허용하는 패턴
    private static final Pattern STORED_NAME_PATTERN = Pattern.compile(
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(jpg|png)"
    );

    // 실행 환경에 따라 로컬 또는 S3로 연결되는 사용자 이미지 저장소
    private final FileStorage fileStorage;

    /**
     * 관리자 사용자 이미지 조회에 사용할 파일 저장소를 구성한다
     *
     * @author SeungHyeon.Kang
     * @param fileStorage 실행 환경에 맞는 사용자 이미지 저장소
     */
    public FileResourceController(FileStorage fileStorage) {

        // 검증된 사용자 이미지 조회에 사용할 저장소를 보관한다
        this.fileStorage = fileStorage;
    }

    /**
     * 관리자 인증을 거친 기존 업로드 URL로 사용자 이미지를 반환한다
     *
     * @author SeungHyeon.Kang
     * @param directory 프로필 또는 배경 이미지 디렉터리
     * @param uploadDate yyMMdd 형식의 업로드 날짜
     * @param storedName UUID 형식의 저장 파일명
     * @return 이미지 바이트 응답, 경로 또는 객체가 없으면 404 응답
     * @throws IOException 저장소 조회 실패 시 발생
     */
    @GetMapping("/uploads/{directory}/{uploadDate}/{storedName}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable String directory
          , @PathVariable String uploadDate
          , @PathVariable String storedName
    ) throws IOException {

        // 관리자 화면도 서버가 생성한 프로필과 배경 이미지 경로만 조회하도록 제한한다
        if (!ALLOWED_DIRECTORIES.contains(directory)
                || !UPLOAD_DATE_PATTERN.matcher(uploadDate).matches()
                || !STORED_NAME_PATTERN.matcher(storedName).matches()) {
            // 허용되지 않은 이미지 경로에 파일 부재 응답을 반환한다
            return ResponseEntity.notFound().build();
        }

        // 검증된 경로 구간으로만 공용 저장소 객체 키를 구성한다
        String objectKey = directory + "/" + uploadDate + "/" + storedName;
        // 현재 환경의 로컬 또는 S3 저장소에서 사용자 이미지를 조회한다
        Optional<StoredFile> storedFile = fileStorage.getFile(objectKey);

        // 물리 삭제됐거나 저장소에 없는 이미지에는 내부 경로 없이 파일 부재를 반환한다
        if (storedFile.isEmpty()) {
            // 존재하지 않는 사용자 이미지 조회 결과를 반환한다
            return ResponseEntity.notFound().build();
        }

        // 저장된 MIME 유형이 없으면 안전한 바이너리 기본 유형을 적용한다
        MediaType mediaType = StringUtil.isEmpty(storedFile.get().contentType())
                ? MediaType.APPLICATION_OCTET_STREAM
                : MediaType.parseMediaType(storedFile.get().contentType());
        // 탈퇴 후 물리 삭제된 이미지가 관리자 브라우저 캐시에 남지 않도록 저장을 금지한다
        return ResponseEntity.ok()
                .contentType(mediaType)
                .contentLength(storedFile.get().bytes().length)
                .cacheControl(CacheControl.noStore())
                .body(storedFile.get().bytes());
    }
}
