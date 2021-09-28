import Vue from "vue";
import VueRouter from "vue-router";

import NotFound from "../components/NotFound.vue";
import Auth from "../components/Auth/index.vue";
import admin from "../components/admin/Admin.vue";
import ProfilePage from "../components/UserProfile/index.vue";
import ModifyUserPage from "../components/UserProfile/ModifyUserPage.vue";
import BusinessProfile from "../components/BusinessProfile/index.vue";
import SearchBusinessResults from "../components/SearchBusinessPage.vue";
import SearchSaleItems from "../components/SaleListing/SearchSaleItems.vue";
import SearchResults from "../components/SearchResults.vue";
import HomePage from "../components/home/HomePage.vue";
import ProductCatalogue from "../components/ProductCatalogue.vue";
import Inventory from "../components/Inventory.vue";
import SalePage from "../components/SalePage.vue";
import Marketplace from "../components/marketplace/Marketplace.vue";
import UserCards from "../components/marketplace/UserCards.vue";
import ImageManager from "../components/image/ImageManager.vue";
import { COOKIE, getCookie } from "@/utils";
import { getStore } from "@/store";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    redirect: "/auth"
  },
  {
    path: "/auth",
    component: Auth,
    meta: { title: 'Authenticate' }
  },
  {
    path: "/home",
    component: HomePage,
    meta: { title: 'Home' }
  },
  {
    path: "/profile",
    component: ProfilePage,
    meta: { title: 'Profile' }
  },
  {
    path: "/profile/:id/modify",
    component: ModifyUserPage,
    meta: { title: 'Modify user' },
  },
  {
    path: "/profile/:id",
    component: ProfilePage,
    meta: { title: 'Profile' }
  },
  {
    path: "/business/:id",
    component: BusinessProfile,
    name: "businessProfile",
    meta: { title: 'Business' },
  },
  {
    path: "/admin",
    component: admin,
    meta: { title: 'Admin' }
  },
  {
    path: "/search",
    component: SearchResults,
    meta: { title: 'Search Users' }
  },
  {
    path: "/search/business",
    component: SearchBusinessResults,
    meta: { title: 'Search Businesses' }
  },
  {
    path: "/search/sales",
    component: SearchSaleItems,
    meta: { title: 'Search Sale Items' }
  },
  {
    path: "/business/:id/products",
    component: ProductCatalogue,
    meta: { title: 'Business Products' }
  },
  {
    path: "/business/:id/listings",
    component: SalePage,
    meta: {title: 'Sales'}
  },
  { // Router for inventory page (Inventory page frontend dev please use this and remove this line afterward)
    path: "/business/:id/inventory",
    component: Inventory,
    meta: { title: 'Inventory' }
  },
  {
    path: "/marketplace",
    component: Marketplace,
    meta: { title: 'Marketplace' }
  },
  {
    path: '/usercards/:id',
    component: UserCards,
    meta: { title: 'User Cards' },
  },
  {
    path: '/imagemanager',
    component: ImageManager,
    meta: { title: 'Image Manager' },
  },
  {
    path: "*",
    component: NotFound,
    meta: { title: 'Not Found' }
  }
];


const router = new VueRouter({
  mode: "history",
  base: process.env.VUE_APP_BASE_URL,
  routes,
});

// Set Page Title
router.afterEach(to => {
  Vue.nextTick(() => {
    const title = to.meta.title;
    document.title = title ? `${title} - LEFT_OVERS` : 'LEFT_OVERS';
  });
});


// Prevent navigation away from /auth if not logged in and prevent nagivation to /auth if already logged in
router.beforeEach(async (to, from, next) => {
  const store = getStore();
  // Handle initial navigation
  if (from === VueRouter.START_LOCATION) {
    const cookie = getCookie(COOKIE.USER);
    if (cookie) {
      await store.dispatch('autoLogin', cookie.split('=')[1]);
    }
  }

  if (to.path === '/auth' && store.getters.isLoggedIn) {
    next('/home');
  } else if (to.path !== '/auth' && !store.getters.isLoggedIn) {
    next('/auth');
  } else {
    next();
  }
});

// Clear global error on navigation
router.afterEach(() => {
  // After changing pages clear the global error message
  getStore().commit('clearError');
});


export default router;
