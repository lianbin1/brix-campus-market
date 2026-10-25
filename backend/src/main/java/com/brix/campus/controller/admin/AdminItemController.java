package com.brix.campus.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.common.R;
import com.brix.campus.model.dto.AuditRejectDTO;
import com.brix.campus.model.dto.BatchAuditDTO;
import com.brix.campus.model.dto.OfflineDTO;
import com.brix.campus.model.query.ItemAuditQuery;
import com.brix.campus.model.vo.AdminItemVO;
import com.brix.campus.security.SecurityUtils;
import com.brix.campus.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 后台商品审核（K07-K10，仅 REVIEWER）。
 */
@RestController
@RequestMapping("/admin/items")
@Validated
public class AdminItemController {

    private final ItemService itemService;

    public AdminItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * 待审核商品列表（按提交时间升序，先到先审）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    public R<IPage<AdminItemVO>> list(@Valid ItemAuditQuery query) {
        return R.ok(itemService.adminPage(query));
    }

    /**
     * 批量（或单条）审核通过。
     *
     * @param dto 商品 ID 列表
     * @return {passed:n}
     */
    @PostMapping("/audit/pass")
    public R<Map<String, Integer>> pass(@RequestBody @Valid BatchAuditDTO dto) {
        int passed = itemService.auditPass(SecurityUtils.currentUserId(), dto.getIds());
        return R.ok(Map.of("passed", passed), "审核通过 " + passed + " 件");
    }

    /**
     * 审核拒绝（原因必填）。
     *
     * @param id  商品 ID
     * @param dto 拒绝原因
     * @return 空
     */
    @PostMapping("/{id}/audit/reject")
    public R<Void> reject(@PathVariable("id") Long id, @RequestBody @Valid AuditRejectDTO dto) {
        itemService.auditReject(SecurityUtils.currentUserId(), id, dto.getReason());
        return R.ok(null, "已拒绝");
    }

    /**
     * 强制下架（不受商品归属限制）。
     *
     * @param id  商品 ID
     * @param dto 下架原因
     * @return 空
     */
    @PostMapping("/{id}/offline")
    public R<Void> offline(@PathVariable("id") Long id,
                           @RequestBody(required = false) @Valid OfflineDTO dto) {
        itemService.forceOffline(SecurityUtils.currentUserId(), id, dto == null ? null : dto.getReason());
        return R.ok(null, "已强制下架");
    }
}
