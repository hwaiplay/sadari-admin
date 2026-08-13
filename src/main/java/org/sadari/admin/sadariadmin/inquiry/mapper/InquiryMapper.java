package org.sadari.admin.sadariadmin.inquiry.mapper;

import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sadari.admin.sadariadmin.inquiry.vo.InquiryAnswerVO;
import org.sadari.admin.sadariadmin.inquiry.vo.InquirySearchVO;
import org.sadari.admin.sadariadmin.inquiry.vo.InquiryVO;

/**
 * fileName       : InquiryMapper
 * author         : SeungHyeon.Kang
 * date           : 2026-08-13
 * description    : 관리자 고객문의와 답변 데이터에 접근한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-13        SeungHyeon.Kang    최초 생성
 */
@Mapper
public interface InquiryMapper {

    List<InquiryVO> getInquiryList(InquirySearchVO search);

    int getInquiryListCount(InquirySearchVO search);

    InquiryVO getInquiryDtl(@Param("inqrNumb") Long inqrNumb);

    InquiryVO getInquiryForUpdate(@Param("inqrNumb") Long inqrNumb);

    List<InquiryAnswerVO> getInquiryAnswerList(@Param("inqrNumb") Long inqrNumb);

    int uptInquiryReviewing(@Param("inqrNumb") Long inqrNumb, @Param("admnNumb") Long admnNumb
            , @Param("updtDate") LocalDateTime updtDate);

    int setInquiryAnswer(@Param("inqrNumb") Long inqrNumb, @Param("answCntn") String answCntn
            , @Param("admnNumb") Long admnNumb);

    int uptInquiryAnswered(@Param("inqrNumb") Long inqrNumb, @Param("admnNumb") Long admnNumb);
}
