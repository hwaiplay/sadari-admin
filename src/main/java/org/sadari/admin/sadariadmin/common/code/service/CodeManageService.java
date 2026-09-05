package org.sadari.admin.sadariadmin.common.code.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterSearchVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * fileName       : CodeManageService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : 코드관리 서비스 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    공통코드 목록 검색 조건 추가
 * 2026-08-05        SeungHyeon.Kang       세부코드 상위 관계와 순환 검증 추가
 */
@Service
@Transactional(readOnly = true)
public class CodeManageService {

    /** 코드 Mapper */
    private final CodeMapper codeMapper;

    /**
     * 코드관리 서비스 생성
     * @author SeungHyeon.Kang
     * @param codeMapper
     * @return
     */
    public CodeManageService(CodeMapper codeMapper) {
        this.codeMapper = codeMapper;
    }

    /**
     * 공통코드 목록 조회
     * @author SeungHyeon.Kang
     * @return
     */
    public PageData<CodeMasterVO> getCommCodeList(CodeMasterSearchVO search) {
        // 요청 페이지에 해당하는 조회 행 범위를 계산한다
        PageRequest pageRequest = new PageRequest(search.getPage());
        // 목록과 건수 조회에 같은 검색 조건과 시작 행을 적용한다
        search.setStartRow(pageRequest.getStartRow());
        // 검색 조건에 페이지 마지막 행을 적용한다
        search.setEndRow(pageRequest.getEndRow());
        // 검색 조건에 맞는 공통코드 목록과 전체 건수로 페이지 응답을 생성한다
        return PageData.of(codeMapper.getCommCodeList(search), codeMapper.getCommCodeListCount(search)
                         , pageRequest);
    }

    /**
     * 공통코드 상세 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public CodeMasterVO getCommCodeDtl(String commCode) {
        return codeMapper.getCommCodeDtl(commCode);
    }

    /**
     * 공통코드 중복 여부 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public boolean isCommCodeDuplicate(String commCode) {
        return codeMapper.getCommCodeCnt(commCode) > 0;
    }

    /**
     * 공통코드 등록
     * @author SeungHyeon.Kang
     * @param codeMaster
     * @param admin
     * @return
     */
    @Transactional
    public CodeMasterVO setCommCode(CodeMasterVO codeMaster, AdminSessionVO admin) {
        // 공통코드와 로그인 관리자 정보가 없으면 등록 데이터를 구성할 수 없으므로 요청을 중단한다
        if (StringUtil.isEmpty(codeMaster)
                || StringUtil.isEmpty(codeMaster.getCommCode())
                || StringUtil.isEmpty(codeMaster.getCodeName())
                || StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 동일한 공통코드가 있으면 중복 오류로 처리한다
        if (isCommCodeDuplicate(codeMaster.getCommCode())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_MASTER_DUPLICATE);
        }

        if (StringUtil.isEmpty(codeMaster.getUseeYsno())) {
            codeMaster.setUseeYsno(Constant.YES);
        }
        codeMaster.setRegiAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMaster.setUpdtAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.setCommCode(codeMaster);
        return codeMapper.getCommCodeDtl(codeMaster.getCommCode());
    }

    /**
     * 공통코드 수정
     * @author SeungHyeon.Kang
     * @param commCode
     * @param codeMaster
     * @return
     */
    @Transactional
    public CodeMasterVO uptCommCode(String commCode, CodeMasterVO codeMaster, AdminSessionVO admin) {
        // 수정 대상과 로그인 관리자 정보가 없으면 수정 이력을 기록할 수 없으므로 요청을 중단한다
        if (StringUtil.isEmpty(commCode)
                || StringUtil.isEmpty(codeMaster)
                || StringUtil.isEmpty(codeMaster.getCodeName())
                || StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        if (codeMapper.getCommCodeCnt(commCode) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.COMMON_NO_DATA);
        }

        codeMaster.setCommCode(commCode);
        if (StringUtil.isEmpty(codeMaster.getUseeYsno())) {
            codeMaster.setUseeYsno(Constant.YES);
        }
        codeMaster.setUpdtAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.uptCommCode(codeMaster);
        return codeMapper.getCommCodeDtl(commCode);
    }

