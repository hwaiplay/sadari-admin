package org.sadari.admin.sadariadmin.alim.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.alim.vo.AlimTempVO;

import java.util.List;

@Mapper
public interface AlimTempMapper {

    /**
     * 알림 템플릿 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    List<AlimTempVO> getAlimTempList();

    /**
     * 알림 템플릿 상세 조회
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @return
     */
    AlimTempVO getAlimTempDtl(@Param("alimSitu") String alimSitu, @Param("tempCode") String tempCode);

    /**
     * 알림 템플릿 중복 개수 조회
     * @Author SeungHyeon.Kang
     * @param alimSitu
     * @param tempCode
     * @return
     */
    int getAlimTempCnt(@Param("alimSitu") String alimSitu, @Param("tempCode") String tempCode);

    /**
     * 알림 템플릿 등록
     * @Author SeungHyeon.Kang
     * @param alimTemp
     * @return
     */
    void setAlimTemp(AlimTempVO alimTemp);

    /**
     * 알림 템플릿 수정
     * @Author SeungHyeon.Kang
     * @param oldAlimSitu
     * @param oldTempCode
     * @param alimTemp
     * @return
     */
    void uptAlimTemp(
            @Param("oldAlimSitu") String oldAlimSitu,
            @Param("oldTempCode") String oldTempCode,
            @Param("alimTemp") AlimTempVO alimTemp
    );
}
