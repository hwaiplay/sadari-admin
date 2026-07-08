package org.sadari.admin.sadariadmin.common.code.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * 코드관리 서비스.
 */
@Service
public class CodeManageService {

    /** 코드 Mapper. */
    private final CodeMapper codeMapper;

    public CodeManageService(CodeMapper codeMapper) {
        this.codeMapper = codeMapper;
    }

    /**
     * 공통코드 목록을 조회한다.
     */
    public List<CodeMasterVO> getCommCodeList() {
        return codeMapper.getCommCodeList();
    }

    /**
     * 공통코드 상세를 조회한다.
     */
    public CodeMasterVO getCommCodeDtl(String commCode) {
        return codeMapper.getCommCodeDtl(commCode);
    }

    /**
     * 공통코드 중복 여부를 조회한다.
     */
    public boolean isCommCodeDuplicate(String commCode) {
        return codeMapper.getCommCodeCnt(commCode) > 0;
    }

    /**
     * 공통코드를 등록한다.
     */
    @Transactional
    public CodeMasterVO setCommCode(CodeMasterVO codeMaster, AdminSessionVO admin) {
        if (isCommCodeDuplicate(codeMaster.getCommCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 등록된 공통코드입니다.");
        }
        if (codeMaster.getUseeYsno() == null || codeMaster.getUseeYsno().isBlank()) {
            codeMaster.setUseeYsno("Y");
        }
        codeMaster.setRegeAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.setCommCode(codeMaster);
        return codeMapper.getCommCodeDtl(codeMaster.getCommCode());
    }

    /**
     * 세부코드 목록을 조회한다.
     */
    public List<CodeVO> getComdCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * 세부코드를 등록한다.
     */
    @Transactional
    public CodeVO setComdCode(String commCode, CodeVO code, AdminSessionVO admin) {
        if (codeMapper.getComdCodeCnt(commCode, code.getComdCode()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이미 등록된 세부코드입니다.");
        }
        code.setCommCode(commCode);
        if (code.getUseeYsno() == null || code.getUseeYsno().isBlank()) {
            code.setUseeYsno("Y");
        }
        code.setRegeAdmn(String.valueOf(admin.getAdmnNumb()));
        codeMapper.setComdCode(code);
        return code;
    }

    /**
     * 세부코드를 삭제한다.
     */
    @Transactional
    public void delComdCode(String commCode, String comdCode) {
        codeMapper.delComdCode(commCode, comdCode);
    }
}
