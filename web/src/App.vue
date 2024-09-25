<template>
  <div id="app">
    <template v-if="route.path.startsWith('/user')">
      <router-view />
    </template>
    <template v-else>
      <BasicLayout />
    </template>
  </div>
</template>

<style>
#app {
}
</style>
<script setup lang="ts">
import BasicLayout from "@/layout/BasicLayout.vue";
import { useRoute } from "vue-router";
import { onMounted } from "vue";

const route = useRoute();

// 全局初始化函数，在全局单词调用的代码，都可以写在这里
const doInit = () => {
  console.log("init");
};

// 在页面加载完成后调用初始化函数
onMounted(() => {
  doInit();
});
</script>
