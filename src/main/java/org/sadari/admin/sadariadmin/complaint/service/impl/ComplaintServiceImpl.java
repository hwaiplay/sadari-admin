package org.sadari.admin.sadariadmin.complaint.service.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import org.sadari.admin.sadariadmin.complaint.mapper.ComplaintMapper;
import org.sadari.admin.sadariadmin.complaint.service.ComplaintService;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintDetailVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintTargetFileVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * fileName       : ComplaintServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 관리자 신고 조회와 상태 처리 및 사용자 신고 대상 이용정지를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang    최초 생성
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    // 사용자 업로드 이미지 공개 경로 접두사
    private static final String UPLOAD_ACCESS_PREFIX = "/uploads/";

    // 프로필 이미지 저장 디렉터리
    private static final Path PROFILE_IMAGE_ROOT = Paths.get("profile");

    // 배경 이미지 저장 디렉터리
    private static final Path BACKGROUND_IMAGE_ROOT = Paths.get("background");

    // 신고자 회원번호 또는 닉네임 검색어 최대 문자 수
    private static final int REPORTER_KEYWORD_MAX_LENGTH = 100;

    // 관리자 신고 처리 내용 최대 저장 바이트
    private static final int PROCESS_CONTENT_MAX_BYTES = 1000;

    // 신고 조회와 처리 Mapper
    private final ComplaintMapper complaintMapper;

    // 신고 검색 코드 검증 Mapper
    private final CodeMapper codeMapper;

    // 사용자 신고 대상 이용정지 서비스
    private final CurrentUserService currentUserService;

    // 피신고자 이미지 물리 파일 저장소
    private final FileStorage fileStorage;

    /**
     * 신고 관리 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param complaintMapper 신고 조회와 처리 Mapper
     * @param codeMapper 신고 검색 코드 검증 Mapper
     * @param currentUserService 사용자 신고 대상 이용정지 서비스
     * @param fileStorage 피신고자 이미지 물리 파일 저장소
     */
    public ComplaintServiceImpl(ComplaintMapper complaintMapper, CodeMapper codeMapper
                               , CurrentUserService currentUserService, FileStorage fileStorage) {

        this.complaintMapper = complaintMapper;
        this.codeMapper = codeMapper;
        this.currentUserService = currentUserService;
        this.fileStorage = fileStorage;
    }

    /**
     * 관리자 검색 조건에 맞는 신고 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 신고 검색 조건
     * @param admin 로그인 관리자
     * @return 검색된 신고 페이지
     */
    @Override
    public PageData<ComplaintVO> getComplaintList(ComplaintSearchVO search, AdminSessionVO admin) {
        // 신고 개인정보는 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 검색 문자열과 공통코드를 안전한 목록 조회 조건으로 정규화한다
        ComplaintSearchVO normalizedSearch = normalizeSearch(search);
        // 요청 페이지에 해당하는 신고 조회 범위를 계산한다
        PageRequest pageRequest = new PageRequest(normalizedSearch.getPage());
        // 신고 목록과 건수 SQL이 같은 시작 행을 사용하도록 설정한다
        normalizedSearch.setStartRow(pageRequest.getStartRow());
        // 신고 목록과 건수 SQL이 같은 마지막 행을 사용하도록 설정한다
        normalizedSearch.setEndRow(pageRequest.getEndRow());
        // 동일한 검색 조건의 신고 목록과 전체 건수로 페이지 응답을 생성한다
        return PageData.of(complaintMapper.getComplaintList(normalizedSearch)
                         , complaintMapper.getComplaintListCount(normalizedSearch), pageRequest);
    }

    /**
     * 신고번호로 신고와 동일 대상 신고 및 피신고자를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 신고 상세
     */
    @Override
    public ComplaintDetailVO getComplaintDtl(Long cmplNumb, AdminSessionVO admin) {
        // 신고 개인정보는 로그인한 관리자만 조회할 수 있도록 인증 상태를 확인한다
        checkLogin(admin);
        // 양수 신고번호만 상세 조회에 사용하도록 검증한다
        validateComplaintNumb(cmplNumb);
        // 신고와 동일 대상 신고 및 사용자 신고 대상을 묶은 상세를 생성한다
        return createComplaintDetail(cmplNumb, admin);
    }

    /**
     * 신고의 검토 시작 또는 최종 처리 상태를 저장한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param update 변경할 신고 처리 정보
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @Transactional
    @Override
    public ComplaintDetailVO uptComplaint(Long cmplNumb, ComplaintUpdateVO update, AdminSessionVO admin) {
        // 신고 처리 권한 확인의 기준이 되는 관리자 로그인 상태를 확인한다
        checkLogin(admin);
        // 다른 신고가 수정되지 않도록 양수 신고번호를 검증한다
        validateComplaintNumb(cmplNumb);
        // 처리 상태와 화면 조회 버전이 모두 전달되었는지 확인한다
        if (StringUtil.isEmpty(update) || StringUtil.hasEmpty(update.getCmplStat(), update.getUpdtDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 같은 신고를 여러 관리자가 동시에 처리하지 못하도록 최신 행을 잠근다
        ComplaintVO currentComplaint = complaintMapper.getComplaintForUpdate(cmplNumb);
        // 존재하지 않는 신고는 처리 상태를 만들지 않고 조회 결과 없음으로 응답한다
        if (StringUtil.isEmpty(currentComplaint)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_NOT_FOUND);
        }

        // 화면 조회 뒤 다른 관리자가 변경한 신고는 오래된 요청으로 덮어쓰지 않는다
        if (!currentComplaint.getUpdtDate().equals(update.getUpdtDate())) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.COMPLAINT_CONFLICT);
        }

        // 요청 상태를 공통코드 저장 형식과 맞는 대문자로 정규화한다
        update.setCmplStat(update.getCmplStat().trim().toUpperCase(Locale.ROOT));
        // 현재 상태와 요청 상태가 허용된 순서로 이어지는지 검증한다
        validateStatusTransition(currentComplaint, update, admin);
        // 최종 처리 메모는 저장 전에 공백을 제거하고 검토 시작에는 남기지 않는다
        setProcessContent(update);
        // 담당자와 상태 및 최종 처리일을 같은 신고 행에 저장한다
        if (complaintMapper.uptComplaint(cmplNumb, update, admin.getAdmnNumb()) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.COMPLAINT_CONFLICT);
        }

        // 저장된 처리 결과와 갱신된 수정일시를 포함한 상세를 반환한다
        return createComplaintDetail(cmplNumb, admin);
    }

    /**
     * 피신고자의 프로필 이미지를 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @Transactional
    @Override
    public ComplaintDetailVO delTargetProfImage(Long cmplNumb, AdminSessionVO admin) {
        // 프로필 이미지 참조와 저장 파일을 정리한 최신 신고 상세를 반환한다
        return delTargetUserImage(cmplNumb, admin, true);
    }

    /**
     * 피신고자의 배경 이미지를 기본 이미지 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @Transactional
    @Override
    public ComplaintDetailVO delTargetBgimImage(Long cmplNumb, AdminSessionVO admin) {
        // 배경 이미지 참조와 저장 파일을 정리한 최신 신고 상세를 반환한다
        return delTargetUserImage(cmplNumb, admin, false);
    }

    /**
     * 피신고자의 자기소개를 NULL 처리한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @Transactional
    @Override
    public ComplaintDetailVO delTargetIntroduction(Long cmplNumb, AdminSessionVO admin) {
        // 현재 신고 행과 접수 시 저장된 피신고자 연결을 잠금 검증한다
        ComplaintVO complaint = getComplaintForModeration(cmplNumb, admin);
        // 이미 소개가 없거나 피신고자가 달라진 요청은 삭제 성공으로 오인하지 않는다
        if (complaintMapper.delTargetUserIntroduction(complaint.getTagtUser()) != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_TARGET_NOT_FOUND);
        }
        // 자기소개가 제거된 현재 피신고자 정보를 포함한 신고 상세를 반환한다
        return createComplaintDetail(cmplNumb, admin);
    }

    /**
     * 신고 유형에 맞는 독후감, 댓글 또는 모임 소개를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 변경된 신고 상세
     */
    @Transactional
    @Override
    public ComplaintDetailVO delTargetContent(Long cmplNumb, AdminSessionVO admin) {
        // 현재 신고 행과 접수 시 저장된 피신고자 연결을 잠금 검증한다
        ComplaintVO complaint = getComplaintForModeration(cmplNumb, admin);
        int deleteCount;

        // 서버가 저장한 대상 유형에 따라 원본 보존 또는 삭제 정책을 구분한다
        switch (complaint.getTagtType()) {
            // 독후감은 연결 데이터를 외래키 순서에 맞춰 제거한 뒤 원본을 완전 삭제한다
            case Constant.CMPL_TARGET_BOOK_REPORT:
                // 독후감 댓글과 답글의 좋아요를 댓글 원본보다 먼저 삭제한다
                complaintMapper.delTargetReportReplyLikes(complaint.getTagtNumb(), complaint.getTagtUser());
                // 부모 댓글의 참조 무결성을 위해 대댓글을 먼저 삭제한다
                complaintMapper.delTagtReportChildReply(complaint.getTagtNumb(), complaint.getTagtUser());
                // 대댓글 제거 뒤 독후감의 나머지 댓글을 삭제한다
                complaintMapper.delTargetReportReplies(complaint.getTagtNumb(), complaint.getTagtUser());
                // 독후감 원본을 참조하는 좋아요를 삭제한다
                complaintMapper.delTargetReportLikes(complaint.getTagtNumb(), complaint.getTagtUser());
                // 종속 데이터가 제거된 독후감 원본을 완전 삭제한다
                deleteCount = complaintMapper.delTargetReport(complaint.getTagtNumb(), complaint.getTagtUser());
                break;
            // 댓글과 답글은 계층 구조를 유지하도록 원본 행의 삭제 여부만 변경한다
            case Constant.CMPL_TARGET_REPLY:
                // 신고 대상 작성자와 일치하는 현재 댓글을 삭제 상태로 변경한다
                deleteCount = complaintMapper.delTargetReply(complaint.getTagtNumb(), complaint.getTagtUser());
                break;
            // 모임은 운영 정보와 회원 관계를 유지하면서 소개 내용만 제거한다
            case Constant.CMPL_TARGET_CLUB:
                // 신고 대상 모임의 현재 소개 내용만 NULL 처리한다
                deleteCount = complaintMapper.delTargetClubIntroduction(complaint.getTagtNumb());
                break;
            // 사용자 신고와 향후 미지원 유형은 콘텐츠 번호를 잘못 변경하지 않도록 거절한다
            default:
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMPLAINT_TARGET_ACTION_INVALID);
        }

        // 이미 조치됐거나 신고 대상과 일치하지 않는 원본은 중복 삭제로 처리하지 않는다
        if (deleteCount != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_TARGET_NOT_FOUND);
        }
        // 원본 존재 여부가 갱신된 신고 상세를 반환한다
        return createComplaintDetail(cmplNumb, admin);
    }

    /**
     * 사용자 신고 대상의 관리자 이용정지 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param pageNumber 페이지 번호
     * @param admin 로그인 관리자
     * @return 이용정지 이력 페이지
     */
    @Transactional
    @Override
    public PageData<CurrentUserSuspensionVO> getTargetUserSuspList(Long cmplNumb, int pageNumber
                                                                      , AdminSessionVO admin) {
        // 신고 대상 회원번호를 서버에서 확정한 뒤 기존 이용정지 이력을 조회한다
        Long userNumb = getTargetUserNumb(cmplNumb, admin);
        // 기간 만료 처리와 코드명을 포함한 기존 이용정지 페이지를 반환한다
        return currentUserService.getSuspensionHistoryList(userNumb, pageNumber, admin);
    }

    /**
     * 사용자 신고 대상에게 기간 또는 무기한 이용정지를 적용한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param suspension 정지 등록값
     * @param admin 처리 관리자
     * @return 등록된 이용정지 이력
     */
    @Transactional
    @Override
    public CurrentUserSuspensionVO setTargetUserSuspension(Long cmplNumb, CurrentUserSuspensionVO suspension
                                                          , AdminSessionVO admin) {
        // 요청에서 회원번호를 받지 않고 신고 대상 회원번호를 서버에서 확정한다
        Long userNumb = getTargetUserNumb(cmplNumb, admin);
        // 현재 사용자 관리와 동일한 검증 및 Outbox 처리로 이용정지를 적용한다
        return currentUserService.setUserSuspension(userNumb, suspension, admin);
    }

    /**
     * 사용자 신고 대상의 적용 중인 이용정지를 관리자 해제한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param spndNumb 정지 이력 번호
     * @param request 해제 메모
     * @param admin 처리 관리자
     */
    @Transactional
    @Override
    public void uptTargetSuspReleased(Long cmplNumb, Long spndNumb
                                               , CurrentUserSuspensionVO request, AdminSessionVO admin) {
        // 요청에서 회원번호를 받지 않고 신고 대상 회원번호를 서버에서 확정한다
        Long userNumb = getTargetUserNumb(cmplNumb, admin);
        // 현재 사용자 관리와 동일한 상태 복구 및 Outbox 처리로 이용정지를 해제한다
        currentUserService.uptUserSuspensionReleased(userNumb, spndNumb, request, admin);
    }

    /**
     * 신고와 동일 대상 신고 및 피신고자를 묶어 상세 응답을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 신고 상세 응답
     */
    private ComplaintDetailVO createComplaintDetail(Long cmplNumb, AdminSessionVO admin) {
        // 신고번호에 해당하는 최신 신고 상세를 조회한다
        ComplaintVO complaint = complaintMapper.getComplaintDtl(cmplNumb);
        // 존재하지 않는 신고는 빈 상세 화면으로 노출하지 않는다
        if (StringUtil.isEmpty(complaint)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_NOT_FOUND);
        }

        // 신고와 판단 참고 데이터를 담을 상세 응답 객체를 생성한다
        ComplaintDetailVO detail = new ComplaintDetailVO();
        // 신고 접수와 현재 처리 상태를 상세 응답에 설정한다
        detail.setComplaint(complaint);
        // 동일 대상의 최근 다른 신고를 상세 응답에 설정한다
        detail.setRelatedComplaints(complaintMapper.getRelatedComplaintList(complaint.getTagtType()
                                                                           , complaint.getTagtNumb(), cmplNumb));
        // 동일 대상의 전체 다른 신고 건수를 상세 응답에 설정한다
        detail.setRelatedComplaintCount(complaintMapper.getRelatedComplaintCnt(complaint.getTagtType()
                                                                                    , complaint.getTagtNumb()
                                                                                    , cmplNumb));
        // 신고 대상 소유 사용자 연결이 남아 있으면 현재 피신고자 정보를 조회한다
        if (!StringUtil.isEmpty(complaint.getTagtUser())) {
            // 영구 삭제되지 않은 피신고자의 현재 회원 정보를 조회한다
            setTargetUser(detail, complaint.getTagtUser(), admin);
            // 신고 유형에 맞는 원본이 남아 있을 때만 관리자 삭제 조치를 허용한다
            detail.setTargetContentExists(complaintMapper.getTargetContentCount(
                    complaint.getTagtType(), complaint.getTagtNumb(), complaint.getTagtUser()) == 1);
        }

        // 신고 처리 화면에 필요한 모든 판단 정보를 반환한다
        return detail;
    }

    /**
     * 피신고자가 현재 존재하면 신고 상세에 회원 정보를 설정한다
     *
     * @author SeungHyeon.Kang
     * @param detail 신고 상세 응답
     * @param userNumb 피신고자 회원번호
     * @param admin 로그인 관리자
     */
    private void setTargetUser(ComplaintDetailVO detail, Long userNumb, AdminSessionVO admin) {
        // 영구 삭제된 신고 대상은 신고 기록만 표시하도록 조회 결과 없음만 분리한다
        try {
            // 기존 현재 사용자 상세 조회를 재사용해 계정 상태와 이미지 정보를 일치시킨다
            CurrentUserVO targetUser = currentUserService.getCurrentUserDtl(userNumb, admin);
            // 조회된 현재 회원 정보를 신고 상세에 설정한다
            detail.setTargetUser(targetUser);
        }

        // 영구 삭제된 현재 사용자와 그 밖의 업무 오류를 구분한다
        catch (BusinessException exception) {
            // 현재 사용자 없음 외의 인증 및 업무 오류는 정상적인 삭제 대상 표시로 숨기지 않는다
            if (!ResultEnum.CURRENT_USER_NOT_FOUND.equals(exception.getResultEnum())) {
                throw exception;
            }

        }
    }

    /**
     * 신고번호에서 이용정지 대상 회원번호를 안전하게 결정한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 로그인 관리자
     * @return 사용자 신고 대상 회원번호
     */
    private Long getTargetUserNumb(Long cmplNumb, AdminSessionVO admin) {
        // 신고 대상 정보를 조회하기 전에 로그인 상태와 신고번호를 검증한다
        checkLogin(admin);
        // 다른 신고가 이용정지 대상으로 사용되지 않도록 신고번호를 검증한다
        validateComplaintNumb(cmplNumb);
        // 신고 대상 유형과 번호를 서버 데이터에서 조회한다
        ComplaintVO complaint = complaintMapper.getComplaintDtl(cmplNumb);
        // 존재하지 않는 신고에서는 이용정지 대상을 만들 수 없다
        if (StringUtil.isEmpty(complaint)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_NOT_FOUND);
        }

        // 모든 신고 유형에서 접수 시점에 확정한 피신고자 회원번호만 이용정지 대상으로 사용한다
        if (StringUtil.isEmpty(complaint.getTagtUser())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_TARGET_NOT_FOUND);
        }

        // 신고 대상 소유 회원번호를 기존 이용정지 서비스에 전달한다
        return complaint.getTagtUser();
    }

    /**
     * 피신고자의 프로필 또는 배경 이미지 참조와 파일 메타정보를 제거한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @param profileImage 프로필 이미지 여부
     * @return 변경된 신고 상세
     */
    private ComplaintDetailVO delTargetUserImage(Long cmplNumb, AdminSessionVO admin, boolean profileImage) {
        // 현재 신고 행과 접수 시 저장된 피신고자 연결을 잠금 검증한다
        ComplaintVO complaint = getComplaintForModeration(cmplNumb, admin);
        ComplaintTargetFileVO targetFile;
        int updateCount;

        // 프로필 조치는 현재 프로필 파일 참조를 잠금 조회한다
        if (profileImage) {
            // 동시 변경 전 현재 프로필 이미지 파일 메타정보를 조회한다
            targetFile = complaintMapper.getTagtProfFileForUpdate(complaint.getTagtUser());
        }

        // 배경 이미지 요청은 프로필 파일과 분리된 현재 참조를 잠금 조회한다
        else {
            // 동시 변경 전 현재 배경 이미지 파일 메타정보를 조회한다
            targetFile = complaintMapper.getTagtBgimFileForUpdate(complaint.getTagtUser());
        }

        // 현재 이미지가 없으면 다른 파일을 삭제하지 않고 이미 조치된 상태를 알린다
        if (StringUtil.isEmpty(targetFile)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_TARGET_NOT_FOUND);
        }

        // 프로필 이미지 요청은 잠금 조회한 파일번호와 현재 참조가 같은 경우에만 제거한다
        if (profileImage) {
            updateCount = complaintMapper.delTargetProfileImage(
                    complaint.getTagtUser(), targetFile.getFileNumb());
        }

        // 배경 이미지 요청도 잠금 조회한 파일번호가 유지될 때만 참조를 제거한다
        else {
            updateCount = complaintMapper.delTargetBackgroundImage(
                    complaint.getTagtUser(), targetFile.getFileNumb());
        }

        // 동시 변경으로 파일 참조가 달라졌으면 최신 이미지를 잘못 삭제하지 않는다
        if (updateCount != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.COMPLAINT_CONFLICT);
        }

        // 다른 프로필 또는 배경에서 참조하지 않는 파일만 메타정보와 물리 파일을 정리한다
        if (complaintMapper.delTagtFileIfUnref(targetFile.getFileNumb()) == 1) {
            setFileCleanupOnCommit(targetFile);
        }
        // 이미지 참조가 제거된 현재 피신고자 정보를 포함한 신고 상세를 반환한다
        return createComplaintDetail(cmplNumb, admin);
    }

    /**
     * 신고 조치에 사용할 신고 행을 잠그고 피신고자 연결을 검증한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 신고 번호
     * @param admin 처리 관리자
     * @return 잠긴 신고 정보
     */
    private ComplaintVO getComplaintForModeration(Long cmplNumb, AdminSessionVO admin) {
        // 신고 개인정보와 조치 권한의 전제인 관리자 로그인 상태를 확인한다
        checkLogin(admin);
        // 다른 신고 행이 변경되지 않도록 양수 신고번호만 허용한다
        validateComplaintNumb(cmplNumb);
        // 같은 신고의 동시 조치가 교차하지 않도록 신고 행을 잠근다
        ComplaintVO complaint = complaintMapper.getComplaintForUpdate(cmplNumb);
        // 존재하지 않는 신고에서는 피신고자나 콘텐츠 조치를 만들지 않는다
        if (StringUtil.isEmpty(complaint)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_NOT_FOUND);
        }
        // 물리 삭제된 피신고자는 현재 정보와 콘텐츠 소유자를 안전하게 확정할 수 없다
        if (StringUtil.isEmpty(complaint.getTagtUser())) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMPLAINT_TARGET_NOT_FOUND);
        }
        // 서버가 잠금 조회한 신고만 관리자 조치 조건으로 반환한다
        return complaint;
    }

    /**
     * 이미지 참조와 메타정보 삭제가 커밋된 뒤 물리 파일을 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param targetFile 삭제할 파일 메타정보
     */
    private void setFileCleanupOnCommit(ComplaintTargetFileVO targetFile) {
        // 공개 접근 경로가 허용된 내부 저장소 객체인지 검증한다
        String objectKey = getStoredObjectKey(targetFile);
        // 외부 URL과 비정상 경로는 파일 메타정보만 제거하고 저장소 접근을 차단한다
        if (StringUtil.isEmpty(objectKey)) {
            // 삭제할 수 없는 저장소 경로의 정리 예약을 종료한다
            return;
        }
        // 트랜잭션 밖의 단위 테스트와 독립 호출에서는 완료된 DB 상태에 맞춰 즉시 정리한다
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            delPhysicalFile(targetFile, objectKey);
            // 즉시 물리 파일 정리를 마친다
            return;
        }
        // DB 롤백 시 기존 파일을 유지하도록 트랜잭션 종료 상태 확인 작업을 등록한다
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            /**
             * DB 커밋이 완료된 경우에만 피신고자 이미지 파일을 삭제한다
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
                delPhysicalFile(targetFile, objectKey);
            }
        });
    }

    /**
     * 검증된 저장소 객체 키에 해당하는 피신고자 이미지를 멱등 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param targetFile 운영 로그에 남길 파일 메타정보
     * @param objectKey 검증된 저장소 객체 키
     */
    private void delPhysicalFile(ComplaintTargetFileVO targetFile, String objectKey) {
        // 물리 파일 실패가 커밋된 데이터베이스 상태를 되돌리지 않도록 예외를 격리한다
        try {
            fileStorage.delFile(objectKey);
        }

        // 커밋 뒤 실패한 파일번호와 객체 키를 재정리 가능한 운영 로그로 남긴다
        catch (IOException exception) {
            log.error("Committed complaint target image cleanup failed. fileNumb={}, objectKey={}"
                    , targetFile.getFileNumb(), objectKey, exception);
        }
    }

    /**
     * 내부 사용자 이미지 공개 경로를 검증된 저장소 객체 키로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param targetFile 파일 메타정보
     * @return 삭제 가능한 객체 키, 외부 또는 비정상 경로이면 null
     */
    private String getStoredObjectKey(ComplaintTargetFileVO targetFile) {
        // 파일명과 내부 공개 경로가 모두 확인되지 않으면 저장소 삭제를 허용하지 않는다
        if (StringUtil.isEmpty(targetFile) || StringUtil.hasEmpty(targetFile.getFilePath(), targetFile.getStorName())
                || !targetFile.getFilePath().startsWith(UPLOAD_ACCESS_PREFIX)) {
            // 외부 또는 불완전한 파일 메타정보에는 저장소 객체 키가 없음을 반환한다
            return null;
        }

        // 상위 디렉터리 이동 문자를 제거한 상대 저장 경로를 생성한다
        Path storedPath = Paths.get(targetFile.getFilePath().substring(UPLOAD_ACCESS_PREFIX.length())).normalize();
        // 프로필과 배경의 날짜별 저장 규격에서 벗어난 경로는 파일 시스템 접근 전에 차단한다
        if (storedPath.isAbsolute() || storedPath.getNameCount() != 3
                || (!storedPath.startsWith(PROFILE_IMAGE_ROOT) && !storedPath.startsWith(BACKGROUND_IMAGE_ROOT))
                || !targetFile.getStorName().equals(storedPath.getFileName().toString())) {
            // 검증되지 않은 공개 경로에는 삭제 가능한 객체 키가 없음을 반환한다
            return null;
        }
        // 운영체제 경로 구분자를 저장소 공통 구분자로 변환한 안전한 객체 키를 반환한다
        return storedPath.toString().replace('\\', '/');
    }

    /**
     * 신고 목록 검색 조건을 정규화하고 공통코드와 날짜 범위를 검증한다
     *
     * @author SeungHyeon.Kang
     * @param search 신고 검색 조건
     * @return 검증된 신고 검색 조건
     */
    private ComplaintSearchVO normalizeSearch(ComplaintSearchVO search) {
        // 비어 있는 검색 객체를 안전한 기본 조건으로 대체한다
        ComplaintSearchVO normalizedSearch = StringUtil.isEmpty(search) ? new ComplaintSearchVO() : search;
        // 신고자 검색어의 앞뒤 공백을 제거한다
        normalizedSearch.setReporterKeyword(trimToNull(normalizedSearch.getReporterKeyword()));
        // 상태 세부코드를 공통코드 저장 형식과 같은 대문자로 정규화한다
        normalizedSearch.setCmplStat(toUpperCase(trimToNull(normalizedSearch.getCmplStat())));
        // 대상 유형 세부코드를 공통코드 저장 형식과 같은 대문자로 정규화한다
        normalizedSearch.setTagtType(toUpperCase(trimToNull(normalizedSearch.getTagtType())));
        // 신고 사유 세부코드를 공통코드 저장 형식과 같은 대문자로 정규화한다
        normalizedSearch.setCmplRson(toUpperCase(trimToNull(normalizedSearch.getCmplRson())));

        // 과도한 신고자 검색어로 목록 SQL 비용이 커지지 않도록 길이를 제한한다
        if (!StringUtil.isEmpty(normalizedSearch.getReporterKeyword())
                && normalizedSearch.getReporterKeyword().length() > REPORTER_KEYWORD_MAX_LENGTH) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 양수가 아닌 신고번호와 대상번호는 목록 식별 조건으로 허용하지 않는다
        if ((!StringUtil.isEmpty(normalizedSearch.getCmplNumb()) && normalizedSearch.getCmplNumb() < 1)
                || (!StringUtil.isEmpty(normalizedSearch.getTagtNumb()) && normalizedSearch.getTagtNumb() < 1)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 각 검색 세부코드가 해당 신고 공통코드 그룹의 활성 값인지 확인한다
        validateSearchCode(Constant.CMPL_STAT, normalizedSearch.getCmplStat());
        // 신고 대상 유형 검색값을 활성 공통코드로 제한한다
        validateSearchCode(Constant.CMPL_TAGT, normalizedSearch.getTagtType());
        // 신고 사유 검색값을 활성 공통코드로 제한한다
        validateSearchCode(Constant.CMPL_RSON, normalizedSearch.getCmplRson());

        // 접수일 시작일이 종료일보다 늦으면 의도와 다른 목록이 조회되므로 거절한다
        if (!StringUtil.isEmpty(normalizedSearch.getRegiDateFrom())
                && !StringUtil.isEmpty(normalizedSearch.getRegiDateTo())
                && normalizedSearch.getRegiDateFrom().isAfter(normalizedSearch.getRegiDateTo())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 목록과 건수 조회에 사용할 검증된 검색 조건을 반환한다
        return normalizedSearch;
    }

    /**
     * 신고 검색 코드가 해당 공통코드 그룹의 활성 세부코드인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param commCode 공통코드 그룹
     * @param comdCode 확인할 세부코드
     */
    private void validateSearchCode(String commCode, String comdCode) {
        // 검색 코드가 없으면 해당 동적 검색 조건을 사용하지 않는다
        if (StringUtil.isEmpty(comdCode)) {
            return;
        }

        // 공통코드에 없는 값으로 SQL 검색 범위가 오염되지 않도록 요청을 거절한다
        if (StringUtil.isEmpty(codeMapper.getCodeName(commCode, comdCode))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 현재 상태와 요청 상태가 신고 처리 순서를 지키는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param currentComplaint 현재 신고 상태와 담당자
     * @param update 변경할 신고 처리 정보
     * @param admin 처리 관리자
     */
    private void validateStatusTransition(ComplaintVO currentComplaint, ComplaintUpdateVO update
                                         , AdminSessionVO admin) {
        // 접수 상태에서는 담당자를 지정하는 검토 시작만 허용한다
        if (Constant.CMPL_STATUS_RECEIVED.equals(currentComplaint.getCmplStat())) {
            // 접수 신고를 최종 상태로 바로 건너뛰지 못하도록 검토 중 상태만 허용한다
            if (!Constant.CMPL_STATUS_REVIEWING.equals(update.getCmplStat())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMPLAINT_INVALID_TRANSITION);
            }

            return;
        }

        // 검토 중 상태에서는 담당자 또는 최고 관리자만 최종 처리할 수 있다
        if (Constant.CMPL_STATUS_REVIEWING.equals(currentComplaint.getCmplStat())) {
            // 다른 일반 관리자가 담당자의 검토 결과를 덮어쓰지 못하도록 권한을 확인한다
            if (!admin.getAdmnNumb().equals(currentComplaint.getProcAdmn())
                    && !Constant.AUTH_CODE_SUPER.equals(admin.getAuthCode())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, ResultEnum.COMPLAINT_ASSIGNEE_FORBIDDEN);
            }

            // 조치 완료와 반려 외의 상태로 되돌리거나 유지하는 요청을 허용하지 않는다
            if (!Constant.CMPL_STATUS_ACTIONED.equals(update.getCmplStat())
                    && !Constant.CMPL_STATUS_REJECTED.equals(update.getCmplStat())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMPLAINT_INVALID_TRANSITION);
            }

            return;
        }

        // 조치 완료와 반려 상태는 1차 운영 정책에서 다시 열지 않는다
        throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMPLAINT_INVALID_TRANSITION);
    }

    /**
     * 최종 처리 메모를 검증하고 검토 시작 요청에는 빈 값으로 설정한다
     *
     * @author SeungHyeon.Kang
     * @param update 변경할 신고 처리 정보
     */
    private void setProcessContent(ComplaintUpdateVO update) {
        // 검토 시작 상태에는 최종 판단 메모를 저장하지 않는다
        if (Constant.CMPL_STATUS_REVIEWING.equals(update.getCmplStat())) {
            // 최종 상태가 아니므로 처리 메모를 초기화한다
            update.setProcCntn(null);
            return;
        }

        // 최종 처리 상태에는 관리자 판단 근거가 남도록 공백을 제거한다
        String processContent = trimToNull(update.getProcCntn());
        // 조치 완료와 반려에는 빈 처리 메모를 허용하지 않는다
        if (StringUtil.isEmpty(processContent)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 데이터베이스 저장 범위를 넘는 다국어 처리 메모를 차단한다
        if (processContent.getBytes(StandardCharsets.UTF_8).length > PROCESS_CONTENT_MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }

        // 검증된 관리자 처리 메모를 수정 요청에 설정한다
        update.setProcCntn(processContent);
    }

    /**
     * 신고번호의 유효 범위를 확인한다
     *
     * @author SeungHyeon.Kang
     * @param cmplNumb 확인할 신고 번호
     */
    private void validateComplaintNumb(Long cmplNumb) {
        // 양수가 아닌 번호로 신고 상세나 이용정지 대상을 조회하지 못하도록 차단한다
        if (StringUtil.isEmpty(cmplNumb) || cmplNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 신고 관리 요청의 관리자 로그인 상태를 확인한다
     *
     * @author SeungHyeon.Kang
     * @param admin 로그인 관리자
     */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체가 없으면 신고 개인정보와 처리 기능을 허용하지 않는다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }

    /**
     * 검색 문자열의 앞뒤 공백을 제거하고 빈 문자열을 Null로 변환한다
     *
     * @author SeungHyeon.Kang
     * @param value 정리할 문자열
     * @return 공백이 제거된 문자열 또는 Null
     */
    private String trimToNull(String value) {
        // 문자열이 없으면 동적 검색과 선택 입력에서 제외하도록 Null을 반환한다
        if (StringUtil.isEmpty(value)) {
            return null;
        }

        // 앞뒤 공백을 제거한 실제 입력 문자열을 준비한다
        String trimmedValue = value.trim();
        // 공백만 입력한 문자열은 저장하거나 검색하지 않도록 Null을 반환한다
        if (trimmedValue.isEmpty()) {
            return null;
        }

        // 실제 문자가 포함된 정리된 문자열을 반환한다
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

        // 공통코드 저장 형식에 맞춘 대문자 검색 코드를 반환한다
        return value.toUpperCase(Locale.ROOT);
    }
}
