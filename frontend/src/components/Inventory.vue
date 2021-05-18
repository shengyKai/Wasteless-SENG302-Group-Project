<template>
  <v-toolbar dark color="primary">
    <v-select
        v-model="orderBy"
        flat
        solo-inverted
        hide-details
        :items="[
          { text: 'Product Code',               value: 'productCode'},  //
          { text: 'Product Name',               value: 'name'},         //  Details from associated product
          { text: 'Description',                value: 'description'},  //
          { text: 'Manufacturer',               value: 'manufacturer'}, //
          { text: 'Price per Item',             value: 'pricePerItem'},
          { text: 'Total Price',                value: 'totalPrice'},
          { text: 'Date Added',                 value: 'created'},
          { text: 'Sell By',                    value: 'sellBy'},
          { text: 'Best Before',                value: 'bestBefore'},
          { text: 'Expires',                    value: 'expires'},
        ]"
        prepend-inner-icon="mdi-sort-variant"
        label="Sort by"
    />
    <v-col class="text-right">
      <v-btn-toggle class="toggle" v-model="reverse" mandatory>
        <v-btn depressed color="primary" :value="false">
          <v-icon>mdi-arrow-up</v-icon>
        </v-btn>
        <v-btn depressed color="primary" :value="true">
          <v-icon>mdi-arrow-down</v-icon>
        </v-btn>
      </v-btn-toggle>
    </v-col>
  </v-toolbar>
  <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
  >
    {{ error }}
  </v-alert>
  <v-list three-line>
    <!--users would produce the results for each page, and then it will show each result with
    SearchResultItem-->
    <template v-for="inventoryItem in inventoryItems">
      <InventoryItem :businessId="businessId" :key="inventoryItem.id" :inventoryItem="inventoryItem"/>
    </template>
  </v-list>
  <v-pagination
      v-model="currentPage"
      :length="totalPages"
      circle
  />
  <!--Text to display range of results out of total number of results-->
  <v-row justify="center" no-gutters>
    {{ resultsMessage }}
  </v-row>
  <v-container>
    <!-- Red block to show this is the admin dashboard/ panel -->
    <v-card height="400px" color="grey lighten-3">
      <v-footer v-bind="localAttrs" :padless="padless">
        <v-card
          height="70px"
          rounded
          width="100%"
          color="blue"
          class=" text-center font-weight-bold "
        >
          <v-divider />

          <v-card-text class="white--text">
            {{ new Date().getFullYear() }} —
            <strong>Inventory template</strong>
          </v-card-text>
        </v-card>
      </v-footer>

      <!-- Admin action list -->
      <v-card height="310" width="256" color="white">
        <!--     class="mx-auto"      -->
        <v-list-item flat tile color="background">
          <v-list-item-content>
            <v-list-item-title class="title">
              Inventory Action List
            </v-list-item-title>
            <v-list-item-subtitle>
              Action to perform
            </v-list-item-subtitle>
          </v-list-item-content>
        </v-list-item>
        <v-list dense nav>
          <v-card color="grey lighten-5">
            <v-list-item
              v-for="item in items"
              :key="item.title"
              link
            >
              <v-list-item-icon>
                <v-icon>{{ item.icon }}</v-icon>
              </v-list-item-icon>

              <v-list-item-content>
                <v-list-item-title>{{
                  item.title
                }}</v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-card>
        </v-list>
      </v-card>

      <v-divider />

      <!-- Action list that admin can execute -->
    </v-card>
  </v-container>
</template>

<script>
import InventoryItem from "./cards/InventoryItem.vue";

export default {
  name: "Inventory",

  components: {
    InventoryItem
  },

  data() {
    return {
      items: [
        { title: "Dashboard", icon: "mdi-view-dashboard" },
        { title: "Inventory thumbnail", icon: "mdi-image" },
        { title: "Inventory Guide", icon: "mdi-help-box" },
      ],
      right: null,
      inventoryItems: [],
      /**
       * Current error message string.
       * If undefined then there is no error.
       */
      error: undefined,
      /**
       * Whether to reverse the search order
       */
      reverse: false,
      /**
       * The current search result order
       */
      orderBy: 'creationDate',
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,
      /**
       * Total number of results for all pages
       */
      totalResults: 0,
      businessId: null
    };
  },
};
</script>

<style scoped></style>
