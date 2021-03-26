<template>
  <v-combobox
    label="District/City Area"
    :items="districtItems"
    :loading="isLoading"
    :search-input.sync="districtSearch"
    no-filter
    clearable
    outlined
  />
</template>

<script>
import {insertResultsFromAPI} from './Methods/autocomplete.ts';

export default {
  name: 'DistrictAutocomplete',
  data () {
    return {
      districtItems: [],
      isLoading: false,
      districtSearch: null
    };
  },

  watch: {
    districtSearch (val) {
      this.districtItems = [];
      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true;
        //append to the api url the input the user has entered
        //added option for api that only produces english results
        let url = `https://photon.komoot.io/api/?lang=en&q=${encodeURIComponent(val)}&osm_tag=place:suburb`;
        insertResultsFromAPI(url, this.districtItems).then(() => {
          //after everything is shown, the loading animation will stop
          this.isLoading = false;
        });
      }
    },
  }
};
</script>
