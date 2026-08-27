package org.sadari.admin.sadariadmin.common.code.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterSearchVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeMasterVO;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;

import java.util.List;

/**
 * fileName       : CodeMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-07-08
 * description    : CodeMapper role
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-08        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    공통코드 목록 검색 조건 추가
 * 2026-08-05        Codex       세부코드 계층 검증 조회 추가
 */
@Mapper
public interface CodeMapper {

    /**
     * 공통코드 목록 조회
     * @author SeungHyeon.Kang
     * @return
     */
    List<CodeMasterVO> getCommCodeList(CodeMasterSearchVO search);

    /** 검색 조건에 맞는 공통코드 전체 건수 조회 */
    int getCommCodeListCount(CodeMasterSearchVO search);

    /**
     * 공통코드 상세 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    CodeMasterVO getCommCodeDtl(@Param("commCode") String commCode);

    /**
     * 공통코드 개수 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    int getCommCodeCnt(@Param("commCode") String commCode);

    /**
     * 공통코드 등록
     * @author SeungHyeon.Kang
     * @param codeMaster
     * @return
     */
    void setCommCode(CodeMasterVO codeMaster);

    /**
     * 공통코드 수정
     * @author SeungHyeon.Kang
     * @param codeMaster
     * @return
     */
    void uptCommCode(CodeMasterVO codeMaster);

    /**
     * 세부코드 목록 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @return
     */
    List<CodeVO> getCodeList(@Param("commCode") String commCode);

    /**
     * 세부코드 상세 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    CodeVO getCodeDtl(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드 개수 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    int getComdCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 지정한 세부코드의 직계 하위코드 개수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param commCode 조회할 공통코드
     * @param comdCode 상위 세부코드
     * @return 직계 하위 세부코드 개수
     */
    int getChildCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 지정한 세부코드 아래에 후보 상위코드가 존재하는지 조회한다
     *
     * @author SeungHyeon.Kang
     * @param commCode 조회할 공통코드
     * @param comdCode 계층 기준 세부코드
     * @param candidateCode 하위 존재 여부를 확인할 후보 코드
     * @return 후보 코드가 하위 계층에 존재하는 건수
     */
    int getDescendantCodeCnt(@Param("commCode") String commCode, @Param("comdCode") String comdCode
                           , @Param("candidateCode") String candidateCode);

    /**
     * 세부코드 등록
     * @author SeungHyeon.Kang
     * @param code
     * @return
     */
    void setComdCode(CodeVO code);

    /**
     * 세부코드 수정
     * @author SeungHyeon.Kang
     * @param code
     * @return
     */
    void uptComdCode(CodeVO code);

    /**
     * 세부코드 삭제
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    void delComdCode(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드명 조회
     * @author SeungHyeon.Kang
     * @param commCode
     * @param comdCode
     * @return
     */
    String getCodeName(@Param("commCode") String commCode, @Param("comdCode") String comdCode);

    /**
     * 세부코드명 단건 조회
     * @author SeungHyeon.Kang
     * @param comdCode
     * @return
     */
    String getCodeNameByComdCode(@Param("comdCode") String comdCode);
}
