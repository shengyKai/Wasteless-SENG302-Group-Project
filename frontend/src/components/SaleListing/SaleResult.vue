<template>
  <div v-if="!showFullListing" @click="showFullListing = !showFullListing">
    <v-list-item color="purple">
      <v-col cols="auto" class="pl-0" align-self="center">
        <Avatar :product="product" size="medium-large"/>
      </v-col>
      <v-col>
        <v-row dense>
          <v-col>
            <h4>
              <a ref="title">{{ product.name }}</a>
              from
              <router-link :to="'/business/' + product.business.id" class="text--secondary">
                {{ product.business.name }}
              </router-link>
            </h4>
          </v-col>
        </v-row>
        <v-row class="my-0 py-0">
          <v-col class="my-0 py-0">
            <label class="font-weight-bold">More Info:</label> {{ saleItem.moreInfo }}
          </v-col>
        </v-row>
        <v-row dense class="mt-0">
          <v-col cols="auto" class="font-weight-bold">
            Total Price:
            <br>
            Creation Date:
          </v-col>
          <v-col cols="auto">
            ${{ saleItem.price }}
            <br>
            {{ createdFormatted }}
          </v-col>
          <v-col cols="auto" class="font-weight-bold">
            Quantity:
            <br>
            Closing Date:
          </v-col>
          <v-col cols="auto">
            {{ saleItem.quantity }}
            <br>
            {{ closesFormatted }}
          </v-col>
        </v-row>
      </v-col>
    </v-list-item>
  </div>
  <div v-else>
    <FullSaleListing
      :saleItem="saleItem"
      @goBack="showFullListing = false"
      @refresh="$emit('refresh')"
    />
  </div>
</template>

<script>
import { currencyFromCountry } from "@/api/currency";
import { formatDate } from '@/utils';
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";
import Avatar from '@/components/utils/Avatar.vue';

export default {
  name: "SaleResult",
  components: {
    FullSaleListing,
    Avatar
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