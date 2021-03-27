<template>
  <v-combobox
    :label="label"
    :items="autocompleteItems"
    :loading="isLoading"
    :search-input.sync="search"
    :rules="mandatoryRules"
    no-filter
    clearable
    outlined
  />
</template>

<script>
import {insertResultsFromAPI} from './Methods/autocomplete.ts';

const AUTOCOMPLETE_TYPES = {
  country: {
    label: 'Country',
    tags: 'osm_tag=place:country',
  },
  state: {
    label: 'State/Province/Region',
    tags: 'osm_tag=place',
  },
  city: {
    label: 'City',
    tags: 'osm_tag=place:city&osm_tag=place:town',
  },
  district: {
    label: 'District/City Area',
    tags: 'osm_tag=place:suburb'
  }
};


export default {
  name: 'LocationAutocomplete',
  props: ['type'],
  data () {
    return {
      label: AUTOCOMPLETE_TYPES[this.type].label,
      autocompleteItems: [],
      isLoading: false,
      search: null,
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ]
    };
  },

  watch: {
    search (val) {
      this.autocompleteItems = [];
      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true;
        //append to the api url the input the user has entered
        //added option for api that only produces english results
        let url = `https://photon.komoot.io/api/?lang=en&q=${encodeURIComponent(val)}&${AUTOCOMPLETE_TYPES[this.type].tags}`;
        insertResultsFromAPI(url, this.autocompleteItems).then(() => {
          //after everything is shown, the loading animation will stop
          this.isLoading = false;
        });
      }
    },
  }
};
</script>
