<template>
  <v-container>
    <SimpleSearchBar
      v-model="simpleSearchParams"
      v-show="!showAdvancedSearch"
      @showAdvancedSearch="showAdvancedSearch=true"
    />
    <AdvancedSearchBar
      v-model="advancedSearchParams"
      v-show="showAdvancedSearch"
      @hideAdvancedSearch="showAdvancedSearch=false"
      @searchListings="updatePageAndQuery"
    />
    <v-alert
      v-if="errorMessage !== undefined"
      type="error"
      dismissible
      @input="errorMessage = undefined"
    >
      {{ errorMessage }}
    </v-alert>
    <!-- PUT RESULTS HERE -->
    <v-list three-line v-if="resultsPage">
      <template v-for="(sale, index) in resultsPage.results">
        <v-divider v-if="sale === undefined" :key="'divider-'+index"/>
        <SaleResult v-else :key="sale.id" :saleItem="sale" @goBack="updatePage" @refresh="updatePage" @viewProfile="viewProfile"/>
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
      errorMessage: undefined,
      resultsPerPage: 10,
      resultsPage: undefined,
      showAdvancedSearch: false,
      wasAdvancedSearch: false,
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
        reverse: false,
      },
      previousQuery: undefined,
      debouncedUpdateQuery: debounce(this.updateSearchQuery, 500),
    };
  },
  computed: {
    /**
     * The total number of results matching the search, or 0 if there is no search or an errorMessage has occured with the search
     */
    totalResults() {
      if (this.resultsPage === undefined) return 0;
      return this.resultsPage.count;
    },
    /**
     * The message to be displayed in the pagination element
     */
    resultsMessage() {
      if(this.totalResults === 0) return 'There are no results to show';
      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = Math.min(pageStartIndex + this.resultsPerPage, this.totalResults);
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
     * Updates the currently viewed page.
     * Does not update advanced query params.
     */
    async updatePage() {
      if(this.showAdvancedSearch) {
        this.advancedSearch();
      }
      else {
        this.simpleSearch();
      }
    },
    /**
     * Updates the page and refreshes the advanced search query
     */
    async updatePageAndQuery() {
      if (this.showAdvancedSearch) {
        this.updateAdvancedSearchQuery();
      }
      await this.updatePage();
    },
    /**
     * Call basic search endpoint with the simpleSearchParams
     */
    async simpleSearch() {
      if(this.simpleSearchParams.query === null) this.simpleSearchParams = "";

      const result = await basicSearchSaleitem(
        this.simpleSearchParams.query,
        this.simpleSearchParams.orderBy,
        this.currentPage,
        this.resultsPerPage,
        this.simpleSearchParams.reverse
      );
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.resultsPage = result;
        this.wasAdvancedSearch = false;
      }
    },
    /**
     * Update the current advanced serach params
     */
    updateAdvancedSearchQuery() {
      if(this.advancedSearchParams.productQuery === null) this.advancedSearchParams.productQuery = "";
      if(this.advancedSearchParams.businessQuery === null) this.advancedSearchParams.businessQuery = "";
      if(this.advancedSearchParams.locationQuery === null) this.advancedSearchParams.locationQuery = "";
      if(this.advancedSearchParams.closesBefore === null) this.advancedSearchParams.closesBefore = "";
      if(this.advancedSearchParams.closesAfter === null) this.advancedSearchParams.closesAfter = "";
      this.currentQuery= {...this.advancedSearchParams};
    },
    /**
     * Performs an advanced search with the current query
     */
    async advancedSearch() {
      const result = await advanceSearchSaleitem(this.currentQuery, this.currentPage, this.resultsPerPage);
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.resultsPage = result;
        this.wasAdvancedSearch = true;
      }
    },
    /**
     * Routes user to the given business profile
     * includes current search parameters as URL queries so that the current state can be restored.
     */
    async viewProfile(businessId) {
      if (this.wasAdvancedSearch) {
        const advancedParams = JSON.stringify(this.advancedSearchParams);
        await this.$router.push({name:'businessProfile', params:{id:businessId}, query:{advancedParams, currentPage:this.currentPage, fromPage:"saleSearch"}});
      } else {
        const simpleParams = JSON.stringify(this.simpleSearchParams);
        await this.$router.push({name:'businessProfile', params:{id:businessId}, query:{simpleParams, currentPage:this.currentPage, fromPage:"saleSearch"}});

      }
    },
    /**
     * Attempts to update the search parameters with the values from URL query
     * Used for when returning from the business profile page.
     */
    async updateSearchFromRoute() {
      if (this.$route.query.currentPage) {
        this.currentPage = parseInt(this.$route.query.currentPage);
      }
      if (this.$route.query.advancedParams) {
        this.advancedSearchParams = JSON.parse(this.$route.query.advancedParams);
        this.showAdvancedSearch = true;
        this.updateAdvancedSearchQuery();
        await this.advancedSearch();
      }
      else if (this.$route.query.simpleParams) {
        this.simpleSearchParams = JSON.parse(this.$route.query.simpleParams);
        await this.simpleSearch();
      }

    }
  },
  watch: {
    /**
     * Whenever the search query, order by parameter or reverse parameter for the simple search changes, update the search results
     */
    simpleSearchParams: {
      deep: true,
      handler() {
        this.updatePageAndQuery();
      }
    },
    /**
     * Ensures that the current page is at least 1 and less than or equal to the total number of pages.
     */
    totalPages() {
      this.currentPage = Math.max(Math.min(this.currentPage, this.totalPages), 1);
    },
    /**
     * Whenever user changes page on advanced search, send a new request with the old query and updated page number.
     * If on the simple search page, send request with the new query and page number.
     */
    async currentPage() {
      this.updatePage();
    },
    resultsPerPage() {
      this.updatePage();
    },
  },
  async beforeMount() {
    await this.updatePage();
    await this.updateSearchFromRoute();
  }
};
</script>