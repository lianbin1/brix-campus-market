package com.brix.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.model.dto.ReportCreateDTO;
import com.brix.campus.model.dto.ReportHandleDTO;
import com.brix.campus.model.query.ReportQuery;
import com.brix.campus.model.vo.ReportVO;

/**
 * 举报服务（H01、H02）。
 */
public interface ReportService {

    /**
     * 提交举报。
     *
     * @param userId 举报人 ID
     * @param itemId 商品 ID
     * @param dto    入参
     * @return 举报记录
     */
    ReportVO create(Long userId, Long itemId, ReportCreateDTO dto);

    /**
     * 后台举报分页。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ReportVO> adminPage(ReportQuery query);

    /**
     * 处理举报：下架商品或驳回。
     *
     * @param reviewerId 审核员 ID
     * @param reportId   举报 ID
     * @param dto        入参
     * @return 处理后的举报
     */
    ReportVO handle(Long reviewerId, Long reportId, ReportHandleDTO dto);
}
