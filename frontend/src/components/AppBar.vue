<template>
  <v-app-bar max-height="64px">
    <div class="container-outer">
      <div class="container-inner">
        <h1>WASTELESS</h1>
      </div>
    </div>
    <SearchBar v-if="$route.path !== '/search'" />
    <div class="text-center">
      <v-menu offset-y>
        <template v-slot:activator="{ on, attrs }">
          <v-btn icon v-bind="attrs" v-on="on">
            <v-avatar>
              <v-icon>
                mdi-account-circle
              </v-icon>
            </v-avatar>
          </v-btn>
        </template>
        <v-list>
          <v-list-item>
            <v-list-item-title class="link" @click="viewProfile">
              Profile
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="link" @click="logout">
              Logout
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="link" @click="viewAdmin">
              Test-Admin
            </v-list-item-title>
          </v-list-item>
          <v-list-item>
            <v-list-item-title class="link" @click="viewCheckBusiness">
              Create-Business
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </div>
  </v-app-bar>
</template>

<script>
import SearchBar from "./utils/SearchBar";

export default {
  name: "AppBar",
  components: {
    SearchBar,
  },
  methods: {
    viewProfile() {
      this.$router.push("/profile");
    },

    logout() {
      this.$store.commit("logoutUser");
      this.$router.push("/login");
    },
    viewAdmin() {
      this.$router.push("/admin");
    },
    viewCheckBusiness() {
      this.$router.push("/create_business");
    },
  },
};
</script>
