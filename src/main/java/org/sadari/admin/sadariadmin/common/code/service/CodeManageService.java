package org.sadari.admin.sadariadmin.common.code.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 코드관리 서비스
 */
@Service
public class CodeManageService {

    /** 코드 Mapper */
    private final CodeMapper codeMapper;

    /**
     * 코드관리 서비스 생성
     * @Author SeungHyeon.Kang
     * @param codeMapper
     * @return
     */
    public CodeManageService(CodeMapper codeMapper) {
        this.codeMapper = codeMapper;
    }

    /**
     * 공통코드 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    public List<CodeMasterVO> getCommCodeList() {
        return codeMapper.getCommCodeList();
    }

    /**
     * 공통코드 상세 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public CodeMasterVO getCommCodeDtl(String commCode) {
        return codeMapper.getCommCodeDtl(commCode);
    }

    /**
     * 공통코드 중복 여부 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public boolean isCommCodeDuplicate(String commCode) {
        return codeMapper.getCommCodeCnt(commCode) > 0;
    }

    /**
     * 공통코드 등록
     * @Author SeungHyeon.Kang
     * @param codeMaster
     * @param admin
     * @return
     */
    @Transactional
    public CodeMasterVO setCommCode(CodeMasterVO codeMaster, AdminSessionVO admin) {
        if (StringUtil.isEmpty(codeMaster.getCommCode()) || StringUtil.isEmpty(codeMaster.getCodeName())) {
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
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param codeMaster
     * @return
     */
    @Transactional
    public CodeMasterVO uptCommCode(String commCode, CodeMasterVO codeMaster, AdminSessionVO admin) {
        if (StringUtil.isEmpty(commCode) || StringUtil.isEmpty(codeMaster.getCodeName())) {
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
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public List<CodeVO> getComdCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * 세부코드 등록
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param code
     * @param admin
     * @return
     */
    @Transactional
    public CodeVO setComdCode(String commCode, CodeVO code, AdminSessionVO admin) {
        if (StringUtil.isEmpty(commCode) || StringUtil.isEmpty(code.getComdCode()) || StringUtil.isEmpty(code.getComdName())) {
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
        code.setRegiAdmn(String.valueOf(admin.getAdmnNumb()));
        code.setUpdtAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.setComdCode(code);
        return codeMapper.getCodeDtl(commCode, code.getComdCode());
    }

    /**
     * 세부코드 수정
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @param code
     * @return
     */
    @Transactional
    public CodeVO uptComdCode(String commCode, String comdCode, CodeVO code, AdminSessionVO admin) {
        if (StringUtil.isEmpty(commCode) || StringUtil.isEmpty(comdCode) || StringUtil.isEmpty(code.getComdName())) {
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
        code.setUpdtAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.uptComdCode(code);
        return codeMapper.getCodeDtl(commCode, comdCode);
    }

    /**
     * 세부코드 삭제
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    @Transactional
    public void delComdCode(String commCode, String comdCode) {
        codeMapper.delComdCode(commCode, comdCode);
    }
}
