package org.sadari.admin.sadariadmin.welcome.service;

import static org.sadari.admin.sadariadmin.common.constant.Constant.NO;
import static org.sadari.admin.sadariadmin.common.constant.Constant.YES;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.welcome.mapper.WelcomePageMapper;
import org.sadari.admin.sadariadmin.welcome.vo.WelcomePageVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 관리자 웰컴페이지 문구와 이미지를 검증하고 불변 배포 버전으로 관리한다. */
@Service
@Transactional(readOnly = true)
public class WelcomePageServiceImpl implements WelcomePageService {

    // 소제목 최대 UTF-8 바이트
    private static final int SUBTITLE_MAX_BYTES = 200;
    // 제목 최대 UTF-8 바이트
    private static final int TITLE_MAX_BYTES = 300;
    // 설명 최대 UTF-8 바이트
    private static final int DESCRIPTION_MAX_BYTES = 1_000;
    // 웰컴페이지에서 허용하는 검증 완료 이미지 공개 경로
    private static final String IMAGE_PATH = "^/uploads/welcome/[0-9]{6}/[0-9a-fA-F-]{36}\\.(jpg|png|webp)$";
    // 웰컴페이지 데이터 접근 객체
    private final WelcomePageMapper welcomePageMapper;

    /** 웰컴페이지 서비스에 데이터 접근 기능을 주입한다. */
    public WelcomePageServiceImpl(WelcomePageMapper welcomePageMapper) {
        this.welcomePageMapper = welcomePageMapper;
    }

    /** 배포본 우선 웰컴페이지 목록을 조회한다. */
    @Override
    public List<WelcomePageVO> getWelcomePageList(AdminSessionVO admin) {
        checkLogin(admin);
        return welcomePageMapper.getWelcomePageList(YES);
    }

    /** 웰컴페이지 복합키 상세를 조회한다. */
    @Override
    public WelcomePageVO getWelcomePageDtl(Long wlcmNumb, Integer versNumb, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(wlcmNumb, versNumb);
        return getRequiredWelcomePage(wlcmNumb, versNumb);
    }

