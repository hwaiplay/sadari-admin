package org.sadari.admin.sadariadmin.emp.controller;

import org.sadari.admin.sadariadmin.common.constant.Constant;
import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.emp.service.EmpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * EMP API 컨트롤러
 */
@RestController
@RequestMapping(Constant.API_EMPLOYEES_PREFIX)
public class EmpController {

    /** EMP 업무 서비스 */
    private final EmpService empService;

    /**
     * EMP API 컨트롤러 생성
     * @Author SeungHyeon.Kang
     * @param empService
     * @return
     */
    public EmpController(EmpService empService) {
        this.empService = empService;
    }

    /**
     * EMP 목록 조회
     * @Author SeungHyeon.Kang
     * @return
     */
    @GetMapping
    public ResultData getEmpList() {
        return ResultData.success(empService.getEmpList());
    }
}
