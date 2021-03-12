<template>
  <v-combobox
      v-model="country"
      label="Country"
      :items="countryItems"
      :loading="isLoading"
      :search-input.sync="countrySearch"
      :rules="mandatoryRules"
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
      country: '',
      countryItems: [],
      isLoading: false,
      countrySearch: null,
      mandatoryRules: [
        //All fields with the class "required" will go through this ruleset to ensure the field is not empty.
        //if it does not follow the format, display error message
        field => !!field || 'Field is required'
      ]
    }
  },

  watch: {
    countrySearch (val) {
      this.countryItems = []

      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true
        //append to the api url the input the user has entered
        let url = 'https://photon.komoot.io/api/?q=' + val

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //country
            //it will add that result into the autocomplete suggestion
            if (feature.properties.country !== undefined) {
              this.countryItems.push(feature.properties.country)
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
