<template>
  <v-combobox
    label="City"
    :items="cityItems"
    :loading="isLoading"
    :search-input.sync="citySearch"
    :rules="mandatoryRules"
    no-filter
    clearable
    outlined
  />
</template>

<script>
import {insertResultsFromAPI} from './Methods/autocomplete.ts';

export default {
  name: 'CityAutocomplete',
  data () {
    return {
      cityItems: [],
      isLoading: false,
      citySearch: null,
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ]
    };
  },

  watch: {
    citySearch (val) {
      this.cityItems = [];

      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true;
        //append to the api url the input the user has entered
        //added option for api that only produces english results
        let url = `https://photon.komoot.io/api/?lang=en&q=${encodeURIComponent(val)}&osm_tag=place:city&osm_tag=place:town`;
        insertResultsFromAPI(url, this.cityItems).then(() => {
          //after everything is shown, the loading animation will stop
          this.isLoading = false;
        });
      }
    },
  }
};

</script>
