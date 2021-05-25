<template>
  <div>
    <v-container fluid>
      <v-row align="center">
        <v-col
          class="d-flex"
          cols="auto"
        >
          <!---Select component for the order in which the cards should be displayed--->
          <v-select
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added', value: 'created'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col cols="auto">
          <!---Reverse the order in which the cards should be displayed--->
          <v-btn-toggle class="toggle" mandatory>
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col
          class="d-flex"
          cols="auto"
        >
          <!---Search for cards by their keywords--->
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Keywords"
            autofocus
          />
        </v-col>
        <v-spacer/>
        <v-col cols="auto" class="text-right" >
          <!---Link to modal for creating new card--->
          <v-btn type="button" color="primary" rounded>
            Create card
          </v-btn>
        </v-col>
      </v-row>
    </v-container>
    <v-tabs
      v-model="tab"
      grow
    >
      <!---Tabs for dividing marketplace into for sale, wanted and exchange sections--->
      <v-tab
        v-for="section in sections"
        :key="section"
      >
        {{ section }}
      </v-tab>
    </v-tabs>

    <v-tabs-items v-model="tab">
      <v-tab-item
        v-for="section in sections"
        :key="section"
      >
        <!---Grid of cards for one section--->
        <v-container class="grey lighten-2">
          <v-row>
            <v-col v-for="card in cards[section]" :key="card.id" cols="12" sm="6" md="4" lg="3">
              <MarketplaceCard :content="card"/>
            </v-col>
          </v-row>
        </v-container>
        <v-pagination
          v-model="currentPage[section]"
          :length="totalPages(section)"
          circle
        />
        <!--Text to display range of results out of total number of results-->
        <v-row justify="center" no-gutters>
          {{ resultsMessage(section) }}
        </v-row>
      </v-tab-item>
    </v-tabs-items>

  </div>
</template>

<script>
import MarketplaceCard from "../cards/MarketplaceCard";
import {getCardsBySection, getCardCount} from "../../api/internal.ts";

export default {
  data() {
    return {
      tab: null,
      sections: ["For Sale", "Wanted", "Exchange"],
      // TODO Get cards for each section with API call when that has been implemented.
      // cards: {
      //   "For Sale": [
      //     {id: 0, creator: {firstName: 'Tim'   , lastName: 'Tam'       , homeAddress: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-19', title: 'Tim Tams from Timmy',             description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus.', keywords: [{name: 'Home Made'}, {name: 'Organic'}]},
      //     {id: 1, creator: {firstName: 'Andy'  , lastName: 'Elliot'    , homeAddress: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-20', title: 'Dunno what to do for this title', description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Fresh'}]},
      //     {id: 2, creator: {firstName: 'Dave'  , lastName: 'Daniel'    , homeAddress: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-21', title: 'Jack Daniels',                    description: 'Short description', keywords: [{name: 'Vegan'}, {name: 'Vegitarian'}, {name: 'Locally Produced'}, {name: 'Other'}]},
      //     {id: 3, creator: {firstName: 'Jeff'  , lastName: 'Bezos'     , homeAddress: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-22', title: 'Amazon Treats',                   description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []},
      //     {id: 4, creator: {firstName: 'Mark'  , lastName: 'Zuckerburg', homeAddress: { country: 'New Zealand', city: 'Christchurch', district: 'Upper Riccarton'}}, created: '2021-05-23', title: 'Facecook',                        description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Spicy'}]},
      //     {id: 5, creator: {firstName: 'Connor', lastName: 'Hitchcock' , homeAddress: { country: 'New Zealand', city: 'Christchurch', district: 'Upper Riccarton'}}, created: '2021-05-24', title: 'Connor\'s magic stuff',           description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []},
      //     {id: 6, creator: {firstName: 'Nathan', lastName: 'Smithies'  , homeAddress: { country: 'New Zealand', city: 'Christchurch', district: 'Hoon Hay'       }}, created: '2021-05-25', title: 'The Nathan Apple',                description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []}
      //   ],
      //   "Wanted": [],
      //   "Exchange": [{id: 0, title: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'}, {id: 1}, {id: 2}, {id: 3}, {id: 4}]
      // },
      cards: {
        "For Sale": [],
        "Wanted": [],
        "Exchange": []
      },
      currentPage: {
        "For Sale": 1,
        "Wanted": 1,
        "Exchange": 1,
      },
      /**
       * Number of results per a result page
       */
      resultsPerPage: 8,
      /**
       * Total number of results for all pages
       * For now, it is hard coded to suit the above aesthetic. once the api method to retrieve the count is created, it can
       * be replaced with dynamic values.
       */
      totalResults: {
        "For Sale": 0,
        "Wanted": 0,
        "Exchange": 0,
      },
      error: ""
    };
  },
  methods: {
    /**
     * Iterates through the 3 sections and gets all the cards and card count
     */
    async updateResults() {
      for (const index in this.sections) {
        const value = await getCardsBySection (
          this.sections[index],
          this.currentPage[this.sections[index]],
          this.resultsPerPage
        );
        this.totalResults[this.sections[index]] = await getCardCount(this.sections[index]);
        if (typeof value === 'string') {
          this.cards[this.sections[index]] = {};
          this.error = value;
        } else {
          this.cards[this.sections[index]] = value;
          this.error = undefined;
        }
      }

    },
    /**
     * The total number of pages required to show all the users
     * May be 0 if there are no results
     */
    totalPages (section) {
      return Math.ceil(this.totalResults[section] / this.resultsPerPage);
    },
    /**
     * The message displayed at the bottom of the page to show how many results there are
     */
    resultsMessage(section) {
      if (this.cards[section].length === 0) return 'There are no results to show';

      const pageStartIndex = (this.currentPage[section] - 1) * this.resultsPerPage;
      const pageEndIndex = pageStartIndex + this.cards[section].length;
      return`Displaying ${pageStartIndex + 1} - ${pageEndIndex} of ${this.totalResults[section]} results`;
    },
  },
  components: {
    MarketplaceCard
  },
  watch: {
    currentPage: {
      handler() {
        console.log("here");
        this.updateResults();
      },
      deep: true
    },
    resultsPerPage() {
      this.updateResults();
    },
  },
  async created() {
    await this.updateResults();
  },
};
</script>
