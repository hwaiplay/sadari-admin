package org.sadari.admin.sadariadmin.emp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.sadari.admin.sadariadmin.emp.vo.EmpVO;

import java.util.List;

@Mapper
public interface EmpMapper {

    /**
     * EMP 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    List<EmpVO> getEmpList();
}