    /**
     * 세부코드 목록 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public List<CodeVO> getComdCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * 세부코드 등록
     * @author SeungHyeon.Kang
     * @param commCode
     * @param code
     * @param admin
     * @return
     */
    @Transactional
    public CodeVO setComdCode(String commCode, CodeVO code, AdminSessionVO admin) {
        // 세부코드와 로그인 관리자 정보가 없으면 등록 데이터를 구성할 수 없으므로 요청을 중단한다
        if (StringUtil.isEmpty(commCode)
                || StringUtil.isEmpty(code)
                || StringUtil.isEmpty(code.getComdCode())
                || StringUtil.isEmpty(code.getComdName())
                || StringUtil.isEmpty(code.getComdEnnm())
                || StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 같은 공통코드 안에 동일한 세부코드가 있으면 중복 오류로 처리한다
        if (codeMapper.getComdCodeCnt(commCode, code.getComdCode()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_DETAIL_DUPLICATE);
        }

        // 같은 공통코드 안에서만 부모를 지정하고 신규 코드의 자기 참조를 차단한다
        code.setUpprCode(normalizeUpprCode(code.getUpprCode()));
        // 등록할 세부코드의 상위 관계를 검증한다
        validateParentCode(commCode, code.getComdCode(), code.getUpprCode(), false);
        code.setCommCode(commCode);
        if (StringUtil.isEmpty(code.getUseeYsno())) {
            code.setUseeYsno(Constant.YES);
        }
        // 정렬 순서가 없거나 1보다 작으면 최초 정렬값을 사용한다
        if (StringUtil.isEmpty(code.getSortOrdr()) || code.getSortOrdr() < Constant.DEFAULT_MENU_SORT_ORDR) {
            code.setSortOrdr(Constant.DEFAULT_MENU_SORT_ORDR);
        }
        code.setRegiAdmn(String.valueOf(admin.getAdmnNumb()));
        code.setUpdtAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.setComdCode(code);
        return codeMapper.getCodeDtl(commCode, code.getComdCode());
    }

    /**
     * 세부코드 수정
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @param code
     * @return
     */
    @Transactional
    public CodeVO uptComdCode(String commCode, String comdCode, CodeVO code, AdminSessionVO admin) {
        // 수정 대상과 로그인 관리자 정보가 없으면 수정 이력을 기록할 수 없으므로 요청을 중단한다
        if (StringUtil.isEmpty(commCode)
                || StringUtil.isEmpty(comdCode)
                || StringUtil.isEmpty(code)
                || StringUtil.isEmpty(code.getComdName())
                || StringUtil.isEmpty(code.getComdEnnm())
                || StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 수정 대상 세부코드가 없으면 조회 결과 없음으로 처리한다
        if (codeMapper.getComdCodeCnt(commCode, comdCode) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CODE_DETAIL_NOT_FOUND);
        }

        // 수정할 세부코드의 빈 상위코드는 최상위 관계로 정규화한다
        code.setUpprCode(normalizeUpprCode(code.getUpprCode()));
        // 기존 하위코드를 새 부모로 지정해 순환 구조가 생기지 않도록 검증한다
        validateParentCode(commCode, comdCode, code.getUpprCode(), true);
        code.setCommCode(commCode);
        code.setComdCode(comdCode);
        if (StringUtil.isEmpty(code.getUseeYsno())) {
            code.setUseeYsno(Constant.YES);
        }
        // 정렬 순서가 없거나 1보다 작으면 최초 정렬값을 사용한다
        if (StringUtil.isEmpty(code.getSortOrdr()) || code.getSortOrdr() < Constant.DEFAULT_MENU_SORT_ORDR) {
            code.setSortOrdr(Constant.DEFAULT_MENU_SORT_ORDR);
        }
        code.setUpdtAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.uptComdCode(code);
        return codeMapper.getCodeDtl(commCode, comdCode);
    }

    /**
     * 세부코드 삭제
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    @Transactional
    public void delComdCode(String commCode, String comdCode) {
        // 하위코드가 남아 있으면 계층이 끊어지므로 부모 세부코드 삭제를 차단한다
        if (codeMapper.getChildCodeCnt(commCode, comdCode) > 0) {
            // "하위 세부코드가 있는 코드는 삭제할 수 없습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_DETAIL_HAS_CHILDREN);
        }

        codeMapper.delComdCode(commCode, comdCode);
    }

    /**
     * 화면에서 비어 있게 전달한 상위 세부코드를 최상위 관계로 정규화한다
     *
     * @author SeungHyeon.Kang
     * @param upprCode 정규화할 상위 세부코드
     * @return 공백을 제거한 상위 세부코드 또는 최상위를 나타내는 null
     */
    private String normalizeUpprCode(String upprCode) {
        // 공백 상위코드는 최상위 세부코드로 저장한다
        if (StringUtil.isEmpty(upprCode)) {
            // 최상위 관계를 나타내는 null을 반환한다
            return null;
        }

        // 코드 비교와 FK 저장에 같은 값을 사용하도록 앞뒤 공백을 제거해 반환한다
        return upprCode.trim();
    }

    /**
     * 세부코드의 부모가 같은 공통코드에 존재하고 순환 관계를 만들지 않는지 검증한다
     *
     * @author SeungHyeon.Kang
     * @param commCode 세부코드가 속한 공통코드
     * @param comdCode 저장할 세부코드
     * @param upprCode 지정한 상위 세부코드
     * @param checkDescendants 기존 하위 계층까지 검사할지 여부
     */
    private void validateParentCode(String commCode, String comdCode, String upprCode, boolean checkDescendants) {
        // 최상위 세부코드는 부모 관계 검증이 필요하지 않다
        if (StringUtil.isEmpty(upprCode)) {
            return;
        }

        // 자기 자신을 상위코드로 지정하면 즉시 순환하므로 저장을 차단한다
        if (comdCode.equals(upprCode)) {
            // "세부코드 계층을 순환 구조로 만들 수 없습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_DETAIL_CYCLE);
        }

        // 다른 공통코드의 세부코드나 존재하지 않는 코드는 부모로 사용할 수 없다
        if (codeMapper.getComdCodeCnt(commCode, upprCode) == 0) {
            // "상위 세부코드가 올바르지 않습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_DETAIL_PARENT_INVALID);
        }

        // 수정 시 현재 코드의 하위 계층을 부모로 지정하면 재귀 탐색이 순환하므로 차단한다
        if (checkDescendants && codeMapper.getDescendantCodeCnt(commCode, comdCode, upprCode) > 0) {
            // "세부코드 계층을 순환 구조로 만들 수 없습니다."
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_DETAIL_CYCLE);
        }
    }
}
