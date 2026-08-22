package org.sadari.admin.sadariadmin.currentuser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.code.mapper.CodeMapper;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.currentuser.mapper.CurrentUserMapper;
import org.sadari.admin.sadariadmin.currentuser.service.CurrentUserService;
import org.sadari.admin.sadariadmin.currentuser.service.impl.CurrentUserServiceImpl;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserFileVO;
import org.sadari.admin.sadariadmin.currentuser.vo.CurrentUserVO;
import org.sadari.admin.sadariadmin.file.storage.FileStorage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * fileName       : CurrentUserModerationTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 현 사용자 상세의 프로필 정보 삭제와 파일 정리 정책을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    프로필 조치와 관련 신고 종결 검증
 */
@ExtendWith(MockitoExtension.class)
class CurrentUserModerationTests {

    // 현재 사용자 조회와 프로필 정보 수정 Mapper 대역
    @Mock
    private CurrentUserMapper currentUserMapper;

    // 현재 사용자 검색 공통코드 Mapper 대역
    @Mock
    private CodeMapper codeMapper;

    // 사용자 이미지 저장소 대역
    @Mock
    private FileStorage fileStorage;

    // 테스트할 현재 사용자 관리 서비스
    private CurrentUserService currentUserService;

    /**
     * 각 테스트에서 사용할 현재 사용자 관리 서비스를 생성한다
     *
     * @author SeungHyeon.Kang
     */
    @BeforeEach
    void setUp() {
        // Mapper와 저장소 대역으로 현재 사용자 관리 서비스를 생성한다
        currentUserService = new CurrentUserServiceImpl(currentUserMapper, codeMapper, fileStorage);
    }

    /**
     * 프로필 이미지 참조와 메타정보 및 내부 저장소 파일을 함께 제거하는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws Exception 파일 저장소 검증 실패
     */
    @Test
    void delUserProfileImage() throws Exception {
        // 현재 사용자에게 연결된 프로필 이미지 파일 메타정보를 생성한다
        CurrentUserFileVO userFile = new CurrentUserFileVO();
        // 삭제할 파일 메타정보의 번호를 설정한다
        userFile.setFileNumb(30L);
        // 저장소에서 삭제할 파일명을 설정한다
        userFile.setStorName("profile.jpg");
        // 프로필 업로드 규격에 맞는 공개 접근 경로를 설정한다
        userFile.setFilePath("/uploads/profile/260822/profile.jpg");
        // 삭제 뒤 반환할 최신 현재 사용자 상세를 생성한다
        CurrentUserVO currentUser = new CurrentUserVO();
        // 최신 상세의 회원번호를 설정한다
        currentUser.setUserNumb(10L);
        // 잠금 조회한 프로필 파일을 반환하도록 설정한다
        when(currentUserMapper.getUserProfFileForUpdate(10L)).thenReturn(userFile);
        // 현재 프로필 파일 참조 제거가 성공하도록 설정한다
        when(currentUserMapper.delUserProfileImage(10L, 30L)).thenReturn(1);
        // 다른 사용자 이미지에서 참조하지 않는 파일 메타정보 삭제가 성공하도록 설정한다
        when(currentUserMapper.delUserFileIfUnref(30L)).thenReturn(1);
        // 조치 뒤 최신 현재 사용자 상세를 반환하도록 설정한다
        when(currentUserMapper.getCurrentUserDtl(10L)).thenReturn(currentUser);

        // 현 사용자 상세와 같은 경로로 프로필 사진 삭제를 실행한다
        CurrentUserVO result = currentUserService.delUserProfImage(10L, createAdminSession());

        // 내부 저장소의 검증된 프로필 객체가 삭제되는지 확인한다
        verify(fileStorage).delFile("profile/260822/profile.jpg");
        // 프로필 사진 초기화로 해결된 같은 사용자의 미처리 신고가 함께 종결되는지 확인한다
        verify(currentUserMapper).uptUserComplaints(
                Constant.CMPL_TARGET_PROFILE_IMAGE, 10L,
                "관리자 원본 수동 조치: 현 사용자 상세에서 프로필 사진 초기화. 관련 미처리 신고를 일괄 종결함.", 1L);
        // 프로필 조치 뒤 같은 회원의 최신 상세가 반환되는지 확인한다
        assertEquals(10L, result.getUserNumb());
    }

    /**
     * 값이 있는 현재 사용자의 한줄 소개를 NULL 처리하고 최신 상세를 반환하는지 확인한다
     *
     * @author SeungHyeon.Kang
     */
    @Test
    void delUserIntroduction() {
        // 한줄 소개 삭제 뒤 반환할 최신 현재 사용자 상세를 생성한다
        CurrentUserVO currentUser = new CurrentUserVO();
        // 최신 상세의 회원번호를 설정한다
        currentUser.setUserNumb(10L);
        // 한줄 소개 NULL 처리가 성공하도록 설정한다
        when(currentUserMapper.delUserIntroduction(10L)).thenReturn(1);
        // 조치 뒤 최신 현재 사용자 상세를 반환하도록 설정한다
        when(currentUserMapper.getCurrentUserDtl(10L)).thenReturn(currentUser);

        // 현 사용자 상세와 같은 경로로 한줄 소개 삭제를 실행한다
        CurrentUserVO result = currentUserService.delUserIntroduction(10L, createAdminSession());

        // 한줄소개 초기화로 해결된 같은 사용자의 미처리 신고가 함께 종결되는지 확인한다
        verify(currentUserMapper).uptUserComplaints(
                Constant.CMPL_TARGET_INTRODUCTION, 10L,
                "관리자 원본 수동 조치: 현 사용자 상세에서 한줄소개 초기화. 관련 미처리 신고를 일괄 종결함.", 1L);
        // 한줄 소개 조치 뒤 같은 회원의 최신 상세가 반환되는지 확인한다
        assertEquals(10L, result.getUserNumb());
    }

    /**
     * 프로필 정보 삭제 요청에 사용할 관리자 세션을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 로그인 검증을 통과할 관리자 세션
     */
    private AdminSessionVO createAdminSession() {
        // 서비스 로그인 검증에 사용할 관리자 세션을 생성한다
        AdminSessionVO admin = new AdminSessionVO();
        // 실제 관리자 메뉴 권한과 연결할 관리자 번호를 설정한다
        admin.setAdmnNumb(1L);
        // 완성된 테스트 관리자 세션을 반환한다
        return admin;
    }
}
