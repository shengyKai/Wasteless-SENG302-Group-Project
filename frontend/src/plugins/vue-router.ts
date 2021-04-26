import Vue from "vue";
import VueRouter from "vue-router";

import Auth from "../components/Auth/index.vue";
import admin from "../components/Admin.vue";
import ProfilePage from "../components/UserProfile.vue";
import BusinessProfile from "../components/BusinessProfile/index.vue";
import SearchResults from "../components/SearchResults.vue";
import CreateBusiness from "../components/BusinessProfile/CreateBusiness.vue";
import HomePage from "../components/HomePage.vue";
import Catalogue from "../components/ProductCatalogueItem.vue";

Vue.use(VueRouter);

// Doesn't actually send status code 404
const NotFound = {
  template: "<h1> 404 Not Found - {{ $route.path }} </h1>",
};

const routes = [
  { path: "/", redirect: "/login" }, // TODO handle case when already logged in
  { path: "/login",           component: Auth },
  { path: "/home",            component: HomePage},
  { path: "/profile",         component: ProfilePage },
  { path: "/profile/:id",     component: ProfilePage },
  { path: "/create_business", component: CreateBusiness },
  { path: "/business/:id",    component: BusinessProfile },
  { path: "/admin",           component: admin },
  { path: "/search",          component: SearchResults },
  { path: "/catalogue",       component: Catalogue},
  { path: "*",                component: NotFound },
];

export default new VueRouter({
  mode: "history",
  routes,
});
