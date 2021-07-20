<template>
  <div>
    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <!---Grid of cards --->
    <v-container class="grey lighten-2 mt-2">
      <v-row>
        <v-col v-for="card in cards" :key="card.id" cols="12" sm="6" md="4" lg="3">
          <MarketplaceCard :showSection="true" :content="card"/>
        </v-col>
      </v-row>
    </v-container>
    <v-pagination
      v-model="currentPage"
      :total-visible="11"
      :length="totalPages"
      @next="updatePage"
      @previous="updatePage"
      @input="updatePage"
      circle
    />
    <!--Text to display range of results out of total number of results-->
    <v-row justify="center" no-gutters>
      {{ resultsMessage }}
    </v-row>
  </div>
</template>

<script>
import MarketplaceCard from "../cards/MarketplaceCard";
import {getMarketplaceCardsByUser} from "../../api/internal.ts";

export default {
  name: 'UserCards',
  components: {
    MarketplaceCard
  },
  data() {
    return {
      cards: [],
      currentPage: 1,
      /**
       * Number of results per a result page
       */
      resultsPerPage: 12,
      /**
       * Total number of results for all pages
       * For now, it is hard coded to suit the above aesthetic. once the api method to retrieve the count is created, it can
       * be replaced with dynamic values.
       */
      totalResults: 0,
      error: undefined,
    };
  },

  computed: {
    /**
     * The total number of pages required to show all the users
     * May be 0 if there are no results
     */
    totalPages() {
      return Math.ceil(this.totalResults / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many results there are
     */
    resultsMessage() {
      if (this.cards.length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.cards.length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults} results`;
    },
  },
  methods: {
    /**
     * Fetches all the marketplace cards and the count of the cards relating to the currently logged in user
     */
    async updatePage() {
      let response = await getMarketplaceCardsByUser(this.$store.state.user?.id, this.resultsPerPage, this.currentPage);
      this.totalResults = response.count;
      this.cards = response.results;
    }
  },
  created() {
    this.updatePage();
  }
};
</script>
