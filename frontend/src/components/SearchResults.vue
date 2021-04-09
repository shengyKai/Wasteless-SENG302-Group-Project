<template>
  <div>
    <v-toolbar dark color="primary" class="mb-1">
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
          { text: 'Relevance',   value: 'relevance'  },
          { text: 'User ID',     value: 'userId'     },
          { text: 'First Name',  value: 'firstName'  },
          { text: 'Middle Name', value: 'middleName' },
          { text: 'Last Name',   value: 'lastName'   },
          { text: 'Nickname',    value: 'nickname'   },
          { text: 'Email',       value: 'email'      },
          { text: 'Address',     value: 'address'    },
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

    <v-alert v-if="error !== undefined" type="error"> {{ error }}</v-alert>
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
import SearchResultItem from './SearchResultItem';
import {getSearchCount, search} from '../api';
import { debounce } from '../utils';

export default {
  name: 'SearchResults',
  data: function() {
    return {
      searchQuery: this.$route.query.query || '',
      searchedQuery: undefined,
      users: [],
      error: undefined,
      reverse: false,
      orderBy: 'relevance',
      currentPage: 1,
      resultsPerPage: 10,
      totalResults: 0,
      debouncedUpdateQuery: debounce(this.updateQuery, 500),
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
      if (this.users.length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.users.length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
  },
  methods: {
    /**
     * This function gets called when the search query is changed.
     */
    async updateQuery() {
      if (!this.searchQuery) return; // If the current search query is empty, do not search

      this.searchedQuery = this.searchQuery;

      await Promise.all([
        getSearchCount(this.searchQuery).then(count => {
          if (typeof count === 'string') {
            this.error = count;
          } else {
            this.totalResults = count;
          }
        }),
        await this.updateNotQuery(),
      ]);
    },

    /**
     * This function gets called when the search results need to change, but the search query has not changed.
     * The page index, results per page, order by and reverse variables notify this function.
     */
    async updateNotQuery() {
      const value = await search (
        this.searchedQuery,
        this.currentPage,
        this.resultsPerPage,
        this.orderBy,
        this.reverse
      );
      if (typeof value === 'string') {
        this.users = [];
        this.error = value;
      } else {
        this.users = value;
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
      this.updateNotQuery();
    },
    reverse() {
      this.updateNotQuery();
    },
    currentPage() {
      this.updateNotQuery();
    },
    resultsPerPage() {
      this.updateNotQuery();
    },
    totalPages() {
      // Ensures that the current page is at least 1 and less than or equal to the total number of pages.
      this.currentPage = Math.max(Math.min(this.currentPage, this.totalPages), 1);
    }
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