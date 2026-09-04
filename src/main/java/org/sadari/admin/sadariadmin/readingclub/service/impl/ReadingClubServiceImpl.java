package org.sadari.admin.sadariadmin.readingclub.service.impl;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.readingclub.mapper.ReadingClubMapper;
import org.sadari.admin.sadariadmin.readingclub.service.ReadingClubService;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubActionVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubSearchVO;
import org.sadari.admin.sadariadmin.readingclub.vo.ReadingClubVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ReadingClubServiceImpl
 * author         : HanWon.Jang
 * date           : 2026-09-04
 * description    : 관리자 독서 모임 조회와 모집·이용 상태 조치를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-09-04        HanWon.Jang        최초 생성
 */
@Service
@Transactional(readOnly = true)
public class ReadingClubServiceImpl implements ReadingClubService {

    // 정상 운영 모임 상태
    private static final String CLUB_ACTIVE = "ACTIVE";

    // 관리자 이용 정지에 사용하는 모임 상태
    private static final String CLUB_PAUSED = "PAUSED";

    // 종료된 모임 상태
    private static final String CLUB_CLOSED = "CLOSED";

    // 신규 회원 모집 중지 조치
    private static final String ACTION_RECRUIT_STOP = "RECRUIT_STOP";

    // 모임 이용 정지 조치
    private static final String ACTION_SUSPEND = "SUSPEND";

    // 관리자 제한 해제 조치
    private static final String ACTION_RESTORE = "RESTORE";

    // 모임 종료 조치
    private static final String ACTION_CLOSE = "CLOSE";

    // 허용하는 모임 상태 검색 코드
    private static final Set<String> CLUB_STATUS_CODES = Set.of("ACTIVE", "OWNER_ELECTION", "PAUSED", "CLOSED");

    // 허용하는 공개 범위 검색 코드
    private static final Set<String> CLUB_VISIBILITY_CODES = Set.of("PUBLIC", "PRIVATE");

    // 허용하는 가입 방식 검색 코드
    private static final Set<String> CLUB_JOIN_CODES = Set.of("OPEN", "APPROVAL", "INVITE");

    // 허용하는 관리자 모임 조치 코드
    private static final Set<String> CLUB_ACTION_CODES = Set.of(
            ACTION_RECRUIT_STOP, ACTION_SUSPEND, ACTION_RESTORE, ACTION_CLOSE);

    // 목록 검색어 최대 문자 수
    private static final int KEYWORD_MAX_LENGTH = 100;

    // 관리자 조치 사유 최대 저장 바이트
    private static final int ACTION_REASON_MAX_BYTES = 2000;

    // 관리자 독서 모임 Mapper
    private final ReadingClubMapper readingClubMapper;

    /**
     * 관리자 독서 모임 서비스를 생성한다.
     *
     * @author HanWon.Jang
     * @param readingClubMapper 관리자 독서 모임 Mapper
     */
    public ReadingClubServiceImpl(ReadingClubMapper readingClubMapper) {
        this.readingClubMapper = readingClubMapper;
    }

    /** {@inheritDoc} */
    @Override
    public PageData<ReadingClubVO> getClubList(ReadingClubSearchVO search, AdminSessionVO admin) {
        // 운영 정보 목록은 로그인한 관리자만 조회하도록 인증 상태를 확인한다.
        checkLogin(admin);
        // 검색 문자열과 코드 및 날짜 범위를 검증된 값으로 정규화한다.
        ReadingClubSearchVO normalizedSearch = normalizeSearch(search);
        // 요청 페이지에 해당하는 조회 행 범위를 계산한다.
        PageRequest pageRequest = new PageRequest(normalizedSearch.getPage());
        // 목록 SQL의 시작 행을 검색 조건에 설정한다.
        normalizedSearch.setStartRow(pageRequest.getStartRow());
        // 목록 SQL의 마지막 행을 검색 조건에 설정한다.
        normalizedSearch.setEndRow(pageRequest.getEndRow());
        // 같은 검색 조건의 목록과 건수로 페이지 응답을 생성한다.
        return PageData.of(readingClubMapper.getClubList(normalizedSearch)
                         , readingClubMapper.getClubCnt(normalizedSearch), pageRequest);
    }

