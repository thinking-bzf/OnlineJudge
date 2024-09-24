<template>
  <a-row class="globalHeader" align="center" :wrap="false">
    <a-col flex="auto">
      <div class="menu">
        <a-menu
          mode="horizontal"
          :selected-keys="selectedKeys"
          :default-selected-keys="['1']"
          @menu-item-click="doMenuClick"
        >
          <a-menu-item
            key="0"
            :style="{ padding: 0, marginRight: '38px' }"
            disabled
          >
            <div class="title-bar">
              <img class="logo" src="../assets/elysia.jpg" />
              <div class="title">SmartCode</div>
            </div>
          </a-menu-item>
          <a-menu-item v-for="item in visibleRoutes" :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </a-menu>
      </div>
    </a-col>
    <a-col flex="100px">
      <div>{{ store.state.user?.loginUser?.userName }}</div>
    </a-col>
  </a-row>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";
import { computed, ref } from "vue";
import { useStore } from "vuex";
import checkAccess from "@/access/CheckAccess";
import ACCESS_ENUM from "@/access/AccessEnum";

const router = useRouter();
const store = useStore();

// 过滤出需要显示的菜单项
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    const loginUser = store.state.user?.loginUser;
    return checkAccess(loginUser, item?.meta?.access as string);
  });
});

// 默认主页
const selectedKeys = ref(["/"]);
// 路由跳转后, 更新选中的菜单项
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path];
});

// 模拟登录
setTimeout(() => {
  store.dispatch("user/getLoginUser", {
    userName: "awsling",
    userRole: ACCESS_ENUM.ADMIN,
  });
}, 3000);

// 菜单点击事件
const doMenuClick = (key: string) => {
  router.push({
    path: key,
  });
};
</script>
<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  font-size: 20px;
  margin-left: 10px;
}

.logo {
  height: 40px;
}
</style>
