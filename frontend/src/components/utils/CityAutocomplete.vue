<template>
  <v-combobox
      v-model="city"
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
export default {
  name: 'CityAutocomplete',
  data () {
    return {
      city: '',
      cityItems: [],
      isLoading: false,
      citySearch: null,
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ]
    }
  },

  watch: {
    citySearch (val) {
      this.cityItems = []

      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true
        //append to the api url the input the user has entered
        let url = 'https://photon.komoot.io/api/?q=' + val

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //city
            //it will add that result into the autocomplete suggestion
            if (feature.properties.city !== undefined) {
              this.cityItems.push(feature.properties.city)
            }
          })
        }).catch(err => {
          console.log(err)
          //after everything is shown, the loading animation will stop
        }).finally(() => (this.isLoading = false))
      }
    },
  }
}
</script>
