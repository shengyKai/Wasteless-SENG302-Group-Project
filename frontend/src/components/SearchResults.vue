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
        v-model="sortByKey"
        flat
        solo-inverted
        hide-details
        :items="Object.keys(comparators)"
        prepend-inner-icon="mdi-sort-variant"
        label="Sort by"
      />
      <v-btn-toggle class="toggle" v-model="isSortDescending" mandatory>
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
      <!--visiblePages would produce the results for each page, and then it will show each result with
      SearchResultItem-->
      <template v-for="(user, index) in visiblePages">
        <v-divider v-if="user === undefined" :key="'divider-'+index"/>
        <SearchResultItem v-else :key="user.id" :user="user"/>
      </template>
    </v-list>
    <!--paginate results-->
    <v-pagination
      v-model="currentPage"
      :length="Math.ceil(users.length/resultsPerPage)"
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
import { search } from '../api';
import { debounce } from '../utils';

// TODO Delete this
const MOCK_USERS = [
  {
    id: 0,
    firstName: 'Tim',
    lastName: 'Tam',
    email: 'tim.tam@hotmail.com',
  },
  {
    id: 1,
    firstName: 'Tim',
    lastName: 'Lame',
    email: 'tim.lame@hotmail.com',
  },
  {
    id: 2,
    firstName: 'Rick',
    lastName: 'Mayo',
    email: 'rick.mayo@hotmail.com',
  },
  {
    id: 3,
    firstName: 'Danny',
    lastName: 'Blast',
    email: 'danny.blast@gmail.com',
  },
  {
    id: 4,
    firstName: 'Barack',
    lastName: 'Obama',
    email: 'barack.obama@gmail.com',
  },
  {
    id: 5,
    firstName: 'Jeff',
    lastName: 'Obama',
    email: 'barack.obama@gmail.com',
  },
];

const USER_COMPARATORS = {
  // If first comparison results in a == b then fallback to other comparator.
  'First Name': (a, b) =>
    a.firstName.localeCompare(b.firstName) ||
    a.lastName.localeCompare(b.lastName),
  'Last Name': (a, b) =>
    a.lastName.localeCompare(b.lastName) ||
    a.firstName.localeCompare(b.firstName),
};

export default {
  data: function () {
    return {
      searchQuery: this.$route.query.query || '',
      users: MOCK_USERS,
      comparators: USER_COMPARATORS,
      error: undefined,
      isSortDescending: false,
      sortByKey: 'First Name',
      currentPage: 1,
      resultsPerPage: 3,
      resultsMessage: ''
    };
  },

  computed: {
    sortedUsers () {
      if (this.users === undefined) return undefined;
      let result = Array.from(this.users).sort(
        this.comparators[this.sortByKey]
      );
      if (this.isSortDescending) result.reverse();
      return result;
    },
    //Formula in method slices the results based on the number of results per page and which page the user is
    //currently at, so that it will show the proper sets of results per page
    visiblePages () {
      return this.sortedUsers.slice((this.currentPage - 1) * this.resultsPerPage, this.currentPage * this.resultsPerPage);
    },
  },

  created () {
    this.debouncedDoQuery = debounce(() => {
      search(this.searchQuery).then(this.setResults);
    }, 500);
  },

  mounted () {
    let query = this.$route.query.query;
    if (query) {
      search(this.searchQuery).then(this.setResults);
    }
  },
  methods: {
    setResults (value) {
      if (typeof value === 'string') {
        this.users = undefined;
        this.error = value;
      } else {
        this.users = value;
        this.error = undefined;
      }
    }
  },

  watch: {
    searchQuery () {
      this.debouncedDoQuery();
    },
    visiblePages: {
      immediate: true,
      handler () {
        if (this.users.length === 0) {
          this.resultsMessage = 'There are no results to show';
        } else if (Math.floor(this.users.length / (this.currentPage * this.resultsPerPage)) === 0) {
          this.resultsMessage = 'Displaying ' + (((this.currentPage - 1) * this.resultsPerPage) + 1) + ' - ' +
            this.users.length + ' of ' + this.users.length + ' results';
        } else {
          this.resultsMessage = 'Displaying ' + (((this.currentPage - 1) * this.resultsPerPage) + 1) + ' - ' +
            this.currentPage * this.resultsPerPage + ' of ' + this.users.length + ' results';
        }
      }
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