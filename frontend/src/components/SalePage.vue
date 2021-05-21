<template>
  <v-row style="margin-top: 2em;" justify="space-around" align="center">
    <SaleItem :business-id="businessId" :sale-item="sale" v-for="sale in salesList" v-bind:key="sale.id"/>
  </v-row>
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
      return this.$route.params.id;
    }
  },
  async created() {
    this.$store.commit('clearError');
    await this.populateSales();
  }
};
</script>
<style scoped>

</style>