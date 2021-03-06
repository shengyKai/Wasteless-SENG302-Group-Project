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
    <v-container fluid>
      <v-row align="center">
        <v-col
          class="d-flex"
          cols="auto"
        >
          <h3 class="creator-text"> Cards created by {{ this.user.firstName }} {{ this.user.lastName }} </h3>
        </v-col>
        <v-spacer/>
        <v-col
          class="d-flex"
          cols="auto"
        >
          <v-btn outlined color="primary" @click="viewProfile">
            Go to profile
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <!---Grid of cards --->
    <v-container class="grey lighten-2 mt-2">
      <v-row>
        <v-col v-for="card in cards" :key="card.id" cols="12" sm="6" md="4" lg="3">
          <MarketplaceCard :showSection="true" :showActions="true" :content="card" @delete-card="updateMarketplace"/>
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
import {getUser} from "@/api/user";
import {getMarketplaceCardsByUser} from "@/api/marketplace";

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
       */
      totalResults: 0,
      error: undefined,
      user: {},
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
      let response = await getMarketplaceCardsByUser(this.$route.params.id, this.resultsPerPage, this.currentPage);
      this.totalResults = response.count;
      this.cards = response.results;
    },
    /**
     * Updates the marketplace based the actions done in the Marketplace Card.
     */
    updateMarketplace(response) {
      if (typeof response === "string") {
        this.error = response;
      } else {
        this.updatePage();
      }
    },
    async updateUser() {
      this.user = await getUser(this.$route.params.id);
    },
    viewProfile() {
      this.$router.push(`/profile/${this.user.id}`);
    }
  },
  created() {
    this.updatePage();
    this.updateUser();
  }
};
</script>

<style>
.creator-text {
  color: var(--v-primary-base);
}
</style>