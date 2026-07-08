package org.sadari.admin.sadariadmin.emp.service.impl;

import org.sadari.admin.sadariadmin.emp.mapper.EmpMapper;
import org.sadari.admin.sadariadmin.emp.service.EmpService;
import org.sadari.admin.sadariadmin.emp.vo.EmpVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmpServiceImpl implements EmpService {

    /** EMP 데이터 접근 Mapper. */
    private final EmpMapper empMapper;

    public EmpServiceImpl(EmpMapper empMapper) {
        this.empMapper = empMapper;
    }

    /**
     * EMP 목록을 조회한다.
     */
    @Override
    public List<EmpVO> getEmpList() {
        return empMapper.getEmpList();
    }
}
