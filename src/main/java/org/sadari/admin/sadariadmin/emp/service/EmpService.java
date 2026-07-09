package org.sadari.admin.sadariadmin.emp.service;

import org.sadari.admin.sadariadmin.emp.vo.EmpVO;

import java.util.List;

/**
 * EMP 업무 서비스
 */
public interface EmpService {

    /**
     * EMP 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    List<EmpVO> getEmpList();
}
