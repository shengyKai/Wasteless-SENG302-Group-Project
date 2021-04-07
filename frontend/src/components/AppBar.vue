<template>
  <v-app-bar max-height="64px">
    <div v-if="showBusinessDialog">
      <CreateBusiness @closeDialog="showBusinessDialog = false" />
    </div>

    <div class="container-outer">
      <h1>WASTELESS</h1>

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
              <v-list-item-title class="link" @click="viewCreateBusiness">
                Create Business
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
      <v-chip v-if="user">
        <UserAvatar :user="user" size="small" />
        <div class="name">
          {{ user.firstName }}
        </div>
      </v-chip>
          </template>
          <v-list>
            <v-list-item-group
                v-model="selectedRole"
                color="primary"
            >
              <v-list-item
                  v-for="(role, index) in roles"
                  :key="index"
              >
                <v-list-item-title>{{ role.displayText }}</v-list-item-title>
              </v-list-item>
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
import CreateBusiness from "./BusinessProfile/CreateBusiness";
import { USER_ROLES } from "../utils";
import {getBusiness} from "@/api";

export default {
  name: "AppBar",
  components: {
    SearchBar,
    CreateBusiness,
    UserAvatar,
  },
  data() {
    return {
      showBusinessDialog: false,
      roles : [],
      user : null,
      selectedRole : 0,
    };
  },
  created() {
    this.user = this.$store.state.user;
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
    viewCreateBusiness() {
      this.showBusinessDialog = true;
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
  },
  watch : {
    async user() {
      // When the user changes, update the list of roles that the user can 'act as'
      this.user = this.$store.state.user;
      this.roles = [ { displayText: this.user.firstName, headerText: "user-" + this.user.id.toString() } ];
      for (let id of this.user.businessesAdministered || []) {
        await getBusiness(id).then((value) => {
          if (typeof value === 'string') {
            // TODO Handle error properly
            console.warn(value);
          } else {
            this.roles.push({ displayText: value.name, headerText: "business-" + value.id.toString() });
          }
        });
      }
    },
    async selectedRole() {
      // Set the role that the user is acting as to the role that has been selected from the list
      this.$store.state.activeRole = this.roles[this.selectedRole].headerText;
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
