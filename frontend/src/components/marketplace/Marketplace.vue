<template>
  <div>
    <v-container fluid>
      <v-row align="center">
        <v-col
          class="d-flex"
          cols="auto"
        >
          <v-select
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added',                 value: 'created'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col cols="auto">
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
        <v-container class="grey lighten-2">
          <v-row>
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
      cards: {
        "For sale": [{id: 0}, {id: 1}, {id: 2}, {id: 3}, {id: 4}, {id: 5}, {id: 6}],
        "Wanted": [],
        "Exchange": [{id: 0}, {id: 1}, {id: 2}, {id: 3}, {id: 4}]
      }
    };
  },
  components: {
    MarketplaceCard
  }
};
</script>
