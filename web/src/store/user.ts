// getters
import { StoreOptions } from "vuex";

const getters = {};

export default {
  namespaced: true,
  // initial state
  state: () => ({
    loginUser: {
      userName: "未登录",
      role: "notLogin",
    },
  }),
  getters,
  actions: {
    async getLoginUser({ commit, state }, payload) {
      commit("updateUser", { userName: "awsling" });
    },
  },
  mutations: {
    updateUser(state, payload) {
      state.loginUser = payload;
    },
  },
} as StoreOptions<any>;
