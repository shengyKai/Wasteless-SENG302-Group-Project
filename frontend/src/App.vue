<template>
  <v-app>
    <div class="notfooter">
      <div v-if="$store.state.createBusinessDialogShown">
        <CreateBusiness @closeDialog="$store.commit('hideCreateBusiness')" />
      </div>
      <div v-if="$store.state.createSaleItemDialog !== undefined">
        <CreateSaleItem @closeDialog="$store.commit('hideCreateSaleItem')"/>
      </div>

      <AppBar v-if="$store.getters.isLoggedIn"/>

      <!-- Global error message -->
      <v-alert
        v-if="$store.state.globalError !== null"
        type="error"
        dismissible
        @input="$store.commit('clearError')"
      >
        {{ $store.state.globalError }}
      </v-alert>

      <v-main>
        <div class="container-outer">
          <div class="container-inner">
            <!-- All content (except AppBar & Footer) should be a child of 'v-main'. -->
            <router-view />
          </div>
        </div>
      </v-main>
    </div>
    <div class="clear"/>
    <AppFooter class="foot"/>
  </v-app>
</template>

<script>
import AppBar from "./components/AppBar";
import AppFooter from "./components/AppFooter";
import CreateBusiness from "./components/BusinessProfile/CreateBusiness";
import CreateSaleItem from "./components/BusinessProfile/CreateSaleItem";

import router from "./plugins/router";
import { getStore } from './store';

// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
export default {
  name: "App",
  components: {
    // list your components here to register them (located under 'components' folder)
    // https://vuejs.org/v2/guide/components-registration.html
    AppBar,
    AppFooter,
    CreateBusiness,
    CreateSaleItem,
  },
  store: getStore(),
  router,
};
</script>

<style scoped>
[v-cloak] {
    display: none;
}

.notfooter {
  min-height: 100%;
  margin-bottom: -50px;
}

.clear {
  height: 50px;
}

.foot {
  height: 50px;
  clear: both;
}

</style>
