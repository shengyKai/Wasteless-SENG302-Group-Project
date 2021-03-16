<template>
  <v-combobox
    label="Country"
    :items="countryItems"
    :loading="isLoading"
    :search-input.sync="countrySearch"
    :rules="mandatoryRules.concat(maxCharRule)"
    no-filter
    clearable
    outlined
  />
</template>

<script>
export default {
  name: 'CountryAutocomplete',
  data () {
    return {
      countryItems: [],
      isLoading: false,
      countrySearch: null,
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ],
      maxCharRule: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
      ]
    };
  },

  watch: {
    countrySearch (val) {
      this.countryItems = [];

      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true;
        //append to the api url the input the user has entered
        let url = `https://photon.komoot.io/api/?q=${encodeURIComponent(val)}&osm_tag=place:country`;

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //country
            //it will add that result into the autocomplete suggestion
            this.countryItems.push(feature.properties.name);
          });
        }).catch(err => {
          console.log(err);
          //after everything is shown, the loading animation will stop
        }).finally(() => (this.isLoading = false));
      }
    },
  }
};
</script>
