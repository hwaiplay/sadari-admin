package org.sadari.admin.sadariadmin.complaint;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.io.Resources;
import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.complaint.vo.ComplaintSearchVO;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * fileName       : ComplaintMapperTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 신고 목록 Mapper의 공통코드 바인딩과 검색 조건 분리를 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    신고 조회와 자동·수동 조치 SQL 검증
 * 2026-08-24        HanWon.Jang        목록 자동조치 담당자 판정 SQL 검증
 * 2026-08-28        OpenAI.Codex       댓글 대상 조회와 독후감 댓글 삭제 SQL 검증
 */
class ComplaintMapperTests {

    // 운영과 동일한 신고 Mapper XML의 전체 네임스페이스
    private static final String MAPPER_NAMESPACE =
            "org.sadari.admin.sadariadmin.complaint.mapper.ComplaintMapper.";

    /**
     * 빈 검색 조건이 공통코드 그룹 바인딩으로 오염되지 않는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void getListNoSearchFilters() throws IOException {
        // 신고 목록 Mapper XML을 해석할 MyBatis 설정을 생성한다
        Configuration configuration = new Configuration();
        // 운영과 동일한 Mapper XML에서 실제 동적 SQL을 구성한다
        try (InputStream mapperStream = Resources.getResourceAsStream(
            "org/sadari/admin/sadariadmin/complaint/mapper/ComplaintMapper.xml"
        )) {
            // 신고 목록 SQL 조각과 공통코드 바인딩을 MyBatis 설정에 등록한다
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                mapperStream, configuration, "ComplaintMapper.xml", configuration.getSqlFragments()
            );
            // 동적 SQL 검증에 사용할 Mapper 구문을 해석한다
            mapperBuilder.parse();
        }

        // 빈 검색 조건의 첫 페이지 범위를 준비한다
        ComplaintSearchVO search = new ComplaintSearchVO();
        // 첫 페이지 시작 행을 설정한다
        search.setStartRow(1);
        // 첫 페이지 종료 행을 설정한다
        search.setEndRow(20);
        // 신고 목록 Mapper 구문을 조회한다
        MappedStatement statement = configuration.getMappedStatement(
            "org.sadari.admin.sadariadmin.complaint.mapper.ComplaintMapper.getComplaintList"
        );
        // 빈 검색 조건으로 실제 실행될 동적 SQL을 생성한다
        BoundSql boundSql = statement.getBoundSql(search);
        // 줄바꿈 차이와 무관하게 조건 포함 여부를 확인하도록 SQL 공백을 정규화한다
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 상태 공통코드 그룹이 빈 상태 검색 조건을 덮어쓰지 않는지 확인한다
        assertFalse(sql.contains("AND C.CMPL_STAT = ?"));
        // 사유 공통코드 그룹이 빈 사유 검색 조건을 덮어쓰지 않는지 확인한다
        assertFalse(sql.contains("AND C.CMPL_RSON = ?"));
        // 상태 코드명 조회에는 검색값과 분리된 공통코드 그룹이 유지되는지 확인한다
        assertEquals(Constant.CMPL_STAT, boundSql.getAdditionalParameter("cmplStatGroup"));
        // 사유 코드명 조회에는 검색값과 분리된 공통코드 그룹이 유지되는지 확인한다
        assertEquals(Constant.CMPL_RSON, boundSql.getAdditionalParameter("cmplRsonGroup"));
        // 신고 시점에 저장된 대상 내용 스냅샷이 목록 조회 결과에 포함되는지 확인한다
        assertTrue(sql.contains("P.TAGT_CNTN"));
        // 신고 대상 소유 사용자 번호가 목록 조회 결과에 포함되는지 확인한다
        assertTrue(sql.contains("P.TAGT_USER"));
        // 대상 유형과 무관하게 저장된 소유 사용자 번호로 피신고자 닉네임을 조회하는지 확인한다
        assertTrue(sql.contains("T.USER_NUMB = C.TAGT_USER"));
        // 동일 대상 버전의 자동조치 이력을 목록 담당자 판정에 사용하는지 확인한다
        assertTrue(sql.contains("FROM TH_CMACTN H"));
        // 조치 완료와 담당자 부재 및 자동조치 이력을 모두 만족할 때만 자동조치로 판정하는지 확인한다
        assertTrue(sql.contains("CASE WHEN C.CMPL_STAT = ? AND C.PROC_ADMN IS NULL "
                + "AND A.ACTN_NUMB IS NOT NULL THEN 1 ELSE 0 END AS AUTO_ACTIONED"));
        // 조치 완료 상태 판정값이 공통 상수에서 바인딩되는지 확인한다
        assertEquals(Constant.CMPL_STATUS_ACTIONED, boundSql.getAdditionalParameter("cmplActioned"));
    }

    /**
     * 신고 상세가 신고자 이미지 파일 경로를 조회하지 않는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void getDtlHidesReporterImages() throws IOException {
        // 운영과 동일한 신고 Mapper XML을 해석한다
        Configuration configuration = createConfiguration();
        // 신고번호로 실행될 상세 조회 SQL을 생성한다
        BoundSql boundSql = configuration.getMappedStatement(
                MAPPER_NAMESPACE + "getComplaintDtl").getBoundSql(1L);
        // 대소문자와 공백 차이에 영향받지 않도록 SQL을 정규화한다
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim().toUpperCase();

        // 신고자 프로필 파일 경로가 상세 응답 SQL에서 제외되는지 확인한다
        assertFalse(sql.contains("REPORTER_PROF_PATH"));
        // 신고자 배경 파일 경로가 상세 응답 SQL에서 제외되는지 확인한다
        assertFalse(sql.contains("REPORTER_BGIM_PATH"));
        // 신고자 프로필 파일 테이블 조인이 상세 응답 SQL에서 제외되는지 확인한다
        assertFalse(sql.contains("TM_FILEXM RPF"));
        // 신고자 배경 파일 테이블 조인이 상세 응답 SQL에서 제외되는지 확인한다
        assertFalse(sql.contains("TM_FILEXM RBF"));
    }

    /**
     * 자동 조치 진행 건수와 실행 이력이 정책 테이블 및 공통코드명을 조회하는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void getAutoActionStatus() throws IOException {
        // 자동 조치 SQL을 검증할 MyBatis 설정을 생성한다
        Configuration configuration = createConfiguration();
        // 반려 제외 누적 건수 SQL을 조회한다
        BoundSql countBoundSql = configuration.getMappedStatement(
                MAPPER_NAMESPACE + "getAutoActionCmplCnt").getBoundSql(new java.util.HashMap<>());
        // 실행 이력 SQL을 조회한다
        BoundSql historyBoundSql = configuration.getMappedStatement(
                MAPPER_NAMESPACE + "getAutoActionList").getBoundSql(new java.util.HashMap<>());
        // SQL 공백 차이와 무관하게 대상 테이블과 조건을 확인하도록 정규화한다
        String countSql = countBoundSql.getSql().replaceAll("\\s+", " ").trim();
        // SQL 공백 차이와 무관하게 실행 이력 조회식을 정규화한다
        String historySql = historyBoundSql.getSql().replaceAll("\\s+", " ").trim();

        // 누적 신고 건수에서 반려 상태를 제외하는지 확인한다
        assertTrue(countSql.contains("C.CMPL_STAT != ?"));
        // 실제 자동 조치 결과 테이블을 조회하는지 확인한다
        assertTrue(historySql.contains("FROM TH_CMACTN A"));
        // 자동 조치와 결과 명칭을 공통코드 함수로 조회하는지 확인한다
        assertTrue(historySql.contains("FN_GET_CODE_NAME"));
        // 반려 제외 바인딩이 공통 상수를 사용하는지 확인한다
        assertEquals(Constant.CMPL_STATUS_REJECTED
                     , countBoundSql.getAdditionalParameter("cmplRejected"));
    }

    /** 자동 조치 대상 조회 SQL이 배경사진을 독립된 현재 파일로 조회하는지 확인한다. */
    @Test
    void getAutoActionTargetBgImg() throws IOException {
        // 배경사진 유형으로 자동 조치 대상 조회 SQL을 생성한다
        java.util.Map<String, Object> parameters = java.util.Map.of(
                "tagtType", Constant.CMPL_TARGET_BACKGROUND_IMAGE,
                "tagtNumb", 10L,
                "userNumb", 10L
        );
        BoundSql boundSql = createConfiguration().getMappedStatement(
                MAPPER_NAMESPACE + "getAutoActionTargetDtl").getBoundSql(parameters);
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 배경사진 파일 참조와 배경사진 유형 바인딩이 포함되는지 확인한다
        assertTrue(sql.contains("U.BGIM_NUMB"));
        assertEquals(Constant.CMPL_TARGET_BACKGROUND_IMAGE,
                     boundSql.getAdditionalParameter("backgroundImage"));
    }

