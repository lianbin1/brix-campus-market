package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.mapper.ReportMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.Item;
import com.brix.campus.model.Report;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ReportCreateDTO;
import com.brix.campus.model.dto.ReportHandleDTO;
import com.brix.campus.model.enums.HandleActionEnum;
import com.brix.campus.model.enums.ItemStatusEnum;
import com.brix.campus.model.enums.ReportReasonEnum;
import com.brix.campus.model.enums.ReportStatusEnum;
import com.brix.campus.model.query.ReportQuery;
import com.brix.campus.model.vo.ReportVO;
import com.brix.campus.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 举报服务实现。
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public ReportServiceImpl(ReportMapper reportMapper, ItemMapper itemMapper, UserMapper userMapper) {
        this.reportMapper = reportMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO create(Long userId, Long itemId, ReportCreateDTO dto) {
        if (!ReportReasonEnum.isValid(dto.getReason())) {
            throw BusinessException.param("举报原因不合法");
        }
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (item.getSellerId().equals(userId)) {
            throw BusinessException.param("不能举报自己的商品");
        }
        Long pendingCount = reportMapper.selectCount(Wrappers.<Report>lambdaQuery()
                .eq(Report::getItemId, itemId)
                .eq(Report::getReporterId, userId)
                .eq(Report::getStatus, ReportStatusEnum.PENDING.getCode()));
        if (pendingCount != null && pendingCount > 0) {
            throw BusinessException.param("你已举报过该商品，请等待处理");
        }

        Report report = new Report();
        report.setItemId(itemId);
        report.setReporterId(userId);
        report.setReason(dto.getReason());
        report.setDescription(dto.getDescription() == null ? "" : dto.getDescription().trim());
        report.setStatus(ReportStatusEnum.PENDING.getCode());
        reportMapper.insert(report);

        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setItemId(itemId);
        vo.setItemTitle(item.getTitle());
        vo.setItemCover(item.getCoverImage());
        vo.setReporterId(userId);
        vo.setReason(report.getReason());
        vo.setReasonDesc(ReportReasonEnum.descOf(report.getReason()));
        vo.setDescription(report.getDescription());
        vo.setStatus(report.getStatus());
        vo.setStatusDesc(ReportStatusEnum.descOf(report.getStatus()));
        vo.setCreatedAt(report.getCreatedAt());
        return vo;
    }

    @Override
    public IPage<ReportVO> adminPage(ReportQuery query) {
        long current = query.getCurrentPage() == null ? 1L : query.getCurrentPage();
        long size = query.getPageSize() == null ? 10L : query.getPageSize();
        Page<ReportVO> page = new Page<>(current, size);
        IPage<ReportVO> result = reportMapper.selectReportPage(page, query);
        result.getRecords().forEach(this::fillDesc);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReportVO handle(Long reviewerId, Long reportId, ReportHandleDTO dto) {
        if (!HandleActionEnum.isValid(dto.getAction())) {
            throw BusinessException.param("处理动作不合法");
        }
        Report report = reportId == null ? null : reportMapper.selectById(reportId);
        if (report == null) {
            throw BusinessException.notFound("举报不存在");
        }
        if (!ReportStatusEnum.PENDING.getCode().equals(report.getStatus())) {
            throw BusinessException.param("该举报已处理");
        }

        if (HandleActionEnum.OFFLINE.getCode().equals(dto.getAction())) {
            Item item = itemMapper.selectById(report.getItemId());
            if (item != null && ItemStatusEnum.ON_SALE.getCode().equals(item.getStatus())) {
                item.setStatus(ItemStatusEnum.OFF_SHELF.getCode());
                item.setRejectReason("举报处理："
                        + ReportReasonEnum.descOf(report.getReason()));
                item.setAuditedBy(reviewerId);
                item.setAuditedAt(LocalDateTime.now());
                itemMapper.updateById(item);
            }
        }

        report.setStatus(ReportStatusEnum.HANDLED.getCode());
        report.setHandleAction(dto.getAction());
        report.setHandlerId(reviewerId);
        report.setHandledAt(LocalDateTime.now());
        reportMapper.updateById(report);

        ReportVO vo = new ReportVO();
        vo.setId(report.getId());
        vo.setItemId(report.getItemId());
        vo.setReporterId(report.getReporterId());
        vo.setReason(report.getReason());
        vo.setDescription(report.getDescription());
        vo.setStatus(report.getStatus());
        vo.setHandleAction(report.getHandleAction());
        vo.setHandlerId(report.getHandlerId());
        vo.setHandledAt(report.getHandledAt());
        vo.setCreatedAt(report.getCreatedAt());
        fillDesc(vo);

        Item item = itemMapper.selectById(report.getItemId());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
            vo.setItemCover(item.getCoverImage());
        }
        User handler = userMapper.selectById(reviewerId);
        if (handler != null) {
            vo.setHandlerNickname(handler.getNickname());
        }
        return vo;
    }

    /**
     * 补齐枚举文案。
     *
     * @param vo 举报 VO
     */
    private void fillDesc(ReportVO vo) {
        vo.setReasonDesc(ReportReasonEnum.descOf(vo.getReason()));
        vo.setStatusDesc(ReportStatusEnum.descOf(vo.getStatus()));
        if (vo.getHandleAction() != null) {
            vo.setHandleActionDesc(HandleActionEnum.descOf(vo.getHandleAction()));
        }
    }
}
