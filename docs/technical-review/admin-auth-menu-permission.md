# Redis 인증과 메뉴 권한을 API에서 함께 검증

관리자 화면은 메뉴를 숨기는 것만으로 접근을 통제할 수 없다. 주소를 직접 입력하거나 API를 별도로 호출할 수 있기 때문에 서버가 요청마다 인증과 업무 권한을 다시 확인해야 한다. 이 글은 Redis 세션을 Spring Security 인증 객체로 복원하고, 메뉴별 읽기·쓰기·삭제 권한을 API 요청에 적용하는 현재 구조를 설명한다.

## 인증 복원

로그인 이후 관리자 세션은 Redis에서 관리한다. `RedisAuthenticationFilter`는 로그인과 공개 공통코드 조회를 제외한 요청에서 세션을 읽고, 유효한 관리자 정보가 있으면 권한 그룹을 Spring Security의 역할로 변환해 `SecurityContext`에 저장한다.

이 방식은 Controller마다 쿠키와 Redis를 직접 확인하는 중복을 줄인다. 이후 계층은 인증된 관리자 객체를 공통 기준으로 사용할 수 있고, 세션이 없으면 보호된 업무 API가 인증되지 않은 요청으로 처리된다.

## 메뉴 권한 검증

`MenuPermissionInterceptor`는 API 경로를 관리자 메뉴 URL에 연결한다. 로그인 관리자의 권한 그룹으로 해당 메뉴의 권한을 조회한 뒤 HTTP 메서드에 따라 다음처럼 판정한다.

| 요청 | 필요한 권한 |
| --- | --- |
| `GET`, `HEAD`, `OPTIONS` | 읽기 |
| `DELETE` | 삭제 |
| 그 외 쓰기 요청 | 쓰기 |

프론트엔드가 메뉴를 감추더라도 서버 인터셉터가 같은 권한을 검사하므로 직접 URL 접근과 임의 API 호출을 함께 차단할 수 있다. 권한 정보가 없거나 요청 종류에 맞는 권한이 없으면 금지 응답을 반환한다.

## 권한 그룹 관리

`AuthGroupServiceImpl`은 권한 그룹과 메뉴별 권한을 하나의 저장 흐름에서 관리한다. 그룹 등록·수정 시 기본 정보를 먼저 검증하고, 연결된 메뉴 권한을 함께 저장한다. 삭제할 때도 인증된 관리자 요청인지 확인하고 사용 중인 관계를 고려한다.

## 트레이드오프

API와 메뉴의 연결을 명시적인 매핑으로 관리하면 검토하기 쉽지만, 새 관리 기능을 추가할 때 매핑을 빠뜨릴 수 있다. 메뉴 등록만으로 보호 범위가 자동 확장되는 구조가 아니므로 API 추가 시 인터셉터 연결과 권한 테스트를 함께 갱신해야 한다.

또한 요청마다 Redis 세션을 복원하므로 Redis 장애가 관리자 인증에 직접 영향을 준다. 현재 구조는 관리자 기능을 계속 허용하는 것보다 인증 실패를 우선하는 방향이다.

## 구현 근거

- 인증 계층: [`RedisAuthenticationFilter`](../../src/main/java/org/sadari/admin/sadariadmin/config/RedisAuthenticationFilter.java)
- 메뉴 권한 계층: [`MenuPermissionInterceptor`](../../src/main/java/org/sadari/admin/sadariadmin/config/MenuPermissionInterceptor.java)
- 권한 그룹 업무 계층: [`AuthGroupServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/authgroup/service/impl/AuthGroupServiceImpl.java)
- 권한 조회 계층: [`AuthGroupMapper.xml`](../../src/main/java/org/sadari/admin/sadariadmin/authgroup/mapper/AuthGroupMapper.xml)

