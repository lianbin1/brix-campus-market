package com.brix.campus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Brix Campus Market 启动类。
 *
 * <p>必须放在根包 {@code com.brix.campus} 下，否则子包（controller / service / mapper）不会被组件扫描。
 * 开启定时任务用于「30 分钟未付款自动关单」（PRD E06）。</p>
 */
@EnableScheduling
@SpringBootApplication
public class BrixCampusApplication {

    public static void main(String[] args) {
        SpringApplication.run(BrixCampusApplication.class, args);
    }
}
