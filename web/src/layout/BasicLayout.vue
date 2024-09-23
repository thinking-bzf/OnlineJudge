<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobleHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://github.com/thinking-bzf" target="_blank">
          awsling github
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<style>
#basicLayout .header {
  background-color: #002a98;
  color: white;
  line-height: 64px;
}

#basicLayout .content {
  background: linear-gradient(to right, #0f2b3d, #1b3b4b);
  margin-bottom: 16px;
}

#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: sticky;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
</style>
<script setup lang="ts">
import GlobleHeader from "@/components/GlobleHeader.vue";
</script>
