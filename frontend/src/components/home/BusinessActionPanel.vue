<template>
  <div>
    <!-- Business actions - Wide version -->
    <v-card
      rounded="lg"
      class="action-pane d-none d-md-block"
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
            <v-list-item @click="goSalePage">
              <v-list-item-icon>
                <v-icon>mdi-view-list</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title >Sale Listing</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card-text>
    </v-card>
    <!-- Business actions - Narrow version -->
    <v-card class="action-pane d-block d-md-none">
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
      <v-btn icon @click="goSalePage" class="action-button">
        <v-icon large>mdi-view-list</v-icon>
      </v-btn>
    </v-card>

    <template v-if="showingCreateProduct">
      <ProductForm :businessId="this.$store.state.activeRole.id" @closeDialog="showingCreateProduct=false"/>
    </template>
  </div>
</template>

<script>
import ProductForm from '@/components/BusinessProfile/ProductForm.vue';

export default {
  name: "BusinessActionPanel",
  components: {
    ProductForm,
  },
  data() {
    return {
      showingCreateProduct: false,
    };
  },
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
      this.showingCreateProduct = true;
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
    goSalePage() {
      this.$router.push(`/business/${this.$store.state.activeRole.id}/listings`);
    },
  },
};
</script>

<style scoped>

.action-pane {
  margin-right: 10px;
  max-height: 400px;
}

.action-button {
  display: block;
  margin: 10px;
}

</style>