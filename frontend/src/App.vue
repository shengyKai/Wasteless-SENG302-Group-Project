<template>
<<<<<<< HEAD
    <v-app>
        <div v-if="loggedIn">
            <AppBar />
            <SearchBar />
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

        <Footer />
    </v-app>
=======
  <v-app>
    <div v-if="loading">
      <v-progress-circular color="primary" />
    </div>
    <div v-else>
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

      <Footer />
    </div>
  </v-app>
>>>>>>> 5ab54c97a5570c58798389e4088714566916fe6f
</template>

<script>
import Auth from "./components/Auth";
import AppBar from "./components/AppBar";
<<<<<<< HEAD
import Footer from "./components/Footer";
import store from "./store";
import router from "./plugins/vue-router";

// function setCookie(name) {
//   const date = new Date();
//   date.setFullYear(date.getFullYear() + 1);
//   document.cookie = `name=${name}`;
// }
=======
import Footer from './components/Footer';
import store from './store';
import router from './plugins/vue-router';
import { COOKIE, getCookie } from './utils';
>>>>>>> 5ab54c97a5570c58798389e4088714566916fe6f

// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
export default {
<<<<<<< HEAD
    name: "app",
    components: {
        // list your components here to register them (located under 'components' folder)
        // https://vuejs.org/v2/guide/components-registration.html
        Auth,
        AppBar,
        Footer,
    },
    store,
    router,
    computed: {
        loggedIn() {
            return this.$store.getters.isLoggedIn;
        },
    },
=======
  name: "app",
  components: {
    // list your components here to register them (located under 'components' folder)
    // https://vuejs.org/v2/guide/components-registration.html
    Auth,
    AppBar,
    Footer
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
    }
  },
  computed: {
    loggedIn() {
      return this.$store.getters.isLoggedIn
    }
  }
>>>>>>> 5ab54c97a5570c58798389e4088714566916fe6f
};
</script>

<style>
[v-cloak] {
    display: none;
}
</style>
