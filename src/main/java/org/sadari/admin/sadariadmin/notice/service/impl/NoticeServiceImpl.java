package org.sadari.admin.sadariadmin.notice.service.impl;

import static org.sadari.admin.sadariadmin.common.constant.Constant.NO;
import static org.sadari.admin.sadariadmin.common.constant.Constant.NOTI_CATE;
import static org.sadari.admin.sadariadmin.common.constant.Constant.VIEW_TYPE_NOTICE;
import static org.sadari.admin.sadariadmin.common.constant.Constant.YES;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.notice.mapper.NoticeMapper;
import org.sadari.admin.sadariadmin.notice.service.NoticeImageService;
import org.sadari.admin.sadariadmin.notice.service.NoticeService;
import org.sadari.admin.sadariadmin.notice.vo.NoticeSearchVO;
import org.sadari.admin.sadariadmin.notice.vo.NoticeVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : NoticeServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 공지사항을 불변 버전으로 저장하고 한 버전만 배포하며 전체 삭제를 처리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    버전 이력 조회와 종속 데이터 및 파일 삭제 추가
 * 2026-08-08        SeungHyeon.Kang    현재 배포 상태 기준 공지 수정 방식 분리
 * 2026-08-08        SeungHyeon.Kang    관리자 목록의 현재 배포 버전 우선 조회 적용
 */
