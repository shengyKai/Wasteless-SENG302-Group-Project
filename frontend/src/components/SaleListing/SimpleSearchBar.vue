<template>
  <v-card color="secondary" dark class="mb-1 search-bar">
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
          v-model="searchParams.query"
        />
      </v-col>
      <v-spacer />
      <v-col cols="2">
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
      reverse: false,
    };
  },
  props: {
    value: Object,
  },
  methods: {
    showAdvancedSearch() {
      this.$emit("showAdvancedSearch");
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