    /**
     * 댓글 신고의 현재 원문이 부모 콘텐츠 종류와 무관하게 댓글 번호로 조회되는지 확인한다
     *
     * @author OpenAI.Codex
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void getAutoActionTargetReply() throws IOException {
        // 프로필·배경사진·독후감 댓글이 같은 조회 경로를 사용하도록 댓글 번호 조건을 준비한다
        java.util.Map<String, Object> parameters = java.util.Map.of(
                "tagtType", Constant.CMPL_TARGET_REPLY,
                "tagtNumb", 10L,
                "userNumb", 20L
        );
        // 댓글 신고 상세에서 실행될 현재 원문 조회 SQL을 생성한다
        BoundSql boundSql = createConfiguration().getMappedStatement(
                MAPPER_NAMESPACE + "getAutoActionTargetDtl").getBoundSql(parameters);
        // 공백 차이와 무관하게 댓글 조회 조건을 확인하도록 SQL을 정규화한다
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 부모 콘텐츠 종류와 무관하게 신고된 댓글 번호와 작성자로 현재 댓글을 조회하는지 확인한다
        assertTrue(sql.contains("FROM TB_REPLXX R WHERE ? = ? AND R.REPL_NUMB = ? AND R.USER_NUMB = ?"));
        // 실제 댓글 테이블에 존재하지 않는 독후감 번호 컬럼을 참조하지 않는지 확인한다
        assertFalse(sql.contains("R.REPT_NUMB"));
        // 댓글 신고 유형 바인딩이 공통 상수를 사용하는지 확인한다
        assertEquals(Constant.CMPL_TARGET_REPLY, boundSql.getAdditionalParameter("reply"));
    }

    /**
     * 독후감 삭제 시 연결된 댓글을 공용 대상 유형과 번호로 조회하는지 확인한다
     *
     * @author OpenAI.Codex
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void delReportRepliesByTarget() throws IOException {
        // 독후감 소유자 검증과 연결 댓글 삭제에 사용할 파라미터를 준비한다
        java.util.Map<String, Object> parameters = java.util.Map.of("reptNumb", 10L, "userNumb", 20L);
        // 댓글 좋아요 삭제 SQL을 생성한다
        BoundSql likeBoundSql = createConfiguration().getMappedStatement(
                MAPPER_NAMESPACE + "delTargetReportReplyLikes").getBoundSql(parameters);
        // 대댓글 삭제 SQL을 생성한다
        BoundSql childBoundSql = createConfiguration().getMappedStatement(
                MAPPER_NAMESPACE + "delTagtReportChildReply").getBoundSql(parameters);
        // 부모 댓글 삭제 SQL을 생성한다
        BoundSql replyBoundSql = createConfiguration().getMappedStatement(
                MAPPER_NAMESPACE + "delTargetReportReplies").getBoundSql(parameters);
        // 각 삭제 SQL의 공백을 정규화한다
        String likeSql = likeBoundSql.getSql().replaceAll("\\s+", " ").trim();
        // 대댓글 삭제 SQL의 공백을 정규화한다
        String childSql = childBoundSql.getSql().replaceAll("\\s+", " ").trim();
        // 부모 댓글 삭제 SQL의 공백을 정규화한다
        String replySql = replyBoundSql.getSql().replaceAll("\\s+", " ").trim();

        // 댓글 좋아요 삭제가 독후감 대상 유형과 번호로 범위를 제한하는지 확인한다
        assertTrue(likeSql.contains("R.TAGT_TYPE = ? AND R.TAGT_NUMB = ?"));
        // 대댓글 삭제가 독후감 대상 유형과 번호로 범위를 제한하는지 확인한다
        assertTrue(childSql.contains("C.TAGT_TYPE = ? AND C.TAGT_NUMB = ?"));
        // 부모 댓글 삭제가 독후감 대상 유형과 번호로 범위를 제한하는지 확인한다
        assertTrue(replySql.contains("R.TAGT_TYPE = ? AND R.TAGT_NUMB = ?"));
        // 독후감 댓글의 공용 대상 유형 바인딩이 공통 상수를 사용하는지 확인한다
        assertEquals(Constant.LIKE_TARGET_REPORT, replyBoundSql.getAdditionalParameter("reportTarget"));
    }

    /**
     * 관리자 원본 조치가 같은 대상의 미처리 신고를 일괄 종결하는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void uptManualComplaintStatus() throws IOException {
        // 운영과 동일한 신고 Mapper XML을 해석한다
        Configuration configuration = createConfiguration();
        // 관리자 수동 조치 일괄 종결 SQL을 생성한다
        BoundSql boundSql = configuration.getMappedStatement(
                MAPPER_NAMESPACE + "uptManualComplaints").getBoundSql(new java.util.HashMap<>());
        // 공백 차이와 무관하게 상태와 대상 범위 조건을 검증하도록 SQL을 정규화한다
        String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();

        // 같은 대상 유형과 번호만 종결 범위로 제한하는지 확인한다
        assertTrue(sql.contains("WHERE TAGT_TYPE = ? AND TAGT_NUMB = ?"));
        // 접수와 검토 중 신고만 조치 완료로 전환하는지 확인한다
        assertTrue(sql.contains("CMPL_STAT IN (?, ?)"));
        // 처리 관리자와 완료 일시를 함께 저장하는지 확인한다
        assertTrue(sql.contains("PROC_ADMN = ?"));
        assertTrue(sql.contains("PROC_DATE = CURRENT_TIMESTAMP(6)"));
    }

    /**
     * 운영과 동일한 신고 Mapper XML을 해석한 설정을 생성한다
     *
     * @author SeungHyeon.Kang
     * @return 신고 Mapper 구문이 등록된 MyBatis 설정
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    private Configuration createConfiguration() throws IOException {
        // 신고 Mapper XML을 해석할 MyBatis 설정을 생성한다
        Configuration configuration = new Configuration();
        // 운영과 동일한 Mapper XML에서 실제 동적 SQL을 구성한다
        try (InputStream mapperStream = Resources.getResourceAsStream(
                "org/sadari/admin/sadariadmin/complaint/mapper/ComplaintMapper.xml")) {
            // 신고 SQL 조각과 구문을 MyBatis 설정에 등록한다
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(
                    mapperStream, configuration, "ComplaintMapper.xml", configuration.getSqlFragments());
            // 동적 SQL 검증에 사용할 Mapper 구문을 해석한다
            mapperBuilder.parse();
        }
        // 해석된 신고 Mapper 설정을 반환한다
        return configuration;
    }
}
