package org.sadari.admin.sadariadmin.welcome.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.welcome.vo.WelcomePageVO;

/** 웰컴페이지 버전과 배포 상태에 접근한다. */
@Mapper
public interface WelcomePageMapper {
    /** 배포본 우선 웰컴페이지 목록을 조회한다. */
    List<WelcomePageVO> getWelcomePageList(@Param("yes") String yes);
    /** 웰컴페이지 버전 상세를 조회한다. */
    WelcomePageVO getWelcomePageDtl(@Param("wlcmNumb") Long wlcmNumb
                                  , @Param("versNumb") Integer versNumb);
    /** 웰컴페이지의 모든 버전을 조회한다. */
    List<WelcomePageVO> getWelcomePageVersionList(Long wlcmNumb);
    /** 최초 등록 관리 정보를 조회한다. */
    WelcomePageVO getWelcomeOriginalAudit(Long wlcmNumb);
    /** 최신 버전을 잠그고 조회한다. */
    Integer getLatestVersionForUpdate(Long wlcmNumb);
    /** 신규 웰컴페이지 버전을 등록한다. */
    int setWelcomePage(WelcomePageVO welcomePage);
    /** 미배포 웰컴페이지 버전을 수정한다. */
    int uptWelcomePage(WelcomePageVO welcomePage);
    /** 기존 배포 버전을 배포 해제한다. */
    int uptWelcomePageDeployOff(@Param("wlcmNumb") Long wlcmNumb
                              , @Param("no") String no, @Param("yes") String yes);
    /** 선택한 버전을 배포한다. */
    int uptWelcomePageDeployOn(@Param("wlcmNumb") Long wlcmNumb
                             , @Param("versNumb") Integer versNumb
                             , @Param("dplyAdmn") Long dplyAdmn
                             , @Param("yes") String yes);
    /** 웰컴페이지의 모든 버전을 삭제한다. */
    int delWelcomePage(Long wlcmNumb);
}
