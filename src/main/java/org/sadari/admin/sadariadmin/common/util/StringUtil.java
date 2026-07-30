package org.sadari.admin.sadariadmin.common.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * fileName       : StringUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-09
 * description    : 문자열과 객체 비어있음 확인 유틸 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-09        SeungHyeon.Kang    최초 생성
 * 2026-07-30        SeungHyeon.Kang    여러 값의 비어 있음 일괄 검사 추가
 */
public class StringUtil {

    /**
     * 비어있음 여부 확인
     * @author SeungHyeon.Kang
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        // null 객체는 비어있는 값으로 판단한다
        if (Objects.isNull(obj)) {
            return true;
        }

        // 문자열은 앞뒤 공백 제거 후 길이가 없으면 비어있는 값으로 판단한다
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }

        // List는 원소 개수가 없으면 비어있는 값으로 판단한다
        if (obj instanceof List) {
            return ((List<?>) obj).isEmpty();
        }

        // Map은 원소 개수가 없으면 비어있는 값으로 판단한다
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }

        // 배열은 길이가 없으면 비어있는 값으로 판단한다
        if (obj instanceof Object[]) {
            return ((Object[]) obj).length == 0;
        }

        return false;
    }

    /**
     * 전달된 값 중 하나라도 비어 있는지 확인한다
     *
     * @author SeungHyeon.Kang
     * @param values 비어 있음 여부를 확인할 값 목록
     * @return 하나 이상의 값이 비어 있으면 true
     */
    public static boolean hasEmpty(Object... values) {
        // 검사할 값 배열 자체가 없으면 필수 입력이 전달되지 않은 것으로 판단한다
        if (isEmpty(values)) {
            // 값 목록이 비어 있음을 반환한다
            return true;
        }

        // 모든 입력값을 같은 비어 있음 정책으로 검사한다
        for (Object value : values) {
            // 하나라도 비어 있으면 나머지 값과 관계없이 즉시 실패로 판단한다
            if (isEmpty(value)) {
                // 필수값 중 비어 있는 항목이 있음을 반환한다
                return true;
            }

        }

        // 모든 값이 유효하게 채워져 있음을 반환한다
        return false;
    }
}
