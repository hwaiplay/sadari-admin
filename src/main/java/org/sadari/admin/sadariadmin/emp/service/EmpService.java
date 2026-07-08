package org.sadari.admin.sadariadmin.emp.service;

import org.sadari.admin.sadariadmin.emp.vo.EmpVO;

import java.util.List;

public interface EmpService {

    /**
     * EMP 목록을 조회한다.
     */
    List<EmpVO> getEmpList();
}
