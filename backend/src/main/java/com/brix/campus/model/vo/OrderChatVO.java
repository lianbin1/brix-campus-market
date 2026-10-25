package com.brix.campus.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单维度私信记录（K13，后台纠纷取证）。
 *
 * <p>优先返回「围绕该商品」的双方消息；若该商品下没有带 {@code item_id} 的消息，
 * 则退化为返回买卖双方之间的全部消息。空对话返回空数组，<b>不返回 404</b>。</p>
 */
@Data
public class OrderChatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单号 */
    private String orderNo;

    /** 商品标题（订单快照） */
    private String itemTitle;

    /** 买家 */
    private ChatUserVO buyer;

    /** 卖家 */
    private ChatUserVO seller;

    /** 双方消息，按发送时间升序 */
    private List<OrderChatMessageVO> messages = new ArrayList<>();

    /** 消息总条数 */
    private Integer total = 0;
}
