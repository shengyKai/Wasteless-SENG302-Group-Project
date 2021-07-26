<template>
  <div>
    <v-toolbar dark color="primary" class="mb-1">
      <v-row align="center">
        <v-col
          class="d-flex mr-12"
          cols="auto"
        >
          <!---Select component for the order in which the cards should be displayed--->
          <v-select
            v-model="orderBy"
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added / Renewed', value: 'lastRenewed'},
              { text: 'Title', value: 'title'},
              { text: 'Location', value: 'location'},
              { text: 'Author First Name', value: 'creatorFirstName'},
              { text: 'Author Last Name', value: 'creatorLastName'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
          <!---Reverse the order in which the cards should be displayed--->
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col class="mt-7 ml-9 mr-n16" >
          <v-select
            flat
            solo-inverted
            no-data-text="No keywords found"
            value = "keywords"
            v-model="selectedKeywords"
            :items="filteredKeywordList"
            label="Select keywords"
            item-text="name"
            item-value="id"
            multiple
            :hint="selectedKeywords"
            @click="selectedKeywords=undefined"
            persistent-hint
            outlined
          >
            <template v-slot:prepend-item>
              <v-list-item>
                <v-list-item-content>
                  <v-text-field
                    label="Search for a keyword" v-model="keywordFilter"
                    clearable
                    :autofocus="true"
                    @click:clear="resetSearch"
                    hint="Keyword name"
                  />
                </v-list-item-content>
              </v-list-item>
            </template>
          </v-select>
        </v-col>
        <v-col class="text-right mr-n16" >
          <!-- Toggle button for user to choose partially or fully matched results -->
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="primary" :value="false">
              AND
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              OR
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-spacer/>
        <v-col cols="auto" class="text-right" >
          <!---Link to modal for creating new card--->
          <v-btn type="button" color="secondary" @click="showCreateCard" rounded>
            Create card
          </v-btn>
        </v-col>
      </v-row>
    </v-toolbar>
    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <v-tabs
      v-model="tab"
      grow
    >
      <!---Tabs for dividing marketplace into for sale, wanted and exchange sections--->
      <v-tab
        v-for="section in sectionNames"
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
              <MarketplaceCard :showActions="true" :content="card" @delete-card="updateMarketplace"/>
            </v-col>
          </v-row>
        </v-container>
        <v-pagination
          v-model="currentPage[section]"
          :total-visible="11"
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
import {getMarketplaceCardsBySection } from "../../api/internal.ts";
import { SECTION_NAMES } from '@/utils';
import { getKeywords } from '../../api/internal.ts';

export default {
  data() {
    return {
      title: "",
      description: "",
      allKeywords: [],
      selectedKeywords: [],
      keywordFilter: "",
      dialog: true,
      errorMessage: undefined,
      // sections: [{text: "For Sale", value: "ForSale"}, {text: "Wanted", value: "Wanted"}, {text: "Exchange", value: "Exchange"}],
      selectedSection: undefined,
      allowedCharsRegex: /^[\s\d\p{L}\p{P}]*$/u,

      tab: null,
      sectionNames: SECTION_NAMES,
      sections: ["ForSale", "Wanted", "Exchange"],
      cards: {
        ForSale: [],
        Wanted: [],
        Exchange: []
      },
      currentPage: {
        ForSale: 1,
        Wanted: 1,
        Exchange: 1,
      },
      /**
       * Number of results per a result page
       */
      resultsPerPage: 12,
      /**
       * Total number of results for all pages
       * For now, it is hard coded to suit the above aesthetic. once the api method to retrieve the count is created, it can
       * be replaced with dynamic values.
       */
      totalResults: {
        ForSale: 0,
        Wanted: 0,
        Exchange: 0,
      },
      error: "",
      orderBy: "lastRenewed",
      /**
       * Note: change the default here to true because backlog states that
       * creation date should be descending by default.
       */
      reverse: true
    };
  },
  mounted() {
    getKeywords()
      .then((response) => {
        if (typeof response === 'string') {
          this.allKeywords = [];
        } else {
          this.allKeywords = response;
        }})
      .catch(() => (this.allKeywords = []));
  },
  computed: {
    filteredKeywordList() {
      return this.allKeywords.filter(x => this.filterKeywords(x));
    },
  },
  methods: {
    /**
     * Updates the provided sections
     * @param sections Section keys to update
     */
    async updateSections(sections) {
      this.error = undefined;

      const results = await Promise.all(
        sections.map(key => getMarketplaceCardsBySection(key, this.currentPage[key], this.resultsPerPage, this.orderBy, this.reverse))
      );

      for (let i = 0; i<sections.length; i++) {
        const key = sections[i];
        const value = results[i];

        if (typeof value === 'string') {
          this.cards[key] = [];
          this.totalResults[key] = 0;
          this.error = value;
        } else {
          this.cards[key] = value.results;
          this.totalResults[key] = value.count;
        }
      }
    },
    resetSearch() {
      this.keywordFilter = "";
    },
    filterKeywords(keyword) {
      const filterText = this.keywordFilter ?? '';
      return keyword.name.toLowerCase().includes(filterText.toLowerCase());
    },
    showCreateCard() {
      this.$store.commit('showCreateMarketplaceCard', this.$store.state.user);
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
    /**
     * Updates the marketplace based the actions done in the Marketplace Card.
     */
    updateMarketplace(response) {
      if (typeof response === "string") {
        this.error = response;
      } else {
        this.updateSections(this.sections);
      }
    }
  },
  components: {
    MarketplaceCard
  },
  watch: {
    orderBy() {
      this.updateSections(this.sections);
    },
    reverse() {
      this.updateSections(this.sections);
    },
    'currentPage.Wanted': function() {
      this.updateSections(['Wanted']);
    },
    'currentPage.ForSale': function() {
      this.updateSections(['ForSale']);
    },
    'currentPage.Exchange': function() {
      this.updateSections(['Exchange']);
    },
    resultsPerPage() {
      this.updateSections(this.sections);
    },
  },
  async created() {
    await this.updateSections(this.sections);
  },
};
</script>
