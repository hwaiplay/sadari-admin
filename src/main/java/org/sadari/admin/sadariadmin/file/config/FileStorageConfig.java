package org.sadari.admin.sadariadmin.file.config;

import java.net.URI;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.sadari.admin.sadariadmin.file.storage.LocalFileStorage;
import org.sadari.admin.sadariadmin.file.storage.S3FileStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

/**
 * fileName       : FileStorageConfig
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 관리자 앱을 사용자 앱과 같은 로컬 또는 S3 저장소에 연결한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 */
@Configuration
public class FileStorageConfig {

    /**
     * 관리자 로컬 실행 환경에서 사용할 공용 디스크 저장소를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param rootDirectory 사용자 앱과 공유할 로컬 저장 루트
     * @return 공용 로컬 디스크 파일 저장소
     */
    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "local", matchIfMissing = true)
    public FileStorage localFileStorage(@Value("${app.storage.local-root}") String rootDirectory) {

        // 사용자 앱과 같은 로컬 루트를 사용하는 파일 저장소를 반환한다
        return new LocalFileStorage(rootDirectory);
    }

    /**
     * 관리자 앱에서 공용 AWS S3 또는 S3 호환 클라이언트를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param region S3 리전 식별값
     * @param endpoint S3 호환 저장소의 선택적 엔드포인트
     * @param pathStyleAccess 경로 방식 주소 사용 여부
     * @param accessKey S3 접근에 사용할 장기 Access Key
     * @param secretKey S3 접근에 사용할 장기 Secret Key
     * @return 사용자 앱과 같은 정적 자격 증명을 사용하는 S3 클라이언트
     */
    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
    public S3Client s3Client(@Value("${app.storage.s3.region}") String region
                           , @Value("${app.storage.s3.endpoint:}") String endpoint
                           , @Value("${app.storage.s3.path-style-access:false}") boolean pathStyleAccess
                           , @Value("${app.storage.s3.access-key}") String accessKey
                           , @Value("${app.storage.s3.secret-key}") String secretKey) {

        // 사용자 앱과 같은 리전과 장기 자격 증명을 적용한 S3 클라이언트 빌더를 생성한다
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(pathStyleAccess).build());

        // AWS가 아닌 S3 호환 저장소를 사용할 때만 같은 사용자 지정 엔드포인트를 적용한다
        if (!StringUtil.isEmpty(endpoint)) {
            // 검증된 공용 엔드포인트를 관리자 S3 클라이언트에 적용한다
            builder.endpointOverride(URI.create(endpoint));
        }

        // 환경변수에서 주입한 공용 장기 자격 증명으로 인증하는 클라이언트를 반환한다
        return builder.build();
    }

    /**
     * 관리자 앱에서 사용자 앱과 같은 버킷을 사용하는 파일 저장소를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param s3Client 공용 S3 API 호출 클라이언트
     * @param bucket 사용자 앱과 공유할 버킷 이름
     * @return 공용 S3 기반 파일 저장소
     */
    @Bean
    @ConditionalOnProperty(name = "app.storage.provider", havingValue = "s3")
    public FileStorage s3FileStorage(S3Client s3Client
                                   , @Value("${app.storage.s3.bucket}") String bucket) {

        // 사용자 앱과 같은 비공개 버킷을 사용하는 저장소를 반환한다
        return new S3FileStorage(s3Client, bucket);
    }
}
