<template>
  <div ref="fullListing">
    <v-card class="pa-2">
      <!-- Image carousel component -->
      <div>
        <ImageCarousel v-if="imagesList.length > 0" :imagesList="product.images" :productId="product.id"/>
        <div/>
        <!-- Product titile and core information -->
        <v-row>
          <v-col cols="12" sm="8">
            <v-card flat>
              <v-card-text class="ml-2">
                <v-row class="mt-1">
                  <h2 ref="productName" class="text--primary font-weight-bold">
                    {{ product.name }}
                  </h2>
                  <h4 class="link ml-4 mt-1 text-decoration-underline" @click="viewProfile(business.id)">FROM {{this.business.name}}</h4>
                </v-row>
                <v-row>
                  <div class="text--primary mt-3">
                    {{ productDescription }}
                  </div>
                </v-row>
              </v-card-text>
            </v-card>
          </v-col>
          <!-- Buy feature will not be implemented yet -->
          <v-row>
            <v-col class="align-self-center text-center">
              <v-btn class="pl-2 pr-2" color="primary darken-1" @click="buy">
                Buy
                <v-icon>mdi-currency-usd</v-icon>
              </v-btn>
              <!-- Thumb up/down button to show and allow user the like & unlike feature -->
              <v-btn ref="likeButton" class=" pl-2 pr-2 ml-2" color="grey lighten-2" @click="changeInterest">
                {{thumbMessage}} {{interestCount}}
                <v-icon class="ml-1">{{thumbIcon}}</v-icon>
              </v-btn>
              <!-- A return button for user to hide full sale listing-->
              <v-btn class="ml-2 mr-1 pl-2 pr-1" color="secondary" @click="goBack">
                Hide
                <v-icon class="ml-1">mdi-arrow-left</v-icon>
              </v-btn>
            </v-col>
          </v-row>
          <!-- </v-col> -->
        </v-row>
      </div>
      <v-alert
        class="ma-2 flex-grow-0"
        v-if="errorMessage !== undefined"
        type="error"
        dismissible
        @input="errorMessage = undefined"
      >
        {{ errorMessage }}
      </v-alert>
      <!-- Listing details -->
      <div>
        <v-container fluid>
          <v-card color="grey lighten-2" class="pa-2 pl-4">
            <v-row no-gutters class="mt-3">
              <v-col cols="6" sm="3">
                <h4 class="font-weight-bold">Price:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class="font-weight-regular">${{ saleItem.price }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Date Created:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ createdFormatted }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Quantity:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ saleItem.quantity }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Manufacturer:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ productManufacturer}}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Expiry Date:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ expiresFormatted }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Closing Date:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class="font-weight-regular">{{ closesFormatted }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Best Before:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ bestBeforeFormatted }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Sell By:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ sellByFormatted }}</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-bold">Country of Sale:</h4>
              </v-col>
              <v-col cols="6" sm="3">
                <h4 class=" font-weight-regular">{{ product.countryOfSale }}</h4>
              </v-col>
              <v-row>
                <v-col cols="6" sm="3">
                  <h4 class=" font-weight-bold">More Info:</h4>
                </v-col>
                <v-col cols="6" sm="6">
                  <h4 class=" font-weight-regular">
                    {{ saleItem.moreInfo }}
                  </h4>
                </v-col>
              </v-row>
            </v-row>
          </v-card>
        </v-container>
      </div>
    </v-card>
  </div>
</template>

<script>
import ImageCarousel from "@/components/image/ImageCarousel";
import { currencyFromCountry } from "@/api/currency";
import { setListingInterest, getListingInterest, purchaseListing} from '../../api/sale';
import { formatDate, formatPrice } from '@/utils';

export default {
  name: "FullSaleListing",
  components: {
    ImageCarousel
  },
  data() {
    return {
      interestCount: "",
      currency: {
        code: "",
        symbol: "",
      },
      extraDetails: false,
      isInterested: "",
      errorMessage: undefined,
    };
  },
  props: {
    saleItem: Object
  },
  /**
   * Get the interestCount of the saleListing when its chose to be rendered
   */
  mounted() {
    this.interestCount = this.saleItem.interestCount;
    console.log(this.saleItem);
  },
  computed: {
    /**
     * Easier access to business properties
     */
    business() {
      return this.product.business;
    },
    /**
     * Stay consistent with other folder by the name imageList and easier access to the product images list
     */
    imagesList() {
      return this.product.images;
    },
    /**
     * Easy access to user id
     */
    userId() {
      return this.$store.state.user.id;
    },
    /**
     * Compute the thumb icon base on user interest status
     */
    thumbIcon() {
      if (this.isInterested) {
        return "mdi-thumb-up";
      } else {
        return "mdi-thumb-up-outline";
      }
    },
    /**
     * Compute the message to be render beside the like icon
     */
    thumbMessage() {
      if (this.isInterested) {
        return "Liked";
      } else {
        return "Like";
      }
    },
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
      if(this.inventoryItem.bestBefore === undefined) return "Not Provided";
      let date = new Date(this.inventoryItem.bestBefore);
      return formatDate(date);
    },
    /**
     * Creates a nicely formatted readable string for the inventory item's sell by date
     * @returns {string} SellByDate
     */
    sellByFormatted() {
      if(this.inventoryItem.sellBy === undefined) return "Not Provided";
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
    /**
     * To handle the issue whe description is empty
     */
    productDescription() {
      return this.product.description || "Description Not Provided";
    },
    /**
     * To handle the issue whe description is empty
     */
    productManufacturer() {
      return this.product.manufacturer || "Not Provided";
    },
  },
  methods: {
    /**
     * Shows the business profile page
     */
    viewProfile(businessId) {
      this.$router.push("/business/" + businessId);
    },
    /** Change the user interest status on the listing (toggle)
     */
    async changeInterest() {
      const result = await setListingInterest(this.saleItem.id, {userId: this.userId, interested: !this.isInterested});
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.isInterested = !this.isInterested;
        if(this.isInterested) this.interestCount += 1;
        else this.interestCount -= 1;
      }
    },
    /**
     * Get the user's interest status on the listing
     */
    async computeIsInterested() {
      const result = await getListingInterest(this.saleItem.id, this.userId);
      if (typeof result === 'string'){
        this.errorMessage = result;
      } else {
        this.errorMessage = undefined;
        this.isInterested = result;
      }
    },
    /**
     * Purchases the sale listing and if successful triggers a refresh otherwise shows an error message
     */
    async buy() {
      let response = await purchaseListing(this.saleItem.id, this.userId);
      if (typeof response === 'string') {
        this.errorMessage = response;
        return;
      }
      this.$emit('refresh');
    },
    /**
     * Computes the currency
     */
    computeCurrency() {
      this.currency = currencyFromCountry(this.product.countryOfSale);
    },
    /**
     * Minimize the full sale listing and back to the listing result page
     */
    goBack() {
      this.$emit('goBack');
    },
  },
  /**
   * Compute the currency to be rendered and check have this user liked this listing
   */
  beforeMount() {
    this.computeCurrency();
    this.computeIsInterested();
  }
};
</script>
