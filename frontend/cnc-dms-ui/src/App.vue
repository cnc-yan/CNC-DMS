<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import MainLayout from '@/layout/MainLayout.vue'

const route = useRoute()

// レイアウトが必要なルートかどうかを判定（ログインページ以外はレイアウト表示）
const hasLayout = computed(() => route.meta?.layout !== false)
</script>

<template>
  <MainLayout v-if="hasLayout" />
  <router-view v-else />
</template>
