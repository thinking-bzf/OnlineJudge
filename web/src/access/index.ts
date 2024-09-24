import router from "@/router";
import store from "@/store";
import ACCESS_ENUM from "@/access/AccessEnum";
import checkAccess from "@/access/CheckAccess";

// 在index中无法使用useRouter和useStore
// const router = useRouter();
// const store = useStore();

// 判断是否有权限
router.beforeEach(async (to, from, next) => {
  const loginUser = store.state.user.loginUser;
  // 自动登录
  if (!loginUser || !loginUser.userRole) {
    // 加 await 是为了等待登录成功后再执行后续的代码
    await store.dispatch("user/getLoginUser");
  }
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NOT_LOGIN;
  // 如果当前需要的权限不是未登录，则需要判断是否登录，否则直接放行
  if (needAccess !== ACCESS_ENUM.NOT_LOGIN) {
    // 判断是否登录，未登录则拦截到登录页
    if (!loginUser || !loginUser.userRole) {
      next(`/user/login?redirect=${to.fullPath}`);
      return;
    }
    // 判断是否有对应的权限
    if (!checkAccess(loginUser, needAccess)) {
      next(`/noAuth`);
      return;
    }
  }
  next();
});
