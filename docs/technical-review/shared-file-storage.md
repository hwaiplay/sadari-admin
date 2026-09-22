# 두 서비스가 같은 파일 저장소 계약을 사용

관리자에서 등록한 공지 이미지와 사용자가 등록한 프로필 이미지는 사용자·관리자 화면 양쪽에서 같은 파일로 해석되어야 한다. 데이터베이스에 같은 파일 정보가 있어도 로컬 저장 루트, S3 버킷 또는 객체 키 규칙이 다르면 한쪽 서비스에서 파일을 찾지 못한다. 현재 구현은 공통 `FileStorage` 계약과 환경별 구현으로 저장소 차이를 감춘다.

## 저장소 추상화

`FileStorage`는 저장, 조회와 멱등 삭제 세 작업만 정의한다. 업무 ServiceImpl은 로컬 경로나 AWS SDK에 직접 의존하지 않고 객체 키와 파일 바이트를 전달한다.

`FileStorageConfig`는 설정값에 따라 로컬 저장소 또는 S3 저장소를 선택한다. 로컬 환경에서는 사용자 서비스와 같은 저장 루트를, S3 환경에서는 같은 비공개 버킷을 사용하도록 구성한다. 저장소 연결값은 코드가 아니라 환경 설정에서 주입한다.

## 로컬 저장소

`LocalFileStorage`는 설정된 루트를 절대 경로로 정규화한다. 객체 키를 결합한 결과가 루트 밖으로 벗어나거나 루트 자체를 가리키면 파일 시스템 접근을 차단한다. 신규 파일은 기존 객체를 덮어쓰지 않고 생성하며, 없는 파일 삭제는 성공으로 처리한다.

이 검증은 상위 경로 이동 문자가 포함된 객체 키로 임의 파일을 읽거나 삭제하는 경로 이탈을 막는다.

## S3 저장소

`S3FileStorage`는 공개 ACL을 설정하지 않고 비공개 버킷에 객체를 저장한다. 조회 시 파일 바이트와 콘텐츠 유형을 함께 반환하고, 존재하지 않는 객체와 일시적인 SDK 오류를 구분한다. SDK 예외는 저장소 계약의 `IOException`으로 변환해 상위 계층이 로컬 구현과 같은 방식으로 실패를 처리하게 한다.

## 객체 키 계약

프로필·배경·공지처럼 업무별 디렉터리와 UUID 파일명을 조합한 객체 키를 두 서비스가 동일하게 사용한다. 사용자 이미지 삭제에서는 허용된 내부 공개 경로인지 확인한 뒤 객체 키로 변환하며, 데이터베이스 커밋 이후에만 물리 파일을 지운다.

## 트레이드오프와 남은 검증

공통 계약은 저장소 구현 교체를 단순하게 하지만 두 서비스의 설정이 자동으로 동기화되는 것은 아니다. 로컬 루트, S3 버킷과 객체 키 prefix가 다르면 컴파일은 성공해도 실행 중 파일 부재가 발생한다. 실제 배포 환경에서 같은 비공개 객체를 양쪽 서비스가 조회하는 통합 검증은 별도로 필요하다.

## 구현 근거

- 공통 저장소 계약: [`FileStorage`](../../src/main/java/org/sadari/admin/sadariadmin/file/storage/FileStorage.java)
- 환경별 저장소 선택: [`FileStorageConfig`](../../src/main/java/org/sadari/admin/sadariadmin/file/config/FileStorageConfig.java)
- 로컬 구현: [`LocalFileStorage`](../../src/main/java/org/sadari/admin/sadariadmin/file/storage/LocalFileStorage.java)
- S3 구현: [`S3FileStorage`](../../src/main/java/org/sadari/admin/sadariadmin/file/storage/S3FileStorage.java)
