import Vue from "vue";
import VueRouter from "vue-router";

import Auth from "../components/Auth/index.vue";
import admin from "../components/Admin.vue";
import CreateInventoryItem from "../components/BusinessProfile/CreateInventory.vue";
import ProfilePage from "../components/UserProfile.vue";
import BusinessProfile from "../components/BusinessProfile/index.vue";
import SearchResults from "../components/SearchResults.vue";
import HomePage from "../components/HomePage.vue";
import ProductCatalogue from "../components/ProductCatalogue.vue";
import Inventory from "../components/Inventory.vue";
import InventoryItem from "../components/cards/InventoryItem.vue";


Vue.use(VueRouter);

// Doesn't actually send status code 404
const NotFound = {
  template: "<h2>404 Not Found - {{ $route.path }}</h2>"
};

const routes = [
  {
    path: "/",
    redirect: "/login"
  },
  {
    path: "/login",
    component: Auth,
    meta: { title: 'Login' }
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
    path: "/profile/:id",
    component: ProfilePage,
    meta: { title: 'Profile' }
  },
  {
    path: "/business/:id",
    component: BusinessProfile,
    meta: { title: 'Business' }
  },
  {
    path: "/admin",
    component: admin,
    meta: { title: 'Admin' }
  },
  {
    path: "/search",
    component: SearchResults,
    meta: { title: 'Search' }
  },
  {
    path: "/business/:id/products",
    component: ProductCatalogue,
    meta: { title: 'Buisness Products' }
  },
  { // Router for inventory page (Inventory page frontend dev please use this and remove this line afterward)
    path: "/inventory",
    component: Inventory,
    meta: { title: 'Inventory' }
  },
  { // TODO Remove once inventory page is created
    path: "/inventoryitem",
    component: InventoryItem,
    meta: { title: 'Inventory Item' }
  },
  { // Router for inventory page dialogue(Inventory page frontend dev please use this and remove this line afterward)
    path: "/inventoryitemdialogue",
    component: CreateInventoryItem,
    meta: { title: 'Inventory Item Dialogue' }
  },
  {
    path: "*",
    component: NotFound,
    meta: { title: 'Not Found' }
  }
];


export default new VueRouter({
  mode: "history",
  base: process.env.VUE_APP_BASE_URL,
  routes,
});
