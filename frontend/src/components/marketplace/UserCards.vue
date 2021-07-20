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

export default {
  name: 'UserCards',
  components: {
    MarketplaceCard
  },
  data() {
    return {
      cards: [
        {id: 0, section: 'ForSale',  creator: {firstName: 'Tim'   , lastName: 'Tam'       , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-19', title: 'Tim Tams from Timmy',             description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus.', keywords: [{name: 'Home Made'}, {name: 'Organic'}]},
        {id: 1, section: 'Wanted',   creator: {firstName: 'Andy'  , lastName: 'Elliot'    , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-20', title: 'Dunno what to do for this title', description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Fresh'}]},
        {id: 2, section: 'ForSale',  creator: {firstName: 'Dave'  , lastName: 'Daniel'    , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-21', title: 'Jack Daniels',                    description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Vegan'}, {name: 'Vegitarian'}, {name: 'Locally Produced'}, {name: 'Other'}]},
        {id: 3, section: 'ForSale',  creator: {firstName: 'Jeff'  , lastName: 'Bezos'     , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-22', title: 'Amazon Treats',                   description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []},
        {id: 4, section: 'Wanted',   creator: {firstName: 'Mark'  , lastName: 'Zuckerburg', location: { country: 'New Zealand', city: 'Christchurch', district: 'Upper Riccarton'}}, created: '2021-05-23', title: 'Facecook',                        description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Spicy'}]},
        {id: 5, section: 'Exchange', creator: {firstName: 'Connor', lastName: 'Hitchcock' , location: { country: 'New Zealand', city: 'Christchurch', district: 'Upper Riccarton'}}, created: '2021-05-24', title: 'Connor\'s magic stuff',           description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []},
        {id: 6, section: 'Exchange', creator: {firstName: 'Nathan', lastName: 'Smithies'  , location: { country: 'New Zealand', city: 'Christchurch', district: 'Hoon Hay'       }}, created: '2021-05-25', title: 'The Nathan Apple',                description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []}
      ],
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
      totalResults: 100,
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
};
</script>
