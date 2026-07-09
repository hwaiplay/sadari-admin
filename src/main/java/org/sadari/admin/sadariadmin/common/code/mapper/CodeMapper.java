package org.sadari.admin.sadariadmin.common.code.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;

import java.util.List;

@Mapper
public interface CodeMapper {

    /**
     * 공통코드 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    List<CodeMasterVO> getCommCodeList();

    /**
     * 공통코드 상세 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    CodeMasterVO getCommCodeDtl(@Param("commCode") String commCode);

    /**
     * 공통코드 개수 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    int getCommCodeCnt(@Param("commCode") String commCode);

    /**
     * 공통코드 등록
     * @Author SeungHyeon.Kang
     * @param codeMaster
     * @return
     */
    void setCommCode(CodeMasterVO codeMaster);

    /**
     * 공통코드 수정
     * @Author SeungHyeon.Kang
     * @param codeMaster
     * @return
     */
    void uptCommCode(CodeMasterVO codeMaster);

    /**
     * 세부코드 목록 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    List<CodeVO> getCodeList(@Param("commCode") String commCode);

    /**
     * 세부코드 상세 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    CodeVO getCodeDtl(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드 개수 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    int getComdCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드 등록
     * @Author SeungHyeon.Kang
     * @param code
     * @return
     */
    void setComdCode(CodeVO code);

    /**
     * 세부코드 수정
     * @Author SeungHyeon.Kang
     * @param code
     * @return
     */
    void uptComdCode(CodeVO code);

    /**
     * 세부코드 삭제
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    void delComdCode(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드명 조회
     * @Author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    String getCodeName(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드명 단건 조회
     * @Author SeungHyeon.Kang
     * @param comdCode
     * @return
     */
    String getCodeNameByComdCode(@Param("comdCode") String comdCode);
}