    /** 웰컴페이지의 모든 버전을 최신순으로 조회한다. */
    @Override
    public List<WelcomePageVO> getWelcomePageVersionList(Long wlcmNumb, AdminSessionVO admin) {
        checkLogin(admin);
        validateWelcomePageNumber(wlcmNumb);
        List<WelcomePageVO> versions = welcomePageMapper.getWelcomePageVersionList(wlcmNumb);
        if (versions.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
        return versions;
    }

    /** 웰컴페이지 최초 미배포 버전을 등록한다. */
    @Override
    @Transactional
    public WelcomePageVO setWelcomePage(WelcomePageVO welcomePage, AdminSessionVO admin) {
        checkLogin(admin);
        normalizeAndValidate(welcomePage);
        welcomePage.setWlcmNumb(null);
        welcomePage.setVersNumb(1);
        welcomePage.setDplyYsno(NO);
        welcomePage.setRegiAdmn(admin.getAdmnNumb());
        welcomePage.setRegiDate(null);
        welcomePage.setUpdtAdmn(null);
        if (welcomePageMapper.setWelcomePage(welcomePage) != 1 || welcomePage.getWlcmNumb() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }
        return getRequiredWelcomePage(welcomePage.getWlcmNumb(), 1);
    }

    /** 미배포 버전은 수정하고 배포 버전은 다음 초안으로 저장한다. */
    @Override
    @Transactional
    public WelcomePageVO uptWelcomePageVersion(Long wlcmNumb, Integer versNumb
                                             , WelcomePageVO welcomePage, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(wlcmNumb, versNumb);
        normalizeAndValidate(welcomePage);
        Integer latestVersion = welcomePageMapper.getLatestVersionForUpdate(wlcmNumb);
        if (latestVersion == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
        WelcomePageVO source = getRequiredWelcomePage(wlcmNumb, versNumb);
        welcomePage.setWlcmNumb(wlcmNumb);
        welcomePage.setUpdtAdmn(admin.getAdmnNumb());
        welcomePage.setDplyYsno(NO);
        if (!YES.equals(source.getDplyYsno())) {
            welcomePage.setVersNumb(versNumb);
            if (welcomePageMapper.uptWelcomePage(welcomePage) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.WELCOME_PAGE_INVALID);
            }
            return getRequiredWelcomePage(wlcmNumb, versNumb);
        }
        WelcomePageVO originalAudit = welcomePageMapper.getWelcomeOriginalAudit(wlcmNumb);
        if (originalAudit == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
        welcomePage.setVersNumb(latestVersion + 1);
        welcomePage.setRegiAdmn(originalAudit.getRegiAdmn());
        welcomePage.setRegiDate(originalAudit.getRegiDate());
        if (welcomePageMapper.setWelcomePage(welcomePage) != 1) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }
        return getRequiredWelcomePage(wlcmNumb, welcomePage.getVersNumb());
    }

    /** 선택한 버전을 현재 사용자 배포본으로 전환한다. */
    @Override
    @Transactional
    public WelcomePageVO uptWelcomePageDeploy(Long wlcmNumb, Integer versNumb, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(wlcmNumb, versNumb);
        if (welcomePageMapper.getLatestVersionForUpdate(wlcmNumb) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
        getRequiredWelcomePage(wlcmNumb, versNumb);
        welcomePageMapper.uptWelcomePageDeployOff(wlcmNumb, NO, YES);
        if (welcomePageMapper.uptWelcomePageDeployOn(wlcmNumb, versNumb, admin.getAdmnNumb(), YES) != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
        return getRequiredWelcomePage(wlcmNumb, versNumb);
    }

    /** 웰컴페이지 주키의 모든 버전을 삭제한다. */
    @Override
    @Transactional
    public void delWelcomePage(Long wlcmNumb, AdminSessionVO admin) {
        checkLogin(admin);
        validateWelcomePageNumber(wlcmNumb);
        if (welcomePageMapper.delWelcomePage(wlcmNumb) < 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
    }

    /** 화면 문구, 이미지 경로와 노출 순서를 정규화하고 검증한다. */
    private void normalizeAndValidate(WelcomePageVO welcomePage) {
        if (welcomePage == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.WELCOME_PAGE_INVALID);
        }
        String subtitle = welcomePage.getSubxTitl() == null ? "" : welcomePage.getSubxTitl().trim();
        String title = welcomePage.getMainTitl() == null ? "" : welcomePage.getMainTitl().trim();
        String description = welcomePage.getPageDesc() == null ? "" : welcomePage.getPageDesc().trim();
        String imageUrl = welcomePage.getImgeUrlx() == null ? null : welcomePage.getImgeUrlx().trim();
        if (subtitle.isBlank() || title.isBlank() || description.isBlank() || welcomePage.getSortOrdr() == null
                || welcomePage.getSortOrdr() < 1 || welcomePage.getSortOrdr() > 9999
                || subtitle.getBytes(StandardCharsets.UTF_8).length > SUBTITLE_MAX_BYTES
                || title.getBytes(StandardCharsets.UTF_8).length > TITLE_MAX_BYTES
                || description.getBytes(StandardCharsets.UTF_8).length > DESCRIPTION_MAX_BYTES
                || (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.matches(IMAGE_PATH))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.WELCOME_PAGE_INVALID);
        }
        welcomePage.setSubxTitl(subtitle);
        welcomePage.setMainTitl(title);
        welcomePage.setPageDesc(description);
        welcomePage.setImgeUrlx(imageUrl == null || imageUrl.isEmpty() ? null : imageUrl);
    }

    /** 웰컴페이지 복합키 형식을 검증한다. */
    private void validateKey(Long wlcmNumb, Integer versNumb) {
        if (wlcmNumb == null || wlcmNumb < 1 || versNumb == null || versNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.WELCOME_PAGE_INVALID);
        }
    }

    /** 웰컴페이지 주키 형식을 검증한다. */
    private void validateWelcomePageNumber(Long wlcmNumb) {
        if (wlcmNumb == null || wlcmNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.WELCOME_PAGE_INVALID);
        }
    }

    /** 웰컴페이지 상세가 없으면 업무 예외를 발생시킨다. */
    private WelcomePageVO getRequiredWelcomePage(Long wlcmNumb, Integer versNumb) {
        WelcomePageVO welcomePage = welcomePageMapper.getWelcomePageDtl(wlcmNumb, versNumb);
        if (welcomePage == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.WELCOME_PAGE_NOT_FOUND);
        }
        return welcomePage;
    }

    /** 관리자 인증 객체가 있는지 확인한다. */
    private void checkLogin(AdminSessionVO admin) {
        if (admin == null || admin.getAdmnNumb() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
