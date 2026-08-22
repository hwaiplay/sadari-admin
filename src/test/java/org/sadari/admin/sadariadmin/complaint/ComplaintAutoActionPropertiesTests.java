package org.sadari.admin.sadariadmin.complaint;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.complaint.config.ComplaintAutoActionProperties;

/**
 * fileName       : ComplaintAutoActionPropertiesTests
 * author         : SeungHyeon.Kang
 * date           : 2026-08-22
 * description    : 배경사진 자동 조치 임계치 매핑을 검증한다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-08-22        SeungHyeon.Kang    최초 생성
 */
class ComplaintAutoActionPropertiesTests {

    /** 배경사진이 프로필 사진과 독립된 임계치 설정을 사용하는지 확인한다. */
    @Test
    void getBackgroundThreshold() {
        // 프로필과 다른 배경사진 임계치를 설정한다
        ComplaintAutoActionProperties properties = new ComplaintAutoActionProperties();
        properties.setProfileImageThreshold(5);
        properties.setBackgroundImageThreshold(1);

        // 배경사진 유형이 전용 설정값을 반환하는지 확인한다
        assertEquals(1, properties.getThreshold(Constant.CMPL_TARGET_BACKGROUND_IMAGE));
        assertEquals(5, properties.getThreshold(Constant.CMPL_TARGET_PROFILE_IMAGE));
    }
}
