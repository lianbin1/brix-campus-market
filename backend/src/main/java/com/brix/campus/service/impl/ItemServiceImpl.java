package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.CategoryMapper;
import com.brix.campus.mapper.FavoriteMapper;
import com.brix.campus.mapper.ItemImageMapper;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.mapper.OrderMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.Category;
import com.brix.campus.model.Favorite;
import com.brix.campus.model.Item;
import com.brix.campus.model.ItemImage;
import com.brix.campus.model.Order;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ItemSaveDTO;
import com.brix.campus.model.dto.ItemStatusDTO;
import com.brix.campus.model.enums.ConditionEnum;
import com.brix.campus.model.enums.ItemStatusEnum;
import com.brix.campus.model.enums.OrderStatusEnum;
import com.brix.campus.model.enums.TradeTypeEnum;
import com.brix.campus.model.query.BaseQuery;
import com.brix.campus.model.query.ItemAuditQuery;
import com.brix.campus.model.query.ItemQuery;
import com.brix.campus.model.vo.AdminItemVO;
import com.brix.campus.model.vo.ItemDetailVO;
import com.brix.campus.model.vo.ItemImageVO;
import com.brix.campus.model.vo.ItemVO;
import com.brix.campus.model.vo.SellerVO;
import com.brix.campus.service.ItemService;
import com.brix.campus.util.ViewCountDedupCache;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 商品服务实现。
 *
 * <p>状态机约束（PRD B08）：{@code ON_SALE → OFF_SHELF}（主动/强制下架）、
 * {@code OFF_SHELF → PENDING}（重新上架需再审）、编辑一律回 {@code PENDING}、{@code SOLD} 不可编辑/上下架。</p>
 */
