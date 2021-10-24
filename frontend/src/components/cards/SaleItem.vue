<template>
  <FullSaleListing
    v-if="showFullView"
    class="my-2 mx-3"
    :saleItem="saleItem"
    @goBack="goBack"
    @refresh="$emit('refresh')"
  />
  <v-col cols="12" lg="6" v-else>
    <v-card class="my-2 pl-2">
      <v-row>
        <v-col cols="8">
          <ImageCarousel :imagesList="product.images" :productId="product.id" />
          <v-card-title>{{ saleItem.quantity + " × " + product.name }}</v-card-title>
          <v-card-subtitle>{{ retailPrice }}</v-card-subtitle>
        </v-col>
        <v-col cols="4">
          <v-timeline dense class="timeline">
            <v-timeline-item color="grey" small>
              <div class="date-label">
                <strong>Created</strong>
                {{createdFormatted}}
              </div>
            </v-timeline-item>
            <v-timeline-item color="orange" small>
              <div class="date-label">
                <strong>Expires</strong>
                {{expiresFormatted}}
              </div>
            </v-timeline-item>
            <v-timeline-item color="red" small>
              <div class="date-label">
                <strong>Closes</strong>
                {{closesFormatted}}
              </div>
            </v-timeline-item>
          </v-timeline>
          <v-card-actions>
            <v-btn ref="viewMoreButton" class="view-more-button" color="secondary" @click="showFullView=true">View More</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
    </v-card>
  </v-col>
</template>

<script>
import FullSaleListing from "@/components/SaleListing/FullSaleListing.vue";
import ImageCarousel from "@/components/image/ImageCarousel.vue";
import { currencyFromCountry } from "@/api/currency";
import { formatDate, formatPrice } from '@/utils';

export default {
  name: "SaleItem",
  components: {
    FullSaleListing,
    ImageCarousel,
  },
  data() {
    return {
      showFullView: false,
      expandSaleListing: false,
      currency: {
        code: "",
        symbol: ""
      },
    };
  },
  props: {
    saleItem: Object,
  },
  methods: {
    /**
     * emit the goBack to update parent components
     */
    goBack() {
      this.showFullView = false;
      this.$emit("goBack");
    },
  },
  computed: {
    /**
      * Easier access to the product for this sale
      * @returns the product
      */
    product() {
      return this.saleItem.inventoryItem.product;
    },
    /**
      * Easier access to the inventory item for this sale
      * @returns Inventory item
      */
    inventoryItem() {
      return this.saleItem.inventoryItem;
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
      * Creates a nicely formatted readable string for the sales expiry date
      * @returns {string} ExpiryDate
      */
    expiresFormatted() {
      let date = new Date(this.saleItem.inventoryItem.expires);
      return formatDate(date);
    },
    /**
      * Creates a nicely formatted readable string for the sales close date
      * @returns {string} CloseDate
      */
    closesFormatted() {
      let date = new Date(this.saleItem.closes);
      return formatDate(date);
    },
    /**
      * Creates a nicely formatted retail price, including the currency
      * @returns {string} RetailPrice
      */
    retailPrice() {
      if (!this.saleItem.price) {
        return "Not set";
      }
      return this.currency.symbol + formatPrice(this.saleItem.price) + " " + this.currency.code;
    },
    productDescription() {
      return this.product.description || "Not set";
    },
  },
  async created() {
    // When the Sale item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.product.countryOfSale);
  }
};
</script>

<style scoped>
.timeline {
  height: 100%;
  margin-left: -50px;
  margin-bottom: 10px;
}

.date-label {
  margin-left: -25px;
}

.expand-button {
  position: absolute;
  bottom: 10px;
  right: 125px;
}

.view-more-button {
  position: absolute;
  bottom: 10px;
  right: 10px;
}
</style>