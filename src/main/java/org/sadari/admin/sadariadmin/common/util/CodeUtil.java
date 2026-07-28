package org.sadari.admin.sadariadmin.common.util;

import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * fileName       : CodeUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-09
 * description    : 공통코드 조회 유틸 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-09        SeungHyeon.Kang    최초 생성
 */
@Component
public class CodeUtil {

    /** 세부코드 Mapper */
    private final CodeMapper codeMapper;

    /**
     * 공통코드 조회 유틸 생성
     * @author SeungHyeon.Kang
     * @param codeMapper
     * @return
     */
    public CodeUtil(CodeMapper codeMapper) {
        this.codeMapper = codeMapper;
    }

    /**
     * 세부코드 목록 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    public List<CodeVO> getCodeList(String commCode) {
        return codeMapper.getCodeList(commCode);
    }

    /**
     * 세부코드명 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    public String getCodeName(String commCode, String comdCode) {
        return codeMapper.getCodeName(commCode, comdCode);
    }

    /**
     * 세부코드명 단건 조회
     * @author SeungHyeon.Kang
     * @param comdCode
     * @return
     */
    public String getCodeName(String comdCode) {
        return codeMapper.getCodeNameByComdCode(comdCode);
    }
}
