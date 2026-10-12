package org.sadari.admin.sadariadmin.serviceinfo.service.impl;

import static org.sadari.admin.sadariadmin.common.constant.Constant.NO;
import static org.sadari.admin.sadariadmin.common.constant.Constant.SVIF_CATE;
import static org.sadari.admin.sadariadmin.common.constant.Constant.YES;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.exception.BusinessException;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.common.pagination.PageRequest;
import org.sadari.admin.sadariadmin.common.result.ResultEnum;
import org.sadari.admin.sadariadmin.common.util.StringUtil;
import org.sadari.admin.sadariadmin.notice.service.NoticeImageService;
import org.sadari.admin.sadariadmin.serviceinfo.mapper.ServiceInfoMapper;
import org.sadari.admin.sadariadmin.serviceinfo.service.ServiceInfoService;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoSearchVO;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoVO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * fileName       : ServiceInfoServiceImpl
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 카테고리를 서비스 정보 식별자로 고정하고 수정 이력을 버전으로 관리한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 * 2026-10-12        SeungHyeon.Kang    공통 본문 정책과 커밋 후 이미지 정리 적용
 */
@Service
@Transactional(readOnly = true)
public class ServiceInfoServiceImpl implements ServiceInfoService {

    // 서비스 정보 제목의 최대 UTF-8 바이트
    private static final int TITLE_MAX_BYTES = 300;
    // 서비스 정보 본문의 최대 UTF-8 바이트
    private static final int CONTENT_MAX_BYTES = 1_000_000;
    // 서비스 정보 데이터 접근 객체
    private final ServiceInfoMapper serviceInfoMapper;
    // Summernote 본문 이미지 저장소 서비스
    private final NoticeImageService noticeImageService;

    /**
     * 서비스 정보 서비스에 데이터와 이미지 저장 기능을 주입한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfoMapper 서비스 정보 버전 데이터 접근 객체
     * @param noticeImageService Summernote 이미지 저장 서비스
     */
    public ServiceInfoServiceImpl(ServiceInfoMapper serviceInfoMapper, NoticeImageService noticeImageService) {
        this.serviceInfoMapper = serviceInfoMapper;
        this.noticeImageService = noticeImageService;
    }

    /**
     * 카테고리별 현재 배포본 또는 최신 초안 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 서비스 정보 검색과 페이징 조건
     * @param admin 접근하는 인증 관리자 정보
     * @return 카테고리별 대표 서비스 정보 버전 목록
     */
    @Override
    public PageData<ServiceInfoVO> getServiceInfoList(ServiceInfoSearchVO search, AdminSessionVO admin) {
        // 관리자 전용 서비스 정보 목록 접근 권한을 확인한다.
        checkLogin(admin);
        // 공통 페이지 범위로 목록 시작 위치와 조회 건수를 계산한다.
        PageRequest pageRequest = new PageRequest(search.getPage());
        search.setStartRow(pageRequest.getStartRow() - 1);
        search.setEndRow(PageRequest.PAGE_SIZE);
        search.setDplyYsno(YES);
        // 배포본 우선 목록과 전체 건수를 함께 반환한다.
        return PageData.of(serviceInfoMapper.getServiceInfoList(search)
                         , serviceInfoMapper.getServiceInfoListCnt(search), pageRequest);
    }

    /**
     * 카테고리와 버전에 일치하는 서비스 정보 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 조회할 버전 번호
     * @param admin 접근하는 인증 관리자 정보
     * @return 지정 버전의 서비스 정보 상세
     */
    @Override
    public ServiceInfoVO getServiceInfoDtl(String cateCode, Integer versNumb, AdminSessionVO admin) {
        // 관리자 전용 서비스 정보 상세 접근 권한을 확인한다.
        checkLogin(admin);
        // 카테고리와 버전 형식이 올바른지 확인한다.
        validateKey(cateCode, versNumb);
        // 존재가 확인된 서비스 정보 상세를 반환한다.
        return getRequiredServiceInfo(cateCode, versNumb);
    }

