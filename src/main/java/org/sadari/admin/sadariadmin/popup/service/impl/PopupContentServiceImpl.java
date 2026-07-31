package org.sadari.admin.sadariadmin.popup.service.impl;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.popup.mapper.PopupContentMapper;
import org.sadari.admin.sadariadmin.popup.service.PopupContentService;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentSearchVO;
import org.sadari.admin.sadariadmin.popup.vo.PopupContentVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * fileName       : PopupContentServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-07-30
 * description    : 팝업 콘텐츠의 복합키와 JSON 목록 및 저장 길이를 검증하여 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-30        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    팝업 콘텐츠 목록 검색 조건 추가
 */
@Service
@Transactional(readOnly = true)
public class PopupContentServiceImpl implements PopupContentService {

    // 팝업 코드에서 허용하는 영문 대문자와 숫자 및 밑줄 형식
    private static final String POPUP_CODE_PATTERN = "^[A-Z][A-Z0-9_]*$";

    // 관리용 제목 저장 가능 바이트
    private static final int TITLE_MAX_BYTES = 200;

    // 각 콘텐츠 영역 저장 가능 바이트
    private static final int CONTENT_MAX_BYTES = 4000;

    // 팝업 콘텐츠 Mapper
    private final PopupContentMapper popupContentMapper;

    // 팝업 목록 JSON 검증과 정규화에 사용하는 객체 변환기
    private final ObjectMapper objectMapper;

    /**
     * 팝업 콘텐츠 관리 서비스에 데이터 접근과 JSON 변환 기능을 주입한다
     *
     * @author SeungHyeon.Kang
     * @param popupContentMapper 팝업 콘텐츠 Mapper
     * @param objectMapper JSON 객체 변환기
     */
    public PopupContentServiceImpl(PopupContentMapper popupContentMapper, ObjectMapper objectMapper) {
        this.popupContentMapper = popupContentMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 로그인 관리자에게 팝업 콘텐츠 목록 페이지를 제공한다
     *
     * @author SeungHyeon.Kang
     * @param search 팝업 콘텐츠 검색 조건
     * @param admin 로그인 관리자 세션
     * @return 팝업 콘텐츠 목록과 페이지 정보
     */
    @Override
    public PageData<PopupContentVO> getPopupContentList(PopupContentSearchVO search, AdminSessionVO admin) {
        // 인증되지 않은 관리자가 운영 콘텐츠를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 요청 페이지를 데이터베이스 조회 행 범위로 변환한다
        PageRequest pageRequest = new PageRequest(search.getPage());
        // 목록과 건수 조회에 같은 검색 조건과 시작 행을 적용한다
        search.setStartRow(pageRequest.getStartRow());
        // 검색 조건에 페이지 마지막 행을 적용한다
        search.setEndRow(pageRequest.getEndRow());
        // 검색 조건에 맞는 목록과 전체 건수로 팝업 콘텐츠 페이지를 생성한다
        return PageData.of(popupContentMapper.getPopupContentList(search)
                         , popupContentMapper.getPopupContentListCnt(search), pageRequest);
    }

    /**
     * 복합키에 해당하는 팝업 콘텐츠 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @param admin 로그인 관리자 세션
     * @return 팝업 콘텐츠 상세
     */
    @Override
    public PopupContentVO getPopupContentDtl(String popuSitu, String popuCode, AdminSessionVO admin) {
        // 인증되지 않은 관리자가 운영 콘텐츠를 조회하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // 비어 있거나 허용 형식이 아닌 복합키로 상세 쿼리가 실행되지 않도록 검증한다
        validateKey(popuSitu, popuCode);
        // 검증된 복합키로 팝업 콘텐츠 상세를 조회한다
        PopupContentVO popupContent = popupContentMapper.getPopupContentDtl(popuSitu, popuCode);
        // 사용자 화면에 연결된 팝업이 없으면 관리자에게 명확한 조회 실패를 제공한다
        if (StringUtil.isEmpty(popupContent)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.POPUP_CONTENT_NOT_FOUND);
        }

        // 복합키에 해당하는 팝업 콘텐츠 상세를 반환한다
        return popupContent;
    }

