<template>
  <div id="app">
    <!-- IF: Logged In -->
    <div v-if="loggedIn">
      <Students />
    </div>

    <!-- IF: Logged Out -->
    <div v-else>
      <Auth />
    </div>

    <footer class="info">
      <h4>Wasteless</h4>
    </footer>
  </div>
</template>

<script>
import Students from "./components/Students";
import Auth from "./components/Auth";
// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
const app = {
  name: "app",
  components: {
    // list your components here to register them (located under 'components' folder)
    // https://vuejs.org/v2/guide/components-registration.html
    Auth,
    Students
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