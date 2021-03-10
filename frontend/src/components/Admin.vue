<template>
  <v-container>
    <!-- Red block to show this is the admin dashbaord/ panel -->
    <v-card height="400px">
      <v-footer v-bind="localAttrs" :padless="padless">
        <v-card flat tile width="100%" class="red lighten-1 text-center">
          <v-card-text>
            <v-btn v-for="icon in icons" :key="icon" class="mx-4" icon>
              <v-icon size="24px">
                {{ icon }}
              </v-icon>
            </v-btn>
          </v-card-text>
          <v-divider></v-divider>

          <v-card-text class="white--text">
            {{ new Date().getFullYear() }} —
            <strong>Admin Dashboard</strong>
          </v-card-text>
        </v-card>
      </v-footer>

      <!--Main button for admin to show favourite or focused profile-->
      <v-bottom-navigation v-model="value">
        <v-btn value="fav_User_1">
          <span>User_1</span>
          <v-icon>mdi-heart</v-icon>
        </v-btn>
        <v-btn value="fav_User_2">
          <span>User_2</span>
          <v-icon>mdi-heart</v-icon>
        </v-btn>
        <v-btn value="fav_User_3">
          <span>User_3</span>
          <v-icon>mdi-heart</v-icon>
        </v-btn>
      </v-bottom-navigation>

      <!-- Admin action list -->
      <v-list-item class="green">
        <v-list-item-content>
          <v-list-item-title class="title">
            Admin Action
          </v-list-item-title>
          <v-list-item-subtitle>
            DGAA Privilege
          </v-list-item-subtitle>
        </v-list-item-content>

	<!--Fav_User Button embedded with Admin Action v-list -->
        <v-btn>
          <span>User_1</span>
          <v-icon>mdi-heart</v-icon>
        </v-btn>
        <v-btn>
          <span>User_2</span>
          <v-icon>mdi-heart</v-icon>
        </v-btn>
        <v-btn>
          <span>User_3</span>
          <v-icon>mdi-heart</v-icon>
        </v-btn>
      </v-list-item>

      <v-divider></v-divider>

      <!-- Action list that admin can execute -->
      <v-list dense nav>
        <v-list-item v-for="item in items" :key="item.title" link>
          <v-list-item-icon>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-icon>

          <v-list-item-content>
            <v-list-item-title>{{ item.title }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-card>
  </v-container>
</template>

<script>
export default {
  data() {
    return {
      items: [
        { title: "Dashboard", icon: "mdi-view-dashboard" },
        { title: "Photos", icon: "mdi-image" },
        { title: "About", icon: "mdi-help-box" },
      ],
      right: null,
    };
  },
};
</script>

<style scoped></style>
