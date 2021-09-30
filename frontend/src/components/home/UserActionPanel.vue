<template>
  <div>
    <!-- User actions - Wide version -->
    <v-card
      rounded="lg"
      class="action-pane small-no-display "
    >
      <v-card-text>
        <v-list>
          <v-list-item-group>
            <v-list-item @click="viewProfile">
              <v-list-item-icon>
                <v-icon color="blue">mdi-account-circle</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Profile</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewCreateBusiness">
              <v-list-item-icon>
                <v-icon color="blue-grey lighten-1">mdi-briefcase-plus</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Add Business</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewUserSearch">
              <v-list-item-icon>
                <v-icon color="cyan darken-2">mdi-account-search</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Search Users</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewBusinessSearch">
              <v-list-item-icon>
                <v-icon color="black lighten-2">mdi-briefcase-search</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Search Businesses</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewSaleItemSearch">
              <v-list-item-icon>
                <v-icon color="red lighten-2">mdi-cart</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Search Sale Items</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewMarketplace">
              <v-list-item-icon>
                <v-icon color="deep-orange lighten-2">mdi-store</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Marketplace</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewMyCards">
              <v-list-item-icon>
                <v-icon color="brown lighten-1">mdi-card-text</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>My Cards</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="isAdmin" @click="viewAdminDashboard">
              <v-list-item-icon>
                <v-icon>mdi-account-tie</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Admin Dashboard</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card-text>
    </v-card>

    <!-- User actions - Narrow version -->
    <v-card class="action-pane large-no-display">
      <v-btn icon @click="viewProfile" class="action-button">
        <v-icon large>mdi-account-circle</v-icon>
      </v-btn>
      <v-btn icon @click="viewCreateBusiness" class="action-button">
        <v-icon large>mdi-briefcase-plus</v-icon>
      </v-btn>
      <v-btn icon @click="viewUserSearch" class="action-button">
        <v-icon large>mdi-account-search</v-icon>
      </v-btn>
      <v-btn icon @click="viewBusinessSearch" class="action-button">
        <v-icon large>mdi-briefcase-search</v-icon>
      </v-btn>
      <v-btn icon @click="viewSaleItemSearch" class="action-button">
        <v-icon large>mdi-cart</v-icon>
      </v-btn>
      <v-btn icon @click="viewMarketplace" class="action-button">
        <v-icon large>mdi-store</v-icon>
      </v-btn>
      <v-btn icon @click="viewMyCards" class="action-button">
        <v-icon large>mdi-card-text</v-icon>
      </v-btn>
      <v-btn v-if="isAdmin" icon @click="viewAdminDashboard" class="action-button">
        <v-icon large>mdi-account-tie</v-icon>
      </v-btn>
    </v-card>
  </div>
</template>

<script>
import { USER_ROLES } from "../../utils";

export default {
  name: "UserActionPanel",
  computed: {
    isAdmin() {
      return [USER_ROLES.DGAA, USER_ROLES.GAA].includes(
        this.$store.getters.role
      );
    },
  },
  methods: {
    /**
     * Shows the user profile page
     */
    viewProfile() {
      this.$router.push("/profile");
    },
    /**
     * Shows the create business dialog
     */
    viewCreateBusiness() {
      this.$store.commit('showCreateBusiness');
    },
    /**
     * Redirect to the user search page
     */
    viewUserSearch() {
      this.$router.push("/search");
    },
    /**
     * Redirect to the business search page
     */
    viewBusinessSearch() {
      this.$router.push("/search/business");
    },
    /**
     * Redirect to the Sale Item search page
     */
    viewSaleItemSearch() {
      this.$router.push("/search/sales");
    },
    /**
     * Redirect to marketplace page
     */
    viewMarketplace() {
      this.$router.push("/marketplace");
    },
    /**
     * Redirect to users marketplace card page
     */
    viewMyCards() {
      this.$router.push(`/usercards/${this.$store.state.user.id}`);
    },
    /**
     * Redirect to the admin page
     */
    viewAdminDashboard() {
      this.$router.push("/admin");
    }
  }
};
</script>


<style scoped>

@media all and (min-width: 992px) {
  .large-no-display {
    display: none !important;
  }
}

@media not all and (min-width: 992px) {
  .small-no-display {
    display: none !important;
  }
}

.action-pane {
  margin-right: 10px;
  max-height: 500px;
}


@media not all and (min-width: 992px) {
  .action-pane {
    display: block;
  }
}

.action-button {
  display: block;
  margin: 10px;
}

</style>