package com.brix.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.model.Report;
import com.brix.campus.model.query.ReportQuery;
import com.brix.campus.model.vo.ReportVO;
import org.apache.ibatis.annotations.Param;

/**
 * 举报 Mapper：后台列表需要联表取商品与用户昵称，走 XML。
 */
public interface ReportMapper extends BaseMapper<Report> {

    /**
     * 后台举报分页（按状态筛选）。
     *
     * @param page  分页对象
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ReportVO> selectReportPage(Page<ReportVO> page, @Param("q") ReportQuery query);
}
