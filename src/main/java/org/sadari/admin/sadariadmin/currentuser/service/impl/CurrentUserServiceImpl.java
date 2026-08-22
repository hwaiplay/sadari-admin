package org.sadari.admin.sadariadmin.currentuser.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;

import lombok.extern.slf4j.Slf4j;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.currentuser.mapper.CurrentUserMapper;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserFileVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserLoginHistoryVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSearchVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserStatusEventVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserWithdrawalHistoryVO;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * fileName       : CurrentUserServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 현재 사용자와 마스킹된 로그인 및 계정 처리 이력 조회 업무를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    회원 상태 변경을 사용자 서버용 Outbox 이벤트와 함께 저장
 * 2026-07-30        SeungHyeon.Kang    로그인 제공자 공통코드 검색 검증
 * 2026-07-30        SeungHyeon.Kang    정지 이력 동기화 상태와 임시 Outbox 전달 적용
 * 2026-08-13        SeungHyeon.Kang    삭제 회원의 유효 제재 목록과 관리자 해제 처리 추가
 * 2026-08-22        SeungHyeon.Kang    신고 이력과 프로필 수동 조치 종결
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class CurrentUserServiceImpl implements CurrentUserService {

    // 사용자 업로드 이미지 공개 경로 접두사
    private static final String UPLOAD_ACCESS_PREFIX = "/uploads/";

    // 프로필 이미지 저장 디렉터리
    private static final Path PROFILE_IMAGE_ROOT = Paths.get("profile");

    // 배경 이미지 저장 디렉터리
    private static final Path BACKGROUND_IMAGE_ROOT = Paths.get("background");

    // 회원번호 또는 닉네임 검색어 최대 문자 수
    private static final int KEYWORD_MAX_LENGTH = 100;

    // 관리자 내부 정지 및 해제 메모 최대 저장 바이트
    private static final int SUSPENSION_CONTENT_MAX_BYTES = 1000;

    // 현 사용자 상세에서 프로필 사진 초기화로 종결할 신고 처리 내용
    private static final String PROFILE_COMPLAINT_PROCESS =
            Constant.CMPL_MANUAL_PROCESS_PREFIX
            + " 현 사용자 상세에서 프로필 사진 초기화. 관련 미처리 신고를 일괄 종결함.";

    // 현 사용자 상세에서 한줄소개 초기화로 종결할 신고 처리 내용
    private static final String INTRO_COMPLAINT_PROCESS =
            Constant.CMPL_MANUAL_PROCESS_PREFIX
            + " 현 사용자 상세에서 한줄소개 초기화. 관련 미처리 신고를 일괄 종결함.";

    // 현재 사용자 조회 Mapper
    private final CurrentUserMapper currentUserMapper;

    // 회원 상태 공통코드 검증 Mapper
    private final CodeMapper codeMapper;

    // 현재 사용자 이미지 물리 파일 저장소
    private final FileStorage fileStorage;

    /**
     * 현재 사용자 조회 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param currentUserMapper 현재 사용자 조회 Mapper
     * @param codeMapper 회원 상태 공통코드 검증 Mapper
     * @param fileStorage 현재 사용자 이미지 물리 파일 저장소
     */
    public CurrentUserServiceImpl(CurrentUserMapper currentUserMapper, CodeMapper codeMapper
                                  , FileStorage fileStorage) {

        this.currentUserMapper = currentUserMapper;
        this.codeMapper = codeMapper;
        this.fileStorage = fileStorage;
    }

    /**
     * 관리자 검색 조건에 맞는 현재 사용자 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 사용자 검색 조건
     * @param admin 로그인한 관리자
     * @return 검색된 현재 사용자 페이지
     */
    @Override
    public PageData<CurrentUserVO> getCurrentUserList(CurrentUserSearchVO search, AdminSessionVO admin) {
        // 회원 개인정보 목록은 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 비어 있는 검색 객체를 안전한 기본 검색 조건으로 보정한다
        CurrentUserSearchVO normalizedSearch = normalizeSearch(search);
        // 검색 요청 페이지에 해당하는 조회 행 범위를 계산한다
        PageRequest pageRequest = new PageRequest(normalizedSearch.getPage());
        // 목록과 건수 SQL이 같은 페이지 범위를 사용하도록 검색 객체에 설정한다
        normalizedSearch.setStartRow(pageRequest.getStartRow());
        // 페이지 마지막 행을 검색 객체에 설정한다
        normalizedSearch.setEndRow(pageRequest.getEndRow());
        // 같은 검색 조건의 목록과 전체 건수로 페이지 응답을 생성한다
        return PageData.of(currentUserMapper.getCurrentUserList(normalizedSearch)
                         , currentUserMapper.getCurrentUserListCount(normalizedSearch), pageRequest);
    }

    /**
     * 회원번호로 현재 사용자 상세와 활동 요약을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param admin 로그인한 관리자
     * @return 현재 사용자 상세정보
     */
    @Override
    public CurrentUserVO getCurrentUserDtl(Long userNumb, AdminSessionVO admin) {
        // 회원 개인정보 상세는 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 현재 사용자 원본을 특정할 수 있도록 회원번호를 검증한다
        validateUserNumb(userNumb);
        // 암호화된 외부 식별값을 제외한 사용자 상세와 활동 집계를 조회한다
        CurrentUserVO currentUser = currentUserMapper.getCurrentUserDtl(userNumb);

        // 영구 삭제되어 현재 회원 원본이 없으면 현재 사용자 상세로 표시하지 않는다
        if (StringUtil.isEmpty(currentUser)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CURRENT_USER_NOT_FOUND);
        }

        // 조회된 현재 사용자 상세와 활동 요약을 반환한다
        return currentUser;
    }

    /**
     * 현재 사용자의 프로필 사진을 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @return 변경된 현재 사용자 상세정보
     */
    @Transactional
    @Override
    public CurrentUserVO delUserProfImage(Long userNumb, AdminSessionVO admin) {
        // 프로필 이미지 참조와 저장 파일을 정리한 최신 사용자 상세를 반환한다
        return delUserImage(userNumb, admin, true);
    }

    /**
     * 현재 사용자의 배경화면을 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @return 변경된 현재 사용자 상세정보
     */
    @Transactional
    @Override
    public CurrentUserVO delUserBgimImage(Long userNumb, AdminSessionVO admin) {
        // 배경 이미지 참조와 저장 파일을 정리한 최신 사용자 상세를 반환한다
        return delUserImage(userNumb, admin, false);
    }

    /**
     * 현재 사용자의 한줄 소개를 NULL 처리한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @return 변경된 현재 사용자 상세정보
     */
    @Transactional
    @Override
    public CurrentUserVO delUserIntroduction(Long userNumb, AdminSessionVO admin) {
        // 프로필 정보 조치 권한과 현재 회원 존재 여부를 확인한다
        checkLogin(admin);
        // 다른 회원을 수정하지 않도록 양수 회원번호만 허용한다
        validateUserNumb(userNumb);
        // 이미 소개가 없거나 회원이 삭제된 요청은 삭제 성공으로 오인하지 않는다
        if (currentUserMapper.delUserIntroduction(userNumb) != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CURRENT_USER_NOT_FOUND);
        }
        // 한줄소개 초기화로 해결된 해당 사용자의 모든 미처리 한줄소개 신고를 함께 종결한다
        currentUserMapper.uptUserComplaints(Constant.CMPL_TARGET_INTRODUCTION, userNumb
                                            , INTRO_COMPLAINT_PROCESS, admin.getAdmnNumb());
        // 한줄 소개가 제거된 최신 현재 사용자 상세를 반환한다
        return getCurrentUserDtl(userNumb, admin);
    }

    /**
     * 현재 사용자의 마스킹된 로그인 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인한 관리자
     * @return 로그인 이력 페이지
     */
    @Override
    public PageData<CurrentUserLoginHistoryVO> getLoginHistoryList(Long userNumb, int pageNumber
                                                                 , AdminSessionVO admin) {
        // 로그인 IP가 포함된 접속 이력은 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 현재 사용자에 연결된 이력만 노출하도록 회원 존재 여부를 확인한다
        checkCurrentUser(userNumb);
        // 로그인 이력의 페이지 범위를 계산한다
        PageRequest pageRequest = new PageRequest(pageNumber);
        // 마스킹된 로그인 이력과 전체 건수로 페이지 응답을 생성한다
        return PageData.of(currentUserMapper.getLoginHistoryList(userNumb, pageRequest.getStartRow()
                                                                , pageRequest.getEndRow())
                         , currentUserMapper.getLoginHistoryListCount(userNumb), pageRequest);
    }

    /**
     * 현재 사용자의 계정 비활성화와 영구 탈퇴 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인한 관리자
     * @return 계정 처리 이력 페이지
     */
    @Override
    public PageData<CurrentUserWithdrawalHistoryVO> getWithdrawalHistoryList(Long userNumb, int pageNumber
                                                                           , AdminSessionVO admin) {
        // 계정 처리 이력은 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 영구 삭제 이력 전용 화면과 분리하기 위해 현재 회원 존재 여부를 확인한다
        checkCurrentUser(userNumb);
        // 계정 처리 이력의 페이지 범위를 계산한다
        PageRequest pageRequest = new PageRequest(pageNumber);
        // 민감한 자유 입력 사유를 제외한 계정 처리 이력 페이지를 생성한다
        return PageData.of(currentUserMapper.getWithdrawalHistoryList(userNumb, pageRequest.getStartRow()
                                                                     , pageRequest.getEndRow())
                         , currentUserMapper.getWithdrawalHistoryCnt(userNumb), pageRequest);
    }

    /**
     * 현재 사용자와 사용자 작성 대상이 받은 신고 누적 건수와 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 조회할 회원번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인한 관리자
     * @return 받은 신고 이력 페이지
     */
    @Override
    public PageData<CurrentUserComplaintVO> getComplaintHistoryList(Long userNumb, int pageNumber
                                                                  , AdminSessionVO admin) {
        // 신고 대상 내용과 신고자 정보는 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 비활성화와 영구 탈퇴 대기를 포함한 현재 회원만 상세 이력에 연결한다
        checkCurrentUser(userNumb);
        // 받은 신고 이력의 요청 페이지 범위를 계산한다
        PageRequest pageRequest = new PageRequest(pageNumber);
        // 대상 소유자 스냅샷으로 집계한 누적 건수와 신고 이력을 함께 반환한다
        return PageData.of(currentUserMapper.getComplaintHistoryList(userNumb, pageRequest.getStartRow()
                                                                    , pageRequest.getEndRow())
                         , currentUserMapper.getComplaintHistoryCnt(userNumb), pageRequest);
    }

    /**
     * 현재 사용자의 관리자 이용 정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 이용 정지 이력 페이지
     */
    @Transactional
    @Override
    public PageData<CurrentUserSuspensionVO> getSuspensionHistoryList(Long userNumb, int pageNumber
                                                                    , AdminSessionVO admin) {
        checkLogin(admin);
        checkCurrentUser(userNumb);
        // 상세 화면 조회 시 종료 시각이 지난 기간 정지를 즉시 만료 처리한다
        expireSuspensionIfNeeded(userNumb, admin.getAdmnNumb());
        // 정지 이력의 요청 페이지 범위를 계산한다
        PageRequest pageRequest = new PageRequest(pageNumber);
        // 코드명과 관리자 내부 메모를 포함한 정지 이력 페이지를 반환한다
        return PageData.of(currentUserMapper.getSuspensionHistoryList(userNumb, pageRequest.getStartRow()
                                                                    , pageRequest.getEndRow())
                         , currentUserMapper.getSuspensionHistoryCnt(userNumb), pageRequest);
    }

    /**
     * 회원에게 기간 또는 무기한 이용 정지를 적용한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param suspension 정지 유형과 사유 및 기간
     * @param admin 처리 관리자
     * @return 등록된 정지 이력
     */
    @Transactional
    @Override
    public CurrentUserSuspensionVO setUserSuspension(Long userNumb, CurrentUserSuspensionVO suspension
                                                    , AdminSessionVO admin) {
        checkLogin(admin);
        validateUserNumb(userNumb);
        validateSuspension(suspension, admin);

        // 같은 회원의 정지 등록과 해제가 교차하지 않도록 회원 행을 먼저 잠근다
        String currentUserStat = currentUserMapper.getUserStatusForUpdate(userNumb);
        if (StringUtil.isEmpty(currentUserStat)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CURRENT_USER_NOT_FOUND);
        }
        if (Constant.USER_STAT_DELETE_PENDING.equals(currentUserStat)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 종료 시각이 지난 이전 기간 정지가 있으면 새 정지 등록 전에 만료시킨다
        expireSuspensionIfNeeded(userNumb, admin.getAdmnNumb());
        // 이전 정지가 만료되며 복구된 최신 상태를 새 정지의 직전 상태로 사용한다
        currentUserStat = currentUserMapper.getUserStatusForUpdate(userNumb);
        if (!StringUtil.isEmpty(currentUserMapper.getActiveSuspForUpdate(userNumb))) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.USER_SUSPENSION_DUPLICATE);
        }

        // 정지 해제 후 되돌릴 현재 상태와 관리자 감사 정보를 기록한다
        suspension.setUserNumb(userNumb);
        suspension.setPrevStat(currentUserStat);
        suspension.setSpndStat(Constant.SPND_STAT_ACTIVE);
        suspension.setSyncStat(Constant.USER_STATUS_SYNC_PENDING);
        suspension.setStrtDate(LocalDateTime.now());
        suspension.setSpndCntn(trimToNull(suspension.getSpndCntn()));
        suspension.setRegiAdmn(admin.getAdmnNumb());
        suspension.setUpdtAdmn(admin.getAdmnNumb());
        currentUserMapper.setUserSuspension(suspension);

        // 정상 또는 비활성화 회원에게만 정지 상태를 투영한다
        if (currentUserMapper.uptCurrentUserSuspended(userNumb) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.COMMON_INVALID_REQUEST);
        }
        // 사용자 서버가 분리되어 있어도 상태 변경을 감지하도록 같은 트랜잭션에 이벤트를 저장한다
        setCurrentUserStatusEvent(userNumb, suspension.getSpndNumb());
        // 이력 번호와 감사 정보가 설정된 정지 등록 결과를 반환한다
        return suspension;
    }

    /**
     * 적용 중인 회원 이용 정지를 관리자 해제한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param spndNumb 정지 이력 번호
     * @param request 해제 메모
     * @param admin 처리 관리자
     */
    @Transactional
    @Override
    public void uptUserSuspensionReleased(Long userNumb, Long spndNumb, CurrentUserSuspensionVO request
                                         , AdminSessionVO admin) {
        checkLogin(admin);
        validateUserNumb(userNumb);
        if (StringUtil.isEmpty(spndNumb) || spndNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        String releaseContent = StringUtil.isEmpty(request) ? null : trimToNull(request.getRlesCntn());
        validateContentLength(releaseContent);
        String currentUserStat = currentUserMapper.getUserStatusForUpdate(userNumb);
        if (StringUtil.isEmpty(currentUserStat)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CURRENT_USER_NOT_FOUND);
        }

        CurrentUserSuspensionVO activeSuspension = currentUserMapper.getActiveSuspForUpdate(userNumb);
        if (StringUtil.isEmpty(activeSuspension)
                || !spndNumb.equals(activeSuspension.getSpndNumb())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.USER_SUSPENSION_NOT_FOUND);
        }

        activeSuspension.setRlesCntn(releaseContent);
        activeSuspension.setRlesAdmn(admin.getAdmnNumb());
        activeSuspension.setUpdtAdmn(admin.getAdmnNumb());
        if (currentUserMapper.uptUserSuspensionReleased(activeSuspension) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.USER_SUSPENSION_NOT_FOUND);
        }

        // 영구 탈퇴 대기 등 다른 상태가 이미 우선 적용됐다면 해당 상태를 유지한다
        if (currentUserMapper.uptUserStatusAfterSuspend(
                userNumb, activeSuspension.getPrevStat()) == 1) {
            // 상태가 실제 복구된 정지 이력을 사용자 서버 반영 대기 상태로 변경한다
            uptUserSuspSyncPending(activeSuspension.getSpndNumb());
            // 복구된 상태를 사용자 서버가 반영하도록 같은 트랜잭션에 이벤트를 저장한다
            setCurrentUserStatusEvent(userNumb, activeSuspension.getSpndNumb());
        }
    }

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 검색할 과거 회원 번호
     * @param pageNumber 조회할 페이지 번호
     * @param admin 로그인 관리자
     * @return 삭제 회원의 유효 제재 페이지
     */
    @Override
    public PageData<CurrentUserSuspensionVO> getDeletedSuspensionList(Long userNumb, int pageNumber
                                                                    , AdminSessionVO admin) {
        // 관리자 인증이 확인된 경우에만 삭제 회원 제재 이력을 조회한다
        checkLogin(admin);

        // 회원 번호 검색값이 있으면 실제 발급 범위에 맞는지 검증한다
        if (!StringUtil.isEmpty(userNumb)) {
            validateUserNumb(userNumb);
        }

        // 공통 관리자 목록과 같은 페이지 범위를 계산한다
        PageRequest pageRequest = new PageRequest(pageNumber);
        // 해시를 노출하지 않고 과거 회원 번호와 제재 감사정보만 반환한다
        return PageData.of(currentUserMapper.getDeletedSuspensionList(userNumb, pageRequest.getStartRow()
                                                                     , pageRequest.getEndRow())
                         , currentUserMapper.getDeletedSuspensionCnt(userNumb), pageRequest);
    }

    /**
     * 물리 삭제된 회원에게 남아 있는 유효 제재를 해제한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 과거 회원 번호
     * @param spndNumb 제재 이력 번호
     * @param request 필수 해제 메모
     * @param admin 처리 관리자
     */
    @Transactional
    @Override
    public void uptDeletedSuspReleased(Long userNumb, Long spndNumb, CurrentUserSuspensionVO request
                                      , AdminSessionVO admin) {
        // 삭제 회원 제재를 해제한 관리자를 감사정보에 남기기 위해 인증을 검증한다
        checkLogin(admin);
        // 삭제된 과거 계정을 특정할 회원 번호를 검증한다
        validateUserNumb(userNumb);

        // 해제할 단일 제재 이력 번호가 없으면 변경하지 않는다
        if (StringUtil.isEmpty(spndNumb) || spndNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 삭제 회원 제재 해제에는 관리 판단 근거가 남도록 메모를 필수로 받는다
        String releaseContent = StringUtil.isEmpty(request) ? null : trimToNull(request.getRlesCntn());
        if (StringUtil.isEmpty(releaseContent)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // DB 저장 크기를 초과하는 메모는 감사정보 절단 없이 요청 단계에서 거절한다
        validateContentLength(releaseContent);
        // 동일 제재를 여러 관리자가 동시에 해제하지 않도록 유효 이력을 잠근다
        CurrentUserSuspensionVO activeSuspension = currentUserMapper.getDeletedActiveSuspForUpdate(userNumb, spndNumb);
        if (StringUtil.isEmpty(activeSuspension)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.USER_SUSPENSION_NOT_FOUND);
        }

        // 관리자 판단 근거를 해제 이력에 기록한다
        activeSuspension.setRlesCntn(releaseContent);
        // 실제 해제 관리자를 감사정보에 기록한다
        activeSuspension.setRlesAdmn(admin.getAdmnNumb());
        // 마지막 수정 관리자를 해제 관리자와 일치시킨다
        activeSuspension.setUpdtAdmn(admin.getAdmnNumb());
        // 회원 행이 없는 제재 이력만 해제하며 사용자 상태와 Outbox는 변경하지 않는다
        if (currentUserMapper.uptUserSuspensionReleased(activeSuspension) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.USER_SUSPENSION_NOT_FOUND);
        }
    }

    /**
     * 종료 시각이 지난 기간 정지를 만료하고 정지 직전 상태를 복구한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 회원 번호
     * @param adminNumb 만료 확인 관리자 번호
     */
    private void expireSuspensionIfNeeded(Long userNumb, Long adminNumb) {
        CurrentUserSuspensionVO activeSuspension = currentUserMapper.getActiveSuspForUpdate(userNumb);
        if (StringUtil.isEmpty(activeSuspension)
                || !Constant.SPND_TYPE_PERIOD.equals(activeSuspension.getSpndType())
                || activeSuspension.getEndxDate().isAfter(LocalDateTime.now())) {
            // 현재 만료할 기간 정지가 없어 상태 변경을 종료한다
            return;
        }

        activeSuspension.setUpdtAdmn(adminNumb);
        if (currentUserMapper.uptUserSuspensionExpired(activeSuspension) != 1) {
            // 다른 요청이 먼저 만료 처리한 정지는 중복 복구하지 않는다
            return;
        }
        if (currentUserMapper.uptUserStatusAfterSuspend(
                userNumb, activeSuspension.getPrevStat()) == 1) {
            // 상태가 실제 복구된 정지 이력을 사용자 서버 반영 대기 상태로 변경한다
            uptUserSuspSyncPending(activeSuspension.getSpndNumb());
            // 만료로 복구된 상태를 사용자 서버가 반영하도록 이벤트를 저장한다
            setCurrentUserStatusEvent(userNumb, activeSuspension.getSpndNumb());
        }
    }

    /**
     * 회원 상태가 변경된 정지 이력을 사용자 서버 반영 대기 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param spndNumb 반영을 기다릴 정지 이력 번호
     */
    private void uptUserSuspSyncPending(Long spndNumb) {

        // 실제 상태 변경과 관리자 반영 표시가 어긋나지 않도록 한 건 수정을 보장한다
        if (currentUserMapper.uptUserSuspSyncPending(spndNumb) != 1) {
            // 반영 대기 상태를 기록하지 못하면 현재 회원 상태 변경도 함께 롤백한다
            throw new IllegalStateException("회원 정지 동기화 대기 상태 변경에 실패했습니다.");
        }

    }

    /**
     * 사용자 서버가 현재 회원 상태를 다시 조회하도록 Outbox 이벤트를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 상태 변경을 알릴 회원 번호
     * @param spndNumb 실제 반영 상태를 기록할 정지 이력 번호
     */
    private void setCurrentUserStatusEvent(Long userNumb, Long spndNumb) {

        // 상태 변경 대상 없이 운영 이벤트가 생성되지 않도록 회원 번호를 검증한다
        if (StringUtil.hasEmpty(userNumb, spndNumb)) {
            // 호출부 오류로 현재 상태 변경 트랜잭션을 롤백한다
            throw new IllegalArgumentException("회원 번호는 필수 값입니다.");
        }

        // 사용자 서버에 전달할 회원 상태 변경 이벤트를 생성한다
        CurrentUserStatusEventVO event = new CurrentUserStatusEventVO();
        // 사용자 서버가 지원하는 회원 상태 변경 이벤트 유형을 설정한다
        event.setEvntType(Constant.EVENT_TYPE_USER_STATUS_CHANGED);
        // 처리 시점에 현재 상태를 다시 조회할 대상 회원 번호를 설정한다
        event.setUserNumb(userNumb);
        // 실제 반영 완료 상태를 기록할 정지 이력 번호를 설정한다
        event.setSpndNumb(spndNumb);
        // 이벤트까지 저장되어야 상태 변경과 서비스 간 전달이 하나의 작업으로 확정된다
        if (currentUserMapper.setCurrentUserStatusEvent(event) != 1) {
            // 상태 변경만 커밋되는 일을 막기 위해 이벤트 등록 실패도 현재 트랜잭션을 롤백한다
            throw new IllegalStateException("회원 상태 변경 Outbox 이벤트 등록에 실패했습니다.");
        }

    }

    /**
     * 정지 등록값과 최고 관리자 전용 무기한 권한을 검증한다
     *
     * @author SeungHyeon.Kang
     * @param suspension 정지 등록값
     * @param admin 처리 관리자
     */
    private void validateSuspension(CurrentUserSuspensionVO suspension, AdminSessionVO admin) {
        if (StringUtil.isEmpty(suspension)
                || StringUtil.hasEmpty(suspension.getSpndType(), suspension.getSpndRson())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        suspension.setSpndType(toUpperCase(trimToNull(suspension.getSpndType())));
        suspension.setSpndRson(toUpperCase(trimToNull(suspension.getSpndRson())));
        if (StringUtil.isEmpty(codeMapper.getCodeName(Constant.SPND_TYPE, suspension.getSpndType()))
                || StringUtil.isEmpty(codeMapper.getCodeName(Constant.SPND_RSON, suspension.getSpndRson()))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        if (Constant.SPND_TYPE_PERIOD.equals(suspension.getSpndType())) {
            if (StringUtil.isEmpty(suspension.getEndxDate())
                    || !suspension.getEndxDate().isAfter(LocalDateTime.now())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
            }
        } else if (Constant.SPND_TYPE_INDEFINITE.equals(suspension.getSpndType())) {
            if (!Constant.AUTH_CODE_SUPER.equals(admin.getAuthCode())) {
                throw new BusinessException(HttpStatus.FORBIDDEN
                                          , ResultEnum.USER_SUSPENSION_INDEFINITE_FORBIDDEN);
            }
            suspension.setEndxDate(null);
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        validateContentLength(trimToNull(suspension.getSpndCntn()));
    }

    /**
     * 관리자 내부 메모의 UTF-8 저장 바이트를 검증한다
     *
     * @author SeungHyeon.Kang
     * @param content 검증할 내부 메모
     */
    private void validateContentLength(String content) {
        if (!StringUtil.isEmpty(content)
                && content.getBytes(StandardCharsets.UTF_8).length > SUSPENSION_CONTENT_MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 현재 사용자의 프로필 또는 배경 이미지 참조와 파일 메타정보를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 처리할 회원번호
     * @param admin 처리 관리자
     * @param profileImage 프로필 이미지 여부
     * @return 변경된 현재 사용자 상세정보
     */
    private CurrentUserVO delUserImage(Long userNumb, AdminSessionVO admin, boolean profileImage) {
        // 프로필 정보 조치 권한과 현재 회원 존재 여부를 확인한다
        checkLogin(admin);
        // 다른 회원의 파일을 수정하지 않도록 양수 회원번호만 허용한다
        validateUserNumb(userNumb);
        CurrentUserFileVO userFile;
        int updateCount;

        // 프로필 조치는 현재 프로필 파일 참조를 잠금 조회한다
        if (profileImage) {
            // 동시 변경 전 현재 프로필 이미지 파일 메타정보를 조회한다
            userFile = currentUserMapper.getUserProfFileForUpdate(userNumb);
        }

        // 배경 이미지 요청은 프로필 파일과 분리된 현재 참조를 잠금 조회한다
        else {
            // 동시 변경 전 현재 배경 이미지 파일 메타정보를 조회한다
            userFile = currentUserMapper.getUserBgimFileForUpdate(userNumb);
        }

        // 현재 이미지가 없으면 다른 파일을 삭제하지 않고 이미 조치된 상태를 알린다
        if (StringUtil.isEmpty(userFile)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CURRENT_USER_NOT_FOUND);
        }

        // 프로필 이미지 요청은 잠금 조회한 파일번호와 현재 참조가 같은 경우에만 제거한다
        if (profileImage) {
            updateCount = currentUserMapper.delUserProfileImage(userNumb, userFile.getFileNumb());
        }

        // 배경 이미지 요청도 잠금 조회한 파일번호가 유지될 때만 참조를 제거한다
        else {
            updateCount = currentUserMapper.delUserBackgroundImage(userNumb, userFile.getFileNumb());
        }

        // 동시 변경으로 파일 참조가 달라졌으면 최신 이미지를 잘못 삭제하지 않는다
        if (updateCount != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.CURRENT_USER_CONFLICT);
        }

        // 다른 프로필 또는 배경에서 참조하지 않는 파일만 메타정보와 물리 파일을 정리한다
        if (currentUserMapper.delUserFileIfUnref(userFile.getFileNumb()) == 1) {
            // 커밋된 파일 메타정보 삭제에 맞춰 물리 파일 정리를 예약한다
            setFileCleanupOnCommit(userFile);
        }

        // 프로필 사진 초기화로 해결된 해당 사용자의 프로필 사진 신고만 함께 종결한다
        if (profileImage) {
            // 배경사진과 무관한 프로필 사진 신고를 관리자 수동 조치 결과로 종결한다
            currentUserMapper.uptUserComplaints(Constant.CMPL_TARGET_PROFILE_IMAGE, userNumb
                                                , PROFILE_COMPLAINT_PROCESS, admin.getAdmnNumb());
        }

        // 이미지 참조가 제거된 최신 현재 사용자 상세를 반환한다
        return getCurrentUserDtl(userNumb, admin);
    }

    /**
     * 이미지 참조와 메타정보 삭제가 커밋된 뒤 물리 파일을 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param userFile 삭제할 파일 메타정보
     */
    private void setFileCleanupOnCommit(CurrentUserFileVO userFile) {
        // 공개 접근 경로가 허용된 내부 저장소 객체인지 검증한다
        String objectKey = getStoredObjectKey(userFile);
        // 외부 URL과 비정상 경로는 파일 메타정보만 제거하고 저장소 접근을 차단한다
        if (StringUtil.isEmpty(objectKey)) {
            // 삭제할 수 없는 저장소 경로의 정리 예약을 종료한다
            return;
        }
        // 트랜잭션 밖의 독립 호출에서는 완료된 DB 상태에 맞춰 즉시 정리한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            // 독립 호출에서 DB 상태와 물리 저장소를 즉시 일치시킨다
            delPhysicalFile(userFile, objectKey);
            // 즉시 물리 파일 정리를 마친다
            return;
        }

        // DB 롤백 시 기존 파일을 유지하도록 트랜잭션 종료 상태 확인 작업을 등록한다
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * DB 커밋이 완료된 경우에만 현재 사용자 이미지 파일을 삭제한다
             *
             * @author SeungHyeon.Kang
             * @param status 트랜잭션 종료 상태
             */
            @Override
            public void afterCompletion(int status) {
                // 롤백된 사용자 참조가 기존 파일을 계속 가리킬 수 있으므로 커밋 외 상태는 유지한다
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    // 기존 물리 파일을 유지하고 정리 콜백을 종료한다
                    return;
                }

                // DB에서 참조와 메타정보가 제거된 저장소 객체를 삭제한다
                delPhysicalFile(userFile, objectKey);
            }
        });
    }

    /**
     * 검증된 저장소 객체 키에 해당하는 현재 사용자 이미지를 멱등 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param userFile 운영 로그에 남길 파일 메타정보
     * @param objectKey 검증된 저장소 객체 키
     */
    private void delPhysicalFile(CurrentUserFileVO userFile, String objectKey) {
        // 물리 파일 실패가 커밋된 데이터베이스 상태를 되돌리지 않도록 예외를 격리한다
        try {
            fileStorage.delFile(objectKey);
        }

        // 커밋 뒤 실패한 파일번호와 객체 키를 재정리 가능한 운영 로그로 남긴다
        catch (IOException exception) {
            log.error("Committed current user image cleanup failed. fileNumb={}, objectKey={}"
                    , userFile.getFileNumb(), objectKey, exception);
        }
    }

    /**
     * 내부 사용자 이미지 공개 경로를 검증된 저장소 객체 키로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param userFile 파일 메타정보
     * @return 삭제 가능한 객체 키, 외부 또는 비정상 경로이면 null
     */
    private String getStoredObjectKey(CurrentUserFileVO userFile) {
        // 파일명과 내부 공개 경로가 모두 확인되지 않으면 저장소 삭제를 허용하지 않는다
        if (StringUtil.isEmpty(userFile) || StringUtil.hasEmpty(userFile.getFilePath(), userFile.getStorName())
                || !userFile.getFilePath().startsWith(UPLOAD_ACCESS_PREFIX)) {
            // 외부 또는 불완전한 파일 메타정보에는 저장소 객체 키가 없음을 반환한다
            return null;
        }

        // 상위 디렉터리 이동 문자를 제거한 상대 저장 경로를 생성한다
        Path storedPath = Paths.get(userFile.getFilePath().substring(UPLOAD_ACCESS_PREFIX.length())).normalize();
        // 프로필과 배경의 날짜별 저장 규격에서 벗어난 경로는 파일 시스템 접근 전에 차단한다
        if (storedPath.isAbsolute() || storedPath.getNameCount() != 3
                || (!storedPath.startsWith(PROFILE_IMAGE_ROOT) && !storedPath.startsWith(BACKGROUND_IMAGE_ROOT))
                || !userFile.getStorName().equals(storedPath.getFileName().toString())) {
            // 검증되지 않은 공개 경로에는 삭제 가능한 객체 키가 없음을 반환한다
            return null;
        }
        // 운영체제 경로 구분자를 저장소 공통 구분자로 변환한 안전한 객체 키를 반환한다
        return storedPath.toString().replace('\\', '/');
    }

    /**
     * 현재 사용자 목록 검색 조건을 공백 제거와 허용값 검증 후 반환한다
     *
     * @author SeungHyeon.Kang
     * @param search 원본 검색 조건
     * @return 검증된 사용자 검색 조건
     */
    private CurrentUserSearchVO normalizeSearch(CurrentUserSearchVO search) {
        // 검색 조건이 없으면 첫 페이지 전체 조회가 가능한 기본 객체를 생성한다
        CurrentUserSearchVO normalizedSearch;
        // 검색 조건이 전달되지 않은 경우에만 기본 검색 객체를 사용한다
        if (StringUtil.isEmpty(search)) {
            normalizedSearch = new CurrentUserSearchVO();
        } else {
            normalizedSearch = search;
        }
        // 검색어 앞뒤 공백을 제거해 회원번호와 닉네임 검색 결과를 일관되게 만든다
        normalizedSearch.setKeyword(trimToNull(normalizedSearch.getKeyword()));
        // 회원 상태 코드의 불필요한 공백을 제거한다
        normalizedSearch.setUserStat(toUpperCase(trimToNull(normalizedSearch.getUserStat())));
        // 로그인 제공자 코드의 불필요한 공백을 제거하고 대문자로 통일한다
        normalizedSearch.setUserProv(toUpperCase(trimToNull(normalizedSearch.getUserProv())));
        // 온보딩 여부의 불필요한 공백을 제거하고 대문자로 통일한다
        normalizedSearch.setOnbdYsno(toUpperCase(trimToNull(normalizedSearch.getOnbdYsno())));

        // 과도한 검색어가 반복 전달되면 목록 SQL 비용이 커지므로 허용 길이를 제한한다
        if (!StringUtil.isEmpty(normalizedSearch.getKeyword())
                && normalizedSearch.getKeyword().length() > KEYWORD_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 회원 상태는 현재 공통코드에 등록된 값만 검색 조건으로 사용한다
        if (!StringUtil.isEmpty(normalizedSearch.getUserStat())
                && StringUtil.isEmpty(codeMapper.getCodeName(Constant.USER_STAT, normalizedSearch.getUserStat()))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 로그인 제공자는 관리자 공통코드에 등록된 활성 값만 검색 조건으로 허용한다
        if (!StringUtil.isEmpty(normalizedSearch.getUserProv())
                && StringUtil.isEmpty(codeMapper.getCodeName(Constant.USER_PROV, normalizedSearch.getUserProv()))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 온보딩 여부는 공통 여부 코드인 Y와 N만 검색할 수 있다
        if (!StringUtil.isEmpty(normalizedSearch.getOnbdYsno())
                && !Constant.YES.equals(normalizedSearch.getOnbdYsno())
                && !Constant.NO.equals(normalizedSearch.getOnbdYsno())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 가입 시작일이 종료일보다 늦으면 의도와 다른 목록이 조회되므로 요청을 거절한다
        if (!StringUtil.isEmpty(normalizedSearch.getJoinDateFrom())
                && !StringUtil.isEmpty(normalizedSearch.getJoinDateTo())
                && normalizedSearch.getJoinDateFrom().isAfter(normalizedSearch.getJoinDateTo())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 검증과 정규화가 끝난 검색 조건을 반환한다
        return normalizedSearch;
    }

    /**
     * 현재 사용자 원본이 존재하는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 회원번호
     */
    private void checkCurrentUser(Long userNumb) {
        // 현재 사용자 원본을 특정할 수 있도록 회원번호를 검증한다
        validateUserNumb(userNumb);

        // 영구 삭제되어 회원 원본이 없으면 현재 사용자 이력 화면에서 조회하지 않는다
        if (currentUserMapper.getCurrentUserCount(userNumb) < 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CURRENT_USER_NOT_FOUND);
        }
    }

    /**
     * 회원번호의 유효 범위를 확인한다
     *
     * @author SeungHyeon.Kang
     * @param userNumb 확인할 회원번호
     */
    private void validateUserNumb(Long userNumb) {
        // 양수가 아닌 회원번호로 다른 사용자 데이터가 조회되지 않도록 요청을 거절한다
        if (StringUtil.isEmpty(userNumb) || userNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 현재 사용자 조회 요청의 관리자 로그인 상태를 확인한다
     *
     * @author SeungHyeon.Kang
     * @param admin 로그인한 관리자
     */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체가 없으면 회원 개인정보 조회를 허용하지 않는다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }

    /**
     * 검색 문자열의 앞뒤 공백을 제거하고 빈 문자열을 Null로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param value 정리할 검색 문자열
     * @return 공백이 제거된 검색값 또는 Null
     */
    private String trimToNull(String value) {
        // 검색 문자열이 없으면 MyBatis 동적 조건에서 제외하도록 Null을 반환한다
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        // 앞뒤 공백을 제거한 검색 문자열을 준비한다
        String trimmedValue = value.trim();
        // 공백만 입력한 경우 검색 조건에서 제외하고 실제 값은 그대로 반환한다
        if (trimmedValue.isEmpty()) {
            return null;
        }

        // 실제 문자가 포함된 검색값을 반환한다
        return trimmedValue;
    }

    /**
     * 검색 코드를 대문자로 정규화한다
     *
     * @author SeungHyeon.Kang
     * @param value 정규화할 검색 코드
     * @return 대문자로 변환된 코드 또는 Null
     */
    private String toUpperCase(String value) {
        // 검색 코드가 없으면 동적 검색 조건에서도 제외되도록 Null을 유지한다
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        // 공통코드와 DB 저장 형식에 맞춘 대문자 검색 코드를 반환한다
        return value.toUpperCase(Locale.ROOT);
    }
}
