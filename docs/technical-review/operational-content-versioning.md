# 운영 콘텐츠의 작성본과 사용자 노출본을 분리

공지사항과 서비스 정보는 저장 버튼을 누른 시점과 사용자에게 공개할 시점이 다르다. 작성 중인 내용을 즉시 같은 행에 덮어쓰면 검토가 끝나지 않은 문구가 노출되거나, 기존 공개본을 되돌릴 기준이 사라진다. 현재 구현은 콘텐츠 버전과 배포 상태를 분리해 초안과 사용자 노출본을 함께 보존한다.

## 버전 생성

신규 공지사항과 서비스 정보는 첫 버전을 미배포 상태로 저장한다. 미배포 버전을 다시 수정하면 같은 버전을 갱신하지만, 이미 배포된 버전을 수정하면 현재 공개 내용을 유지한 채 다음 버전의 초안을 만든다.

버전 번호는 화면 값만 믿고 증가시키지 않는다. `NoticeServiceImpl`과 `ServiceInfoServiceImpl`은 콘텐츠 단위로 최신 버전을 잠근 뒤 현재 배포 상태를 다시 조회한다. 수정과 배포가 동시에 요청되어도 배포본을 초안으로 덮어쓰거나 같은 버전 번호를 중복 생성하지 않게 하기 위한 경계다.

## 배포 전환

관리자가 특정 버전을 배포하면 기존 배포본을 해제하고 선택한 버전만 배포 상태로 변경한다. 두 변경은 하나의 트랜잭션에서 처리하므로 중간 실패 시 기존 사용자 노출본을 잃지 않는다. 사용자 서비스는 배포된 버전만 조회한다.

등록자·수정자·배포자와 각 처리 시점을 분리해 누가 초안을 만들고 누가 실제 노출을 결정했는지 추적할 수 있다.

## 입력과 파일 안전성

공지사항과 서비스 정보의 HTML은 허용 요소와 속성만 남기고 정제한다. 외부 이미지와 실행 가능한 스타일 표현을 제거하고, 관리자 파일 업로드 규칙으로 생성된 내부 이미지 경로만 허용한다. 콘텐츠 전체를 삭제할 때는 과거 버전에만 포함된 이미지도 수집해 정리 대상에 포함한다.

팝업은 화면과 팝업 식별값 조합을 유지하며 한국어·영어 제목과 JSON 콘텐츠를 검증한다. 공지사항처럼 버전을 만들지는 않지만, 잘못된 화면 값과 중복 조합이 사용자 조회를 모호하게 만들지 않도록 저장 전에 차단한다.

## 트레이드오프

버전 구조는 공개 안정성과 이력 추적에 유리하지만 저장 행과 편집 규칙이 늘어난다. 현재는 한 콘텐츠에 하나의 배포본만 유지하며 예약 배포와 승인 단계는 포함하지 않는다. 다단계 검수가 필요해지면 초안·검토·승인·배포 상태를 별도 워크플로로 확장해야 한다.

## 구현 근거

- 공지사항 업무 계층: [`NoticeServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/notice/service/impl/NoticeServiceImpl.java)
- 서비스 정보 업무 계층: [`ServiceInfoServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/serviceinfo/service/impl/ServiceInfoServiceImpl.java)
- 팝업 업무 계층: [`PopupContentServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/popup/service/impl/PopupContentServiceImpl.java)
- 공지 이미지 업무 계층: [`NoticeImageService`](../../src/main/java/org/sadari/admin/sadariadmin/notice/service/NoticeImageService.java)

