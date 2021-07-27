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
        <BusinessSearchResult v-else :key="business.id" :business="business"/>
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
import BusinessSearchResult from './cards/BusinessSearchResult';
import { searchBusinesses, BUSINESS_TYPES } from '../api/internal';
import { debounce } from '../utils';

export default {
  name: 'SearchBusinessResults',
  data() {
    return {
      /**
       * The contents of the search box.
       */
      searchQuery: this.$route.query.query || '',
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
      reverse: false,
      /**
       * The current search result order
       */
      orderBy: 'name',
      /**
       * Currently selected page (1 is first page)
       */
      currentPage: 1,
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
      selectedBusinessType: undefined,
      /**
       * Options for ordering the business search results
       */
      orderByOptions: [
        {text: "Date Registered", value: "created"},
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
      if (!this.searchQuery && filterBusinessType === undefined) return; // If the current search query is empty, do not search

      this.searchedQuery = this.searchQuery;

      const value = await searchBusinesses(
        this.searchedQuery,
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
    BusinessSearchResult,
  },
};
</script>

<style scoped>
.toggle {
  margin-left: 10px;
}

</style>