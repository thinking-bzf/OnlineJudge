<template>
  <a-row class="globalHeader" style="margin-bottom: 16px" align="center">
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
          <a-menu-item v-for="item in routes" :key="item.path">
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
import { ref } from "vue";
import { useStore } from "vuex";

const router = useRouter();

// 默认主页
const selectedKeys = ref(["/"]);
// 路由跳转后, 更新选中的菜单项
router.afterEach((to, from, next) => {
  selectedKeys.value = [to.path];
});

const store = useStore();
// console.log(store.state.user.loginUser.userName);

// setTimeout(() => {
//   store.dispatch("user/getLoginUser", {
//     userName: "awsling",
//   });
// }, 3000);

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
