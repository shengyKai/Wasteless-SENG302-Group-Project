<template>
  <v-combobox
    label="City"
    :items="cityItems"
    :loading="isLoading"
    :search-input.sync="citySearch"
    :rules="mandatoryRules.concat(maxCharRule)"
    no-filter
    clearable
    outlined
  />
</template>

<script>
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
      ],
      maxCharRule: [
        field => (field.length <= 100) || 'Reached max character limit: 100'
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
        let url = `https://photon.komoot.io/api/?q=${encodeURIComponent(val)}&osm_tag=place:city&osm_tag=place:town`;

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //city
            //it will add that result into the autocomplete suggestion
            this.cityItems.push(feature.properties.name);
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
