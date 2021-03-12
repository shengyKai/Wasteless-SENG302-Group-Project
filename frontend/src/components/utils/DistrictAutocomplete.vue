<template>
  <v-combobox
      v-model="district"
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
export default {
  name: 'DistrictAutocomplete',
  data () {
    return {
      district: '',
      districtItems: [],
      isLoading: false,
      districtSearch: null
    }
  },

  watch: {
    districtSearch (val) {
      this.districtItems = []

      //if input length exists, and has a length more than 2
      if (val && val.length > 2) {
        //show loading animation
        this.isLoading = true
        //append to the api url the input the user has entered
        let url = 'https://photon.komoot.io/api/?q=' + val

        fetch(url).then(res => res.json()).then(res => {
          res.features.forEach(feature => {
            //If the returned GEOJSON has any of these key(s):
            //district
            //it will add that result into the autocomplete suggestion
            if (feature.properties.district !== undefined) {
              this.districtItems.push(feature.properties.district)
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
