<template>
  <v-combobox
    v-model="state"
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
export default {
  name: 'StateAutocomplete',
  data () {
    return {
      state: '',
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
        let url = `https://photon.komoot.io/api/?q=${encodeURIComponent(val)}&osm_tag=place:state&osm_tag=place:region&osm_tag=place:province`;

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //state, province, region
            //it will add that result into the autocomplete suggestion
            this.stateItems.push(feature.properties.name);
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
