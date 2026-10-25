package com.brix.campus.controller;

import com.brix.campus.common.R;
import com.brix.campus.model.dto.ReportCreateDTO;
import com.brix.campus.model.vo.ReportVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报接口（H01）。
 */
@RestController
@Validated
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 提交举报。
     *
     * @param id  商品 ID
     * @param dto 入参
     * @return 举报记录
     */
    @PostMapping("/items/{id}/reports")
    public R<ReportVO> create(@PathVariable("id") Long id, @RequestBody @Valid ReportCreateDTO dto) {
        return R.ok(reportService.create(SecurityUtils.currentUserId(), id, dto), "举报已提交");
    }
}
