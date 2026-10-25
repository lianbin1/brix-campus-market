package com.brix.campus.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 下单入参（E01）。{@code tradeType} 仅当商品交易方式为 {@code BOTH} 时生效。
 */
@Data
public class OrderCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 商品 ID */
    @NotNull(message = "商品不能为空")
    private Long itemId;

    /** 买家选择的交易方式：MEET / SHIP（商品为 BOTH 时必填） */
    private String tradeType;

    /** 面交地点（校区 + 宿舍楼） */
    @Size(max = 100, message = "面交地点过长")
    private String meetPlace;

    /** 买家备注 / 邮寄收货信息占位 */
    @Size(max = 200, message = "备注不能超过 200 个字")
    private String buyerNote;
}
