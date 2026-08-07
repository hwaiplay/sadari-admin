package org.sadari.admin.sadariadmin.notice.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.notice.vo.NoticeSearchVO;
import org.sadari.admin.sadariadmin.notice.vo.NoticeVO;

/**
 * fileName       : NoticeMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-07
 * description    : 공지사항 버전과 배포 상태 및 삭제 대상 데이터에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-07        SeungHyeon.Kang    최초 생성
 * 2026-08-07        SeungHyeon.Kang    버전 이력과 삭제 대상 조회 및 삭제 쿼리 추가
 * 2026-08-07        SeungHyeon.Kang    목록 조회를 공지번호별 최신 버전으로 정리
 * 2026-08-08        SeungHyeon.Kang    미배포 공지 버전 수정 쿼리 추가
 */
@Mapper
public interface NoticeMapper {

    List<NoticeVO> getNoticeList(NoticeSearchVO search);

    int getNoticeListCnt(NoticeSearchVO search);

    NoticeVO getNoticeDtl(@Param("notiNumb") Long notiNumb, @Param("versNumb") Integer versNumb);

    /** 공지사항의 전체 버전과 관리 정보를 최신순으로 조회한다. */
    List<NoticeVO> getNoticeVersionList(Long notiNumb);

    /** 실제 파일 삭제 대상을 찾기 위해 전체 버전 본문을 조회한다. */
    List<String> getNoticeContentList(Long notiNumb);

    NoticeVO getNoticeOriginalAudit(Long notiNumb);

    Integer getLatestVersionForUpdate(Long notiNumb);

    int getNoticeCategoryCnt(@Param("cateCgrp") String cateCgrp, @Param("cateCode") String cateCode
            , @Param("useeYsno") String useeYsno);

    int setNotice(NoticeVO notice);

    /**
     * 현재 배포 중이 아닌 공지 버전의 편집 내용을 같은 복합키에 반영한다
     *
     * @author SeungHyeon.Kang
     * @param notice 수정할 공지사항 버전 정보
     * @return 수정된 공지사항 행 수
     */
    int uptNotice(NoticeVO notice);

    int uptNoticeDeployOff(@Param("notiNumb") Long notiNumb, @Param("no") String no
            , @Param("yes") String yes);

    int uptNoticeDeployOn(@Param("notiNumb") Long notiNumb, @Param("versNumb") Integer versNumb
            , @Param("dplyAdmn") Long dplyAdmn, @Param("yes") String yes);

    int delNoticeView(@Param("viewType") String viewType, @Param("tagtNumb") Long tagtNumb);

    /** 공지사항 주키에 속한 모든 버전을 삭제한다. */
    int delNotice(Long notiNumb);
}
