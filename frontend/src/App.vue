<template>
  <v-app>
    <template v-if="loading">
      <v-progress-circular color="primary" />
    </template>
    <template v-else>
      <div class="notfooter">
        <div v-if="loggedIn">
          <AppBar />

          <v-main>
            <div class="container-outer">
              <div class="container-inner">
                <!-- All content (except AppBar & Footer) should be a child of 'v-main'. -->
                <router-view />
              </div>
            </div>
          </v-main>
        </div>

        <div v-else>
          <v-main>
            <div class="container-outer">
              <div class="container-inner">
                <!-- All content (except AppBar & Footer) should be a child of 'v-main'. -->
                <Auth />
              </div>
            </div>
          </v-main>
        </div>
        <div class="clear"/>
      </div>
      <AppFooter class="foot"/>
    </template>
  </v-app>
</template>

<script>
import Auth from "./components/Auth";
import AppBar from "./components/AppBar";
import AppFooter from "./components/AppFooter";
import store from "./store";
import router from "./plugins/vue-router";
import { COOKIE, getCookie } from './utils';

// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
export default {
  name: "App",
  components: {
    // list your components here to register them (located under 'components' folder)
    // https://vuejs.org/v2/guide/components-registration.html
    Auth,
    AppBar,
    AppFooter,
  },
  async created() {
    const cookie = getCookie(COOKIE.USER);
    if (cookie) {
      this.loading = true;
      await this.$store.dispatch('getUser', cookie.split('=')[1]);
      if (this.$route.path === '/login') this.$router.push('/profile');
      this.loading = false;
    } else {
      if (this.$route.path !== '/login') this.$router.push('/login');
    }
  },
  store,
  router,
  data() {
    return {
      loading: false
    };
  },
  computed: {
    loggedIn() {
      return this.$store.getters.isLoggedIn;
    }
  }
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
