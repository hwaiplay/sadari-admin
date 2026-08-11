package org.sadari.admin.sadariadmin.serviceinfo.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoSearchVO;
import org.sadari.admin.sadariadmin.serviceinfo.vo.ServiceInfoVO;

/**
 * fileName       : ServiceInfoMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-10
 * description    : 카테고리별 서비스 정보 버전과 배포 상태에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-10        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface ServiceInfoMapper {

    /**
     * 카테고리별 대표 서비스 정보 버전을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 서비스 정보 검색과 페이징 조건
     * @return 카테고리별 대표 버전 목록
     */
    List<ServiceInfoVO> getServiceInfoList(ServiceInfoSearchVO search);

    /**
     * 검색 조건에 일치하는 서비스 정보 카테고리 수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param search 서비스 정보 검색 조건
     * @return 검색된 서비스 정보 카테고리 수
     */
    int getServiceInfoListCnt(ServiceInfoSearchVO search);

    /**
     * 카테고리와 버전으로 서비스 정보 상세를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 조회할 버전 번호
     * @return 지정 버전의 서비스 정보 상세
     */
    ServiceInfoVO getServiceInfoDtl(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode
                                  , @Param("versNumb") Integer versNumb);

    /**
     * 카테고리의 전체 서비스 정보 버전을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @return 최신순 서비스 정보 버전 목록
     */
    List<ServiceInfoVO> getServiceInfoVersionList(@Param("cateCgrp") String cateCgrp
                                                , @Param("cateCode") String cateCode);

    /**
     * 카테고리의 모든 버전에서 저장된 HTML 본문을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @return 본문 이미지 정리에 사용할 HTML 본문 목록
     */
    List<String> getServiceInfoContentList(@Param("cateCgrp") String cateCgrp
                                         , @Param("cateCode") String cateCode);

    /**
     * 등록 가능한 활성 서비스 정보 카테고리인지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @param useeYsno 허용할 공통코드 사용 여부
     * @return 조건에 일치하는 활성 카테고리 수
     */
    int getServiceInfoCategoryCnt(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode
                                , @Param("useeYsno") String useeYsno);

    /**
     * 카테고리에 이미 등록된 서비스 정보 버전 수를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @return 카테고리에 속한 전체 버전 수
     */
    int getServiceInfoCnt(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode);

    /**
     * 최초 서비스 정보 버전의 등록 감사정보를 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @return 최초 등록 관리자와 등록 일시
     */
    ServiceInfoVO getServiceInfoOrigAudit(@Param("cateCgrp") String cateCgrp
                                            , @Param("cateCode") String cateCode);

    /**
     * 다음 버전 생성과 배포를 직렬화하며 현재 최신 버전을 조회한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @return 잠금이 적용된 최신 버전 번호
     */
    Integer getLatestVersionForUpdate(@Param("cateCgrp") String cateCgrp
                                    , @Param("cateCode") String cateCode);

    /**
     * 서비스 정보의 새 버전을 등록한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfo 등록할 서비스 정보 버전
     * @return 등록된 행 수
     */
    int setServiceInfo(ServiceInfoVO serviceInfo);

    /**
     * 현재 미배포 서비스 정보 버전을 수정한다
     *
     * @author SeungHyeon.Kang
     * @param serviceInfo 수정할 서비스 정보 버전과 본문
     * @return 수정된 행 수
     */
    int uptServiceInfo(ServiceInfoVO serviceInfo);

    /**
     * 카테고리의 기존 현재 배포본을 배포 해제한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @param no 배포 해제 상태 값
     * @param yes 현재 배포 상태 값
     * @return 배포 해제된 행 수
     */
    int uptServiceInfoDeployOff(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode
                              , @Param("no") String no, @Param("yes") String yes);

    /**
     * 선택한 서비스 정보 버전을 현재 배포본으로 지정한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @param versNumb 배포할 버전 번호
     * @param dplyAdmn 배포 관리자 번호
     * @param yes 현재 배포 상태 값
     * @return 배포된 행 수
     */
    int uptServiceInfoDeployOn(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode
                             , @Param("versNumb") Integer versNumb, @Param("dplyAdmn") Long dplyAdmn
                             , @Param("yes") String yes);

    /**
     * 카테고리에 속한 서비스 정보 전체 버전을 삭제한다
     *
     * @author SeungHyeon.Kang
     * @param cateCgrp 서비스 정보 카테고리 공통코드
     * @param cateCode 서비스 정보 카테고리 코드
     * @return 삭제된 서비스 정보 버전 수
     */
    int delServiceInfo(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode);
}