@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {

    /** 商品图片上限 */
    private static final int MAX_IMAGES = 9;
    /** 进行中的订单状态 */
    private static final List<String> ONGOING_ORDER_STATUS = List.of(
            OrderStatusEnum.UNPAID.getCode(),
            OrderStatusEnum.TO_SHIP.getCode(),
            OrderStatusEnum.TO_RECEIVE.getCode(),
            OrderStatusEnum.TO_MEET.getCode());

    private final ItemImageMapper itemImageMapper;
    private final CategoryMapper categoryMapper;
    private final UserMapper userMapper;
    private final FavoriteMapper favoriteMapper;
    private final OrderMapper orderMapper;
    private final ViewCountDedupCache viewCountDedupCache;

    public ItemServiceImpl(ItemImageMapper itemImageMapper,
                           CategoryMapper categoryMapper,
                           UserMapper userMapper,
                           FavoriteMapper favoriteMapper,
                           OrderMapper orderMapper,
                           ViewCountDedupCache viewCountDedupCache) {
        this.itemImageMapper = itemImageMapper;
        this.categoryMapper = categoryMapper;
        this.userMapper = userMapper;
        this.favoriteMapper = favoriteMapper;
        this.orderMapper = orderMapper;
        this.viewCountDedupCache = viewCountDedupCache;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO create(Long sellerId, ItemSaveDTO dto) {
        validateSaveDTO(dto);

        Item item = new Item();
        item.setTitle(dto.getTitle().trim());
        item.setDescription(dto.getDescription().trim());
        item.setCategoryId(dto.getCategoryId());
        item.setPrice(dto.getPrice());
        item.setOriginalPrice(dto.getOriginalPrice());
        item.setConditionLevel(dto.getConditionLevel());
        item.setCampus(dto.getCampus().trim());
        item.setTradeType(dto.getTradeType());
        item.setContact(dto.getContact() == null ? null : dto.getContact().trim());
        item.setStatus(ItemStatusEnum.PENDING.getCode());
        item.setCoverImage("");
        item.setViewCount(0);
        item.setFavoriteCount(0);
        item.setSellerId(sellerId);
        save(item);

        List<String> images = dto.getImages() == null ? Collections.emptyList() : dto.getImages();
        replaceImages(item.getId(), images);
        item.setCoverImage(images.isEmpty() ? "" : images.get(0));
        updateById(item);

        return toVO(item);
    }

    @Override
    public IPage<ItemVO> pageForFront(ItemQuery query) {
        Page<Item> page = new Page<>(pageOf(query), sizeOf(query));
        LambdaQueryWrapper<Item> wrapper = Wrappers.<Item>lambdaQuery()
                .eq(Item::getStatus, ItemStatusEnum.ON_SALE.getCode());
        applyFilters(wrapper, query);
        applySort(wrapper, query.getSort());
        IPage<Item> itemPage = baseMapper.selectPage(page, wrapper);
        return convertPage(itemPage);
    }

    @Override
    public ItemDetailVO detail(Long itemId, Long viewerId, String viewerKey) {
        Item item = getEntity(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        boolean owner = viewerId != null && viewerId.equals(item.getSellerId());
        boolean hidden = ItemStatusEnum.PENDING.getCode().equals(item.getStatus())
                || ItemStatusEnum.REJECTED.getCode().equals(item.getStatus());
        if (hidden && !owner) {
            throw BusinessException.notFound("商品不存在或尚未上架");
        }

        // 浏览量 +1（同一访问者 10 分钟内不重复计数）
        if (viewCountDedupCache.tryAcquire(viewerKey, itemId)) {
            baseMapper.update(null, new LambdaUpdateWrapper<Item>()
                    .eq(Item::getId, itemId)
                    .setSql("view_count = view_count + 1"));
            item.setViewCount(item.getViewCount() == null ? 1 : item.getViewCount() + 1);
        }

        ItemDetailVO vo = new ItemDetailVO();
        copyBase(item, vo);
        vo.setDescription(item.getDescription());
        vo.setContact(item.getContact());
        vo.setImages(listImages(itemId));

        User seller = userMapper.selectById(item.getSellerId());
        if (seller != null) {
            SellerVO sellerVO = new SellerVO();
            sellerVO.setId(seller.getId());
            sellerVO.setNickname(seller.getNickname());
            sellerVO.setAvatar(seller.getAvatar());
            sellerVO.setCampus(seller.getCampus());
            sellerVO.setCreditScore(seller.getCreditScore());
            vo.setSeller(sellerVO);
            vo.setSellerNickname(seller.getNickname());
        }
        Category category = categoryMapper.selectById(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        vo.setIsOwner(owner);
        if (viewerId != null) {
            Long favoriteCount = favoriteMapper.selectCount(Wrappers.<Favorite>lambdaQuery()
                    .eq(Favorite::getUserId, viewerId)
                    .eq(Favorite::getItemId, itemId));
            vo.setIsFavorite(favoriteCount != null && favoriteCount > 0);
        }
        return vo;
    }

    @Override
    public IPage<ItemVO> pageMine(Long sellerId, ItemQuery query) {
        Page<Item> page = new Page<>(pageOf(query), sizeOf(query));
        LambdaQueryWrapper<Item> wrapper = Wrappers.<Item>lambdaQuery()
                .eq(Item::getSellerId, sellerId);
        if (query.getStatus() != null && !query.getStatus().isBlank()) {
            wrapper.eq(Item::getStatus, query.getStatus());
        }
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Item::getTitle, keyword).or().like(Item::getDescription, keyword));
        }
        wrapper.orderByDesc(Item::getCreatedAt).orderByDesc(Item::getId);
        return convertPage(baseMapper.selectPage(page, wrapper));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long sellerId, Long itemId, ItemSaveDTO dto) {
        Item item = assertOwner(itemId, sellerId);
        if (ItemStatusEnum.SOLD.getCode().equals(item.getStatus())) {
            throw BusinessException.param("已售出商品不可编辑");
        }
        validateSaveDTO(dto);

        item.setTitle(dto.getTitle().trim());
        item.setDescription(dto.getDescription().trim());
        item.setCategoryId(dto.getCategoryId());
        item.setPrice(dto.getPrice());
        item.setOriginalPrice(dto.getOriginalPrice());
        item.setConditionLevel(dto.getConditionLevel());
        item.setCampus(dto.getCampus().trim());
        item.setTradeType(dto.getTradeType());
        item.setContact(dto.getContact() == null ? null : dto.getContact().trim());
        // 编辑后一律重新进入待审核
        item.setStatus(ItemStatusEnum.PENDING.getCode());
        item.setRejectReason(null);
        item.setAuditedBy(null);
        item.setAuditedAt(null);

        List<String> images = dto.getImages() == null ? Collections.emptyList() : dto.getImages();
        item.setCoverImage(images.isEmpty() ? "" : images.get(0));
        updateById(item);
        replaceImages(item.getId(), images);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long sellerId, Long itemId) {
        Item item = assertOwner(itemId, sellerId);
        Long ongoing = orderMapper.selectCount(Wrappers.<Order>lambdaQuery()
                .eq(Order::getItemId, item.getId())
                .in(Order::getStatus, ONGOING_ORDER_STATUS));
        if (ongoing != null && ongoing > 0) {
            throw BusinessException.param("有进行中的订单，无法删除");
        }
        removeById(itemId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ItemVO updateStatus(Long sellerId, Long itemId, ItemStatusDTO dto) {
        Item item = assertOwner(itemId, sellerId);
        String action = dto.getAction() == null ? "" : dto.getAction().trim();

        if (ItemStatusEnum.SOLD.getCode().equals(item.getStatus())) {
            throw BusinessException.param("当前商品状态不支持该操作");
        }
        if (ItemStatusEnum.ON_SALE.getCode().equals(action)) {
            if (!ItemStatusEnum.OFF_SHELF.getCode().equals(item.getStatus())) {
                throw BusinessException.param("当前商品状态不支持该操作");
            }
            // 重新上架需重新审核
            item.setStatus(ItemStatusEnum.PENDING.getCode());
            item.setRejectReason(null);
            item.setAuditedBy(null);
            item.setAuditedAt(null);
        } else if (ItemStatusEnum.OFF_SHELF.getCode().equals(action)) {
            if (!ItemStatusEnum.ON_SALE.getCode().equals(item.getStatus())) {
                throw BusinessException.param("当前商品状态不支持该操作");
            }
            item.setStatus(ItemStatusEnum.OFF_SHELF.getCode());
        } else {
            throw BusinessException.param("当前商品状态不支持该操作");
        }
        updateById(item);
        return toVO(item);
    }

    @Override
    public IPage<AdminItemVO> adminPage(ItemAuditQuery query) {
        Page<AdminItemVO> page = new Page<>(pageOf(query), sizeOf(query));
        IPage<AdminItemVO> result = baseMapper.selectAdminPage(page, query);
        result.getRecords().forEach(vo -> vo.setStatusDesc(ItemStatusEnum.descOf(vo.getStatus())));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditPass(Long reviewerId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw BusinessException.param("请选择要审核的商品");
        }
        LocalDateTime now = LocalDateTime.now();
        int passed = 0;
        for (Long id : ids) {
            Item item = getEntity(id);
            if (item == null || !ItemStatusEnum.PENDING.getCode().equals(item.getStatus())) {
                continue;
            }
            item.setStatus(ItemStatusEnum.ON_SALE.getCode());
            item.setRejectReason(null);
            item.setAuditedBy(reviewerId);
            item.setAuditedAt(now);
            updateById(item);
            passed++;
        }
        return passed;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReject(Long reviewerId, Long itemId, String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw BusinessException.param("拒绝原因不能为空");
        }
        Item item = getEntity(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (!ItemStatusEnum.PENDING.getCode().equals(item.getStatus())) {
            throw BusinessException.param("当前商品状态不支持该操作");
        }
        item.setStatus(ItemStatusEnum.REJECTED.getCode());
        item.setRejectReason(reason.trim());
        item.setAuditedBy(reviewerId);
        item.setAuditedAt(LocalDateTime.now());
        updateById(item);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceOffline(Long reviewerId, Long itemId, String reason) {
        Item item = getEntity(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (!ItemStatusEnum.ON_SALE.getCode().equals(item.getStatus())) {
            throw BusinessException.param("当前商品状态不支持该操作");
        }
        item.setStatus(ItemStatusEnum.OFF_SHELF.getCode());
        if (reason != null && !reason.trim().isEmpty()) {
            String trimmed = reason.trim();
            item.setRejectReason(trimmed.length() > 200 ? trimmed.substring(0, 200) : trimmed);
        }
        item.setAuditedBy(reviewerId);
        item.setAuditedAt(LocalDateTime.now());
        updateById(item);
    }

    @Override
    public Item getEntity(Long itemId) {
        if (itemId == null) {
            return null;
        }
        return getById(itemId);
    }

    @Override
    public ItemVO toVO(Item item) {
        if (item == null) {
            return null;
        }
        ItemVO vo = new ItemVO();
        copyBase(item, vo);
        Category category = categoryMapper.selectById(item.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        User seller = userMapper.selectById(item.getSellerId());
        if (seller != null) {
            vo.setSellerNickname(seller.getNickname());
        }
        return vo;
    }

    /* --------------------------- 内部工具方法 --------------------------- */

    /**
     * 校验归属并返回商品实体。
     *
     * @param itemId   商品 ID
     * @param sellerId 卖家 ID
     * @return 商品实体
     */
    private Item assertOwner(Long itemId, Long sellerId) {
        Item item = getEntity(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }
        if (!item.getSellerId().equals(sellerId)) {
            throw BusinessException.forbidden("无权操作该商品");
        }
        return item;
    }

    /**
     * 校验发布 / 编辑入参的业务规则（枚举、价格、图片数量）。
     *
     * @param dto 入参
     */
    private void validateSaveDTO(ItemSaveDTO dto) {
        if (!ConditionEnum.isValid(dto.getConditionLevel())) {
            throw BusinessException.param("成色不合法");
        }
        if (!TradeTypeEnum.isValid(dto.getTradeType())) {
            throw BusinessException.param("交易方式不合法");
        }
        if (categoryMapper.selectById(dto.getCategoryId()) == null) {
            throw BusinessException.param("分类不存在");
        }
        BigDecimal price = dto.getPrice();
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0
                || price.compareTo(new BigDecimal("99999.99")) > 0) {
            throw BusinessException.param("价格必须大于 0 且不超过 99999.99");
        }
        if (dto.getOriginalPrice() != null && dto.getOriginalPrice().compareTo(price) < 0) {
            throw BusinessException.param("原价不能低于现价");
        }
        if (dto.getImages() != null && dto.getImages().size() > MAX_IMAGES) {
            throw BusinessException.param("商品图片最多 9 张");
        }
    }

    /**
     * 应用筛选条件。
     *
     * @param wrapper 查询条件
     * @param query   入参
     */
    private void applyFilters(LambdaQueryWrapper<Item> wrapper, ItemQuery query) {
        if (query.getKeyword() != null && !query.getKeyword().isBlank()) {
            String keyword = query.getKeyword().trim();
            wrapper.and(w -> w.like(Item::getTitle, keyword).or().like(Item::getDescription, keyword));
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(Item::getCategoryId, query.getCategoryId());
        }
        if (query.getCampus() != null && !query.getCampus().isBlank()) {
            wrapper.eq(Item::getCampus, query.getCampus().trim());
        }
        if (query.getMinPrice() != null) {
            wrapper.ge(Item::getPrice, query.getMinPrice());
        }
        if (query.getMaxPrice() != null) {
            wrapper.le(Item::getPrice, query.getMaxPrice());
        }
    }

    /**
     * 应用排序（必须带唯一列兜底，避免翻页重复/丢数据）。
     *
     * @param wrapper 查询条件
     * @param sort    排序方式
     */
    private void applySort(LambdaQueryWrapper<Item> wrapper, String sort) {
        String mode = sort == null ? "latest" : sort.trim();
        switch (mode) {
            case "price_asc" -> wrapper.orderByAsc(Item::getPrice).orderByDesc(Item::getId);
            case "price_desc" -> wrapper.orderByDesc(Item::getPrice).orderByDesc(Item::getId);
            case "hot" -> wrapper.orderByDesc(Item::getViewCount)
                    .orderByDesc(Item::getFavoriteCount)
                    .orderByDesc(Item::getId);
            default -> wrapper.orderByDesc(Item::getCreatedAt).orderByDesc(Item::getId);
        }
    }

    /**
     * 分页结果转 VO 并批量补齐分类名与卖家昵称。
     *
     * @param itemPage 实体分页
     * @return VO 分页
     */
    private IPage<ItemVO> convertPage(IPage<Item> itemPage) {
        List<Item> records = itemPage.getRecords();
        Map<Long, String> categoryNames = new HashMap<>();
        Map<Long, String> sellerNames = new HashMap<>();
        if (!records.isEmpty()) {
            Set<Long> categoryIds = records.stream()
                    .map(Item::getCategoryId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
            Set<Long> sellerIds = records.stream()
                    .map(Item::getSellerId).filter(java.util.Objects::nonNull).collect(Collectors.toSet());
            if (!categoryIds.isEmpty()) {
                for (Category category : categoryMapper.selectByIds(categoryIds)) {
                    categoryNames.put(category.getId(), category.getName());
                }
            }
            if (!sellerIds.isEmpty()) {
                for (User user : userMapper.selectByIds(sellerIds)) {
                    sellerNames.put(user.getId(), user.getNickname());
                }
            }
        }

        Page<ItemVO> voPage = new Page<>(itemPage.getCurrent(), itemPage.getSize(), itemPage.getTotal());
        List<ItemVO> voList = new ArrayList<>(records.size());
        for (Item item : records) {
            ItemVO vo = new ItemVO();
            copyBase(item, vo);
            vo.setCategoryName(categoryNames.get(item.getCategoryId()));
            vo.setSellerNickname(sellerNames.get(item.getSellerId()));
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 实体基础字段复制到 VO。
     *
     * @param item 实体
     * @param vo   目标 VO
     */
    private void copyBase(Item item, ItemVO vo) {
        vo.setId(item.getId());
        vo.setTitle(item.getTitle());
        vo.setCoverImage(item.getCoverImage());
        vo.setPrice(item.getPrice());
        vo.setOriginalPrice(item.getOriginalPrice());
        vo.setConditionLevel(item.getConditionLevel());
        vo.setConditionDesc(ConditionEnum.descOf(item.getConditionLevel()));
        vo.setCampus(item.getCampus());
        vo.setTradeType(item.getTradeType());
        vo.setTradeTypeDesc(TradeTypeEnum.descOf(item.getTradeType()));
        vo.setStatus(item.getStatus());
        vo.setStatusDesc(ItemStatusEnum.descOf(item.getStatus()));
        vo.setViewCount(item.getViewCount());
        vo.setFavoriteCount(item.getFavoriteCount());
        vo.setCategoryId(item.getCategoryId());
        vo.setSellerId(item.getSellerId());
        vo.setRejectReason(item.getRejectReason());
        vo.setCreatedAt(item.getCreatedAt());
    }

    /**
     * 查询商品的图片列表（按 sort 升序）。
     *
     * @param itemId 商品 ID
     * @return 图片 VO 列表
     */
    private List<ItemImageVO> listImages(Long itemId) {
        List<ItemImage> images = itemImageMapper.selectList(Wrappers.<ItemImage>lambdaQuery()
                .eq(ItemImage::getItemId, itemId)
                .orderByAsc(ItemImage::getSort)
                .orderByAsc(ItemImage::getId));
        List<ItemImageVO> result = new ArrayList<>(images.size());
        for (ItemImage image : images) {
            result.add(new ItemImageVO(image.getId(), image.getUrl(), image.getSort()));
        }
        return result;
    }

    /**
     * 整组物理替换商品图片。
     *
     * @param itemId 商品 ID
     * @param urls   图片 URL 列表
     */
    private void replaceImages(Long itemId, List<String> urls) {
        itemImageMapper.delete(Wrappers.<ItemImage>lambdaQuery().eq(ItemImage::getItemId, itemId));
        if (urls == null || urls.isEmpty()) {
            return;
        }
        int sort = 0;
        for (String url : urls) {
            if (url == null || url.isBlank()) {
                continue;
            }
            ItemImage image = new ItemImage();
            image.setItemId(itemId);
            image.setUrl(url.trim());
            image.setSort(sort++);
            itemImageMapper.insert(image);
        }
    }

    /**
     * 取页码，空值兜底为 1。
     *
     * @param query 查询条件
     * @return 页码
     */
    private long pageOf(BaseQuery query) {
        return query.getCurrentPage() == null ? 1L : query.getCurrentPage();
    }

    /**
     * 取每页条数，空值兜底为 10。
     *
     * @param query 查询条件
     * @return 条数
     */
    private long sizeOf(BaseQuery query) {
        return query.getPageSize() == null ? 10L : query.getPageSize();
    }
}
