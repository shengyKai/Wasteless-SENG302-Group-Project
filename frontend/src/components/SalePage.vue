<template>
  <div>
    <v-container fluid>
      <v-row align="center">
        <v-col
          class="d-flex"
        >
          <!---Select component for the order in which the cards should be displayed--->
          <v-select
            flat
            solo-inverted
            hide-details
            :items="[
              { text: 'Date Added', value: 'created'},
              { text: 'Closing Date', value: 'closes'},
            ]"
            prepend-inner-icon="mdi-sort-variant"
            label="Sort by"
          />
        </v-col>
        <v-col cols="auto">
          <!---Reverse the order in which the cards should be displayed--->
          <v-btn-toggle class="toggle" mandatory>
            <v-btn depressed color="primary" :value="false">
              <v-icon>mdi-arrow-up</v-icon>
            </v-btn>
            <v-btn depressed color="primary" :value="true">
              <v-icon>mdi-arrow-down</v-icon>
            </v-btn>
          </v-btn-toggle>
        </v-col>
        <v-col
          class="d-flex"
          cols="auto"
        >
          <!---Search for cards by their keywords--->
          <v-text-field
            clearable
            flat
            solo-inverted
            hide-details
            prepend-inner-icon="mdi-magnify"
            label="Keywords"
            autofocus
          />
        </v-col>
      </v-row>
    </v-container>
    <v-container class="grey lighten-2">
      <v-row justify="space-around">
        <v-col v-for="sale in salesList" v-bind:key="sale.id" cols="auto">
          <SaleItem :business-id="businessId" :sale-item="sale" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
import {getBusiness, getBusinessSales} from "@/api/internal";
import SaleItem from "@/components/cards/SaleItem";

export default {
  name: "SalePage",
  components: {SaleItem},
  data() {
    return {
      salesList: [],
      business: Object,
    };
  },
  methods: {
    async populateSales() {
      const result = await getBusinessSales(this.businessId);
      if (typeof result === 'string') {
        this.$store.commit('setError', result);
      } else {
        this.salesList = result;
      }
    },
    async populateBusinessData() {
      const result = await getBusiness(this.businessId);
      if (typeof result === 'string') {
        this.$store.commit(result);
      } else {
        this.business = result;
      }
    },
  },
  computed: {
    businessId() {
      return parseInt(this.$route.params.id);
    }
  },
  async created() {
    this.$store.commit('clearError');
    await this.populateSales();
    await this.populateBusinessData();
  }
};
</script>
<style scoped>

</style>