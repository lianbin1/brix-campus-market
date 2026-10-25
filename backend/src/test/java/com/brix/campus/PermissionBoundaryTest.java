package com.brix.campus;

import com.brix.campus.common.BusinessException;
import com.brix.campus.common.ResultCode;
import com.brix.campus.model.dto.ItemSaveDTO;
import com.brix.campus.service.OrderService;
import com.brix.campus.service.ItemService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * 权限边界测试（PRD 2.2 / 7.4）。
 *
 * <p>启用前提：先初始化数据库；商品 id=1、id=2 属于卖家 id=4，订单 id=1 的买卖双方为 1 与 8。</p>
 */
@Disabled("需先初始化数据库（db/schema.sql + db/data.sql）并配置 DB_USERNAME/DB_PASSWORD 后手工启用")
@SpringBootTest
class PermissionBoundaryTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderService orderService;

    @Test
    @Transactional
    @DisplayName("非本人编辑商品：抛 403 越权异常")
    void updateItem_notOwner_throwsForbidden() {
        ItemSaveDTO dto = new ItemSaveDTO();
        dto.setTitle("我要改别人的商品");
        dto.setDescription("越权测试");
        dto.setCategoryId(1L);
        dto.setPrice(new BigDecimal("1.00"));
        dto.setConditionLevel("LIGHT_USE");
        dto.setCampus("东校区");
        dto.setTradeType("MEET");
        dto.setImages(new ArrayList<>());

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                () -> itemService.update(5L, 1L, dto), "非本人编辑商品必须失败");
        Assertions.assertEquals(ResultCode.FORBIDDEN, ex.getResultCode(), "错误码必须为 403");
    }

    @Test
    @Transactional
    @DisplayName("非订单方查看订单详情：抛 403 越权异常")
    void orderDetail_notParticipant_throwsForbidden() {
        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                () -> orderService.detail(5L, "USER", 1L), "第三方查看订单必须失败");
        Assertions.assertEquals(ResultCode.FORBIDDEN, ex.getResultCode(), "错误码必须为 403");
    }

    @Test
    @Transactional
    @DisplayName("购买自己的商品：抛业务异常")
    void createOrder_buyOwnItem_throwsException() {
        com.brix.campus.model.dto.OrderCreateDTO dto = new com.brix.campus.model.dto.OrderCreateDTO();
        dto.setItemId(1L);
        dto.setTradeType("MEET");
        Assertions.assertThrows(BusinessException.class, () -> orderService.create(4L, dto),
                "不能购买自己发布的商品");
    }
}
