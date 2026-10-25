package com.brix.campus.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.brix.campus.model.Item;
import com.brix.campus.model.dto.ItemSaveDTO;
import com.brix.campus.model.dto.ItemStatusDTO;
import com.brix.campus.model.query.ItemAuditQuery;
import com.brix.campus.model.query.ItemQuery;
import com.brix.campus.model.vo.AdminItemVO;
import com.brix.campus.model.vo.ItemDetailVO;
import com.brix.campus.model.vo.ItemVO;

import java.util.List;

/**
 * 商品服务（B01-B09 + 后台审核 K07-K10）。
 */
public interface ItemService {

    /**
     * 发布商品：状态为 {@code PENDING}，前台列表不可见。
     *
     * @param sellerId 卖家 ID（取自 token）
     * @param dto      入参
     * @return 新商品（含 id 与状态）
     */
    ItemVO create(Long sellerId, ItemSaveDTO dto);

    /**
     * 前台商品列表：仅 {@code ON_SALE}，支持关键词 / 分类 / 校区 / 价格区间 / 排序。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<ItemVO> pageForFront(ItemQuery query);

    /**
     * 商品详情：浏览量 +1（同一访问者 10 分钟内不重复计数），登录态补充 isFavorite。
     *
     * @param itemId    商品 ID
     * @param viewerId  当前登录用户 ID（可空）
     * @param viewerKey 访问者标识（登录用户为 userId，游客为 IP）
     * @return 详情
     */
    ItemDetailVO detail(Long itemId, Long viewerId, String viewerKey);

    /**
     * 我的商品：返回本人全部状态。
     *
     * @param sellerId 卖家 ID
     * @param query    查询条件
     * @return 分页结果
     */
    IPage<ItemVO> pageMine(Long sellerId, ItemQuery query);

    /**
     * 编辑商品：仅本人，编辑后状态回到 {@code PENDING}；{@code SOLD} 不可编辑。
     *
     * @param sellerId 卖家 ID
     * @param itemId   商品 ID
     * @param dto      入参
     */
    void update(Long sellerId, Long itemId, ItemSaveDTO dto);

    /**
     * 删除商品（逻辑删除）：存在进行中订单时拒绝。
     *
     * @param sellerId 卖家 ID
     * @param itemId   商品 ID
     */
    void delete(Long sellerId, Long itemId);

    /**
     * 上架 / 下架。
     *
     * @param sellerId 卖家 ID
     * @param itemId   商品 ID
     * @param dto      入参
     * @return 更新后的商品
     */
    ItemVO updateStatus(Long sellerId, Long itemId, ItemStatusDTO dto);

    /**
     * 后台审核分页（默认待审核，按提交时间升序）。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<AdminItemVO> adminPage(ItemAuditQuery query);

    /**
     * 批量审核通过。
     *
     * @param reviewerId 审核员 ID
     * @param ids        商品 ID 列表
     * @return 通过条数
     */
    int auditPass(Long reviewerId, List<Long> ids);

    /**
     * 审核拒绝（原因必填）。
     *
     * @param reviewerId 审核员 ID
     * @param itemId     商品 ID
     * @param reason     拒绝原因
     */
    void auditReject(Long reviewerId, Long itemId, String reason);

    /**
     * 强制下架（不受商品归属限制）。
     *
     * @param reviewerId 审核员 ID
     * @param itemId     商品 ID
     * @param reason     下架原因
     */
    void forceOffline(Long reviewerId, Long itemId, String reason);

    /**
     * 取商品实体，不存在返回 null。
     *
     * @param itemId 商品 ID
     * @return 实体
     */
    Item getEntity(Long itemId);

    /**
     * 实体转列表 VO（自动补齐分类名与卖家昵称）。
     *
     * @param item 实体
     * @return VO
     */
    ItemVO toVO(Item item);
}
