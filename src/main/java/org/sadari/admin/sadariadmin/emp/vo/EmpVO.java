package org.sadari.admin.sadariadmin.emp.vo;

import lombok.Data;

/**
 * EMP 목록 조회 VO
 */
@Data
public class EmpVO {

    /** 사원 번호 */
    private Integer empno;

    /** 사원명 */
    private String ename;
}
