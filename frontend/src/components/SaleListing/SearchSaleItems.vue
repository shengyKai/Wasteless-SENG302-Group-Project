<template>
  <v-container>
    <v-card color="secondary" dark class="mb-1 search-bar" v-show="!showAdvancedSearch">
      <v-row>
        <v-col>
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Search"
            color="secondary"
            class="search-field"
          />
        </v-col>
        <v-spacer/>
        <v-col cols="2">
          <v-card-actions>
            <v-btn outlined @click="showAdvancedSearch=true">Advanced search</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
    </v-card>
    <v-card color="secondary" dark class="mb-1 search-bar" v-show="showAdvancedSearch">
      <v-row>
        <v-col cols="4">
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Product"
            color="secondary"
            class="search-field"
          />
        </v-col>
        <v-spacer/>
        <v-col cols="2">
          <v-card-actions>
            <v-btn outlined @click="showAdvancedSearch=false">Simple search</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="4">
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Business"
            color="secondary"
            class="search-field"
          />
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="4">
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Location"
            color="secondary"
            class="search-field"
          />
        </v-col>
      </v-row>
    </v-card>
    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <!-- PUT RESULTS HERE -->
    <!--paginate results-->
    <v-pagination
      v-model="currentPage"
      :total-visible="11"
      :length="totalPages"
      circle
    />
    <!--Text to display range of results out of total number of results-->
    <v-row justify="center" no-gutters>
      {{ resultsMessage }}
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: "SearchSaleItems",
  data() {
    return {
      currentPage: 1,
      totalPages: 1,
      error: undefined,
      resultsPerPage: 10,
      results: undefined,
      showAdvancedSearch: false
    };
  },
  computed: {
    totalResults() {
      if (this.results === undefined) return 0;
      return this.results.count;
    },
    resultsMessage() {
      // TODO implement computing results message based on number of results when linking to endpoint
      return "There are no results to show";
    }
  }
};
</script>

<style scoped>

.search-bar{
  border-radius:0px;
  padding-bottom:10px;
}

.search-field{
  padding-left:10px;
}

</style>