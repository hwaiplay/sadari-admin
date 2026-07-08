package org.sadari.admin.sadariadmin.common.code.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;

import java.util.List;

@Mapper
public interface CodeMapper {

    /**
     * 공통코드 목록을 조회한다.
     */
    List<CodeMasterVO> getCommCodeList();

    /**
     * 공통코드 상세를 조회한다.
     */
    CodeMasterVO getCommCodeDtl(@Param("commCode") String commCode);

    /**
     * 공통코드 개수를 조회한다.
     */
    int getCommCodeCnt(@Param("commCode") String commCode);

    /**
     * 공통코드를 등록한다.
     */
    void setCommCode(CodeMasterVO codeMaster);

    /**
     * 공통코드에 속한 세부코드 목록을 조회한다.
     */
    List<CodeVO> getCodeList(@Param("commCode") String commCode);

    /**
     * 세부코드 개수를 조회한다.
     */
    int getComdCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드를 등록한다.
     */
    void setComdCode(CodeVO code);

    /**
     * 세부코드를 삭제한다.
     */
    void delComdCode(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 공통코드와 세부코드로 세부코드명을 조회한다.
     */
    String getCodeName(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드로 세부코드명을 조회한다.
     */
    String getCodeNameByComdCode(@Param("comdCode") String comdCode);
}
