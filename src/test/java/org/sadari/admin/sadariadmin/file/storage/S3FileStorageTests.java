package org.sadari.admin.sadariadmin.file.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * fileName       : S3FileStorageTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 관리자 S3 저장소가 공용 버킷과 객체 키를 사용하는지 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@ExtendWith(MockitoExtension.class)
class S3FileStorageTests {

    // 공용 S3 API 호출 클라이언트 대역
    @Mock
    private S3Client s3Client;

    /**
     * 관리자 파일 쓰기 요청에 설정된 공용 버킷과 객체 키가 전달되는지 검증한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException S3 저장소 계약상 발생 가능
     */
    @Test
    void setFileUsesSharedBucket() throws IOException {

        // 테스트 공용 버킷을 사용하는 관리자 S3 저장소를 생성한다
        S3FileStorage fileStorage = new S3FileStorage(s3Client, "shared-bucket");
        // 관리자 객체를 테스트 공용 버킷에 저장한다
        fileStorage.setFile("admin/260807/test.png", new byte[] {1}, "image/png");

        ArgumentCaptor<PutObjectRequest> requestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        // S3 쓰기 요청에 전달된 메타정보를 수집한다
        verify(s3Client).putObject(requestCaptor.capture(), any(RequestBody.class));
        // 설정된 공용 버킷 이름이 사용되는지 확인한다
        assertEquals("shared-bucket", requestCaptor.getValue().bucket());
        // 관리자 객체 키가 사용자 앱과 공유할 버킷에 그대로 전달되는지 확인한다
        assertEquals("admin/260807/test.png", requestCaptor.getValue().key());
    }
}