    /**
     * 카테고리에 속한 서비스 정보 버전 이력을 최신순으로 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param admin 접근하는 인증 관리자 정보
     * @return 카테고리에 속한 전체 서비스 정보 버전
     */
    @Override
    public List<ServiceInfoVO> getServiceInfoVersionList(String cateCode, AdminSessionVO admin) {
        // 관리자 전용 버전 이력 접근 권한을 확인한다.
        checkLogin(admin);
        // 카테고리 코드가 비어 있으면 전체 버전 조회를 차단한다.
        validateCategory(cateCode);
        // 선택한 카테고리의 버전 이력을 최신순으로 조회한다.
        List<ServiceInfoVO> versions = serviceInfoMapper.getServiceInfoVersionList(SVIF_CATE, cateCode);
        // 등록된 글이 없는 카테고리는 찾기 실패로 처리한다.
        if (StringUtil.isEmpty(versions)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        // 최신 버전부터 정렬된 서비스 정보 이력을 반환한다.
        return versions;
    }

    /**
     * 글이 없는 카테고리에 최초 서비스 정보 버전을 등록한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfo 최초 버전으로 저장할 서비스 정보
     * @param admin 등록하는 인증 관리자 정보
     * @return 등록된 최초 서비스 정보 버전
     */
    @Override
    @Transactional
    public ServiceInfoVO setServiceInfo(ServiceInfoVO serviceInfo, AdminSessionVO admin) {
        // 서비스 정보 등록 권한과 입력값을 검증한다.
        checkLogin(admin);
        normalizeAndValidate(serviceInfo);
        // 카테고리를 논리 글 식별자로 사용하여 두 번째 글 등록을 차단한다.
        if (serviceInfoMapper.getServiceInfoCnt(SVIF_CATE, serviceInfo.getCateCode()) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.SERVICE_INFO_INVALID);
        }

        serviceInfo.setCateCgrp(SVIF_CATE);
        serviceInfo.setVersNumb(1);
        serviceInfo.setDplyYsno(NO);
        serviceInfo.setRegiAdmn(admin.getAdmnNumb());
        serviceInfo.setRegiDate(null);
        serviceInfo.setUpdtAdmn(null);
        // 최초 버전이 한 행으로 등록되지 않으면 저장을 실패 처리한다.
        if (serviceInfoMapper.setServiceInfo(serviceInfo) != 1) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }

