<template>
  <div>
    <v-toolbar dark color="secondary" class="mb-1">
      <!-- Search field for user to input search term -->
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
      <v-select
        class="ml-2"
        style="max-width: 300px"
        v-model="selectedBusinessType"
        flat
        solo-inverted
        hide-details
        :items="businessTypeOptions"
        item-text="text"
        item-value="value"
        prepend-inner-icon="mdi-sort-variant"
        label="Filter by Business type"
      />
      <v-spacer/>
      <!-- Dropdown select box to allow user to change ordering of businesses -->
      <v-select
        style="max-width: 300px"
        v-model="orderBy"
        flat
        solo-inverted
        hide-details
        :items="orderByOptions"
        prepend-inner-icon="mdi-sort-variant"
        label="Order By"
      />
      <v-btn-toggle class="toggle" v-model="reverse" mandatory>
        <v-btn depressed color="secondary" :value="false">
          <v-icon>mdi-arrow-up</v-icon>
        </v-btn>
        <v-btn depressed color="secondary" :value="true">
          <v-icon>mdi-arrow-down</v-icon>
        </v-btn>
      </v-btn-toggle>
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
      <!--The users would produce the results for each page, and then it will show each result with
      SearchBusinessResultItem-->
      <template v-for="(business, index) in businesss">
        <v-divider v-if="business === undefined" :key="'divider-'+index"/>
        <SearchBusinessResult v-else :key="business.id" :business="business" @view-profile="viewProfile(business.id)" />
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
  </div>
</template>

<script>
import SearchBusinessResult from './cards/SearchBusinessResult';
import { searchBusinesses, BUSINESS_TYPES } from '../api/internal';
import { debounce } from '../utils';

export default {
  name: 'SearchBusinessPage',
  data() {
    return {
      /**
       * The contents of the search box.
       */
      searchQuery: this.$route.query.searchQuery || '',
      /**
       * Current query that is being searched.
       */
      searchedQuery: undefined,
      /**
       * The search response data for this page.
       */
      results: undefined,
      /**
       * Current error message string.
       * If undefined then there is no error.
       */
      error: undefined,
      /**
       * Whether to reverse the search order
       */
      reverse: this.$route.query.reverse === "true" || false,
      /**
       * The current search result order
       */
      orderBy: this.$route.query.orderBy || 'name',
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: this.$route.query.page ? parseInt(this.$route.query.page) : 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 10,
      /**
       * Function that is called whenever the "searchQuery" variable is updated.
       * This function is rate limited to avoid too many queries to the backend.
       */
      debouncedUpdateQuery: debounce(this.updateSearchQuery, 500),
      /**
       * Business type to filter search by
       */
      selectedBusinessType: this.$route.query.businessType || undefined,
      /**
       * Options for ordering the business search results
       */
      orderByOptions: [
        {text: "Name", value: "name"},
        {text: "Location", value: "location"},
        {text: "Business Type", value: "businessType"}
      ],
    };
  },

  computed: {
    /**
     * Total number of results
     */
    totalResults() {
      if (this.results === undefined) return 0;
      return this.results.count;
    },
    /**
     * List of businesss on the current page
     */
    businesss() {
      if (this.results === undefined) return [];
      return this.results.results;
    },
    /**
     * The total number of pages required to show all the businesss
     * May be 0 if there are no results
     */
    totalPages () {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many results there are
     */
    resultsMessage() {
      if (this.businesss.length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.businesss.length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
    businessTypeOptions() {
      const tempBusinessTypes = [];
      tempBusinessTypes.push({text: "Any", value: undefined});
      for (let businessType of BUSINESS_TYPES) {
        tempBusinessTypes.push({text: businessType, value: businessType});
      }
      return tempBusinessTypes;
    }
  },
  methods: {
    /**
     * This function is called when the search query changes.
     */
    async updateSearchQuery() {
      this.currentPage = 1; // Makes sure we start on the first page
      this.results = undefined; // Remove results
      this.updateResults();
    },
    /**
     * This function gets called when the search results need to be updated
     */
    async updateResults() {
      let filterBusinessType = this.selectedBusinessType;
      if (this.selectedBusinessType === 'Any') {
        filterBusinessType = undefined;
      }
      let queryToSend = this.searchQuery;
      if (queryToSend === "") {
        queryToSend = undefined;
      }
      if (queryToSend === undefined && filterBusinessType === undefined) return; // If the current search query is empty, do not search

      this.searchedQuery = this.searchQuery;

      const value = await searchBusinesses(
        queryToSend,
        filterBusinessType,
        this.currentPage,
        this.resultsPerPage,
        this.orderBy,
        this.reverse
      );
      if (typeof value === 'string') {
        this.results = undefined;
        this.error = value;
      } else {
        this.results = value;
        this.error = undefined;
      }
    },
    /**
     * Visits a given business's profile page
     * Populates route query so this page can be returned to
     * @param businessId Id of the business to visit
     */
    async viewProfile(businessId) {
      const query = {
        businessType : this.selectedBusinessType,
        orderBy : this.orderBy,
        page : this.currentPage.toString(),
        reverse : this.reverse.toString(),
        searchQuery : this.searchQuery
      };
      await this.$router.push({name: 'businessProfile',params:{id:businessId} , query});
    }
  },

  watch: {
    searchQuery: {
      handler() {
        this.debouncedUpdateQuery();
      },
      immediate: true,
    } ,
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
    totalPages() {
      // Ensures that the current page is at least 1 and less than or equal to the total number of pages.
      this.currentPage = Math.max(Math.min(this.currentPage, this.totalPages), 1);
    },
    selectedBusinessType() {
      this.updateResults();
    },
  },
  components: {
    SearchBusinessResult,
  },
};
</script>

<style scoped>
.toggle {
  margin-left: 10px;
}

</style>