package org.sadari.admin.sadariadmin.common.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 메시지 리소스 조회 유틸
 */
@Component
public class MessageUtil {

    /** 메시지 리소스 */
    private static MessageSource messageSource;

    /**
     * 메시지 유틸 생성
     * @Author SeungHyeon.Kang
     * @param messageSource
     * @return
     */
    public MessageUtil(MessageSource messageSource) {
        MessageUtil.messageSource = messageSource;
    }

    /**
     * 메시지 키로 메시지를 조회한다
     * @Author SeungHyeon.Kang
     * @param messageKey
     * @return
     */
    public static String getMessage(String messageKey) {
        if (messageSource == null) {
            return messageKey;
        }
        return messageSource.getMessage(messageKey, null, messageKey, LocaleContextHolder.getLocale());
    }
}

