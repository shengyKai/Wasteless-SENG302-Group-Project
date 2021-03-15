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
        let url = 'https://photon.komoot.io/api/?q=' + val;

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //state, province, region
            //it will add that result into the autocomplete suggestion
            if (feature.properties.state !== undefined) {
              this.stateItems.push(feature.properties.state);
            }
            if (feature.properties.province !== undefined) {
              this.stateItems.push(feature.properties.province);
            }
            if (feature.properties.region !== undefined) {
              this.stateItems.push(feature.properties.region);
            }
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
