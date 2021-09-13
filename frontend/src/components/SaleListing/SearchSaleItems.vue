<template>
  <v-container>
    <v-card
      color="secondary"
      dark
      class="mb-1 search-bar"
      v-show="!showAdvancedSearch"
    >
      <v-row>
        <v-col cols="4" md="4" sm="6" xs="8">
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Search"
            color="secondary"
            class="search-field"
            v-model="simpleQuery"
          />
        </v-col>
        <v-spacer />
        <v-col cols="2">
          <v-select
            v-model="orderBy"
            flat
            solo-inverted
            hide-details
            :items="orderByOptions"
            prepend-inner-icon="mdi-sort-variant"
            color="secondary"
            label="Order By"
          />
        </v-col>
        <v-col cols="1">
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="secondary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="secondary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col cols="2" md="2" sm="4">
          <v-card-actions>
            <v-btn outlined @click="showAdvancedSearch = true">Advanced</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
    </v-card>
    <v-card
      color="secondary"
      dark
      class="mb-1 search-bar"
      v-show="showAdvancedSearch"
    >
      <v-row>
        <v-col cols="3">
          <v-text-field
            clearable
            flat
            outlined
            filled
            hide-details
            label="Product"
            class="search-field"
            v-model="productQuery"
          />
        </v-col>
        <v-col cols="2">
          <v-text-field
            clearable
            flat
            outlined
            filled
            hide-details
            label="Lowest price"
          />
        </v-col>
        <v-col cols="2">
          <v-text-field
            clearable
            flat
            outlined
            filled
            hide-details
            label="Highest price"
          />
        </v-col>
        <v-col cols="2">
          <v-select
            style="max-width: 300px"
            v-model="orderBy"
            flat
            solo-inverted
            hide-details
            :items="orderByOptions"
            prepend-inner-icon="mdi-sort-variant"
            color="secondary"
            label="Order By"
          />
        </v-col>
        <v-col cols="1">
          <v-btn-toggle class="toggle" v-model="reverse" mandatory>
            <v-btn depressed color="secondary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="secondary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col cols="2">
          <v-card-actions>
            <v-btn outlined @click="showAdvancedSearch = false">Simple</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="3">
          <v-text-field
            clearable
            flat
            outlined
            filled
            hide-details
            label="Business"
            class="search-field"
            v-model="businessQuery"
          />
        </v-col>
        <v-col cols="2">
          <v-dialog
            ref="dialog"
            v-model="showDatePicker"
            width="300px"
            persistent
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                label="Closing after"
                prepend-inner-icon="mdi-calendar"
                readonly
                v-bind="attrs"
                v-on="on"
                outlined
              />
            </template>
            <v-date-picker scrollable>
              <v-spacer />
              <v-btn text color="primary" @click="showDatePicker = false">
                Cancel
              </v-btn>
              <v-btn
                text
                color="primary"
              >
                OK
              </v-btn>
            </v-date-picker>
          </v-dialog>
        </v-col>
        <v-col cols="2">
          <v-dialog
            ref="dialog"
            v-model="showDatePicker"
            width="300px"
            persistent
          >
            <template v-slot:activator="{ on, attrs }">
              <v-text-field
                label="Closing before"
                prepend-inner-icon="mdi-calendar"
                readonly
                v-bind="attrs"
                v-on="on"
                outlined
              />
            </template>
            <v-date-picker scrollable>
              <v-spacer />
              <v-btn text color="primary" @click="showDatePicker = false">
                Cancel
              </v-btn>
              <v-btn
                text
                color="primary"
              >
                OK
              </v-btn>
            </v-date-picker>
          </v-dialog>
        </v-col>
      </v-row>
      <v-row>
        <v-col cols="3">
          <v-text-field
            clearable
            flat
            outlined
            filled
            hide-details
            label="Location"
            class="search-field"
            v-model="locationQuery"
          />
        </v-col>
        <v-col cols="4">
          <v-select
            v-model="orderBy"
            flat
            solo-inverted
            hide-details
            :items="orderByOptions"
            prepend-inner-icon="mdi-sort-variant"
            color="secondary"
            label="Business type"
          />
        </v-col>
        <v-spacer/>
        <v-col cols="2" md="2" sm="4">
          <v-card-actions>
            <v-btn
              color="white"
              class="secondary--text"
              @click="searchListings"
            >
              Search
              <v-icon right dark> mdi-magnify </v-icon>
            </v-btn>
          </v-card-actions>
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
      showAdvancedSearch: false,
      simpleQuery: undefined,
      productQuery: undefined,
      businessQuery: undefined,
      locationQuery: undefined,
      reverse: false,
      orderByOptions: [
        { text: "Name", value: "name" },
        { text: "Price", value: "price" },
        { text: "Seller", value: "businessName" },
        { text: "Location", value: "businessLocation" },
        { text: "Expiry date", value: "expiry" },
        { text: "Closing date", value: "closes" },
        { text: "Created date", value: "created" },
        { text: "Quantity", value: "quantity" },
      ],
      orderBy: undefined,
      showDatePicker: false,
    };
  },
  computed: {
    /**
     * The total number of results matching the search, or 0 if there is no search or an error has occured with the search
     */
    totalResults() {
      if (this.results === undefined) return 0;
      return this.results.count;
    },
    resultsMessage() {
      // TODO implement computing results message based on number of results when linking to endpoint
      return "There are no results to show";
    },
  },
  methods: {
    searchListings() {
      //TODO implement when linked to endpoint
    },
  },
};
</script>

<style scoped>
.search-bar {
  border-radius: 0px;
  padding-bottom: 10px;
}

.search-field {
  padding-left: 10px;
}
</style>