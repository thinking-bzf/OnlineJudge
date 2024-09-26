<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <a href="https://github.com/thinking-bzf" target="_blank">
          Awsling github
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<style>
#basicLayout .header {
  line-height: 75px;
}

#basicLayout .content {
  background: linear-gradient(to right, #f0f2f5, #e8e8e8);
  margin-bottom: 16px;
  padding: 20px;
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
import GlobalHeader from "@/components/GlobalHeader.vue";
</script>
