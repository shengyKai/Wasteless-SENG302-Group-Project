<template>
  <div>
    <!-- Business actions - Wide version -->
    <v-card
      rounded="lg"
      class="action-pane small-no-display "
    >
      <v-card-text>
        <v-list>
          <v-list-item-group>
            <v-list-item @click="viewProfile">
              <v-list-item-icon>
                <v-icon>mdi-account-circle</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Profile</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewCreateProduct">
              <v-list-item-icon>
                <v-icon>mdi-tooltip-plus</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Add Product</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="goToCataloguePage">
              <v-list-item-icon>
                <v-icon>mdi-view-list</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Catalogue</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="goInventoryPage">
              <v-list-item-icon>
                <v-icon>mdi-view-list</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title >Inventory</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item @click="viewCreateInventory">
              <v-list-item-icon>
                <v-icon>mdi-view-list</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title >Create Inventory Item (temporary)</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card-text>
    </v-card>
    <!-- Business actions - Narrow version -->
    <v-card class="action-pane large-no-display">
      <v-btn icon @click="viewProfile" class="action-button">
        <v-icon large>mdi-account-circle</v-icon>
      </v-btn>
      <v-btn icon @click="viewCreateProduct" class="action-button">
        <v-icon large>mdi-tooltip-plus</v-icon>
      </v-btn>
      <v-btn icon @click="goToCataloguePage" class="action-button">
        <v-icon large>mdi-view-list</v-icon>
      </v-btn>
      <v-btn icon @click="goInventoryPage" class="action-button">
        <v-icon large>mdi-view-list</v-icon>
      </v-btn>
    </v-card>
  </div>
</template>

<script>
export default {
  name: "BusinessActionPanel",
  methods: {
    /**
     * Shows the business profile page
     */
    viewProfile() {
      this.$router.push("/business/" + this.$store.state.activeRole.id);
    },
    /**
     * Shows the create product dialog
     */
    viewCreateProduct() {
      this.$store.commit('showCreateProduct', this.$store.state.activeRole.id);
    },
    /**
     * Shows the create Inventory dialog
     */
    viewCreateInventory() {
      this.$store.commit('showCreateInventory', this.$store.state.activeRole.id);
    },
    /**
     * Shows the Catalogue page
     */
    goToCataloguePage() {
      this.$router.push(`/business/${this.$store.state.activeRole.id}/products`);
    },
    /**
     * Shows the Inventory page
     */
    goInventoryPage() {
      this.$router.push(`/business/${this.$store.state.activeRole.id}/inventory`);
    },
  },
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
  max-height: 350px;
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