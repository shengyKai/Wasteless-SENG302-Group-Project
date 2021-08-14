<template>
  <div>
    <v-toolbar dark color="primary">
      <v-row>
        <v-col cols="4">
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            v-model="searchQuery"
            prepend-inner-icon="mdi-magnify"
            label="Search"
            autofocus
          />
        </v-col>
        <v-col cols="4">
          <v-select
            v-model="searchBy"
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Product Code',               value: 'productCode'},
              { text: 'Product Name',               value: 'name'},
              { text: 'Description',                value: 'description'},
              { text: 'Manufacturer',               value: 'manufacturer'}
            ]"
            prepend-inner-icon="mdi-shopping-search"
            label="Search by"
            multiple
          >
            <template v-slot:selection="{ item, index }">
              <span v-if="index < maxDisplay">{{ item.text }}, &nbsp;</span>
              <span
                v-if="index === maxDisplay"
                class="white--text caption"
              >(+{{ searchBy.length - maxDisplay }} search)</span>
            </template>
          </v-select>
        </v-col>
        <v-col cols="3">
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
              { text: 'Date Added',                 value: 'created'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col class="text-right" cols="1">
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
      </v-row>
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
        <ProductCatalogueItem
          :businessId="businessId"
          :key="product.id"
          :product="product"
          @content-changed="updateResults"
        />
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
  </div>
</template>

<script>
import { getProducts} from '../api/internal';
import ProductCatalogueItem from './cards/ProductCatalogueItem.vue';

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
      /**
       * Total number of results for all pages
       */
      totalResults: 0,
      /**
       * ID of the business
       */
      businessId: null,
      /**
       * User input query to search the catalogue with
       */
      searchQuery: "",
      /**
       * List of product attributes to search along with the searchQuery
       */
      searchBy: [],
      /**
       * How many selections will be displayed on `<v-select/>` for SearchBy
       */
      maxDisplay: 2
    };
  },
  computed: {
    /**
     * The total number of pages required to show all the users
     * May be 0 if there are no results
     */
    totalPages () {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many results there are
     */
    resultsMessage() {
      if (this.products.length === 0) return 'There are no results to show';
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.products.length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
  },
  async created() {
    await this.updateResults();
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
        this.totalResults = 0;
        this.error = value;
      } else {
        this.products = value.results;
        this.totalResults = value.count;
        this.error = undefined;
      }
    }
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