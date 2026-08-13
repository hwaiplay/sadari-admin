package org.sadari.admin.sadariadmin.inquiry.service;

import java.nio.charset.StandardCharsets;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserSuspensionVO;
import org.sadari.admin.sadariadmin.inquiry.mapper.InquiryMapper;
import org.sadari.admin.sadariadmin.inquiry.vo.InquiryActionVO;
import org.sadari.admin.sadariadmin.inquiry.vo.InquirySearchVO;
import org.sadari.admin.sadariadmin.inquiry.vo.InquiryVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : InquiryService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 고객문의 담당 지정과 답변 및 연결 이용정지 해제를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Service
@Transactional(readOnly = true)
public class InquiryService {

    // 관리자 답변 최대 저장 바이트
    private static final int ANSWER_MAX_BYTES = 4000;
    // 고객문의 데이터 접근 객체
    private final InquiryMapper inquiryMapper;
    // 현재 사용자 이용정지 업무 서비스
    private final CurrentUserService currentUserService;

    /**
     * 관리자 고객문의 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param inquiryMapper 고객문의 데이터 접근 객체
     * @param currentUserService 현재 사용자 이용정지 업무 서비스
     */
    public InquiryService(InquiryMapper inquiryMapper, CurrentUserService currentUserService) {

        this.inquiryMapper = inquiryMapper;
        this.currentUserService = currentUserService;
    }

    /**
     * 관리자 검색 조건에 맞는 고객문의 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 고객문의 검색 조건
     * @param admin 로그인 관리자
     * @return 검색된 고객문의 페이지
     */
    public PageData<InquiryVO> getInquiryList(InquirySearchVO search, AdminSessionVO admin) {

        checkLogin(admin);
        InquirySearchVO normalized = StringUtil.isEmpty(search) ? new InquirySearchVO() : search;
        if (!StringUtil.isEmpty(normalized.getInqrCatg())) normalized.setInqrCatg(normalized.getInqrCatg().trim());
        if (!StringUtil.isEmpty(normalized.getInqrStat())) normalized.setInqrStat(normalized.getInqrStat().trim());
        if (!StringUtil.isEmpty(normalized.getUserKeyword())) normalized.setUserKeyword(normalized.getUserKeyword().trim());
        PageRequest pageRequest = new PageRequest(normalized.getPage());
        normalized.setStartRow(pageRequest.getStartRow());
        normalized.setEndRow(pageRequest.getEndRow());
        // 검색된 고객문의 목록과 전체 건수를 반환한다
        return PageData.of(inquiryMapper.getInquiryList(normalized), inquiryMapper.getInquiryListCount(normalized)
                , pageRequest);
    }

    /**
     * 고객문의와 관리자 답변을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param inqrNumb 고객문의 번호
     * @param admin 로그인 관리자
     * @return 고객문의 상세
     */
    public InquiryVO getInquiryDtl(Long inqrNumb, AdminSessionVO admin) {

        checkLogin(admin);
        // 고객문의 답변을 포함한 상세를 반환한다
        return createDetail(inqrNumb);
    }

    /**
     * 접수된 고객문의를 현재 관리자의 검토 중 상태로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param inqrNumb 고객문의 번호
     * @param action 화면 조회 수정일시
     * @param admin 처리 관리자
     * @return 검토 중으로 변경된 고객문의 상세
     */
    @Transactional
    public InquiryVO uptInquiryReviewing(Long inqrNumb, InquiryActionVO action, AdminSessionVO admin) {

        checkLogin(admin);
        if (StringUtil.isEmpty(action) || StringUtil.isEmpty(action.getUpdtDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        InquiryVO current = lockInquiry(inqrNumb);
        if (!"INQR_RECEIVED".equals(current.getInqrStat())
                || inquiryMapper.uptInquiryReviewing(inqrNumb, admin.getAdmnNumb(), action.getUpdtDate()) != 1) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.INQUIRY_CONFLICT);
        }
        // 담당자와 검토 중 상태가 반영된 상세를 반환한다
        return createDetail(inqrNumb);
    }

