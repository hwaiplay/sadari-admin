package org.sadari.admin.sadariadmin.authgroup.service;

import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupVO;

import java.util.List;

public interface AuthGroupService {

    /** 권한그룹 목록 조회 */
    List<AuthGroupVO> getAuthGroupList(AdminSessionVO admin);

    /** 권한그룹 상세 조회 */
    AuthGroupVO getAuthGroup(String authCode, AdminSessionVO admin);

    /** 권한 코드 중복 확인 */
    boolean isDuplicate(String authCode, AdminSessionVO admin);

    /** 권한그룹 등록 */
    AuthGroupVO setAuthGroup(AuthGroupVO authGroup, AdminSessionVO admin);

    /** 권한그룹 수정 */
    AuthGroupVO uptAuthGroup(AuthGroupVO authGroup, AdminSessionVO admin);

    /** 권한그룹 삭제 */
    void delAuthGroup(String authCode, AdminSessionVO admin);
}