    /** {@inheritDoc} */
    @Override
    public ReadingClubVO getClubDtl(Long clubNumb, AdminSessionVO admin) {
        // 회원 작성 정보가 포함된 상세는 로그인한 관리자만 조회하도록 인증 상태를 확인한다.
        checkLogin(admin);
        // 조회 대상 모임 번호가 유효한지 확인한다.
        validateClubNumb(clubNumb);
        // 관리자용 모임 운영 상세를 조회한다.
        ReadingClubVO club = readingClubMapper.getClubDtl(clubNumb);
        // 삭제됐거나 존재하지 않는 모임이면 상세 화면을 제공하지 않는다.
        if (StringUtil.isEmpty(club)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.READING_CLUB_NOT_FOUND);
        }

        // 회원 작성 소개를 수정하지 않은 조회 전용 상세를 반환한다.
        return club;
    }

    /** {@inheritDoc} */
    @Override
    public PageData<ReadingClubActionVO> getActionList(Long clubNumb, int pageNumber, AdminSessionVO admin) {
        // 관리자 감사 이력은 로그인한 관리자만 조회하도록 인증 상태를 확인한다.
        checkLogin(admin);
        // 조회 대상 모임 번호가 유효하고 현재 존재하는지 확인한다.
        checkClub(clubNumb);
        // 요청 페이지에 해당하는 조회 행 범위를 계산한다.
        PageRequest pageRequest = new PageRequest(pageNumber);
        // 모임별 감사 이력 목록과 건수로 페이지 응답을 생성한다.
        return PageData.of(readingClubMapper.getActionList(clubNumb, pageRequest.getStartRow()
                                                          , pageRequest.getEndRow())
                         , readingClubMapper.getActionCnt(clubNumb), pageRequest);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ReadingClubVO setClubAction(Long clubNumb, ReadingClubActionVO action, AdminSessionVO admin) {
        // 관리자 조치 권한의 기반이 되는 로그인 상태를 확인한다.
        checkLogin(admin);
        // 조치 대상 모임 번호와 조치 유형 및 사유를 검증한다.
        validateAction(clubNumb, action);
        // 동일 모임의 상태 전이를 직렬화하도록 모임 행을 잠금 조회한다.
        ReadingClubVO club = readingClubMapper.getClubForUpdate(clubNumb);
        // 삭제됐거나 존재하지 않는 모임에는 감사 이력을 만들지 않는다.
        if (StringUtil.isEmpty(club)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.READING_CLUB_NOT_FOUND);
        }

        // 현재 상태와 모집 여부에 맞는 관리자 조치를 적용한다.
        String afterStatus = applyAction(club, action.getActnType());
        // 감사 이력에 대상 모임 번호를 설정한다.
        action.setClubNumb(clubNumb);
        // 감사 이력에 로그인 관리자 번호를 설정한다.
        action.setAdmnNumb(admin.getAdmnNumb());
        // 감사 이력에 조치 전 모임 상태를 설정한다.
        action.setBefrStat(club.getClubStat());
        // 감사 이력에 조치 후 모임 상태를 설정한다.
        action.setAftrStat(afterStatus);
        // 모임 행 잠금 안에서 모임별 다음 감사 번호를 계산해 이력을 등록한다.
        if (readingClubMapper.setClubAction(action) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.READING_CLUB_ACTION_CONFLICT);
        }

        // 모임장과 활성 모임원에게 상태 조치 안내 알림을 생성한다.
        readingClubMapper.setActionNotifications(action);
        // 조치 결과가 반영된 최신 관리자용 상세를 조회한다.
        ReadingClubVO updatedClub = readingClubMapper.getClubDtl(clubNumb);
        // 상태 변경과 감사 이력이 모두 반영된 최신 모임 정보를 반환한다.
        return updatedClub;
    }

    /** 관리자 조치 유형에 맞는 상태 전이를 적용한다. */
    private String applyAction(ReadingClubVO club, String actionType) {
        // 모집 중지는 정상 운영과 모집 가능 상태에서만 적용한다.
        if (ACTION_RECRUIT_STOP.equals(actionType)) {
            // 이미 제한된 상태에 중복 이력을 남기지 않도록 현재 값을 확인한다.
            if (!CLUB_ACTIVE.equals(club.getClubStat()) || !Constant.YES.equals(club.getRcrtYsno())
                    || readingClubMapper.uptRecruitStopped(club.getClubNumb()) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.READING_CLUB_ACTION_CONFLICT);
            }

            // 모집 중지는 기존 모임원 활동을 유지하므로 운영 상태를 그대로 반환한다.
            return club.getClubStat();
        }

        // 이용 정지는 정상 운영 중인 모임만 일시 중지로 전환한다.
        if (ACTION_SUSPEND.equals(actionType)) {
            // 다른 운영 상태를 관리자 정지로 덮어쓰지 않도록 현재 값을 확인한다.
            if (!CLUB_ACTIVE.equals(club.getClubStat())
                    || readingClubMapper.uptClubSuspended(club.getClubNumb()) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.READING_CLUB_ACTION_CONFLICT);
            }

            // 관리자 이용 정지 뒤의 모임 상태를 반환한다.
            return CLUB_PAUSED;
        }

        // 해제는 최근 관리자 조치로 모집 또는 이용이 제한된 모임에만 적용한다.
        if (ACTION_RESTORE.equals(actionType)) {
            // 최근 조치 유형으로 정책 상태와 관리자 제한을 구분한다.
            ReadingClubActionVO latestAction = readingClubMapper.getLatestAction(club.getClubNumb());
            // 최근 모집 중지 또는 이용 정지 조치가 아니면 관리자 해제를 허용하지 않는다.
            if (!canRestore(club, latestAction) || readingClubMapper.uptClubRestored(club.getClubNumb()) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.READING_CLUB_ACTION_CONFLICT);
            }

            // 관리자 제한 해제 뒤의 정상 운영 상태를 반환한다.
            return CLUB_ACTIVE;
        }

        // 종료는 아직 종료되지 않은 모임에만 적용한다.
        if (ACTION_CLOSE.equals(actionType)) {
            // 이미 종료된 모임에 중복 종료 이력을 남기지 않는다.
            if (CLUB_CLOSED.equals(club.getClubStat())
                    || readingClubMapper.uptClubClosed(club.getClubNumb()) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.READING_CLUB_ACTION_CONFLICT);
            }

            // 관리자 종료 뒤의 모임 상태를 반환한다.
            return CLUB_CLOSED;
        }

        // 허용 목록을 통과하지 못한 조치 유형은 공통 입력 오류로 거절한다.
        throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.READING_CLUB_ACTION_INVALID);
    }

    /** 최근 관리자 조치와 현재 상태로 해제 가능 여부를 판단한다. */
    private boolean canRestore(ReadingClubVO club, ReadingClubActionVO latestAction) {
        // 관리자 조치 이력이 없으면 정책상 일시 중지 상태를 임의 해제하지 않는다.
        if (StringUtil.isEmpty(latestAction)) {
            return false;
        }

        // 모집 중지 이력이 최신이고 모집이 실제 중지된 정상 운영 모임만 모집을 재개한다.
        if (ACTION_RECRUIT_STOP.equals(latestAction.getActnType())) {
            return CLUB_ACTIVE.equals(club.getClubStat()) && Constant.NO.equals(club.getRcrtYsno());
        }

        // 이용 정지 이력이 최신이고 현재 일시 중지인 모임만 정상 운영으로 복원한다.
        return ACTION_SUSPEND.equals(latestAction.getActnType()) && CLUB_PAUSED.equals(club.getClubStat());
    }

    /** 관리자 조치 입력값을 검증하고 저장 형식으로 정규화한다. */
    private void validateAction(Long clubNumb, ReadingClubActionVO action) {
        // 조치 대상 모임 번호가 유효한지 확인한다.
        validateClubNumb(clubNumb);
        // 조치 유형과 사유가 없으면 상태 변경을 시작하지 않는다.
        if (StringUtil.isEmpty(action) || StringUtil.isEmpty(action.getActnType())
                || StringUtil.isEmpty(action.getActnRson())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 공통코드 저장 형식에 맞춰 조치 유형을 대문자로 정규화한다.
        String actionType = action.getActnType().trim().toUpperCase(Locale.ROOT);
        // 앞뒤 공백을 제거한 관리자 판단 근거를 준비한다.
        String actionReason = action.getActnRson().trim();
        // 허용되지 않은 조치나 공백 사유 및 DB 저장 길이 초과를 거절한다.
        if (!CLUB_ACTION_CODES.contains(actionType) || actionReason.isEmpty()
                || actionReason.getBytes(StandardCharsets.UTF_8).length > ACTION_REASON_MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.READING_CLUB_ACTION_INVALID);
        }

        // 검증된 조치 유형을 감사 이력 저장값으로 설정한다.
        action.setActnType(actionType);
        // 검증된 조치 사유를 감사 이력 저장값으로 설정한다.
        action.setActnRson(actionReason);
    }

    /** 검색 조건을 검증하고 동적 SQL에 사용할 값으로 정규화한다. */
    private ReadingClubSearchVO normalizeSearch(ReadingClubSearchVO search) {
        // 검색 객체가 없으면 전체 목록을 조회할 기본 객체를 생성한다.
        ReadingClubSearchVO normalizedSearch = StringUtil.isEmpty(search) ? new ReadingClubSearchVO() : search;
        // 검색어의 앞뒤 공백을 제거하고 공백 입력은 검색 조건에서 제외한다.
        normalizedSearch.setKeyword(trimToNull(normalizedSearch.getKeyword()));
        // 모임 상태 코드를 대문자 형식으로 정규화한다.
        normalizedSearch.setClubStat(toUpperCase(normalizedSearch.getClubStat()));
        // 공개 범위 코드를 대문자 형식으로 정규화한다.
        normalizedSearch.setClubVisb(toUpperCase(normalizedSearch.getClubVisb()));
        // 가입 방식 코드를 대문자 형식으로 정규화한다.
        normalizedSearch.setJoinType(toUpperCase(normalizedSearch.getJoinType()));
        // 모집 가능 여부를 대문자 형식으로 정규화한다.
        normalizedSearch.setRcrtYsno(toUpperCase(normalizedSearch.getRcrtYsno()));
        // 검색어와 코드 및 날짜 범위가 허용 범위인지 확인한다.
        validateSearch(normalizedSearch);
        // 검증이 끝난 검색 조건을 반환한다.
        return normalizedSearch;
    }

    /** 검색어와 코드 및 날짜 범위를 검증한다. */
    private void validateSearch(ReadingClubSearchVO search) {
        // 과도한 검색어가 반복 전달되지 않도록 허용 길이를 제한한다.
        if (!StringUtil.isEmpty(search.getKeyword()) && search.getKeyword().length() > KEYWORD_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 모임 상태는 현재 정책에서 정의한 코드만 허용한다.
        if (!StringUtil.isEmpty(search.getClubStat()) && !CLUB_STATUS_CODES.contains(search.getClubStat())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 공개 범위는 공개와 비공개 코드만 허용한다.
        if (!StringUtil.isEmpty(search.getClubVisb()) && !CLUB_VISIBILITY_CODES.contains(search.getClubVisb())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 가입 방식은 현재 모임 정책에서 정의한 코드만 허용한다.
        if (!StringUtil.isEmpty(search.getJoinType()) && !CLUB_JOIN_CODES.contains(search.getJoinType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 모집 가능 여부는 공통 Y/N 값만 허용한다.
        if (!StringUtil.isEmpty(search.getRcrtYsno()) && !Constant.YES.equals(search.getRcrtYsno())
                && !Constant.NO.equals(search.getRcrtYsno())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 생성 시작일이 종료일보다 늦으면 의도와 다른 목록이 조회되므로 거절한다.
        if (!StringUtil.isEmpty(search.getRegiDateFrom()) && !StringUtil.isEmpty(search.getRegiDateTo())
                && search.getRegiDateFrom().isAfter(search.getRegiDateTo())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /** 모임 원본이 현재 존재하는지 확인한다. */
    private void checkClub(Long clubNumb) {
        // 조회 대상 모임 번호가 유효한지 확인한다.
        validateClubNumb(clubNumb);
        // 모임 상세가 없으면 연결된 감사 이력 화면도 제공하지 않는다.
        if (StringUtil.isEmpty(readingClubMapper.getClubDtl(clubNumb))) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.READING_CLUB_NOT_FOUND);
        }
    }

    /** 모임 번호의 유효 범위를 확인한다. */
    private void validateClubNumb(Long clubNumb) {
        // 양수가 아닌 모임 번호로 임의 데이터가 조회되지 않도록 거절한다.
        if (StringUtil.isEmpty(clubNumb) || clubNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /** 관리자 로그인 상태를 확인한다. */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체와 관리자 번호가 없으면 운영 정보 조회와 상태 변경을 허용하지 않는다.
        if (StringUtil.isEmpty(admin) || StringUtil.isEmpty(admin.getAdmnNumb())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }

    /** 검색 문자열의 앞뒤 공백을 제거하고 공백 입력을 Null로 변환한다. */
    private String trimToNull(String value) {
        // 문자열이 없으면 동적 검색 조건에서도 제외하도록 Null을 반환한다.
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        // 실제 문자가 없으면 검색 조건에서 제외하도록 정리한다.
        String trimmedValue = value.trim();
        // 공백만 입력한 경우 검색 조건을 만들지 않는다.
        if (trimmedValue.isEmpty()) {
            return null;
        }

        // 앞뒤 공백이 제거된 검색값을 반환한다.
        return trimmedValue;
    }

    /** 검색 코드를 대문자로 정규화한다. */
    private String toUpperCase(String value) {
        // 코드가 없으면 동적 검색 조건에서도 제외하도록 Null을 유지한다.
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        // DB 공통코드 저장 형식에 맞춘 대문자 값을 반환한다.
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
