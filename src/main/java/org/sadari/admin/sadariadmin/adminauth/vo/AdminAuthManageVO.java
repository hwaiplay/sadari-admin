package org.sadari.admin.sadariadmin.adminauth.vo;

import lombok.Data;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupVO;

import java.util.List;

/** 관리자 권한 부여 화면 데이터 VO */
@Data
public class AdminAuthManageVO {

    /** 관리자 목록 */
    private List<AdminAuthVO> admins;

    /** 권한그룹 목록 */
    private List<AuthGroupVO> authGroups;
}