    /**
     * 신규 팝업 콘텐츠의 화면 코드와 JSON 목록을 검증하여 등록한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 등록할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @return 등록된 팝업 콘텐츠 상세
     */
    @Override
    @Transactional
    public PopupContentVO setPopupContent(PopupContentVO popupContent, AdminSessionVO admin) {
        // 인증되지 않은 관리자가 운영 콘텐츠를 등록하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // DB 변경 전에 복합키와 필수 제목 및 JSON 목록을 정규화하고 검증한다
        validateAndNormalize(popupContent);
        // 공통코드에 등록된 화면에서만 사용자 팝업을 연결할 수 있도록 화면 코드를 확인한다
        validatePopupSitu(popupContent.getPopuSitu());
        // 같은 화면과 팝업 코드 조합을 중복 등록하면 사용자 조회 결과가 모호해지므로 차단한다
        if (popupContentMapper.getPopupContentCnt(popupContent.getPopuSitu(), popupContent.getPopuCode()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.POPUP_CONTENT_DUPLICATE);
        }

        // 최초 등록자와 수정자를 현재 로그인 관리자로 기록한다
        setAuditAdmin(popupContent, admin, true);
        // 검증과 정규화를 마친 팝업 콘텐츠를 등록한다
        popupContentMapper.setPopupContent(popupContent);
        // 저장된 DB 값과 관리 이력이 포함된 팝업 콘텐츠 상세를 반환한다
        return popupContentMapper.getPopupContentDtl(popupContent.getPopuSitu(), popupContent.getPopuCode());
    }

    /**
     * 기존 복합키를 유지하면서 팝업 제목과 JSON 목록 콘텐츠를 수정한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     * @param popupContent 수정할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @return 수정된 팝업 콘텐츠 상세
     */
    @Override
    @Transactional
    public PopupContentVO uptPopupContent(String popuSitu, String popuCode, PopupContentVO popupContent
                                        , AdminSessionVO admin) {
        // 인증되지 않은 관리자가 운영 콘텐츠를 수정하지 못하도록 로그인 상태를 확인한다
        checkLogin(admin);
        // URL 복합키가 실제 상세 데이터 식별 형식에 맞는지 확인한다
        validateKey(popuSitu, popuCode);
        // 수정 중 복합키가 바뀌어 사용자 화면 연결이 끊기지 않도록 URL 값을 요청 객체에 고정한다
        popupContent.setPopuSitu(popuSitu);
        // 수정 중 복합키가 바뀌어 사용자 화면 연결이 끊기지 않도록 URL 값을 요청 객체에 고정한다
        popupContent.setPopuCode(popuCode);
        // DB 변경 전에 제목과 JSON 목록을 정규화하고 저장 길이를 검증한다
        validateAndNormalize(popupContent);
        // 수정 대상이 실제로 존재하는지 확인하여 무의미한 성공 응답을 방지한다
        getPopupContentDtl(popuSitu, popuCode, admin);
        // 수정 관리자만 현재 로그인 관리자로 갱신하여 최초 등록 이력을 보존한다
        setAuditAdmin(popupContent, admin, false);
        // 기존 복합키를 유지하면서 관리 제목과 네 개 콘텐츠 영역을 수정한다
        popupContentMapper.uptPopupContent(popupContent);
        // 수정된 DB 값과 관리 이력이 포함된 팝업 콘텐츠 상세를 반환한다
        return popupContentMapper.getPopupContentDtl(popuSitu, popuCode);
    }

