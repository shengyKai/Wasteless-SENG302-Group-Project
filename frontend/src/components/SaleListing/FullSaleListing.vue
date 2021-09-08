<template>
  <div>
    <v-card class="body">
      <div>
        <ImageCarousel :imagesList="product.images" :productId="product.id"/>
        <v-card-actions class="action-btn-container">
          <v-btn class="action-btn white-text" color="primary">
            <v-icon>mdi-currency-usd</v-icon>
            Buy
          </v-btn>
          <v-btn class="action-btn white--text" color="green">
            <v-icon>mdi-thumb-up</v-icon>
            Like 69
          </v-btn>
          <v-btn class="action-btn white--text" color="purple">
            <v-icon>mdi-arrow-left</v-icon>
            Go Back
          </v-btn>
        </v-card-actions>
        <div class="gap"/>
        <h2 class="saleTitle">{{ product.name }}</h2>
        <label class="saleTitleJoin"> From </label>
        <a class="businessLink">Nathan Apple LTD</a>
      </div>
      <div>
        <label class="description"> {{ productDescription }} </label>
      </div>
      <div>
        <v-row no-gutters>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Total Price:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">${{ saleItem.price }}</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Date Created:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">{{ createdFormatted }}</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Quantity:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">{{ saleItem.quantity }}</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Expiry Date:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">{{ expiresFormatted }}</label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">More Info:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">
              {{ saleItem.moreInfo }}
            </label>
          </v-col>
          <v-col class="column" cols="6" sm="2">
            <label class="leadingLabel">Closing Date:</label>
          </v-col>
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">{{ createdFormatted }}</label>
          </v-col>
          <v-btn class="product-btn" color=orange outlined @click="extraDetails = !extraDetails;">Extra Details</v-btn>
        </v-row>
        <div v-if="extraDetails">
          <v-row no-gutters>
            <v-col class="column" cols="6" sm="2">
              <label class="leadingLabel">Best Before Date:</label>
            </v-col>
            <v-col class="column" cols="6" sm="4">
              <label class="followingLabel">{{ bestBeforeFormatted }}</label>
            </v-col>
            <v-col class="column" cols="6" sm="2">
              <label class="leadingLabel">Sell By Date:</label>
            </v-col>
            <v-col class="column" cols="6" sm="4">
              <label class="followingLabel">{{ sellByFormatted }}</label>
            </v-col>
            <v-col class="column" cols="6" sm="2">
              <label class="leadingLabel">Country:</label>
            </v-col>
            <v-col class="column" cols="6" sm="4">
              <label class="followingLabel">{{ product.countryOfSale }}</label>
            </v-col>
            <v-col class="column" cols="6" sm="2">
              <label class="leadingLabel">Manufacturer:</label>
            </v-col>
            <v-col class="column" cols="6" sm="4">
              <label class="followingLabel">{{ product.manufacturer }}</label>
            </v-col>
            <v-col class="column" cols="6" sm="2">
              <label class="leadingLabel">Original Name:</label>
            </v-col>
            <v-col class="column" cols="6" sm="4">
              <label class="followingLabel">{{ product.name }}</label>
            </v-col>
          </v-row>
        </div>
      </div>
    </v-card>
  </div>
</template>

<script>
import ImageCarousel from "@/components/utils/ImageCarousel";
import { currencyFromCountry } from "@/api/currency";
import { formatDate, formatPrice } from '@/utils';

export default {
  name: "SaleListingPage",
  components: {
    ImageCarousel
  },
  data() {
    return {
      currency: {
        code: "",
        symbol: ""
      },
      extraDetails: false
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
     * Creates a nicely formatted readable string for the inventory item's best before date
     * @returns {string} BestBeforeDate
     */
    bestBeforeFormatted() {
      let date = new Date(this.inventoryItem.bestBefore);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the inventory item's sell by date
     * @returns {string} SellByDate
     */
    sellByFormatted() {
      let date = new Date(this.inventoryItem.sellBy);
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

.product-btn {
  margin-top: 10px;
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

.description {
  font-size: 20px;
}
</style>