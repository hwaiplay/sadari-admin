package org.sadari.admin.sadariadmin.serviceinfo.service;

import java.util.List;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.common.pagination.PageData;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoSearchVO;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoVO;

/**
 * fileName       : ServiceInfoService
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 카테고리별 단일 서비스 정보의 버전 조회와 저장 및 배포를 정의한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
public interface ServiceInfoService {

    /**
     * 서비스 정보 카테고리별 대표 버전 목록을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 서비스 정보 검색과 페이징 조건
     * @param admin 접근하는 인증 관리자 정보
     * @return 카테고리별 대표 서비스 정보 버전 목록
     */
    PageData<ServiceInfoVO> getServiceInfoList(ServiceInfoSearchVO search, AdminSessionVO admin);

    /**
     * 서비스 정보 카테고리의 지정 버전 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 조회할 버전 번호
     * @param admin 접근하는 인증 관리자 정보
     * @return 지정 버전의 서비스 정보 상세
     */
    ServiceInfoVO getServiceInfoDtl(String cateCode, Integer versNumb, AdminSessionVO admin);

    /**
     * 서비스 정보 카테고리의 전체 버전 이력을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param admin 접근하는 인증 관리자 정보
     * @return 카테고리에 속한 전체 서비스 정보 버전
     */
    List<ServiceInfoVO> getServiceInfoVersionList(String cateCode, AdminSessionVO admin);

    /**
     * 아직 글이 없는 카테고리에 최초 서비스 정보를 등록한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfo 최초 버전으로 저장할 서비스 정보
     * @param admin 등록하는 인증 관리자 정보
     * @return 등록된 최초 서비스 정보 버전
     */
    ServiceInfoVO setServiceInfo(ServiceInfoVO serviceInfo, AdminSessionVO admin);

    /**
     * 미배포 버전은 수정하고 배포 버전은 다음 버전으로 저장한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 수정 기준 버전 번호
     * @param serviceInfo 저장할 제목과 HTML 본문
     * @param admin 수정하는 인증 관리자 정보
     * @return 저장된 초안 버전
     */
    ServiceInfoVO uptServiceInfoVersion(String cateCode, Integer versNumb, ServiceInfoVO serviceInfo
                                      , AdminSessionVO admin);

    /**
     * 서비스 정보 카테고리의 지정 버전을 현재 배포본으로 전환한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 배포할 버전 번호
     * @param admin 배포하는 인증 관리자 정보
     * @return 현재 배포본으로 전환된 서비스 정보 버전
     */
    ServiceInfoVO uptServiceInfoDeploy(String cateCode, Integer versNumb, AdminSessionVO admin);

    /**
     * 서비스 정보 카테고리의 모든 버전과 본문 이미지를 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cateCode 삭제할 서비스 정보 카테고리 코드
     * @param admin 삭제하는 인증 관리자 정보
     */
    void delServiceInfo(String cateCode, AdminSessionVO admin);
}
