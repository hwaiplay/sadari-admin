# 회원 상태를 DB Outbox로 사용자 세션까지 전달

관리자가 회원을 정지해 데이터베이스 상태만 바꾸면 이미 발급된 사용자 세션은 이전 상태로 남을 수 있다. 반대로 관리자 요청 안에서 사용자 Redis를 직접 수정하면 두 애플리케이션의 인증 책임이 섞이고, Redis 장애가 관리자 트랜잭션의 정합성까지 흔들 수 있다. 현재 구현은 상태 변경과 Outbox 이벤트를 함께 저장하고 사용자 백엔드가 Redis 반영을 맡는다.

## 이용정지 등록

`CurrentUserServiceImpl`은 같은 회원의 정지 등록과 해제가 교차하지 않도록 회원 상태를 잠근다. 기간이 지난 정지가 있으면 먼저 만료 처리하고, 활성 정지가 남아 있으면 중복 등록을 거부한다.

새 정지 이력에는 정지 직전 상태를 보관한다. 기간 정지와 무기한 정지를 구분하며, 무기한 정지는 최고 관리자만 등록할 수 있다. 탈퇴 대기처럼 정지보다 우선하는 상태에는 새 정지를 적용하지 않는다.

## 상태 복구

정지를 해제하거나 기간 정지가 만료되면 저장해 둔 직전 상태로 복구한다. 다만 그 사이 더 우선하는 계정 상태가 적용되었다면 이를 덮어쓰지 않는다. 상태가 실제로 변경된 경우에만 사용자 서비스 반영 대기 상태와 Outbox 이벤트를 다시 저장한다.

물리 삭제된 회원의 제재 이력은 별도로 조회·해제할 수 있지만, 회원 행과 사용자 세션이 없으므로 Outbox 이벤트는 만들지 않는다. 감사 이력을 관리하는 작업과 현재 계정 상태를 변경하는 작업을 구분한 것이다.

## Outbox 경계

회원 상태, 정지 이력, 사용자 서비스 반영 대기 표시와 Outbox 이벤트는 같은 데이터베이스 트랜잭션에 포함된다. 이벤트 저장이 실패하면 상태 변경도 롤백된다. 사용자 백엔드는 이벤트를 소비할 때 현재 회원 상태를 다시 조회해 Redis를 갱신하므로 빠르게 연속된 정지와 해제도 최신 상태로 수렴할 수 있다.

## 트레이드오프

Outbox는 Redis 장애를 관리자 요청과 분리하고 재시도를 가능하게 하지만 상태 반영이 즉시 완료된다고 보장하지는 않는다. 관리자 화면의 저장 성공과 사용자 세션 반영 완료를 구분해야 하며, 지연과 반복 실패를 확인할 운영 지표가 필요하다.

## 구현 근거

- 회원 상태·정지 업무 계층: [`CurrentUserServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/currentuser/service/impl/CurrentUserServiceImpl.java)
- 회원 상태 조회·저장 계층: [`CurrentUserMapper.xml`](../../src/main/java/org/sadari/admin/sadariadmin/currentuser/mapper/CurrentUserMapper.xml)
- 사용자 프로젝트의 이벤트 소비 계층: [`UserStatusEventServiceImpl`](https://github.com/hwaiplay/sadari/blob/sprint/26.07/src/main/java/org/our/sadari/global/scheduler/service/UserStatusEventServiceImpl.java)
