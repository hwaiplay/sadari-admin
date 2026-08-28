package org.sadari.admin.sadariadmin.welcome.service;

import java.util.List;
import org.sadari.admin.sadariadmin.admin.vo.AdminSessionVO;
import org.sadari.admin.sadariadmin.welcome.vo.WelcomePageVO;

/** 웰컴페이지 조회, 버전 저장, 배포와 삭제 기능을 정의한다. */
public interface WelcomePageService {
    /** 배포본 우선 웰컴페이지 목록을 조회한다. */
    List<WelcomePageVO> getWelcomePageList(AdminSessionVO admin);
    /** 웰컴페이지 버전 상세를 조회한다. */
    WelcomePageVO getWelcomePageDtl(Long wlcmNumb, Integer versNumb, AdminSessionVO admin);
    /** 웰컴페이지 전체 버전을 조회한다. */
    List<WelcomePageVO> getWelcomePageVersionList(Long wlcmNumb, AdminSessionVO admin);
    /** 웰컴페이지 최초 버전을 등록한다. */
    WelcomePageVO setWelcomePage(WelcomePageVO welcomePage, AdminSessionVO admin);
    /** 현재 배포 상태를 기준으로 같은 버전을 수정하거나 새 초안을 생성한다. */
    WelcomePageVO uptWelcomePageVersion(Long wlcmNumb, Integer versNumb
                                      , WelcomePageVO welcomePage, AdminSessionVO admin);
    /** 선택한 웰컴페이지 버전을 배포한다. */
    WelcomePageVO uptWelcomePageDeploy(Long wlcmNumb, Integer versNumb, AdminSessionVO admin);
    /** 웰컴페이지의 모든 버전을 삭제한다. */
    void delWelcomePage(Long wlcmNumb, AdminSessionVO admin);
}
