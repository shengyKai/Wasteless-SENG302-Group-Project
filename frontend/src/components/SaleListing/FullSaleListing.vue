<template>
  <div>
    <v-card class="body">
      <div>
        <ImageCarousel :imagesList="product.images" :productId="product.id"/>
        <v-card-actions class="float-right">
          <!-- Buy feature will not be implemented yet -->
          <v-btn class="ml-2 pl-3" color="primary darken-1">
            Buy
            <v-icon>mdi-currency-usd</v-icon>
          </v-btn>
          <!-- Thumb up/down button to show and allow user the like & unlike feature -->
          <v-btn class="ml-2 pl-3" color="primary darken-2">
            Like 69
            <v-icon class="ml-1">mdi-thumb-up</v-icon>
          </v-btn>
          <!-- A return button for user to go back to business profile-->
          <v-btn class="ml-2 pl-3" color="secondary" @click="viewProfile">
            Go Back
            <v-icon class="ml-1">mdi-arrow-left</v-icon>
          </v-btn>
        </v-card-actions>
        <div class="gap"/>
        <h2 class="d-inline-block">{{ product.name }}</h2>
        <label class="saleTitleJoin"> From </label>
        <a class="businessLink">Nathan Apple LTD</a>
      </div>
      <div>
        <label class="fontSize"> {{ productDescription }} </label>
      </div>
      <div>
        <v-row no-gutters>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="fontSize">Total Price:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="fontSize">${{ saleItem.price }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="leadingLabel">Date Created:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="fontSize">{{ createdFormatted }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="leadingLabel">Quantity:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="fontSize">{{ saleItem.quantity }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="leadingLabel">Expiry Date:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="fontSize">{{ expiresFormatted }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="leadingLabel">More Info:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="fontSize">
              {{ saleItem.moreInfo }}
            </label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="leadingLabel">Closing Date:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="fontSize">{{ createdFormatted }}</label>
          </v-col>
          <v-col align=right>
            <v-btn class="mt-2 right" color=orange outlined @click="extraDetails = !extraDetails;">Extra Details</v-btn>
          </v-col>
        </v-row>
        <div v-if="extraDetails">
          <v-row no-gutters>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="leadingLabel">Best Before Date:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="fontSize">{{ bestBeforeFormatted }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="leadingLabel">Sell By Date:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="fontSize">{{ sellByFormatted }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="leadingLabel">Country:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="fontSize">{{ product.countryOfSale }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="leadingLabel">Manufacturer:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="fontSize">{{ product.manufacturer }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="leadingLabel">Original Name:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="fontSize">{{ product.name }}</label>
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
      return this.product.fontSize || "Not set";
    }
  },
  methods: {
    /**
     * Shows the business profile page
     */
    viewProfile() {
      this.$router.push("/business/" + this.$store.state.activeRole.id);
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
.body {
  padding: 16px;
  margin-top: 15px;
  width: auto;
}

.leadingLabel {
  font-weight: bold;
  font-size: 20px;
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

.fontSize {
  font-size: 20px;
}
</style>