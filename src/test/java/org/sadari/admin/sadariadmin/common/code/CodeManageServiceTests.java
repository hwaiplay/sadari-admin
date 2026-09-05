package org.sadari.admin.sadariadmin.common.code;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.code.service.CodeManageService;
import org.sadari.admin.sadariadmin.common.code.vo.CodeVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : CodeManageServiceTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-05
 * description    : 세부코드 계층 무결성 검증 단위 테스트
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-05        SeungHyeon.Kang       최초 생성
 * 2026-09-05        Codex                  영문 코드명 필수값 반영
 */
@ExtendWith(MockitoExtension.class)
class CodeManageServiceTests {

    // 코드 Mapper 대역
    @Mock
    private CodeMapper codeMapper;

    // 테스트 대상 코드관리 서비스
    @InjectMocks
    private CodeManageService codeManageService;

    /**
     * 같은 공통코드에 존재하지 않는 상위 세부코드를 등록할 수 없는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void setCodeRejectsNoParent() {
        // 존재하지 않는 상위 세부코드를 지정한 신규 세부코드를 준비한다
        CodeVO code = createCode("CHILD", "MISSING");
        // 신규 코드와 상위코드가 아직 존재하지 않는 조회 결과를 설정한다
        when(codeMapper.getComdCodeCnt("GROUP", "CHILD")).thenReturn(0);
        when(codeMapper.getComdCodeCnt("GROUP", "MISSING")).thenReturn(0);

        // 잘못된 상위코드가 업무 예외로 차단되는지 확인한다
        BusinessException exception = assertThrows(BusinessException.class
                                                   , () -> codeManageService.setComdCode("GROUP", code
                                                                                      , createAdminSession()));
        // 상위 세부코드 오류가 호출자에게 정확히 전달되는지 확인한다
        assertEquals(ResultEnum.CODE_DETAIL_PARENT_INVALID, exception.getResultEnum());
        // 검증 실패 데이터가 저장되지 않았는지 확인한다
        verify(codeMapper, never()).setComdCode(code);
    }

    /**
     * 현재 코드의 하위 세부코드를 새 부모로 지정해 순환 구조를 만들 수 없는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void uptCodeRejectsDescendant() {
        // 현재 코드의 하위 세부코드를 새 부모로 지정한 수정 데이터를 준비한다
        CodeVO code = createCode("PARENT", "DESCENDANT");
        // 수정 대상과 지정 부모가 모두 존재하도록 조회 결과를 설정한다
        when(codeMapper.getComdCodeCnt("GROUP", "PARENT")).thenReturn(1);
        when(codeMapper.getComdCodeCnt("GROUP", "DESCENDANT")).thenReturn(1);
        // 지정 부모가 현재 코드의 하위 계층임을 설정한다
        when(codeMapper.getDescendantCodeCnt("GROUP", "PARENT", "DESCENDANT")).thenReturn(1);

        // 순환 관계가 업무 예외로 차단되는지 확인한다
        BusinessException exception = assertThrows(BusinessException.class
                                                   , () -> codeManageService.uptComdCode("GROUP", "PARENT", code
                                                                                      , createAdminSession()));
        // 순환 구조 오류가 호출자에게 정확히 전달되는지 확인한다
        assertEquals(ResultEnum.CODE_DETAIL_CYCLE, exception.getResultEnum());
        // 검증 실패 데이터가 수정되지 않았는지 확인한다
        verify(codeMapper, never()).uptComdCode(code);
    }

    /**
     * 하위 세부코드가 남아 있는 부모 세부코드를 삭제할 수 없는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delCodeRejectsChildren() {
        // 삭제 대상에 직접 하위 세부코드가 존재하도록 조회 결과를 설정한다
        when(codeMapper.getChildCodeCnt("GROUP", "PARENT")).thenReturn(1);

        // 하위코드가 있는 부모 삭제가 업무 예외로 차단되는지 확인한다
        BusinessException exception = assertThrows(BusinessException.class
                                                   , () -> codeManageService.delComdCode("GROUP", "PARENT"));
        // 하위 세부코드 존재 오류가 호출자에게 정확히 전달되는지 확인한다
        assertEquals(ResultEnum.CODE_DETAIL_HAS_CHILDREN, exception.getResultEnum());
        // 부모 세부코드가 삭제되지 않았는지 확인한다
        verify(codeMapper, never()).delComdCode("GROUP", "PARENT");
    }

    /**
     * 계층 검증 테스트에 사용할 세부코드를 생성한다
     *
     * @author SeungHyeon.Kang
     * @param comdCode 세부코드
     * @param upprCode 상위 세부코드
     * @return 필수 표시명이 설정된 세부코드
     */
    private CodeVO createCode(String comdCode, String upprCode) {
        // 서비스 필수값 검증을 통과할 세부코드를 생성한다
        CodeVO code = new CodeVO();
        // 테스트할 세부코드 식별자를 설정한다
        code.setComdCode(comdCode);
        // 필수 표시명을 설정한다
        code.setComdName("테스트 코드");
        // 필수 영문 표시명을 설정한다
        code.setComdEnnm("Test code");
        // 검증할 상위 세부코드를 설정한다
        code.setUpprCode(upprCode);
        // 완성된 테스트 세부코드를 반환한다
        return code;
    }

    /**
     * 코드 변경 감사정보에 사용할 관리자 세션을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 관리자 번호가 설정된 세션
     */
    private AdminSessionVO createAdminSession() {
        // 테스트용 관리자 세션을 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 감사정보에 기록할 관리자 번호를 설정한다
        admin.setAdmnNumb(1L);
        // 완성된 관리자 세션을 반환한다
        return admin;
    }
}
