<template>
  <footer class="footer">
    <div class="brix-container footer__inner">
      <div class="footer__brand">
        <span class="footer__mark">B</span>
        <span>Brix Campus Market</span>
      </div>
      <p class="footer__desc">
        严格校园版二手交易平台 · 交易以校内面交为主、邮寄为备选 · 面交确认码保障「先验货再付款」
      </p>
      <p class="footer__meta">示例大学 · 东校区 / 西校区 / 南校区 · 仅用于课程演示</p>
    </div>
  </footer>
</template>

<script setup>
// 页脚：纯展示
</script>

<style scoped>
.footer {
  margin-top: 48px;
  padding: 28px 0 36px;
  background: #fff;
  border-top: 0.5px solid var(--brix-border);
}

.footer__inner {
  text-align: center;
  color: var(--brix-text-secondary);
}

.footer__brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--brix-text);
}

.footer__mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 22px;
  height: 22px;
  border-radius: 6px;
  background: var(--brix-primary);
  color: #fff;
  font-size: 12px;
}

.footer__desc {
  margin-top: 10px;
  font-size: 13px;
}

.footer__meta {
  margin-top: 4px;
  font-size: 12px;
  color: var(--brix-text-placeholder);
}
</style>
