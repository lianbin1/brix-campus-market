package com.brix.campus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.dto.ReportHandleDTO;
import com.brix.campus.model.query.ReportQuery;
import com.brix.campus.model.vo.ReportVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台举报处理（H02，仅 REVIEWER）。
 */
@RestController
@RequestMapping("/admin/reports")
@Validated
public class AdminReportController {

    private final ReportService reportService;

    public AdminReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    /**
     * 举报列表（按状态筛选）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public R<IPage<ReportVO>> list(@Valid ReportQuery query) {
        return R.ok(reportService.adminPage(query));
    }

    /**
     * 处理举报：下架商品 / 驳回举报。
     *
     * @param id  举报 ID
     * @param dto 处理动作
     * @return 处理后的举报
     */
    @PostMapping("/{id}/handle")
    public R<ReportVO> handle(@PathVariable("id") Long id, @RequestBody @Valid ReportHandleDTO dto) {
        return R.ok(reportService.handle(SecurityUtils.currentUserId(), id, dto), "处理完成");
    }
}
