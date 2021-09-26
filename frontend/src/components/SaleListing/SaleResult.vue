<template>
  <div v-if="!showFullListing">
    <v-list-item color="purple">
      <v-list-item-avatar width="70" height="70">
        <img src="../../assets/grumpy.webp">
      </v-list-item-avatar>
      <v-list-item-content>
        <v-list-item-title>
          <a class="result-title" @click="showFullListing = !showFullListing">{{ product.name }}</a>
          <label class="result-title-join">From </label>
          <label class="result-title-business">{{ product.business.name }}</label>
        </v-list-item-title>
        <v-list-item-subtitle>
          <label>More Info: {{ saleItem.moreInfo }}</label>
        </v-list-item-subtitle>
        <v-list-item-subtitle>
          <label class="total-price-label">Total Price:</label>
          <label>${{ saleItem.price }}</label>
          <label class="divider1"/>
          <label class="quantity-label">Quantity:</label>
          <label>{{ saleItem.quantity }}</label>
        </v-list-item-subtitle>
        <v-list-item-subtitle>
          <label class="creation-date-label">Creation Date:</label>
          <label>{{ createdFormatted }}</label>
          <label class="divider2"/>
          <label class="closing-date-label">Closing Date:</label>
          <label>{{ closesFormatted }}</label>
        </v-list-item-subtitle>
      </v-list-item-content>
    </v-list-item>
  </div>
  <div v-else>
    <FullSaleListing
      :saleItem="saleItem"
      @goBack="goBack"
    />
  </div>
</template>

<script>
import { currencyFromCountry } from "@/api/currency";
import { formatDate } from '@/utils';
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";

export default {
  name: "SaleResult",
  components: {
    FullSaleListing
  },
  data() {
    return {
      showFullListing: false,
      currency: {
        code: "",
        symbol: ""
      }
    };
  },
  props: {
    saleItem: Object
  },
  computed: {
    /**
     * Easy access to the product information of the sale item
     */
    product() {
      return this.saleItem.inventoryItem.product;
    },
    /**
     * Creates a nicely formatted readable string for the sales creation date
     * @returns {string} CreatedDate
     */
    createdFormatted() {
      let date = new Date(this.saleItem.created);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the sales close date
     * @returns {string} CloseDate
     */
    closesFormatted() {
      let date = new Date(this.saleItem.closes);
      return formatDate(date);
    }
  },
  methods: {
    goBack() {
      this.showFullListing = false;
      this.$emit("goBack");
    },
    /**
     * Computes the currency
     */
    computeCurrency() {
      this.currency = currencyFromCountry(this.product.countryOfSale);
    }
  },
  beforeMount() {
    this.computeCurrency();
  }
};
</script>

<style scoped>
.result-title {
  font-size: 19px;
  font-weight: bold;
}

.result-title-join {
  font-size: 15px;
  margin-left: 10px;
}

.result-title-business {
  font-size: 15px;
}

.divider1 {
  margin-right: 74px;
}

.divider2 {
  margin-right: 30px;
}

.total-price-label {
  margin-right: 28px;
}

.quantity-label {
  margin-right: 37px;
}

.creation-date-label {
  margin-right: 10px;
}

.closing-date-label {
  margin-right: 10px;
}

.vertical-line {
  border-left: 1px solid black;
  margin-top: 3px;
  margin-right: 10px;
  height: 15px;
}

.result-image {
  height: 100px;
}
</style>