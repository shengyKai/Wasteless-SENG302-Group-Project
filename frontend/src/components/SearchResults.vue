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
          {text: 'Relevance', value: 'relevance' },
          {text: 'User Id', value: 'userId' },
          {text: 'First Name', value: 'firstName' },
          {text: 'Middle Name', value: 'middleName'},
          {text: 'Last Name', value: 'lastName' },
          {text: 'Nickname', value: 'nickname' },
          {text: 'Email', value: 'email' },
          {text: 'Address', value: 'address' }
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
    <v-list v-if="users !== undefined" three-line>
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
      resultsMessage: '',
      totalResults: undefined
    };
  },

  computed: {
    totalPages () {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
  },

  created () {
    this.debouncedDoQuery = debounce(() => {
      console.log(this.totalResults);
      if (this.searchQuery) {
        this.updateQuery();
      }
    }, 500);
  },

  mounted () {
    let query = this.$route.query.query;
    if (query) {
      this.updateQuery();
    }
  },
  methods: {
    /**
     * This function gets called when the search query is changed.
     */
    async updateQuery() {
      this.totalResults = await getSearchCount(this.searchQuery);
      this.searchedQuery = this.searchQuery;
      await this.updateNotQuery();
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
        this.reverse.toString()
      );
      if (typeof value === 'string') {
        this.users = undefined;
        this.error = value;
      } else {
        this.users = value;
        this.error = undefined;
      }
    },
  },

  watch: {
    searchQuery() {
      this.debouncedDoQuery();
    },
    orderBy() {
      this.updateNotQuery();
    },
    reverse() {
      this.updateNotQuery();
    },
    currentPage() {
      this.updateNotQuery();
    },

    users: {
      immediate: true,
      handler() {
        // TODO move this into the template or into a computed property
        const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
        const pageEndIndex = pageStartIndex + this.users.length;

        if (pageStartIndex === pageEndIndex) {
          this.resultsMessage = 'There are no results to show';
        } else {
          this.resultsMessage = `Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
        }
      }
    },
    totalPages() {
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