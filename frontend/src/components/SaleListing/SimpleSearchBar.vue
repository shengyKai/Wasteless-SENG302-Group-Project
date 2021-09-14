<template>
  <v-card color="secondary" dark class="mb-1 pa-2 pt-0">
    <v-row>
      <v-col cols="12" md="4">
        <v-text-field
          clearable
          flat
          solo-inverted
          hide-details
          prepend-inner-icon="mdi-magnify"
          label="Search"
          color="secondary"
          v-model="searchParams.query"
        />
      </v-col>
      <v-spacer />
      <v-col cols="6" md="4" lg="2">
        <v-select
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
      <v-col cols="3" md="2" lg="1">
        <v-btn-toggle class="toggle" v-model="searchParams.reverse" mandatory>
          <v-btn depressed color="secondary" :value="false">
            <v-icon>mdi-arrow-up</v-icon>
          </v-btn>
          <v-btn depressed color="secondary" :value="true">
            <v-icon>mdi-arrow-down</v-icon>
          </v-btn>
        </v-btn-toggle>
      </v-col>
      <v-col cols="3" md="4" lg="2">
        <v-card-actions class="justify-end">
          <v-btn outlined @click="showAdvancedSearch">Advanced</v-btn>
        </v-card-actions>
      </v-col>
    </v-row>
  </v-card>
</template>

<script>
export default {
  name: "SimpleSearchBar",
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
    };
  },
  props: {
    value: Object,
  },
  methods: {
    /**
     * Emit a message to the parent so that it will show the advanced search view.
     */
    showAdvancedSearch() {
      this.$emit("showAdvancedSearch");
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
  },
};
</script>