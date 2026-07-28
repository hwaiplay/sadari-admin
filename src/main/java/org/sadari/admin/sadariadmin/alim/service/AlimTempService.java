package org.sadari.admin.sadariadmin.alim.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.alim.vo.AlimTempVO;

import java.util.List;

/**
 * fileName       : AlimTempService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-24
 * description    : AlimTempService role
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-24        SeungHyeon.Kang    최초 생성
 */public interface AlimTempService {

    /**
     * 알림 템플릿 목록 조회
     * @author SeungHyeon.Kang
     * @param admin
     * @return
     */
    List<AlimTempVO> getAlimTempList(AdminSessionVO admin);

    /**
     * 알림 템플릿 상세 조회
     * @author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param admin
     * @return
     */
    AlimTempVO getAlimTempDtl(String alimSitu, String tempCode, AdminSessionVO admin);

    /**
     * 알림 템플릿 중복 여부 조회
     * @author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @param admin
     * @return
     */
    boolean getAlimTempDuplicate(String alimSitu, String tempCode, AdminSessionVO admin);

    /**
     * 알림 템플릿 등록
     * @author SeungHyeon.Kang
     * @param alimTemp
     * @param admin
     * @return
     */
    AlimTempVO setAlimTemp(AlimTempVO alimTemp, AdminSessionVO admin);

    /**
     * 알림 템플릿 수정
     * @author SeungHyeon.Kang
     * @param oldAlimSitu
     * @param oldTempCode
     * @param alimTemp
     * @param admin
     * @return
     */
    AlimTempVO uptAlimTemp(String oldAlimSitu, String oldTempCode, AlimTempVO alimTemp, AdminSessionVO admin);
}
