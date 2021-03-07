<template>
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
</template>

<script>
import Auth from "./components/Auth";
import AppBar from "./components/AppBar";
import Footer from "./components/Footer";
import store from "./store";
import router from "./plugins/vue-router";

// function setCookie(name) {
//   const date = new Date();
//   date.setFullYear(date.getFullYear() + 1);
//   document.cookie = `name=${name}`;
// }

// Vue app instance
// it is declared as a reusable component in this case.
// For global instance https://vuejs.org/v2/guide/instance.html
// For comparison: https://stackoverflow.com/questions/48727863/vue-export-default-vs-new-vue
export default {
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
};
</script>

<style>
[v-cloak] {
    display: none;
}
</style>
