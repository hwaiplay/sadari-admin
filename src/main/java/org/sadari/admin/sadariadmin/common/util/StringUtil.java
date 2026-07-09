package org.sadari.admin.sadariadmin.common.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 문자열과 객체 비어있음 확인 유틸
 */
public class StringUtil {

    /**
     * 비어있음 여부 확인
     * @Author SeungHyeon.Kang
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
}