    /**
     * 팝업 콘텐츠의 필수값과 JSON 목록을 DB 저장 형태로 검증하고 정규화한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 검증할 팝업 콘텐츠
     */
    private void validateAndNormalize(PopupContentVO popupContent) {
        // 필수 식별값과 관리 제목 및 첫 번째 콘텐츠가 없으면 저장할 수 없는 요청으로 처리한다
        if (StringUtil.isEmpty(popupContent) || StringUtil.hasEmpty(popupContent.getPopuSitu()
                                                                 , popupContent.getPopuCode()
                                                                 , popupContent.getMngmTitl()
                                                                 , popupContent.getContFirs())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 복합키가 허용 형식과 길이를 만족하는지 저장 전에 확인한다
        validateKey(popupContent.getPopuSitu(), popupContent.getPopuCode());
        // 관리용 제목이 데이터베이스 컬럼의 실제 바이트 한도를 넘지 않는지 확인한다
        validateByteLength(popupContent.getMngmTitl().trim(), TITLE_MAX_BYTES);
        // 첫 번째 콘텐츠는 사용자 화면의 필수 목록이므로 하나 이상의 문구를 유지하도록 정규화한다
        popupContent.setContFirs(normalizeContent(popupContent.getContFirs(), true));
        // 선택 콘텐츠는 문구가 없을 때 Null로 통일하여 화면 영역 존재 여부를 명확히 한다
        popupContent.setContSeco(normalizeContent(popupContent.getContSeco(), false));
        // 선택 콘텐츠는 문구가 없을 때 Null로 통일하여 화면 영역 존재 여부를 명확히 한다
        popupContent.setContThir(normalizeContent(popupContent.getContThir(), false));
        // 선택 콘텐츠는 문구가 없을 때 Null로 통일하여 화면 영역 존재 여부를 명확히 한다
        popupContent.setContFour(normalizeContent(popupContent.getContFour(), false));
        // 관리 화면과 목록에 동일한 제목이 보이도록 앞뒤 공백을 제거한다
        popupContent.setMngmTitl(popupContent.getMngmTitl().trim());
    }

    /**
     * JSON 문자열 배열의 공백과 중복을 제거하고 데이터베이스 저장 한도를 확인한다
     *
     * @author SeungHyeon.Kang
     * @param content JSON 문자열 배열
     * @param required 하나 이상의 문구가 필요한지 여부
     * @return 정규화된 JSON 문자열 배열 또는 선택 영역의 Null
     */
    private String normalizeContent(String content, boolean required) {
        // 선택 콘텐츠가 비어 있으면 사용자 화면에 불필요한 빈 영역이 생기지 않도록 Null로 저장한다
        if (StringUtil.isEmpty(content) && !required) {
            // 선택 콘텐츠 영역이 없음을 나타내는 Null을 반환한다
            return null;
        }

        // JSON 구문과 배열 원소를 한 번에 검증하여 잘못된 원문이 DB에 저장되지 않도록 한다
        try {
            // 문자열 JSON을 배열 구조로 검증할 트리 객체로 읽는다
            JsonNode contentNode = objectMapper.readTree(content);
            // 팝업 콘텐츠는 React 목록으로 사용하므로 문자열 배열 이외의 JSON 구조를 차단한다
            if (StringUtil.isEmpty(contentNode) || !contentNode.isArray()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.POPUP_CONTENT_INVALID);
            }

            // 입력 순서는 유지하면서 같은 문구가 중복 저장되지 않도록 집합을 생성한다
            Set<String> contentItems = new LinkedHashSet<>();
            // 모든 배열 원소가 공백이 아닌 문자열인지 순서대로 검사한다
            Iterator<JsonNode> contentIterator = contentNode.values().iterator();
            // 사용자 화면에 그대로 표시할 각 목록 문구를 검증하고 정규화한다
            while (contentIterator.hasNext()) {
                // 현재 배열 원소의 JSON 타입과 실제 문구를 확인한다
                JsonNode contentItem = contentIterator.next();
                // 객체나 숫자 및 빈 문자열은 사용자 목록 문구로 사용할 수 없어 차단한다
                if (!contentItem.isString() || StringUtil.isEmpty(contentItem.stringValue())) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.POPUP_CONTENT_INVALID);
                }

                // 목록 표시 순서가 유지되도록 공백을 제거한 문구를 순서 집합에 추가한다
                contentItems.add(contentItem.stringValue().trim());
            }