    /**
     * 담당 고객문의에 관리자 답변을 등록하고 답변 완료로 변경한다
     *
     * @author SeungHyeon.Kang
     * @param inqrNumb 고객문의 번호
     * @param action 관리자 답변과 화면 조회 수정일시
     * @param admin 처리 관리자
     * @return 답변이 등록된 고객문의 상세
     */
    @Transactional
    public InquiryVO setInquiryAnswer(Long inqrNumb, InquiryActionVO action, AdminSessionVO admin) {

        checkLogin(admin);
        if (StringUtil.isEmpty(action) || StringUtil.hasEmpty(action.getAnswCntn(), action.getUpdtDate())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        String answer = action.getAnswCntn().trim();
        if (answer.isEmpty() || answer.getBytes(StandardCharsets.UTF_8).length > ANSWER_MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        InquiryVO current = lockInquiry(inqrNumb);
        if (!current.getUpdtDate().equals(action.getUpdtDate())
                || !"INQR_REVIEWING".equals(current.getInqrStat())
                || (!admin.getAdmnNumb().equals(current.getAsgnAdmn())
                && !"SUPER".equals(admin.getAuthCode()))) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.INQUIRY_CONFLICT);
        }
        inquiryMapper.setInquiryAnswer(inqrNumb, answer, admin.getAdmnNumb());
        inquiryMapper.uptInquiryAnswered(inqrNumb, admin.getAdmnNumb());
        // 저장된 관리자 답변을 포함한 상세를 반환한다
        return createDetail(inqrNumb);
    }

    /**
     * 고객문의에 연결된 현재 사용자의 이용정지를 해제한다
     *
     * @author SeungHyeon.Kang
     * @param inqrNumb 고객문의 번호
     * @param action 관리자 해제 메모
     * @param admin 처리 관리자
     */
    @Transactional
    public void uptInquirySuspensionReleased(Long inqrNumb, InquiryActionVO action, AdminSessionVO admin) {

        checkLogin(admin);
        InquiryVO inquiry = lockInquiry(inqrNumb);
        if (StringUtil.isEmpty(inquiry.getUserNumb()) || StringUtil.isEmpty(inquiry.getSpndNumb())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        CurrentUserSuspensionVO request = new CurrentUserSuspensionVO();
        if (!StringUtil.isEmpty(action) && !StringUtil.isEmpty(action.getRlesCntn())) {
            request.setRlesCntn(action.getRlesCntn().trim());
        }
        currentUserService.uptUserSuspensionReleased(inquiry.getUserNumb(), inquiry.getSpndNumb(), request, admin);
    }

    /** 고객문의 행을 잠그고 현재 처리 상태를 반환한다 */
    private InquiryVO lockInquiry(Long inqrNumb) {

        if (StringUtil.isEmpty(inqrNumb) || inqrNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        InquiryVO inquiry = inquiryMapper.getInquiryForUpdate(inqrNumb);
        if (StringUtil.isEmpty(inquiry)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.INQUIRY_NOT_FOUND);
        }
        // 잠금 조회한 고객문의를 반환한다
        return inquiry;
    }

    /** 고객문의와 관리자 답변을 묶어 상세 응답을 생성한다 */
    private InquiryVO createDetail(Long inqrNumb) {

        if (StringUtil.isEmpty(inqrNumb) || inqrNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }
        InquiryVO inquiry = inquiryMapper.getInquiryDtl(inqrNumb);
        if (StringUtil.isEmpty(inquiry)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.INQUIRY_NOT_FOUND);
        }
        inquiry.setAnswers(inquiryMapper.getInquiryAnswerList(inqrNumb));
        // 고객문의 본문과 답변 목록을 반환한다
        return inquiry;
    }

    /** 로그인 관리자 여부를 확인한다 */
    private void checkLogin(AdminSessionVO admin) {

        if (StringUtil.isEmpty(admin) || StringUtil.isEmpty(admin.getAdmnNumb())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
