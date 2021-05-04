<template>
  <div>
    <v-toolbar dark color="primary">
      <v-select
        v-model="orderBy"
        flat
        solo-inverted
        hide-details
        :items="[
          { text: 'Product Code',               value: 'productCode'},
          { text: 'Product Name',               value: 'name'},
          { text: 'Description',                value: 'description'},
          { text: 'Manufacturer',               value: 'manufacturer'},
          { text: 'Recommended Retail Price',   value: 'recommendedRetailPrice'},
          { text: 'Created',                    value: 'created'},
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
      <template v-for="product in products">
        <ProductCatalogueItem :businessId="businessId" :key="product.id" :product="product"/>
      </template>
    </v-list>
    <!-- <v-pagination
      v-model="currentPage"
      :length="totalPages"
      circle
    /> -->
  </div>
</template>

<script>
import { getProducts } from '../api/internal';
import ProductCatalogueItem from './ProductCatalogueItem.vue';

export default {
  name: "ProductCatalogue",

  components: {
    ProductCatalogueItem
  },
  data() {
    return {
      products: [],
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
      orderBy: 'productCode',
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,
      businessId: null
    };
  },
  // computed: {
  //   /**
  //    * The total number of pages required to show all the users
  //    * May be 0 if there are no results
  //    */
  //   totalPages () {
  //     return Math.ceil(this.totalResults / this.resultsPerPage);
  //   },
  //   /**
  //    * The message displayed at the bottom of the page to show how many results there are
  //    */
  //   resultsMessage() {
  //     if (this.users.length === 0) return 'There are no results to show';

  //     const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
  //     const pageEndIndex = pageStartIndex + this.users.length;
  //     return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
  //   },
  // },
  created() {
    this.updateResults();
  },
  methods: {
    /**
     * This function gets called when the product list needs to be updated.
     * The page index, results per page, order by and reverse variables notify this function.
     */
    async updateResults() {
      this.businessId = parseInt(this.$route.params.id);
      if (isNaN(this.businessId)) {
        this.error = `Invalid business id "${this.$route.params.id}"`;
        return;
      }

      const value = await getProducts (
        this.businessId,
        this.currentPage,
        this.resultsPerPage,
        this.orderBy,
        this.reverse
      );
      if (typeof value === 'string') {
        this.products = [];
        this.error = value;
      } else {
        this.products = value;
        this.error = undefined;
      }
    },
  },

  watch: {
    orderBy() {
      this.updateResults();
    },
    reverse() {
      this.updateResults();
    },
    currentPage() {
      this.updateResults();
    },
    resultsPerPage() {
      this.updateResults();
    },
  },
};

</script>

<style scoped>
.product-fields {
  padding-top: 0;
}
</style>