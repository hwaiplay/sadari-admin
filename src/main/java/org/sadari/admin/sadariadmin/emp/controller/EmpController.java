package org.sadari.admin.sadariadmin.emp.controller;

import org.sadari.admin.sadariadmin.common.result.ResultData;
import org.sadari.admin.sadariadmin.emp.service.EmpService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/employees")
public class EmpController {

    /** EMP 업무 서비스. */
    private final EmpService empService;

    public EmpController(EmpService empService) {
        this.empService = empService;
    }

    /**
     * EMP 목록을 조회한다.
     */
    @GetMapping
    public ResultData getEmpList() {
        return ResultData.success(empService.getEmpList());
    }
}
