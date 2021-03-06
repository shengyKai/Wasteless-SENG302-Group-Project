<template>
  <v-app>
    <AppBar />
    
    <div class="container-outer">
      <div class="container-inner">
        <!-- All content (except AppBar & Footer) should be a child of 'v-main'. -->
        <v-main>
          <router-view />
        </v-main>
      </div>
    </div>

    <Footer/>
  </v-app>
</template>

<script>
import ProfilePage from "./components/ProfilePage.vue";
import Auth from "./components/Auth";
import AppBar from "./components/AppBar";
import Footer from './components/Footer';
// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
const app = {
  name: "app",
  components: {
    // list your components here to register them (located under 'components' folder)
    // https://vuejs.org/v2/guide/components-registration.html
    ProfilePage,
    Auth,
    AppBar,
    Footer
  },
  // app initial state
  // https://vuejs.org/v2/guide/instance.html#Data-and-Methods
  data: () => ({
    loggedIn: false
  })
};

// make the 'app' available
export default app;
</script>

<style>
[v-cloak] {
  display: none;
}
</style>