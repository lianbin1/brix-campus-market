package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.FavoriteMapper;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.Favorite;
import com.brix.campus.model.Item;
import com.brix.campus.model.User;
import com.brix.campus.model.enums.ItemStatusEnum;
import com.brix.campus.model.query.BaseQuery;
import com.brix.campus.model.vo.FavoriteVO;
import com.brix.campus.service.FavoriteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 收藏服务实现。
 *
 * <p>取消收藏用<b>物理删除</b>，否则 {@code uk_user_item} 唯一索引会挡住再次收藏。</p>
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    public FavoriteServiceImpl(FavoriteMapper favoriteMapper, ItemMapper itemMapper, UserMapper userMapper) {
        this.favoriteMapper = favoriteMapper;
        this.itemMapper = itemMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> toggle(Long userId, Long itemId) {
        Item item = itemMapper.selectById(itemId);
        if (item == null) {
            throw BusinessException.notFound("商品不存在");
        }

        Favorite existing = favoriteMapper.selectOne(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getItemId, itemId)
                .last("LIMIT 1"));

        boolean favorited;
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                    .eq(Item::getId, itemId)
                    .setSql("favorite_count = GREATEST(favorite_count - 1, 0)"));
            favorited = false;
        } else {
            Favorite favorite = new Favorite();
            favorite.setUserId(userId);
            favorite.setItemId(itemId);
            favoriteMapper.insert(favorite);
            itemMapper.update(null, new LambdaUpdateWrapper<Item>()
                    .eq(Item::getId, itemId)
                    .setSql("favorite_count = favorite_count + 1"));
            favorited = true;
        }

        Item latest = itemMapper.selectById(itemId);
        Map<String, Object> result = new HashMap<>(4);
        result.put("favorited", favorited);
        result.put("favoriteCount", latest == null || latest.getFavoriteCount() == null
                ? 0 : latest.getFavoriteCount());
        return result;
    }

    @Override
    public IPage<FavoriteVO> page(Long userId, BaseQuery query) {
        long current = query.getCurrentPage() == null ? 1L : query.getCurrentPage();
        long size = query.getPageSize() == null ? 10L : query.getPageSize();
        Page<Favorite> page = new Page<>(current, size);

        IPage<Favorite> favoritePage = favoriteMapper.selectPage(page, Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt)
                .orderByDesc(Favorite::getId));

        List<Favorite> records = favoritePage.getRecords();
        Map<Long, Item> itemMap = new HashMap<>();
        Map<Long, String> sellerNames = new HashMap<>();
        if (!records.isEmpty()) {
            Set<Long> itemIds = new HashSet<>();
            records.forEach(favorite -> itemIds.add(favorite.getItemId()));
            for (Item item : itemMapper.selectByIds(itemIds)) {
                itemMap.put(item.getId(), item);
            }
            Set<Long> sellerIds = new HashSet<>();
            itemMap.values().stream().map(Item::getSellerId).filter(Objects::nonNull).forEach(sellerIds::add);
            if (!sellerIds.isEmpty()) {
                for (User user : userMapper.selectByIds(sellerIds)) {
                    sellerNames.put(user.getId(), user.getNickname());
                }
            }
        }

        Page<FavoriteVO> voPage = new Page<>(favoritePage.getCurrent(), favoritePage.getSize(),
                favoritePage.getTotal());
        List<FavoriteVO> voList = new ArrayList<>(records.size());
        for (Favorite favorite : records) {
            Item item = itemMap.get(favorite.getItemId());
            FavoriteVO vo = new FavoriteVO();
            vo.setId(favorite.getId());
            vo.setItemId(favorite.getItemId());
            vo.setCreatedAt(favorite.getCreatedAt());
            if (item != null) {
                vo.setItemTitle(item.getTitle());
                vo.setItemCover(item.getCoverImage());
                vo.setItemPrice(item.getPrice());
                vo.setItemStatus(item.getStatus());
                vo.setItemStatusDesc(ItemStatusEnum.descOf(item.getStatus()));
                vo.setSellerNickname(sellerNames.get(item.getSellerId()));
            }
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }
}
