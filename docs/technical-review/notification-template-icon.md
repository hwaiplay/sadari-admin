# 알림 템플릿과 아이콘의 저장 책임을 분리

알림에는 발생 시점에 확정해야 하는 제목·본문·이동 경로와, 목록을 조회할 때 바꿔도 되는 아이콘이 함께 보인다. 두 데이터를 하나로 저장하면 아이콘만 교체해도 템플릿과 기존 알림을 함께 수정해야 한다. 관리자 프로젝트는 템플릿과 아이콘을 별도 기능으로 관리한다.

## 템플릿 검증

`AlimTempServiceImpl`은 알림 상황과 템플릿 코드 조합의 중복을 확인하고, 한국어·영어 제목과 본문을 함께 검증한다. 사용자 이름이나 콘텐츠 이름처럼 실행 시점에 치환할 값은 허용 목록에 포함된 자리표시자만 저장할 수 있다. 형식이 깨졌거나 지원하지 않는 자리표시자는 저장 전에 거부한다.

이 검증은 운영자가 작성한 템플릿이 사용자 서비스에서 치환되지 않은 문자열로 노출되는 문제를 줄인다. 이동 링크와 사용 여부도 템플릿과 함께 관리하므로 상황별 알림 동작을 앱 재배포 없이 조정할 수 있다.

## 아이콘 검증

아이콘은 PNG와 SVG를 지원하지만 브라우저가 보낸 파일명이나 콘텐츠 유형만 신뢰하지 않는다. `AlimIconServiceImpl`은 업로드 바이트를 읽어 형식을 판정하고 다음 검증을 적용한다.

- PNG 시그니처와 이미지 크기 확인
- 디코딩 후 안전한 PNG로 다시 생성해 불필요한 메타데이터 제거
- SVG의 스크립트·이벤트 속성·외부 URL·외부 문서 참조 차단
- 상대 단위와 비정상적인 크기 값 제한
- 실제 알림 상황으로 등록된 값인지 확인

같은 알림 상황에 아이콘이 이미 있으면 신규 행을 늘리지 않고 이미지를 교체한다.

## 책임 분리

사용자 서비스는 알림을 만들 때 활성 템플릿으로 문구와 이동 정보를 확정하고, 알림 목록을 조회할 때 상황에 맞는 아이콘을 결합한다. 따라서 문구 변경은 이후 생성되는 알림에 적용되고, 아이콘 변경은 기존 목록에도 반영될 수 있다.

## 트레이드오프

템플릿과 아이콘을 분리하면 운영 변경 범위가 작아지지만 사용자 서비스의 생성 로직과 조회 로직이 같은 알림 상황 계약을 따라야 한다. 코드가 한쪽에만 추가되면 기본 아이콘이나 템플릿 조회 실패로 이어질 수 있으므로 두 저장소의 계약 테스트가 필요하다.

## 구현 근거

- 템플릿 업무 계층: [`AlimTempServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/alim/service/impl/AlimTempServiceImpl.java)
- 템플릿 조회·저장 계층: [`AlimTempMapper.xml`](../../src/main/java/org/sadari/admin/sadariadmin/alim/mapper/AlimTempMapper.xml)
- 아이콘 검증 계층: [`AlimIconServiceImpl`](../../src/main/java/org/sadari/admin/sadariadmin/alimicon/service/impl/AlimIconServiceImpl.java)
- 아이콘 조회·저장 계층: [`AlimIconMapper.xml`](../../src/main/java/org/sadari/admin/sadariadmin/alimicon/mapper/AlimIconMapper.xml)
