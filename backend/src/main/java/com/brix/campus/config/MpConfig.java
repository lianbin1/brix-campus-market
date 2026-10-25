package com.brix.campus.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 配置：分页插件、Mapper 扫描、审计字段自动填充、Long → String 序列化。
 */
@Configuration
@MapperScan("com.brix.campus.mapper")
public class MpConfig {

    /**
     * 分页插件。必须依赖 {@code mybatis-plus-jsqlparser-4.9}，否则编译/运行期找不到符号。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页上限，防止恶意超大分页
        pagination.setMaxLimit(100L);
        // 页码越界返回空集，而不是回到第一页
        pagination.setOverflow(false);
        interceptor.addInnerInterceptor(pagination);
        return interceptor;
    }

    /**
     * 审计字段填充器：createdAt / updatedAt。
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MyMetaObjectHandler();
    }

    /**
     * 所有 {@code Long} 包装类型序列化为字符串，避免 JS 53 位精度丢失。
     *
     * <p>⚠️ 只注册 {@code Long.class}，不注册 {@code long.class}：
     * 否则 {@code IPage.total/size/current} 会变成字符串，前端分页组件页码算错（架构 R12）。</p>
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder.serializerByType(Long.class, ToStringSerializer.instance);
    }
}