@Service
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    // 공지 제목의 최대 UTF-8 바이트
    private static final int TITLE_MAX_BYTES = 300;
    // 공지 본문의 최대 UTF-8 바이트
    private static final int CONTENT_MAX_BYTES = 1_000_000;
    // 공지 전용 저장 경로로 생성된 이미지만 허용하는 URL 형식
    private static final String NOTICE_IMAGE_PATH = "^/uploads/notice/[0-9]{6}/[0-9a-fA-F-]{36}\\.(jpg|png)$";

    // 공지사항 데이터 접근 객체
    private final NoticeMapper noticeMapper;
    // 공지사항 전용 이미지 저장소 서비스
    private final NoticeImageService noticeImageService;

    /**
     * 공지사항 서비스에 데이터 접근 기능을 주입한다.
     *
     * @param noticeMapper 공지사항 데이터 접근 객체
     * @param noticeImageService 공지사항 전용 이미지 저장소 서비스
     */
    public NoticeServiceImpl(NoticeMapper noticeMapper, NoticeImageService noticeImageService) {
        this.noticeMapper = noticeMapper;
        this.noticeImageService = noticeImageService;
    }

    @Override
    public PageData<NoticeVO> getNoticeList(NoticeSearchVO search, AdminSessionVO admin) {
        checkLogin(admin);
        PageRequest pageRequest = new PageRequest(search.getPage());
        search.setStartRow(pageRequest.getStartRow() - 1);
        search.setEndRow(PageRequest.PAGE_SIZE);
        // 요청값과 무관하게 서버 공통코드로 현재 배포 버전 선택 기준을 설정한다.
        search.setDplyYsno(YES);
        return PageData.of(noticeMapper.getNoticeList(search), noticeMapper.getNoticeListCnt(search), pageRequest);
    }

    @Override
    public NoticeVO getNoticeDtl(Long notiNumb, Integer versNumb, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(notiNumb, versNumb);
        return getRequiredNotice(notiNumb, versNumb);
    }

    @Override
    public List<NoticeVO> getNoticeVersionList(Long notiNumb, AdminSessionVO admin) {
        // 관리자 전용 버전 이력 접근에 인증을 요구한다.
        checkLogin(admin);
        // 잘못된 주키로 전체 버전 조회가 실행되지 않게 차단한다.
        validateNoticeNumber(notiNumb);
        // 상세 하단 표에 표시할 모든 버전의 관리 정보를 조회한다.
        List<NoticeVO> versions = noticeMapper.getNoticeVersionList(notiNumb);
        // 존재하지 않는 공지 번호는 빈 이력 대신 명시적인 찾기 실패로 처리한다.
        if (versions.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }
        // 최신 버전부터 정렬된 공지 버전 이력을 반환한다.
        return versions;
    }

    @Override
    @Transactional
    public NoticeVO setNotice(NoticeVO notice, AdminSessionVO admin) {
        checkLogin(admin);
        normalizeAndValidate(notice);
        notice.setNotiNumb(null);
        notice.setVersNumb(1);
        notice.setCateCgrp(NOTI_CATE);
        notice.setDplyYsno(NO);
        notice.setRegiAdmn(admin.getAdmnNumb());
        notice.setRegiDate(null);
        notice.setUpdtAdmn(null);
        if (noticeMapper.setNotice(notice) != 1 || notice.getNotiNumb() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }
        return getRequiredNotice(notice.getNotiNumb(), 1);
    }

    /**
     * 현재 배포 상태를 서버에서 판정하여 같은 버전을 수정하거나 다음 버전을 생성한다
     *
     * @author SeungHyeon.Kang
     * @param notiNumb 수정할 공지사항 번호
     * @param versNumb 수정 기준 버전 번호
     * @param notice 수정할 공지사항 내용
     * @param admin 수정 요청 관리자 세션
     * @return 수정되거나 새로 생성된 공지사항 버전
     */
    @Override
    @Transactional
    public NoticeVO uptNoticeVersion(Long notiNumb, Integer versNumb, NoticeVO notice, AdminSessionVO admin) {
        // 수정 권한을 관리자 로그인 세션으로 검증한다.
        checkLogin(admin);
        // 유효한 공지번호와 버전만 저장 분기에 진입하도록 검증한다.
        validateKey(notiNumb, versNumb);
        // 저장할 제목과 본문 및 분류값을 정규화하고 검증한다.
        normalizeAndValidate(notice);
        // 공지 단위 잠금으로 수정과 배포가 동시에 수행될 때의 버전 판정을 직렬화한다.
        Integer latestVersion = noticeMapper.getLatestVersionForUpdate(notiNumb);
        // 잠금 대상 공지가 없으면 잘못된 버전 행을 생성하지 않고 조회 실패로 처리한다.
        if (latestVersion == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }

        // 화면 상태를 신뢰하지 않고 잠금 이후 선택 버전의 현재 배포 상태를 다시 확인한다.
        NoticeVO source = getRequiredNotice(notiNumb, versNumb);
        // 현재 배포 플래그만 새 버전 생성 기준으로 사용한다.
        boolean isDeployed = YES.equals(source.getDplyYsno());
        // 현재 미배포 버전은 반복 저장으로 불필요한 버전이 생성되지 않게 같은 행을 수정한다.
        if (!isDeployed) {
            // 수정할 공지사항 번호를 기존 복합키에 유지한다.
            notice.setNotiNumb(notiNumb);
            // 수정할 버전 번호를 기존 복합키에 유지한다.
            notice.setVersNumb(versNumb);
            // 공지사항 카테고리 공통코드 그룹을 서버 기준으로 설정한다.
            notice.setCateCgrp(NOTI_CATE);
            // 같은 버전 수정 대상이 미배포 상태임을 Mapper 조건에 설정한다.
            notice.setDplyYsno(NO);
            // 수정 작업을 수행한 관리자 번호를 관리 컬럼에 설정한다.
            notice.setUpdtAdmn(admin.getAdmnNumb());
            // 동시 상태 변경으로 미배포 조건을 충족하지 못하면 배포 내용을 덮어쓰지 않도록 중단한다.
            if (noticeMapper.uptNotice(notice) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.NOTICE_INVALID);
            }

            // 같은 버전에 반영된 최신 공지사항 상세를 반환한다.
            return getRequiredNotice(notiNumb, versNumb);
        }

        // 현재 배포 중인 버전은 노출 내용을 보존하고 새 초안을 MAX + 1 버전으로 생성한다.
        NoticeVO originalAudit = noticeMapper.getNoticeOriginalAudit(notiNumb);
        // 최초 등록 관리 정보가 없으면 불완전한 이력을 생성하지 않도록 중단한다.
        if (originalAudit == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }

        // 새 버전을 기존 공지사항 번호에 연결한다.
        notice.setNotiNumb(notiNumb);
        // 공지 잠금 시점의 최대 버전 다음 번호를 새 버전에 설정한다.
        notice.setVersNumb(latestVersion + 1);
        // 공지사항 카테고리 공통코드 그룹을 서버 기준으로 설정한다.
        notice.setCateCgrp(NOTI_CATE);
        // 새 버전은 관리자가 배포하기 전까지 사용자에게 노출되지 않도록 설정한다.
        notice.setDplyYsno(NO);
        // 최초 등록 관리자 번호를 새 버전에도 유지한다.
        notice.setRegiAdmn(originalAudit.getRegiAdmn());
        // 최초 등록 일시를 새 버전에도 유지한다.
        notice.setRegiDate(originalAudit.getRegiDate());
        // 새 버전을 만든 관리자 번호를 수정 관리 컬럼에 설정한다.
        notice.setUpdtAdmn(admin.getAdmnNumb());
        // 새 버전 행 생성에 실패하면 현재 배포본을 유지한 채 저장 트랜잭션을 실패 처리한다.
        if (noticeMapper.setNotice(notice) != 1) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }

        // 현재 배포본에서 분기되어 생성된 새 공지사항 버전을 반환한다.
        return getRequiredNotice(notiNumb, notice.getVersNumb());
    }

    @Override
    @Transactional
    public NoticeVO uptNoticeDeploy(Long notiNumb, Integer versNumb, AdminSessionVO admin) {
        checkLogin(admin);
        validateKey(notiNumb, versNumb);
        if (noticeMapper.getLatestVersionForUpdate(notiNumb) == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }
        getRequiredNotice(notiNumb, versNumb);
        noticeMapper.uptNoticeDeployOff(notiNumb, NO, YES);
        if (noticeMapper.uptNoticeDeployOn(notiNumb, versNumb, admin.getAdmnNumb(), YES) != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }
        noticeMapper.delNoticeView(VIEW_TYPE_NOTICE, notiNumb);
        return getRequiredNotice(notiNumb, versNumb);
    }

    @Override
    @Transactional
    public void delNotice(Long notiNumb, AdminSessionVO admin) {
        // 공지 운영 데이터 삭제는 인증된 관리자에게만 허용한다.
        checkLogin(admin);
        // 잘못된 주키가 종속 데이터 삭제 조건에 사용되지 않게 차단한다.
        validateNoticeNumber(notiNumb);
        // 실제 파일 경로를 잃기 전에 모든 버전 본문을 먼저 조회한다.
        List<String> contents = noticeMapper.getNoticeContentList(notiNumb);
        // 삭제할 공지가 없으면 저장소 작업을 시작하지 않는다.
        if (contents.isEmpty()) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }
        // 같은 이미지가 여러 버전에 포함되어도 저장소 삭제는 한 번만 수행한다.
        Set<String> imagePaths = collectNoticeImagePaths(contents);
        // 모든 계정 상태에서 생성된 공지 읽음 이력을 삭제한다.
        noticeMapper.delNoticeView(VIEW_TYPE_NOTICE, notiNumb);
        // 공지 주키에 속한 전체 버전이 삭제되지 않으면 파일 삭제를 시작하지 않는다.
        if (noticeMapper.delNotice(notiNumb) < 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }
        // 저장소 실패를 데이터베이스 롤백으로 연결하기 위해 트랜잭션 안에서 실제 파일을 삭제한다.
        try {
            noticeImageService.delNoticeImages(imagePaths);
        // 일부 파일만 남는 상황에서 공지 행이 사라지지 않도록 업무 예외로 변환한다.
        } catch (IOException exception) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }
    }

    /** 모든 공지 버전 본문에서 공지 전용 이미지 공개 경로를 중복 없이 수집한다. */
    private Set<String> collectNoticeImagePaths(List<String> contents) {
        Set<String> imagePaths = new LinkedHashSet<>();
        // 과거 버전에만 남아 있는 이미지도 삭제 대상에 포함한다.
        for (String content : contents) {
            // 외부 파일과 다른 업무 디렉터리는 저장소 삭제 대상에서 제외한다.
            for (Element image : Jsoup.parseBodyFragment(content == null ? "" : content).select("img[src]")) {
                String imagePath = image.attr("src");
                // 공지 업로드 규격으로 발급한 상대 경로만 수집한다.
                if (imagePath.matches(NOTICE_IMAGE_PATH)) {
                    imagePaths.add(imagePath);
                }

            }

        }
        // 중복이 제거된 공지 전용 이미지 공개 경로를 반환한다.
        return imagePaths;
    }

    /** 입력 HTML에서 실행 가능한 요소와 외부 이미지를 제거한다. */
    private String sanitizeHtml(String html) {
        Safelist safelist = Safelist.relaxed()
                .addTags("div", "span", "hr", "table", "thead", "tbody", "tr", "th", "td")
                .addAttributes(":all", "class", "style")
                .addAttributes("a", "target", "rel")
                .addAttributes("img", "width", "height")
                .removeProtocols("img", "src", "http", "https");
        Document dirty = Jsoup.parseBodyFragment(html == null ? "" : html);
        Document clean = new Cleaner(safelist).clean(dirty);
        for (Element image : clean.select("img")) {
            if (!image.attr("src").matches(NOTICE_IMAGE_PATH)) {
                image.remove();
            }
        }
        for (Element styled : clean.select("[style]")) {
            String style = styled.attr("style").toLowerCase();
            if (style.contains("url(") || style.contains("expression") || style.contains("@import")) {
                styled.removeAttr("style");
            }
        }
        return clean.body().html().trim();
    }

    /** 제목과 본문을 정규화하고 저장 가능 범위를 검증한다. */
    private void normalizeAndValidate(NoticeVO notice) {
        if (notice == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_INVALID);
        }
        String categoryCode = notice.getCateCode() == null ? "" : notice.getCateCode().trim();
        String topFixed = notice.getTopxYsno() == null ? NO : notice.getTopxYsno().trim();
        String title = notice.getNotiTitl() == null ? "" : notice.getNotiTitl().trim();
        String content = sanitizeHtml(notice.getNotiCntn());
        Document contentDocument = Jsoup.parseBodyFragment(content);
        boolean hasContent = !contentDocument.text().isBlank() || !contentDocument.select("img").isEmpty();
        if (categoryCode.isBlank() || noticeMapper.getNoticeCategoryCnt(NOTI_CATE, categoryCode, YES) != 1
                || (!YES.equals(topFixed) && !NO.equals(topFixed)) || title.isBlank() || !hasContent
                || title.getBytes(StandardCharsets.UTF_8).length > TITLE_MAX_BYTES
                || content.getBytes(StandardCharsets.UTF_8).length > CONTENT_MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_INVALID);
        }
        notice.setNotiTitl(title);
        notice.setNotiCntn(content);
        notice.setCateCode(categoryCode);
        notice.setTopxYsno(topFixed);
    }

    /** 복합키 형식을 검증한다. */
    private void validateKey(Long notiNumb, Integer versNumb) {
        if (notiNumb == null || notiNumb < 1 || versNumb == null || versNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_INVALID);
        }
    }

    /** 공지사항 주키 형식을 검증한다. */
    private void validateNoticeNumber(Long notiNumb) {
        if (notiNumb == null || notiNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.NOTICE_INVALID);
        }
    }

    /** 공지사항 상세가 없으면 업무 예외를 발생시킨다. */
    private NoticeVO getRequiredNotice(Long notiNumb, Integer versNumb) {
        NoticeVO notice = noticeMapper.getNoticeDtl(notiNumb, versNumb);
        if (notice == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.NOTICE_NOT_FOUND);
        }
        return notice;
    }

    /** 관리자 인증 객체가 있는지 확인한다. */
    private void checkLogin(AdminSessionVO admin) {
        if (admin == null || admin.getAdmnNumb() == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
