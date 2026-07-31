package org.sadari.admin.sadariadmin.adminauth.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthManageVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthSearchVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthVO;

import java.util.List;

/**
 * fileName       : AdminAuthManageService
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : AdminAuthManageService role
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 * 2026-07-31        SeungHyeon.Kang    관리자 권한 목록 검색 조건 추가
 */public interface AdminAuthManageService {

    /** 관리자 권한 부여 화면 데이터 조회 */
    AdminAuthManageVO getAdminAuthManage(AdminAuthSearchVO search, AdminSessionVO admin);

    /** 관리자 권한 일괄 수정 */
    AdminAuthManageVO uptAdminAuthList(List<AdminAuthVO> admins, AdminAuthSearchVO search
                                    , AdminSessionVO admin);
}