            // 필수 영역은 중복 제거 후에도 하나 이상의 목록 문구가 있어야 한다
            if (StringUtil.isEmpty(contentItems)) {
                // 선택 영역의 빈 배열은 사용자 화면에 영역 자체가 생기지 않도록 Null로 정규화한다
                if (!required) {
                    // 선택 콘텐츠 영역이 없음을 나타내는 Null을 반환한다
                    return null;
                }

                throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.POPUP_CONTENT_INVALID);
            }

            // 검증한 문자열 목록을 일관된 JSON 배열 문자열로 직렬화한다
            String normalizedContent = objectMapper.writeValueAsString(contentItems);
            // 데이터베이스 컬럼에 저장되는 실제 UTF-8 바이트 길이를 확인한다
            validateByteLength(normalizedContent, CONTENT_MAX_BYTES);
            // 중복과 불필요한 공백이 제거된 JSON 배열 문자열을 반환한다
            return normalizedContent;
        } catch (JacksonException exception) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.POPUP_CONTENT_INVALID);
        }
    }

    /**
     * 문자열의 UTF-8 바이트 길이가 데이터베이스 컬럼 한도를 넘지 않는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param value 길이를 확인할 문자열
     * @param maxBytes 허용할 최대 바이트
     */
    private void validateByteLength(String value, int maxBytes) {
        // 한글을 포함한 실제 DB 저장 바이트가 컬럼 한도를 넘으면 사전에 사용자 오류로 처리한다
        if (value.getBytes(StandardCharsets.UTF_8).length > maxBytes) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.POPUP_CONTENT_TOO_LONG);
        }
    }

    /**
     * 팝업 콘텐츠 복합키의 형식과 컬럼 길이를 검증한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     * @param popuCode 팝업 식별 코드
     */
    private void validateKey(String popuSitu, String popuCode) {
        // 복합키가 비어 있으면 상세 조회와 저장 대상을 식별할 수 없어 요청을 차단한다
        if (StringUtil.hasEmpty(popuSitu, popuCode)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 영문 대문자 기반 코드만 허용하고 DB 컬럼 길이를 넘는 식별값을 차단한다
        if (!popuSitu.matches(POPUP_CODE_PATTERN) || popuSitu.length() > 50
                || !popuCode.matches(POPUP_CODE_PATTERN) || popuCode.length() > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 팝업 사용 화면 구분이 활성 공통코드에 등록되어 있는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param popuSitu 팝업 사용 화면 구분 코드
     */
    private void validatePopupSitu(String popuSitu) {
        // 화면 구분이 공통코드에 없으면 사용자 API와 관리자 표시명이 일치하지 않아 등록을 차단한다
        if (popupContentMapper.getPopupSituCnt(popuSitu) == 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_INVALID_REQUEST);
        }
    }

    /**
     * 현재 로그인 관리자를 등록 또는 수정 이력에 설정한다
     *
     * @author SeungHyeon.Kang
     * @param popupContent 이력을 설정할 팝업 콘텐츠
     * @param admin 로그인 관리자 세션
     * @param isNew 신규 등록 여부
     */
    private void setAuditAdmin(PopupContentVO popupContent, AdminSessionVO admin, boolean isNew) {
        // 관리 컬럼에 관리자 번호를 일관된 문자열로 저장한다
        String adminNumb = String.valueOf(admin.getAdmnNumb());
        // 신규 등록일 때만 최초 등록자를 기록하여 이후 수정에서도 원래 등록 이력을 보존한다
        if (isNew) {
            // 팝업 콘텐츠를 최초 생성한 관리자 식별값을 설정한다
            popupContent.setRegiAdmn(adminNumb);
        }

        // 마지막으로 팝업 콘텐츠를 변경한 관리자 식별값을 설정한다
        popupContent.setUpdtAdmn(adminNumb);
    }

    /**
     * 관리자 인증 객체가 존재하는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param admin 로그인 관리자 세션
     */
    private void checkLogin(AdminSessionVO admin) {
        // 인증 객체가 없으면 운영 콘텐츠를 조회하거나 변경하지 못하도록 로그인 오류로 처리한다
        if (StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
