package org.sadari.admin.sadariadmin.adminauth.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthManageVO;
import org.sadari.admin.sadariadmin.adminauth.vo.AdminAuthVO;

import java.util.List;

public interface AdminAuthManageService {

    /** 관리자 권한 부여 화면 데이터 조회 */
    AdminAuthManageVO getAdminAuthManage(AdminSessionVO admin);

    /** 관리자 권한 일괄 수정 */
    AdminAuthManageVO uptAdminAuthList(List<AdminAuthVO> admins, AdminSessionVO admin);
}
