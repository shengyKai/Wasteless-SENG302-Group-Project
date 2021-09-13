<template>
  <v-card
    color="secondary"
    dark
    class="mb-1 search-bar"
  >
    <v-row>
      <v-col cols="3">
        <v-text-field
          clearable
          flat
          outlined
          filled
          hide-details
          label="Product name"
          class="search-field"
          v-model="searchParams.productQuery"
        />
      </v-col>
      <v-col cols="4">
        <v-select
          v-model="searchParams.businessType"
          flat
          solo-inverted
          hide-details
          :items="businessTypeOptions"
          prepend-inner-icon="mdi-sort-variant"
          color="secondary"
          label="Business type"
        />
      </v-col>
      <v-col cols="2">
        <v-select
          style="max-width: 300px"
          v-model="searchParams.orderBy"
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
          <v-btn outlined @click="hideAdvancedSearch()">Simple</v-btn>
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
          label="Business name"
          class="search-field"
          v-model="searchParams.businessQuery"
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
          v-model="searchParams.lowestPrice"
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
          v-model="searchParams.highestPrice"
        />
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
          v-model="searchParams.locationQuery"
        />
      </v-col>
      <v-col cols="2">
        <DatePickerDialog :label="closesBeforeLabel" v-model="searchParams.closesBefore"/>
      </v-col>
      <v-col cols="2">
        <DatePickerDialog :label="closesAfterLabel" v-model="searchParams.closesAfter"/>
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
</template>

<script>
import DatePickerDialog from "./DatePickerDialog.vue";
import { BUSINESS_TYPES } from "@/api/internal.ts";

export default {
  name: "AdvancedSearchBar",
  data() {
    return {
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
      reverse: false,
      closesBeforeLabel: "Closes before",
      closesAfterLabel: "Closes after",
    };
  },
  props: {
    value: Object,
  },
  methods: {
    hideAdvancedSearch() {
      this.$emit("hideAdvancedSearch");
    },
    searchListings() {
      this.$emit("searchListings");
    },
  },
  computed: {
    searchParams: {
      get() {
        return this.value;
      },
      set(value) {
        this.$emit("input", value);
      },
    },
    /**
     * Return a list of text-value pairs for the business types
     */
    businessTypeOptions() {
      const tempBusinessTypes = [];
      tempBusinessTypes.push({text: "Any", value: undefined});
      for (let businessType of BUSINESS_TYPES) {
        tempBusinessTypes.push({text: businessType, value: businessType});
      }
      return tempBusinessTypes;
    }
  },
  components: {
    DatePickerDialog
  }
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
