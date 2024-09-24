// getters
import { StoreOptions } from "vuex";
import ACCESS_ENUM from "@/access/AccessEnum";

const getters = {};

export default {
  namespaced: true,
  // initial state
  state: () => ({
    loginUser: {
      userName: "未登录",
      userRole: ACCESS_ENUM.NOT_LOGIN,
    },
  }),
  getters,
  actions: {
    async getLoginUser({ commit, state }, payload) {
      // 改为远程请求
      commit("updateUser", payload);
    },
  },
  mutations: {
    updateUser(state, payload) {
      state.loginUser = payload;
    },
  },
} as StoreOptions<any>;
