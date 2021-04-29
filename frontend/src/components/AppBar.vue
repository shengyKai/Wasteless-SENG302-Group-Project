<template>
  <v-app-bar max-height="64px">
    <div class="container-outer">
      <h1 class="link" @click="showHome">WASTELESS</h1>

      <!-- Space between the app name and the controls -->
      <div class="spacer"/>

      <!-- Search Bar component to perform search and show result, if not on search page -->
      <SearchBar v-if="$route.path !== '/search'" />

      <!-- Action menu -->
      <div class="text-center">
        <v-menu offset-y>
          <template v-slot:activator="{ on, attrs }">
            <v-btn icon v-bind="attrs" v-on="on">
              <v-avatar>
                <v-icon> mdi-account-circle </v-icon>
              </v-avatar>
            </v-btn>
          </template>
          <v-list class="list">
            <v-list-item v-if="isDGAA" class="admin link" @click="viewAdmin">
              <v-list-item-title class="admin"> ADMIN </v-list-item-title>
            </v-list-item>
            <v-list-item v-else-if="isAdmin" class="admin">
              <v-list-item-title class="admin"> ADMIN </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title class="link" @click="showHome">
                Home
              </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title class="link" @click="viewProfile">
                Profile
              </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title class="link" @click="viewCreateBusiness">
                Create Business
              </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title class="link" @click="showCatalogue">
                Testing_Page
              </v-list-item-title>
            </v-list-item>
            <v-list-item>
              <v-list-item-title class="link" @click="logout">
                Logout
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </div>
      <!-- Dropdown menu for selecting role which user is currently acting as -->
      <div class="role-menu">
        <v-menu offset-y>
          <template v-slot:activator="{ on, attrs }">
            <!-- User name and icon -->
            <v-chip
              v-if="user"
              v-bind="attrs"
              v-on="on"
            >
              <UserAvatar :user="user" size="small" />
              <div class="name">
                {{ roles[selectedRole].displayText }}
              </div>
            </v-chip>
          </template>
          <v-list>
            <v-list-item-group
              v-model="selectedRole"
              color="primary"
            >
              <template v-for="(role, index) in roles">
                <v-list-item :key="index">
                  <v-list-item-title>{{ role.displayText }}</v-list-item-title>
                </v-list-item>
              </template>
            </v-list-item-group>
          </v-list>
        </v-menu>
      </div>
    </div>
  </v-app-bar>
</template>

<script>
import SearchBar from "./utils/SearchBar";
import UserAvatar from "./utils/UserAvatar";
import { USER_ROLES } from "../utils";

export default {
  name: "AppBar",
  components: {
    SearchBar,
    UserAvatar,
  },
  data() {
    return {
      selectedRole : 0,
    };
  },
  methods: {
    viewProfile() {
      // Navigate to the profile page of the current active role
      switch (this.$store.state.activeRole.type) {
      case "user":
        this.$router.push("/profile");
        break;
      case "business":
        this.$router.push("/business/" + this.$store.state.activeRole.id);
        break;
      default:
        this.$router.push("/profile");
      }
    },
    showHome() {
      this.$router.push("/home");
    },
    showCatalogue() {
      this.$router.push("/business/:id/products");
    },
    logout() {
      this.$store.commit("logoutUser");
      this.$router.push("/login");
    },
    viewAdmin() {
      this.$router.push("/admin");
    },
    viewCreateBusiness() {
      this.$store.commit('showCreateBusiness');
    },
  },
  computed: {
    isAdmin() {
      return [USER_ROLES.DGAA, USER_ROLES.GAA].includes(
        this.$store.getters.role
      );
    },
    isDGAA() {
      return this.$store.getters.role === USER_ROLES.DGAA;
    },
    user() {
      return this.$store.state.user;
    },
    roles() {
      let result = [
        { displayText: this.user.firstName, type: "user", id: this.user.id }
      ];

      for (const business of this.user.businessesAdministered) {
        result.push({ displayText: business.name, type: "business", id: business.id });
      }

      return result;
    }
  },
  watch : {
    selectedRole() {
      // Set the role that the user is acting as to the role that has been selected from the list
      const role = this.roles[this.selectedRole];

      // If we've selected an error entry then do nothing
      if (role.type === 'error') return;


      this.$store.state.activeRole = { type: role.type, id: role.id };
    },
  }
};
</script>

<style scoped>
.spacer {
  flex: 1;
  max-width: 900px;
}

.name {
  align-self: center;
  margin-left: 5px;
}

.admin {
  color: rgb(255, 16, 16);
  background-color: rgb(212, 212, 212);
  font-weight: 500;
}

.list {
  padding-top: 0;
  padding-bottom: 0;
}
</style>
