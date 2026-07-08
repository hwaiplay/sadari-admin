# Project Naming and Coding Conventions

이 문서는 프로젝트의 데이터베이스 및 백엔드(Java) 개발 시 AI와 개발자가 반드시 준수해야 하는 엄격한 명명 규칙(Naming Convention)을 정의합니다. 코드를 생성하거나 수정할 때 아래의 규칙을 예외 없이 적용하십시오.

## 1. Database Table Naming Rule
테이블 명칭은 성격에 따라 접두사를 구분하며, 접두사 이후의 글자 수를 엄격하게 제한합니다.

* **마스터 테이블 (Master Data):** `TM_` 로 시작
* **파생/트랜잭션 테이블 (Derived Data):** `TB_` 로 시작
* **길이 및 패딩 규칙:**
    * 접두사(`TM_`, `TB_`) 뒤에는 **반드시 영문 6자리**가 와야 합니다.
    * 의미를 담기에 글자 수가 6자리보다 짧거나 애매한 경우, 남는 뒷자리는 대문자 `X`로 채워서 6자리를 맞춥니다.
* **Examples:**
    * `TM_USERXM` (TM_ + USERXM 6자)
    * `TM_ADMINX` (TM_ + ADMINX 6자)

## 2. Database Column Naming Rule
컬럼 명칭은 언더바(`_`)를 기준으로 앞뒤 글자 수를 고정합니다.

* **형식 규칙:** `[영문 4자]_[영문 4자]` (예: `AAAA_BBBB`)
* **길이 및 패딩 규칙:**
    * 언더바 앞과 뒤는 각각 **반드시 영문 4자리**여야 합니다.
    * 단어의 길이가 4자리보다 짧은 경우, 남는 뒷자리는 대문자 `X`로 채워서 4자리를 맞춥니다.
* **고정으로 사용해야할 컬럼 단어:**
  * 사용자: USER
  * 사용: USEE
  * 'Y'나 'N'으로 여부 표시: YSNO
  * 등록: REGI(등록일 -> REGI_DATE)
  * 관리자: ADMN(관리자 아이디 - > IDXX)
* **Examples:**
    * `USER_IDXX` (`USER` 4자 + `_` + `IDXX` 4자)
    * `MENU_NMSX` (`MENU` 4자 + `_` + `NMSX` 4자)


## 3. Java Method Naming Rule
비즈니스 로직 및 데이터베이스 접근(DAO/Repository)을 처리하는 메서드는 수행하는 기능(Action)에 따라 접두사(Prefix)와 접미사(Suffix) 규칙을 준수해야 합니다. (`***`는 해당 도메인 또는 엔티티의 명칭을 의미합니다.)

* **다건 조회 (List):** `get***List`
    * 예: `getUserList()`, `getBoardList()`
* **카운트 조회 (Count):** `get***Cnt`
  * 예: `getUserCnt()`, `getBoardCnt()`
* **단건 조회 (Detail):** `get***Dtl`
    * 예: `getUserDtl()`, `getBoardDtl()`
* **데이터 등록 (Insert):** `set***`
    * 예: `setUser()`, `setBoard()`
* **데이터 수정 (Update):** `upt***`
    * 예: `uptUser()`, `uptBoard()`
* **데이터 삭제 (Delete):** `del***`
    * 예: `delUser()`, `delBoard()`
* **중복 검사 (Duplicate Check):** `dup***`
    * 예: `dupUser()`, `dupEmail()`