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
          <v-row justify="space-around">
            <v-col v-for="card in cards[section]" :key="card.id" cols="auto">
              <MarketplaceCard :content="card"/>
            </v-col>
          </v-row>
        </v-container>
      </v-tab-item>
    </v-tabs-items>
  </div>
</template>

<script>
import MarketplaceCard from "../cards/MarketplaceCard";

export default {
  data() {
    return {
      tab: null,
      sections: ["For sale", "Wanted", "Exchange"],
      // TODO Get cards for each section with API call when that has been implemented.
      cards: {
        "For sale": [
          {id: 0, creator: {firstName: 'Tim'   , lastName: 'Tam'       , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-19', title: 'Tim Tams from Timmy',             description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus.', keywords: [{name: 'Home Made'}, {name: 'Organic'}]},
          {id: 1, creator: {firstName: 'Andy'  , lastName: 'Elliot'    , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-20', title: 'Dunno what to do for this title', description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Fresh'}]},
          {id: 2, creator: {firstName: 'Dave'  , lastName: 'Daniel'    , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-21', title: 'Jack Daniels',                    description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Vegan'}, {name: 'Vegitarian'}, {name: 'Locally Produced'}, {name: 'Other'}]},
          {id: 3, creator: {firstName: 'Jeff'  , lastName: 'Bezos'     , location: { country: 'New Zealand', city: 'Auckland',     district: 'Wherever'       }}, created: '2021-05-22', title: 'Amazon Treats',                   description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []},
          {id: 4, creator: {firstName: 'Mark'  , lastName: 'Zuckerburg', location: { country: 'New Zealand', city: 'Christchurch', district: 'Upper Riccarton'}}, created: '2021-05-23', title: 'Facecook',                        description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: [{name: 'Spicy'}]},
          {id: 5, creator: {firstName: 'Connor', lastName: 'Hitchcock' , location: { country: 'New Zealand', city: 'Christchurch', district: 'Upper Riccarton'}}, created: '2021-05-24', title: 'Connor\'s magic stuff',           description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []},
          {id: 6, creator: {firstName: 'Nathan', lastName: 'Smithies'  , location: { country: 'New Zealand', city: 'Christchurch', district: 'Hoon Hay'       }}, created: '2021-05-25', title: 'The Nathan Apple',                description: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit. Vivamus hendrerit nisl ac pharetra cursus. Vestibulum gravida varius purus, in maximus ante fermentum sed. Curabitur ultrices accumsan metus ia', keywords: []}
        ],
        "Wanted": [],
        "Exchange": [{id: 0, title: 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa'}, {id: 1}, {id: 2}, {id: 3}, {id: 4}]
      }
    };
  },
  components: {
    MarketplaceCard
  }
};
</script>
