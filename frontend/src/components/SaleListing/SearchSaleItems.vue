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
    <v-list three-line v-if="resultsPage">
      <template v-for="(sale, index) in resultsPage.results">
        <v-divider v-if="sale === undefined" :key="'divider-'+index"/>
        <SaleResult v-else :key="sale.id" :saleItem="sale" @goBack="updatePage" @refresh="updateResults"/>
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
import { debounce } from '@/utils';
import {basicSearchSaleitem, advanceSearchSaleitem} from "@/api/sale";

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
      error: undefined,
      resultsPerPage: 10,
      resultsPage: undefined,
      showAdvancedSearch: false,
      simpleSearchParams: {
        query: "",
        orderBy: "created",
        reverse: false
      },
      advancedSearchParams: {
        productQuery: "",
        businessQuery: "",
        locationQuery: "",
        closesBefore: "",
        closesAfter: "",
        orderBy: "created",
        businessTypes: [],
        lowestPrice: "",
        highestPrice: "",
        reverse: false
      },
      debouncedUpdateQuery: debounce(this.updateSearchQuery, 500),
    };
  },
  computed: {
    /**
     * The total number of results matching the search, or 0 if there is no search or an error has occured with the search
     */
    totalResults() {
      if (this.resultsPage === undefined) return 0;
      return this.resultsPage.count;
    },
    resultsMessage() {
      if(this.resultsPage === undefined) return 'There are no results to show';
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.resultsPerPage;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
    /**
     * The total number of pages required to show all the users
     * May be 0 if there are no results
     */
    totalPages () {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
  },
  methods: {
    /**
     * Call basic search endpoint with the simpleSearchParams
     */
    async simpleSearch() {
      if(this.simpleSearchParams.query === null) this.simpleSearchParams = "";
      const result = await basicSearchSaleitem(this.simpleSearchParams.query, this.simpleSearchParams.orderBy,
        this.currentPage, this.resultsPerPage, this.simpleSearchParams.reverse);
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.resultsPage = result;
      }
    },
    /**
     * Call advanceSearch endpoint with advanceSearchParams
     */
    async advancedSearch() {
      if(this.advancedSearchParams.productQuery === null) this.advancedSearchParams.productQuery = "";
      if(this.advancedSearchParams.businessQuery === null) this.advancedSearchParams.businessQuery = "";
      if(this.advancedSearchParams.locationQuery === null) this.advancedSearchParams.locationQuery = "";
      const result = await advanceSearchSaleitem(this.advancedSearchParams, this.currentPage, this.resultsPerPage);
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.resultsPage = result;
      }
    },
    async updatePage() {
      if(this.showAdvancedSearch) this.advancedSearch();
      else this.simpleSearch();
    },
    /**
     * Fetches a new set of results
     */
    async updateResults() {
      this.resultsPage = (await basicSearchSaleitem("", "created", 1, this.resultsPerPage, false));
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
    },
    advanceSearchParams: {
      deep: true,
      handler() {
        this.advancedSearch();
      }
    },
    currentPage() {
      this.updatePage();
    },
    resultsPerPage() {
      this.updatePage();
    },
  },
  async beforeMount() {
    this.updateResults();
  }
};
</script>