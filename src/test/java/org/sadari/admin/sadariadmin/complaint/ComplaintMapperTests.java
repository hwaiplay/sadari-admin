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
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
class ComplaintMapperTests {

    /**
     * 빈 검색 조건이 공통코드 그룹 바인딩으로 오염되지 않는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @throws IOException Mapper XML을 읽을 수 없을 때 발생
     */
    @Test
    void getListWithoutSearchFilters() throws IOException {
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
    }
}
