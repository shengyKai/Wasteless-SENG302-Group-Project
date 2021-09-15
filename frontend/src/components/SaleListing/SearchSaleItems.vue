<template>
  <v-container>
    <SimpleSearchBar v-model="simpleSearchParams" v-show="!showAdvancedSearch" @showAdvancedSearch="showAdvancedSearch=true"/>
    <AdvancedSearchBar v-model="advancedSearchParams" v-show="showAdvancedSearch" @hideAdvancedSearch="showAdvancedSearch=false" @searchListings="advancedSearch()"/>
    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <!-- PUT RESULTS HERE -->
    <v-list three-line>
      <template v-for="(sale, index) in results">
        <v-divider v-if="sale === undefined" :key="'divider-'+index"/>
        <SaleResult v-else :key="sale.id" :saleItem="sale"/>
      </template>
    </v-list>
    <!--paginate results-->
    <v-pagination
      v-model="currentPage"
      :total-visible="11"
      :length="totalPages"
      circle
    />
    <!--Text to display range of results out of total number of results-->
    <v-row justify="center" no-gutters>
      {{ resultsMessage }}
    </v-row>
  </v-container>
</template>

<script>
import AdvancedSearchBar from './AdvancedSearchBar.vue';
import SimpleSearchBar from './SimpleSearchBar.vue';
import SaleResult from './SaleResult.vue';
import {getDummySaleItemSearchResult} from "@/api/internal";

export default {
  name: "SearchSaleItems",
  components: {
    SaleResult,
    AdvancedSearchBar,
    SimpleSearchBar
  },
  data() {
    return {
      currentPage: 1,
      totalPages: 1,
      error: undefined,
      resultsPerPage: 10,
      results: undefined,
      showAdvancedSearch: false,
      simpleSearchParams: {
        query: undefined,
        orderBy: undefined,
        reverse: false
      },
      advancedSearchParams: {
        productQuery: undefined,
        businessQuery: undefined,
        locationQuery: undefined,
        closesBefore: undefined,
        closesAfter: undefined,
        orderBy: undefined,
        businessTypes: [],
        lowestPrice: undefined,
        highestPrice: undefined,
        reverse: false
      },
    };
  },
  computed: {
    /**
     * The total number of results matching the search, or 0 if there is no search or an error has occured with the search
     */
    totalResults() {
      if (this.results === undefined) return 0;
      return this.results.count;
    },
    resultsMessage() {
      // TODO implement computing results message based on number of results when linking to endpoint
      return "There are no results to show";
    },
  },
  methods: {
    simpleSearch() {
      //TODO implement when linked to endpoint
    },
    advancedSearch() {
      //TODO implement when linked to endpoint
    },
  },
  watch: {
    /**
     * Whenever the search query, order by parameter or reverse parameter for the simple search changes, update the search results
     */
    simpleSearchParams: {
      deep: true,
      handler() {
        this.simpleSearch();
      }
    }
  },
  async beforeMount() {
    this.results = await (await getDummySaleItemSearchResult()).results;
  }
};
</script>