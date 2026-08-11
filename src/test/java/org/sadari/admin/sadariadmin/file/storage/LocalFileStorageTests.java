package org.sadari.admin.sadariadmin.file.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * fileName       : LocalFileStorageTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 관리자 공용 로컬 저장소의 파일 쓰기와 조회 및 삭제 계약을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
class LocalFileStorageTests {

    // 테스트별로 격리된 공용 로컬 저장 루트
    @TempDir
    private Path storageRoot;

    /**
     * 같은 객체 키로 저장한 파일을 조회하고 멱등하게 삭제할 수 있는지 검증한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException 로컬 저장소 계약상 발생 가능
     */
    @Test
    void fileOpsUseSameKey() throws IOException {

        // 테스트 전용 공용 루트를 사용하는 로컬 저장소를 생성한다
        LocalFileStorage fileStorage = new LocalFileStorage(storageRoot.toString());
        byte[] fileBytes = {1, 2, 3};
        String objectKey = "admin/260807/test.png";

        // 관리자 파일을 공용 객체 키에 저장한다
        fileStorage.setFile(objectKey, fileBytes, "image/png");
        // 같은 객체 키로 저장된 파일을 조회한다
        Optional<StoredFile> storedFile = fileStorage.getFile(objectKey);

        // 저장 직후 파일이 존재하는지 확인한다
        assertTrue(storedFile.isPresent());
        // 조회한 파일 바이트가 저장한 값과 같은지 확인한다
        assertArrayEquals(fileBytes, storedFile.get().bytes());

        // 조회가 끝난 공용 파일을 삭제한다
        fileStorage.delFile(objectKey);
        // 삭제된 파일이 더 이상 조회되지 않는지 확인한다
        assertFalse(fileStorage.getFile(objectKey).isPresent());
    }
}
