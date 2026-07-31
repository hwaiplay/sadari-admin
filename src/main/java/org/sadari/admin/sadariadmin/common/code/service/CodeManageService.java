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
                || StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 같은 공통코드 안에 동일한 세부코드가 있으면 중복 오류로 처리한다
        if (codeMapper.getComdCodeCnt(commCode, code.getComdCode()) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.CODE_DETAIL_DUPLICATE);
        }

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
                || StringUtil.isEmpty(admin)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.COMMON_REQUIRED_VALUE);
        }

        // 수정 대상 세부코드가 없으면 조회 결과 없음으로 처리한다
        if (codeMapper.getComdCodeCnt(commCode, comdCode) == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.CODE_DETAIL_NOT_FOUND);
        }

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
        codeMapper.delComdCode(commCode, comdCode);
    }
}
