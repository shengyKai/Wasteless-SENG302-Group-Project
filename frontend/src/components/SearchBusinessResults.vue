<template>
  <div>
    <v-toolbar dark color="darkGrey" class="mb-1">
      <!-- Temporary stand in for search bar component -->
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
      <v-spacer/>
      <v-select
        v-model="orderBy"
        flat
        solo-inverted
        hide-details
        :items="[
          { text: 'RRRelevance',   value: 'relevance'  },
          { text: 'User ID',     value: 'userId'     },
          { text: 'First Name',  value: 'firstName'  },
          { text: 'Middle Name', value: 'middleName' },
          { text: 'Last Name',   value: 'lastName'   },
          { text: 'Nickname',    value: 'nickname'   },
          { text: 'Email',       value: 'email'      },
        ]"
        prepend-inner-icon="mdi-sort-variant"
        label="Sort by"
      />
      <v-btn-toggle class="toggle" v-model="reverse" mandatory>
        <v-btn depressed color="primary" :value="false">
          <v-icon>mdi-arrow-up</v-icon>
        </v-btn>
        <v-btn depressed color="primary" :value="true">
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
      <!--users would produce the results for each page, and then it will show each result with
      SearchResultItem-->
      <template v-for="(user, index) in users">
        <v-divider v-if="user === undefined" :key="'divider-'+index"/>
        <SearchResultItem v-else :key="user.id" :user="user"/>
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
      <!-- {{ resultsMessage }} -->
      HAHA waiting for CONORRRRRR
    </v-row>
  </div>
</template>

<script>
import SearchResultItem from './cards/SearchResultItem';
import { search } from '../api/internal';
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
      orderBy: 'relevance',
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
     * List of users on the current page
     */
    users() {
      if (this.results === undefined) return [];
      return this.results.results;
    },
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
      if (this.users.length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.users.length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
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
      if (!this.searchQuery) return; // If the current search query is empty, do not search

      this.searchedQuery = this.searchQuery;

      const value = await search (
        this.searchedQuery,
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
  },

  components: {
    SearchResultItem,
  },
};
</script>

<style scoped>
.toggle {
  margin-left: 10px;
}
</style>