package org.sadari.admin.sadariadmin.adminauth.vo;

import lombok.Data;
import org.sadari.admin.sadariadmin.authgroup.vo.AuthGroupVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;

import java.util.List;

/**
 * fileName       : AdminAuthManageVO
 * author         : SeungHyeon.Kang
 * date           : 2026-07-27
 * description    : 관리자 권한 부여 화면 데이터 VO
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-27        SeungHyeon.Kang    최초 생성
 */
@Data
public class AdminAuthManageVO {

    /** 관리자 목록 */
    private PageData<AdminAuthVO> admins;

    /** 권한그룹 목록 */
    private List<AuthGroupVO> authGroups;
}
