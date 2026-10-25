package com.brix.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.brix.campus.common.BusinessException;
import com.brix.campus.mapper.ItemMapper;
import com.brix.campus.mapper.OrderMapper;
import com.brix.campus.mapper.ReviewMapper;
import com.brix.campus.mapper.UserMapper;
import com.brix.campus.model.Item;
import com.brix.campus.model.Order;
import com.brix.campus.model.Review;
import com.brix.campus.model.User;
import com.brix.campus.model.dto.ReviewCreateDTO;
import com.brix.campus.model.enums.OrderStatusEnum;
import com.brix.campus.model.query.CommentQuery;
import com.brix.campus.model.vo.ReviewPageVO;
import com.brix.campus.model.vo.ReviewSummaryVO;
import com.brix.campus.model.vo.ReviewVO;
import com.brix.campus.service.ReviewService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 评价服务实现。
 *
 * <p>信用分规则（PRD G03）：4-5 星 +2、3 星 +1、1-2 星 -1，初始 100，范围 clamp 到 [0,200]。</p>
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewServiceImpl.class);

    /** 信用分下限 */
    private static final int MIN_CREDIT = 0;
    /** 信用分上限 */
    private static final int MAX_CREDIT = 200;
    /** 评价图片上限 */
    private static final int MAX_REVIEW_IMAGES = 3;

    private final ReviewMapper reviewMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ItemMapper itemMapper;
    private final ObjectMapper objectMapper;

    public ReviewServiceImpl(ReviewMapper reviewMapper,
                             OrderMapper orderMapper,
                             UserMapper userMapper,
                             ItemMapper itemMapper,
                             ObjectMapper objectMapper) {
        this.reviewMapper = reviewMapper;
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.itemMapper = itemMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewVO create(Long buyerId, Long orderId, ReviewCreateDTO dto) {
        Order order = orderId == null ? null : orderMapper.selectById(orderId);
        if (order == null) {
            throw BusinessException.notFound("订单不存在");
        }
        if (!order.getBuyerId().equals(buyerId)) {
            throw BusinessException.forbidden("无权评价该订单");
        }
        if (!OrderStatusEnum.DONE.getCode().equals(order.getStatus())) {
            throw BusinessException.param("仅已完成订单可评价");
        }
        Long exists = reviewMapper.selectCount(Wrappers.<Review>lambdaQuery()
                .eq(Review::getOrderId, orderId));
        if (exists != null && exists > 0) {
            throw BusinessException.param("该订单已评价");
        }
        Integer rating = dto.getRating();
        if (rating == null || rating < 1 || rating > 5) {
            throw BusinessException.param("评分必须为 1-5");
        }
        List<String> images = dto.getImages() == null ? Collections.emptyList() : dto.getImages();
        if (images.size() > MAX_REVIEW_IMAGES) {
            throw BusinessException.param("评价图片最多 3 张");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        review.setItemId(order.getItemId());
        review.setBuyerId(buyerId);
        review.setSellerId(order.getSellerId());
        review.setRating(rating);
        review.setContent(dto.getContent() == null ? "" : dto.getContent().trim());
        review.setImages(writeImages(images));
        reviewMapper.insert(review);

        adjustCreditScore(order.getSellerId(), rating);

        ReviewVO vo = toVO(review);
        User buyer = userMapper.selectById(buyerId);
        if (buyer != null) {
            vo.setBuyerNickname(maskNickname(buyer.getNickname()));
        }
        Item item = itemMapper.selectById(order.getItemId());
        if (item != null) {
            vo.setItemTitle(item.getTitle());
        }
        return vo;
    }

    @Override
    public ReviewPageVO pageBySeller(Long sellerId, CommentQuery query) {
        long current = query.getCurrentPage() == null ? 1L : query.getCurrentPage();
        long size = query.getPageSize() == null ? 10L : query.getPageSize();
        Page<Review> page = new Page<>(current, size);

        IPage<Review> reviewPage = reviewMapper.selectPage(page, Wrappers.<Review>lambdaQuery()
                .eq(Review::getSellerId, sellerId)
                .orderByDesc(Review::getCreatedAt)
                .orderByDesc(Review::getId));

        List<Review> records = reviewPage.getRecords();
        Map<Long, String> nicknames = new HashMap<>();
        Map<Long, String> itemTitles = new HashMap<>();
        if (!records.isEmpty()) {
            Set<Long> buyerIds = new HashSet<>();
            Set<Long> itemIds = new HashSet<>();
            records.forEach(review -> {
                buyerIds.add(review.getBuyerId());
                itemIds.add(review.getItemId());
            });
            for (User user : userMapper.selectByIds(buyerIds)) {
                nicknames.put(user.getId(), maskNickname(user.getNickname()));
            }
            for (Item item : itemMapper.selectByIds(itemIds)) {
                itemTitles.put(item.getId(), item.getTitle());
            }
        }

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        List<ReviewVO> voList = new ArrayList<>(records.size());
        for (Review review : records) {
            ReviewVO vo = toVO(review);
            vo.setBuyerNickname(nicknames.get(review.getBuyerId()));
            vo.setItemTitle(itemTitles.get(review.getItemId()));
            voList.add(vo);
        }
        voPage.setRecords(voList);

        ReviewSummaryVO summary = new ReviewSummaryVO();
        Double avg = reviewMapper.selectAvgRating(sellerId);
        summary.setAvgRating(avg == null
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        summary.setTotal(reviewPage.getTotal());

        ReviewPageVO result = new ReviewPageVO();
        result.setPage(voPage);
        result.setSummary(summary);
        return result;
    }

    /**
     * 按评分调整卖家信用分（clamp 到 [0,200]）。
     *
     * @param sellerId 卖家 ID
     * @param rating   评分
     */
    private void adjustCreditScore(Long sellerId, int rating) {
        User seller = userMapper.selectById(sellerId);
        if (seller == null) {
            return;
        }
        int delta;
        if (rating >= 4) {
            delta = 2;
        } else if (rating == 3) {
            delta = 1;
        } else {
            delta = -1;
        }
        int current = seller.getCreditScore() == null ? 100 : seller.getCreditScore();
        int updated = Math.min(MAX_CREDIT, Math.max(MIN_CREDIT, current + delta));
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, sellerId)
                .set(User::getCreditScore, updated));
    }

    /**
     * 昵称脱敏：保留首尾字符。
     *
     * @param nickname 原昵称
     * @return 脱敏昵称
     */
    private String maskNickname(String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            return "匿名用户";
        }
        int length = nickname.length();
        if (length == 1) {
            return nickname;
        }
        if (length == 2) {
            return nickname.charAt(0) + "*";
        }
        return nickname.charAt(0) + "*".repeat(length - 2) + nickname.charAt(length - 1);
    }

    /**
     * 图片列表序列化为 JSON 字符串。
     *
     * @param images 图片 URL 列表
     * @return JSON 字符串
     */
    private String writeImages(List<String> images) {
        if (images == null || images.isEmpty()) {
            return "";
        }
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            log.warn("评价图片序列化失败", e);
            return "";
        }
    }

    /**
     * JSON 字符串反序列化为图片列表。
     *
     * @param json JSON 字符串
     * @return 图片 URL 列表
     */
    private List<String> readImages(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.warn("评价图片反序列化失败：{}", json);
            return new ArrayList<>();
        }
    }

    /**
     * 实体转 VO。
     *
     * @param review 评价实体
     * @return VO
     */
    private ReviewVO toVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setOrderId(review.getOrderId());
        vo.setItemId(review.getItemId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setImages(readImages(review.getImages()));
        vo.setSellerId(review.getSellerId());
        vo.setCreatedAt(review.getCreatedAt());
        return vo;
    }
}
