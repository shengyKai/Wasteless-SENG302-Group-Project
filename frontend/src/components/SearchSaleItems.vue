<template>
  <div>
    <v-toolbar dark color="secondary" class="mb-1">
      <!-- PUT SEARCH STUFF HERE -->
    </v-toolbar>
    <v-alert
      v-if="error !== undefined"
      type="error"
      dismissible
      @input="error = undefined"
    >
      {{ error }}
    </v-alert>
    <!-- PUT RESULTS HERE -->
    <div class="results">
      <!-- Three cards per row -->
      <SaleResult class="sale-result"/>
      <SaleResult class="sale-result"/>
      <SaleResult class="sale-result"/>
      <SaleResult class="sale-result"/>
      <SaleResult class="sale-result"/>
    </div>
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
  </div>
</template>

<script>
import SaleResult from "@/components/SaleListing/SaleResult";

export default {
  name: "SearchSaleItems",
  components: {
    SaleResult
  },
  data() {
    return {
      currentPage: 1,
      totalPages: 1,
      error: undefined,
      resultsPerPage: 10,
      results: undefined
    };
  },
  computed: {
    totalResults() {
      if (this.results === undefined) return 0;
      return this.results.count;
    },
  }
};
</script>

<style scoped>

</style>