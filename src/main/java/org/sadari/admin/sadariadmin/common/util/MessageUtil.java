package org.sadari.admin.sadariadmin.common.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * fileName       : MessageUtil
 * author         : SeungHyeon.Kang
 * date           : 2026-07-09
 * description    : 메시지 리소스 조회 유틸 /
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-09        SeungHyeon.Kang    최초 생성
 */
@Component
public class MessageUtil {

    /** 메시지 리소스 */
    private static MessageSource messageSource;

    /**
     * 메시지 유틸 생성
     * @author SeungHyeon.Kang
     * @param messageSource
     * @return
     */
    public MessageUtil(MessageSource messageSource) {
        MessageUtil.messageSource = messageSource;
    }

    /**
     * 메시지 키로 메시지를 조회한다
     * @author SeungHyeon.Kang
     * @param messageKey
     * @return
     */
    public static String getMessage(String messageKey) {
        // 메시지 소스가 초기화되지 않은 환경에서는 메시지 키를 대체 문자열로 사용한다
        if (StringUtil.isEmpty(messageSource)) {
            // 조회 가능한 메시지가 없으므로 전달받은 메시지 키를 반환한다
            return messageKey;
        }
        // 현재 요청의 로케일에 맞는 사용자 메시지를 반환한다
        return messageSource.getMessage(messageKey, null, messageKey, LocaleContextHolder.getLocale());
    }
}
