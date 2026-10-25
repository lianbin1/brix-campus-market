<template>
  <div class="basic-layout">
    <NavBar />
    <main class="basic-layout__main brix-container">
      <router-view v-slot="{ Component }">
        <transition name="brix-fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    <AppFooter />
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
import AppFooter from './components/AppFooter.vue'
</script>

<style scoped>
.basic-layout {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.basic-layout__main {
  flex: 1;
  padding: 24px 0 0;
}
</style>
