<template>
  <div id="globalHeader">
    <div class="menu-demo">
      <a-menu
        mode="horizontal"
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
  </div>
</template>

<script setup lang="ts">
import { routes } from "@/router/routes";
import { useRouter } from "vue-router";

const router = useRouter();

const doMenuClick = (key) => {
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
