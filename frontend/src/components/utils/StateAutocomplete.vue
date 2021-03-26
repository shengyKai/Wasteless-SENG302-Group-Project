<template>
  <v-combobox
    label="State/Province/Region"
    :items="stateItems"
    :loading="isLoading"
    :search-input.sync="stateSearch"
    :rules="mandatoryRules"
    no-filter
    clearable
    outlined
  />
</template>

<script>
import {insertResultsFromAPI} from './Methods/autocomplete.ts';

export default {
  name: 'StateAutocomplete',
  data () {
    return {
      stateItems: [],
      isLoading: false,
      stateSearch: null,
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ]
    };
  },

  watch: {
    stateSearch (val) {
      this.stateItems = [];
      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true;
        //append to the api url the input the user has entered
        //added option for api that only produces english results
        let url = `https://photon.komoot.io/api/?lang=en&q=${encodeURIComponent(val)}&osm_tag=place`;
        insertResultsFromAPI(url, this.stateItems).then(() => {
          //after everything is shown, the loading animation will stop
          this.isLoading = false;
        });
      }
    },
  }
};
</script>
