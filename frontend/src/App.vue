<template>
  <div id="app">
    <!-- Sizes your content based upon application components -->
    <RegistrationPage 
    fullName="Tim Tam" 
    nickname="Timmy" 
    bio="Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    email="tim.tam@hotmail.com"
    dateOfBirth="1/1/1900"
    phoneNo="0800 83 83 83"
    address="10 Downing Street"
    />
  </div>
</template>

<script>
import RegistrationPage from "./components/RegistrationPage.vue";
// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
const app = {
  name: "app",
  components: {
    // list your components here to register them (located under 'components' folder)
    // https://vuejs.org/v2/guide/components-registration.html
    RegistrationPage,
  },
  // app initial state
  // https://vuejs.org/v2/guide/instance.html#Data-and-Methods
  data: () => {
    return {};
  },
};

// make the 'app' available
export default app;
</script>

<style>
[v-cloak] {
  display: none;
}
</style>