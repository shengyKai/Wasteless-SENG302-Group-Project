<template>
  <div class="page-container">
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
                <v-icon>mdi-account-circle</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Profile</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="isBusiness" @click="viewCreateProduct">
              <v-list-item-icon>
                <v-icon>mdi-tooltip-plus</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Add Product</v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-list-item v-if="!isBusiness" @click="viewCreateBusiness">
              <v-list-item-icon>
                <v-icon>mdi-briefcase-plus</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Add Business</v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-list-item v-if="isBusiness" @click="goToCataloguePage">
              <v-list-item-icon>
                <v-icon>mdi-view-list</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title>Catalogue</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
            <v-list-item v-if="isBusiness" @click="goInventoryPage">
              <v-list-item-icon>
                <v-icon>mdi-view-list</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title >Inventory</v-list-item-title>
              </v-list-item-content>
            </v-list-item>

            <v-list-item v-if="isBusiness" @click="goSalePage">
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

    <!-- User actions - Narrow version -->
    <v-card class="action-pane large-no-display">
      <v-btn icon @click="viewProfile" class="action-button">
        <v-icon large>mdi-account-circle</v-icon>
      </v-btn>
      <v-btn v-if="!isBusiness" icon @click="viewCreateBusiness" class="action-button">
        <v-icon large>mdi-briefcase-plus</v-icon>
      </v-btn>
      <v-btn v-if="isBusiness" icon @click="viewCreateProduct" class="action-button">
        <v-icon large>mdi-tooltip-plus</v-icon>
      </v-btn>
    </v-card>

    <div class="newsfeed">
      <!-- Inventory items -->
      <v-card
        v-if="isBusiness"
        outlined
        rounded="lg"
        class="newsfeed-item"
      >
        <v-card-title>
          Inventory
        </v-card-title>
        <v-card-text class="inventory-container">
          <v-card
            v-for="(item, index) in inventoryItems"
            :key="index"
            class="inventory-item"
          >
            <v-img
              src="https://edit.co.uk/uploads/2016/12/Image-1-Alternatives-to-stock-photography-Thinkstock.jpg"
              height="50px"
            />
            {{ item }}
          </v-card>
        </v-card-text>
      </v-card>

      <!-- Newsfeed -->
      <v-card
        v-for="(item, index) in newsfeedItems"
        :key="index"
        outlined
        rounded="lg"
        class="newsfeed-item"
      >
        <v-card-title>
          {{ item.title }}
        </v-card-title>
        <v-card-text>
          <pre>{{ item.content }}</pre>
        </v-card-text>
      </v-card>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      newsfeedItems: [
        {
          title: "Somebody commented on your post",
          content: "Hey this is a comment",
        },
        {
          title: "10 Shocking FACTS, Number 7 will...",
          content:
            "1. The sky is blue\n2. All squares have 4 corners\n3. Exposure to PHP decreases lifespan by 10 years\n4. I'm out of ideas",
        },
      ],
    };
  },
  methods: {
    /**
     * Shows the profile page
     * base on the current active role
     */
    viewProfile() {
      switch (this.role.type) {
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
    /**
     * Shows the create business dialog
     */
    viewCreateBusiness() {
      this.$store.commit('showCreateBusiness');
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
    goSalePage() {
      this.$router.push(`/business/${this.$store.state.activeRole.id}/sales`);
    },
  },
  computed: {
    /**
     * Current active user role
     */
    role() {
      return this.$store.state.activeRole;
    },
    /**
     * Is the current user acting as a business
     */
    isBusiness() {
      return this.role?.type === "business";
    },
    /**
     * List of inventory items to be shown
     */
    inventoryItems() {
      if (!this.isBusiness) return undefined;
      return [...Array(10).keys()].map(i => `Item ${i}`);
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

pre {
  white-space: pre-wrap;
}

.page-container {
  display: flex;
  width: 100%;
  margin-right: auto;
  margin-left: auto;
  max-width: 900px;
}

@media all and (min-width: 992px) {
  .page-container {
    padding: 10px;
  }
}

@media not all and (min-width: 992px) {
  .page-container {
    padding-right: 5px;
    padding-top: 10px;
  }
}

.newsfeed {
  flex: 1;
  min-width: 260px;
}

.inventory-container {
  display: flex;
  flex-flow: row wrap;
}

.inventory-item {
  max-width: 100px;
  margin: 5px;
}

.newsfeed-item {
  margin-bottom: 10px;
}

.action-pane {
  margin-right: 10px;
  max-height: 400px;
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