package org.sadari.admin.sadariadmin.common.code;

import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 공통코드 조회 유틸.
 */
@Component
public class CodeUtil {

    /** 세부코드 Mapper. */
    private final CodeMapper codeMapper;

    public CodeUtil(CodeMapper codeMapper) {
        this.codeMapper = codeMapper;
    }

    /**
     * 공통코드에 속한 세부코드 목록을 조회한다.
     */
    public List<CodeVO> getCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * 공통코드와 세부코드로 세부코드명을 조회한다.
     */
    public String getCodeName(String commCode, String comdCode) {
        return codeMapper.getCodeName(commCode, comdCode);
    }

    /**
     * 세부코드로 세부코드명을 조회한다.
     */
    public String getCodeName(String comdCode) {
        return codeMapper.getCodeNameByComdCode(comdCode);
    }
}
