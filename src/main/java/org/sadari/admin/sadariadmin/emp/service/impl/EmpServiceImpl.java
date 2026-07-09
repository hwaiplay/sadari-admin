package org.sadari.admin.sadariadmin.emp.service.impl;

import org.sadari.admin.sadariadmin.emp.mapper.EmpMapper;
import org.sadari.admin.sadariadmin.emp.service.EmpService;
import org.sadari.admin.sadariadmin.emp.vo.EmpVO;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * EMP 업무 서비스 구현체
 */
@Service
public class EmpServiceImpl implements EmpService {

    /** EMP 데이터 접근 Mapper */
    private final EmpMapper empMapper;

    /**
     * EMP 업무 서비스 생성
     * @Author SeungHyeon.Kang
     * @param empMapper
     * @return
     */
    public EmpServiceImpl(EmpMapper empMapper) {
        this.empMapper = empMapper;
    }

    /**
     * EMP 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    @Override
    public List<EmpVO> getEmpList() {
        return empMapper.getEmpList();
    }
}
