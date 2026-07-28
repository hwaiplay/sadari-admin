package org.sadari.admin.sadariadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * fileName       : SadariAdminApplication
 * author         : SeungHyeon.Kang
 * date           : 2026-07-07
 * description    : SadariAdminApplication role
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 2026-07-07        SeungHyeon.Kang    최초 생성
 */@SpringBootApplication
public class SadariAdminApplication {

    /**
     * 관리자 서버 실행
     * @author SeungHyeon.Kang
     * @param args
     * @return
     */
    public static void main(String[] args) {
        SpringApplication.run(SadariAdminApplication.class, args);
    }
}
