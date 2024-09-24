<template>
  <div id="app">
    <BasicLayout />
  </div>
</template>

<style>
#app {
}
</style>
<script setup lang="ts">
import BasicLayout from "@/layout/BasicLayout.vue";
import { useRouter } from "vue-router";
import { useStore } from "vuex";
import { onMounted } from "vue";

// 全局初始化函数，在全局单词调用的代码，都可以写在这里
const doInit = () => {
  console.log("init");
};

// 在页面加载完成后调用初始化函数
onMounted(() => {
  doInit();
});

const router = useRouter();
const store = useStore();
// 判断是否有权限
router.beforeEach((to, from, next) => {
  if (to.meta?.access === "canAdmin") {
    if (store.state.user.loginUser?.role !== "admin") {
      next({ path: "/noAuth" });
      return;
    }
  }
  next();
});
</script>