        // 등록된 카테고리의 최초 서비스 정보 버전을 반환한다.
        return getRequiredServiceInfo(serviceInfo.getCateCode(), 1);
    }

    /**
     * 미배포 버전은 수정하고 배포된 버전은 다음 초안 버전으로 저장한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 수정 기준 버전 번호
     * @param serviceInfo 저장할 제목과 HTML 본문
     * @param admin 수정하는 인증 관리자 정보
     * @return 저장된 초안 버전
     */
    @Override
    @Transactional
    public ServiceInfoVO uptServiceInfoVersion(String cateCode, Integer versNumb, ServiceInfoVO serviceInfo
                                              , AdminSessionVO admin) {
        // 수정 권한과 복합키 및 입력 내용을 순서대로 검증한다.
        checkLogin(admin);
        validateKey(cateCode, versNumb);
        serviceInfo.setCateCode(cateCode);
        normalizeAndValidate(serviceInfo);
        // 카테고리 단위 잠금으로 버전 번호 증가를 직렬화한다.
        Integer latestVersion = serviceInfoMapper.getLatestVersionForUpdate(SVIF_CATE, cateCode);
        // 잠금 대상이 없으면 잘못된 버전을 생성하지 않는다.
        if (StringUtil.isEmpty(latestVersion)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        // 선택 버전의 현재 배포 상태를 서버에서 다시 조회한다.
        ServiceInfoVO source = getRequiredServiceInfo(cateCode, versNumb);
        // 미배포 초안은 같은 버전에 반복 저장한다.
        if (!YES.equals(source.getDplyYsno())) {
            serviceInfo.setCateCgrp(SVIF_CATE);
            serviceInfo.setVersNumb(versNumb);
            serviceInfo.setDplyYsno(NO);
            serviceInfo.setUpdtAdmn(admin.getAdmnNumb());
            // 동시 배포로 수정 조건이 달라지면 현재 배포본을 보호한다.
            if (serviceInfoMapper.uptServiceInfo(serviceInfo) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, ResultEnum.SERVICE_INFO_INVALID);
            }

            // 같은 버전에 저장된 서비스 정보 상세를 반환한다.
            return getRequiredServiceInfo(cateCode, versNumb);
        }

        // 배포본 수정은 기존 공개 내용을 보존하도록 새 버전을 만든다.
        ServiceInfoVO originalAudit = serviceInfoMapper.getServiceInfoOrigAudit(SVIF_CATE, cateCode);
        // 최초 등록 감사정보가 없으면 불완전한 버전 생성을 차단한다.
        if (StringUtil.isEmpty(originalAudit)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        serviceInfo.setCateCgrp(SVIF_CATE);
        serviceInfo.setVersNumb(latestVersion + 1);
        serviceInfo.setDplyYsno(NO);
        serviceInfo.setRegiAdmn(originalAudit.getRegiAdmn());
        serviceInfo.setRegiDate(originalAudit.getRegiDate());
        serviceInfo.setUpdtAdmn(admin.getAdmnNumb());
        // 다음 버전이 한 행으로 등록되지 않으면 저장을 실패 처리한다.
        if (serviceInfoMapper.setServiceInfo(serviceInfo) != 1) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ResultEnum.COMMON_SERVER_ERROR);
        }

        // 배포본에서 분기된 새 서비스 정보 버전을 반환한다.
        return getRequiredServiceInfo(cateCode, serviceInfo.getVersNumb());
    }

    /**
     * 선택한 서비스 정보 버전을 카테고리의 현재 배포본으로 전환한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 배포할 버전 번호
     * @param admin 배포하는 인증 관리자 정보
     * @return 현재 배포본으로 전환된 서비스 정보 버전
     */
    @Override
    @Transactional
    public ServiceInfoVO uptServiceInfoDeploy(String cateCode, Integer versNumb, AdminSessionVO admin) {
        // 서비스 정보 배포 권한과 대상 복합키를 검증한다.
        checkLogin(admin);
        validateKey(cateCode, versNumb);
        // 카테고리 단위 잠금 대상으로 존재 여부를 확인한다.
        if (StringUtil.isEmpty(serviceInfoMapper.getLatestVersionForUpdate(SVIF_CATE, cateCode))) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        getRequiredServiceInfo(cateCode, versNumb);
        serviceInfoMapper.uptServiceInfoDeployOff(SVIF_CATE, cateCode, NO, YES);
        // 선택한 한 버전만 현재 배포본으로 설정한다.
        if (serviceInfoMapper.uptServiceInfoDeployOn(
                SVIF_CATE, cateCode, versNumb, admin.getAdmnNumb(), YES) != 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        // 새로 배포된 서비스 정보 상세를 반환한다.
        return getRequiredServiceInfo(cateCode, versNumb);
    }

    /**
     * 카테고리의 서비스 정보 전체 버전과 본문 이미지를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 삭제할 서비스 정보 카테고리 코드
     * @param admin 삭제하는 인증 관리자 정보
     */
    @Override
    @Transactional
    public void delServiceInfo(String cateCode, AdminSessionVO admin) {
        // 서비스 정보 삭제 권한과 카테고리 형식을 검증한다.
        checkLogin(admin);
        validateCategory(cateCode);
        // 파일 경로를 잃기 전에 모든 버전 본문을 조회한다.
        List<String> contents = serviceInfoMapper.getServiceInfoContentList(SVIF_CATE, cateCode);
        // 등록된 버전이 없으면 삭제를 시작하지 않는다.
        if (StringUtil.isEmpty(contents)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        // 여러 버전에서 중복 참조한 이미지 경로를 한 번만 삭제한다.
        Set<String> imagePaths = noticeImageService.getNoticeImagePaths(contents);
        // 카테고리의 모든 버전이 삭제되지 않으면 파일 삭제를 시작하지 않는다.
        if (serviceInfoMapper.delServiceInfo(SVIF_CATE, cateCode) < 1) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        // 본문 행 삭제가 커밋된 뒤 실제 이미지 파일을 정리한다
        noticeImageService.setImageCleanupOnCommit(imagePaths);
    }

    /** 서비스 정보 카테고리와 제목 및 본문을 정규화하고 저장 범위를 검증한다. */
    private void normalizeAndValidate(ServiceInfoVO serviceInfo) {
        // 요청 객체 자체가 없으면 필수 항목을 확인할 수 없으므로 중단한다.
        if (StringUtil.isEmpty(serviceInfo)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.SERVICE_INFO_INVALID);
        }

        String categoryCode = StringUtil.isEmpty(serviceInfo.getCateCode()) ? "" : serviceInfo.getCateCode().trim();
        String title = StringUtil.isEmpty(serviceInfo.getSvciTitl()) ? "" : serviceInfo.getSvciTitl().trim();
        String englishTitle = StringUtil.isEmpty(serviceInfo.getSvciEntl()) ? "" : serviceInfo.getSvciEntl().trim();
        String content = noticeImageService.sanitizeContentHtml(serviceInfo.getSvciCntn());
        String englishContent = noticeImageService.sanitizeContentHtml(serviceInfo.getSvciEnct());
        Document contentDocument = Jsoup.parseBodyFragment(content);
        Document englishContentDocument = Jsoup.parseBodyFragment(englishContent);
        boolean hasContent = !contentDocument.text().isBlank() || !contentDocument.select("img").isEmpty();
        boolean hasEnglishContent = !englishContentDocument.text().isBlank()
                || !englishContentDocument.select("img").isEmpty();
        // 공통코드와 필수값 및 저장 길이가 모두 정상일 때만 저장한다.
        if (categoryCode.isBlank() || serviceInfoMapper.getServiceInfoCategoryCnt(SVIF_CATE, categoryCode, YES) != 1
                || title.isBlank() || englishTitle.isBlank() || !hasContent || !hasEnglishContent
                || title.getBytes(StandardCharsets.UTF_8).length > TITLE_MAX_BYTES
                || englishTitle.getBytes(StandardCharsets.UTF_8).length > TITLE_MAX_BYTES
                || content.getBytes(StandardCharsets.UTF_8).length > CONTENT_MAX_BYTES
                || englishContent.getBytes(StandardCharsets.UTF_8).length > CONTENT_MAX_BYTES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.SERVICE_INFO_INVALID);
        }

        serviceInfo.setCateCode(categoryCode);
        serviceInfo.setSvciTitl(title);
        serviceInfo.setSvciEntl(englishTitle);
        serviceInfo.setSvciCntn(content);
        serviceInfo.setSvciEnct(englishContent);
    }

    /** 서비스 정보 카테고리와 버전 복합키 형식을 검증한다. */
    private void validateKey(String cateCode, Integer versNumb) {
        // 빈 카테고리나 양수가 아닌 버전은 조회와 변경 조건에 사용하지 않는다.
        if (StringUtil.isEmpty(cateCode) || StringUtil.isEmpty(versNumb) || versNumb < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.SERVICE_INFO_INVALID);
        }
    }

    /** 서비스 정보 카테고리 형식을 검증한다. */
    private void validateCategory(String cateCode) {
        // 빈 카테고리는 전체 행 변경 조건으로 사용하지 않는다.
        if (StringUtil.isEmpty(cateCode)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, ResultEnum.SERVICE_INFO_INVALID);
        }
    }

    /** 지정 카테고리와 버전의 서비스 정보가 없으면 업무 예외를 발생시킨다. */
    private ServiceInfoVO getRequiredServiceInfo(String cateCode, Integer versNumb) {
        ServiceInfoVO serviceInfo = serviceInfoMapper.getServiceInfoDtl(SVIF_CATE, cateCode, versNumb);
        // 요청한 서비스 정보 버전이 없으면 찾기 실패로 처리한다.
        if (StringUtil.isEmpty(serviceInfo)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, ResultEnum.SERVICE_INFO_NOT_FOUND);
        }

        // 존재가 확인된 서비스 정보 버전을 반환한다.
        return serviceInfo;
    }

    /** 인증된 관리자 번호가 있는지 확인한다. */
    private void checkLogin(AdminSessionVO admin) {
        // 관리자 세션이나 관리자 번호가 없으면 관리 API 접근을 차단한다.
        if (StringUtil.isEmpty(admin) || StringUtil.isEmpty(admin.getAdmnNumb())) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, ResultEnum.AUTH_REQUIRED_LOGIN);
        }
    }
}
