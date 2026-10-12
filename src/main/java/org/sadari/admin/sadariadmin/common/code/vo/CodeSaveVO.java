package org.sadari.admin.sadariadmin.common.code.vo;

import lombok.Data;

import java.util.List;

/**
 * fileName       : CodeSaveVO
 * author         : SeungHyeon.Kang
 * date           : 2026-10-12
 * description    : 공통코드와 기존 및 신규 세부코드를 한 요청으로 전달한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-10-12        SeungHyeon.Kang    최초 생성
 */
@Data
public class CodeSaveVO {

    // 수정할 공통코드
    private CodeMasterVO master;

    // 수정할 기존 세부코드
    private List<CodeVO> detailCodes;

    // 등록할 신규 세부코드
    private List<CodeVO> newCodes;
}
