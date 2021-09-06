<template>
  <div>
    <v-card class="body">
      <div style="flex: 1;">
        <ImageCarousel :imagesList="product.images" :productId="product.id"/>
        <v-card-actions class="action-btn-container">
          <v-btn class="action-btn">
            <v-icon>mdi-currency-usd</v-icon>
            Buy
          </v-btn>
          <v-btn class="action-btn">
            <v-icon>mdi-thumb-up</v-icon>
            Like
          </v-btn>
          <v-btn class="action-btn">
            <v-icon>mdi-arrow-left</v-icon>
            Go Back
          </v-btn>
        </v-card-actions>
        <div class="gap"/>
        <h2 class="saleTitle">{{ product.name }}</h2>
        <label class="saleTitleJoin"> From </label>
        <a class="businessLink" @click="placeholder">Nathan Apple LTD</a>
      </div>
      <div style="flex: 1;">
        <v-row no-gutters>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Total Price:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">$120.40</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Date Created:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">6 Jan 2021</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Quantity:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">7</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Expiry Date:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">5 Sep 2021</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">More Info:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">Tradable for 2kgs of lemons</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Closing Date:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">9 Oct 2021</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Product:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <v-btn color=normal>Orange Apple</v-btn>
          </v-col>
        </v-row>
      </div>
    </v-card>
  </div>
</template>

<script>
import ImageCarousel from "@/components/utils/ImageCarousel";
//import FullProductDescription from "@/components/utils/FullProductDescription";
//import { currencyFromCountry } from "@/api/currency";
import { formatDate, formatPrice } from '@/utils';

export default {
  name: "SaleListingPage",
  components: {
    ImageCarousel //FullProductDescription
  },
  data() {
    return {
      currency: {
        code: "",
        symbol: ""
      }
    };
  },
  props: {
    saleItem: Object,
    businessId: Number
  },
  computed: {
    /**
     * Easy access to the product information of the sale item
     */
    product() {
      return this.saleItem.inventoryItem.product;
    },
    /**
     * Easy access to the inventory item information of the sale item
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
  }
};
</script>

<style scoped>
.body {
  padding: 16px;
  margin-top: 15px;
  width: auto;
}

.column {
  padding-top: 10px;
}

.leadingLabel {
  font-weight: bold;
  font-size: 20px;
}

.followingLabel {
  font-size: 20px;
}

.saleTitle {
  display: inline;
}

.saleTitleJoin {
  display: inline;
  margin-left: 10px;
  font-size: 23px;
}

.businessLink {
  display: inline;
  font-size: 23px;
  text-decoration: underline;
  color: grey;
}

.action-btn-container {
  float: right;
}

.action-btn {
  margin-left: 15px;
}

.space {
  margin-top: 25px;
}
</style>