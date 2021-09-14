<template>
  <v-card
    color="secondary"
    dark
    class="mb-1 pa-2 pt-0"
  >
    <v-row>
      <v-col cols="12" md="3">
        <v-text-field
          clearable
          flat
          outlined
          filled
          hide-details
          label="Product name"
          v-model="searchParams.productQuery"
        />
      </v-col>
      <v-col cols="12" md="4">
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
      <v-col cols="6" md="2" sm="4">
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
      <v-col cols="3" md="1" sm="2">
        <v-btn-toggle class="toggle" v-model="reverse" mandatory>
          <v-btn depressed color="secondary" :value="false">
            <v-icon>mdi-arrow-up</v-icon>
          </v-btn>
          <v-btn depressed color="secondary" :value="true">
            <v-icon>mdi-arrow-down</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-col>
      <v-col cols="3" md="2" sm="4">
        <v-card-actions>
          <v-btn outlined @click="hideAdvancedSearch()">Simple</v-btn>
        </v-card-actions>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" md="3">
        <v-text-field
          clearable
          flat
          outlined
          filled
          hide-details
          label="Business name"
          v-model="searchParams.businessQuery"
        />
      </v-col>
      <v-col cols="12" md="2" sm="6">
        <v-text-field ref="lowestPriceField"
                      flat
                      outlined
                      filled
                      :hide-details="lowestPriceValid"
                      label="Lowest price"
                      v-model="searchParams.lowestPrice"
                      :rules="lowestPriceRules"
        />
      </v-col>
      <v-col cols="12" md="2" sm="6">
        <v-text-field ref="highestPriceField"
                      flat
                      outlined
                      filled
                      :hide-details="highestPriceValid"
                      label="Highest price"
                      v-model="searchParams.highestPrice"
                      :rules="highestPriceRules"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12" md="3">
        <v-text-field
          clearable
          flat
          outlined
          filled
          hide-details
          label="Location"
          v-model="searchParams.locationQuery"
        />
      </v-col>
      <v-col cols="12" md="2" sm="6">
        <DatePickerDialog :label="closesAfterLabel" v-model="searchParams.closesAfter" :maxDate="searchParams.closesBefore"/>
      </v-col>
      <v-col cols="12" md="2" sm="6">
        <DatePickerDialog :label="closesBeforeLabel" v-model="searchParams.closesBefore" :minDate="searchParams.closesAfter"/>
      </v-col>
      <v-spacer/>
      <v-col cols="12" md="2" sm="4">
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
import { hugePriceRules } from "@/utils.ts";

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
    /**
     * Emit a message to the parent component so it will stop showing the advanced search view.
     */
    hideAdvancedSearch() {
      this.$emit("hideAdvancedSearch");
    },
    /**
     * Emit a message to the parent component so it will perform the search with the parameters given by the value prop.
     */
    searchListings() {
      this.$emit("searchListings");
    },
    /**
     * Perform validation on both price fields.
     */
    validatePrices() {
      this.$refs.lowestPriceField.validate();
      this.$refs.highestPriceField.validate();
    },
  },
  computed: {
    /**
     * Allow searchParams to update the value prop passed in through v-model.
     */
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
    },
    /**
     * Rule to check that the price entered by the user conforms to the expected format for a price,
     * and that it is less than the highest price if one is given.
     */
    lowestPriceRules() {
      if (!this.searchParams.lowestPrice) return [];
      return hugePriceRules.concat([
        (price) => {
          if (!price || !this.searchParams.highestPrice) return true;
          if (parseFloat(price) < parseFloat(this.searchParams.highestPrice)) return true;
          return 'Lowest price must be less than highest price';
        }
      ]);
    },
    /**
     * Rule to check that the price entered by the user conforms to the expected format for a price,
     * and that it is greater than the lowest price if one is given.
     */
    highestPriceRules() {
      if (!this.searchParams.highestPrice) return [];
      return hugePriceRules.concat([
        (price) => {
          if (!price || !this.searchParams.lowestPrice) return true;
          if (parseFloat(price) > parseFloat(this.searchParams.lowestPrice)) return true;
          return 'Highest price must be greater than lowest price';
        }
      ]);
    },
    /**
     * Return true if the value in the lowest price field is valid, false otherwise.
     */
    lowestPriceValid() {
      return this.lowestPriceRules.every(rule => rule(this.searchParams.lowestPrice) === true);
    },
    /**
     * Return true if the value in the highest prcie field is valid, false otherwise.
     */
    highestPriceValid() {
      return this.highestPriceRules.every(rule => rule(this.searchParams.highestPrice) === true);
    }
  },
  watch: {
    /**
     * Update the validation on both price fields if the lowest price changes.
     */
    "searchParams.lowestPrice"() {
      this.validatePrices();
    },
    /**
     * Update the validatoin on both price fields if the highest price changes.
     */
    "searchParams.highestPrice"() {
      this.validatePrices();
    }
  },
  components: {
    DatePickerDialog
  }
};
</script>