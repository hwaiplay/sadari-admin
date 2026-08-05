package org.sadari.admin.sadariadmin.complaint.service.impl;

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
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintUpdateVO;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintVO;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

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
public class ComplaintServiceImpl implements ComplaintService {

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

    /**
     * 신고 관리 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param complaintMapper 신고 조회와 처리 Mapper
     * @param codeMapper 신고 검색 코드 검증 Mapper
     * @param currentUserService 사용자 신고 대상 이용정지 서비스
     */
    public ComplaintServiceImpl(ComplaintMapper complaintMapper, CodeMapper codeMapper
                               , CurrentUserService currentUserService) {

        this.complaintMapper = complaintMapper;
        this.codeMapper = codeMapper;
        this.currentUserService = currentUserService;
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
     * 신고번호로 신고와 동일 대상 신고 및 사용자 신고 대상을 조회한다
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
    public PageData<CurrentUserSuspensionVO> getTargetUserSuspensionList(Long cmplNumb, int pageNumber
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
    public void uptTargetUserSuspensionReleased(Long cmplNumb, Long spndNumb
                                               , CurrentUserSuspensionVO request, AdminSessionVO admin) {
        // 요청에서 회원번호를 받지 않고 신고 대상 회원번호를 서버에서 확정한다
        Long userNumb = getTargetUserNumb(cmplNumb, admin);
        // 현재 사용자 관리와 동일한 상태 복구 및 Outbox 처리로 이용정지를 해제한다
        currentUserService.uptUserSuspensionReleased(userNumb, spndNumb, request, admin);
    }

    /**
     * 신고와 동일 대상 신고 및 사용자 신고 대상을 묶어 상세 응답을 생성한다
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
        detail.setRelatedComplaintCount(complaintMapper.getRelatedComplaintListCount(complaint.getTagtType()
                                                                                    , complaint.getTagtNumb()
                                                                                    , cmplNumb));
        // 사용자 신고일 때만 신고 대상 번호를 회원번호로 해석한다
        if (Constant.CMPL_TARGET_USER.equals(complaint.getTagtType())) {
            // 영구 삭제되지 않은 사용자 신고 대상의 현재 회원 정보를 조회한다
            setTargetUser(detail, complaint.getTagtNumb(), admin);
        }

        // 신고 처리 화면에 필요한 모든 판단 정보를 반환한다
        return detail;
    }

    /**
     * 사용자 신고 대상이 현재 존재하면 신고 상세에 회원 정보를 설정한다
     *
     * @author SeungHyeon.Kang
     * @param detail 신고 상세 응답
     * @param userNumb 신고 대상 회원번호
     * @param admin 로그인 관리자
     */
    private void setTargetUser(ComplaintDetailVO detail, Long userNumb, AdminSessionVO admin) {
        // 영구 삭제된 신고 대상은 신고 기록만 표시하도록 조회 결과 없음만 분리한다
        try {
            // 기존 현재 사용자 상세 조회를 재사용해 상태와 Outbox 반영 결과를 일치시킨다
            CurrentUserVO targetUser = currentUserService.getCurrentUserDtl(userNumb, admin);
            // 조회된 현재 회원 정보를 신고 상세에 설정한다
            detail.setTargetUser(targetUser);
        } catch (BusinessException exception) {
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

        // 사용자 신고가 아닌 콘텐츠 신고 번호를 회원번호로 오인하지 않도록 차단한다
        if (!Constant.CMPL_TARGET_USER.equals(complaint.getTagtType())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMPLAINT_TARGET_NOT_USER);
        }

        // 사용자 신고의 대상 번호를 기존 이용정지 서비스에 전달할 회원번호로 반환한다
        return complaint.getTagtNumb();
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
