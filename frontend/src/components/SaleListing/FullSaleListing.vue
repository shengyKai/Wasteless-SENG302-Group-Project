<template>
  <div>
    <v-card class="pa-2">
      <div>
        <ImageCarousel v-if="imagesList.length > 0" :imagesList="product.images" :productId="product.id"/>
        <div/>
        <v-row>
          <v-col cols="12" sm="8">
            <v-card flat>
              <v-card-text>
                <p class="text-h4 text--primary d-inline-block">
                  {{ product.name }}
                </p>
                <p class="ml-2 text-h5 text--secondary d-inline-block text-decoration-underline">FROM {{this.product.manufacturer}}</p>
                <div class="text--primary  text-left">
                  {{ productDescription }}
                </div>
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
              <v-btn class=" pl-2 pr-2 ml-2" color="grey lighten-2" @click="changeInterest">
                {{thumbMessage}} {{interestedCount}}
                <v-icon class="ml-1">{{thumbIcon}}</v-icon>
              </v-btn>
              <!-- A return button for user to go back to business profile-->
              <v-btn class="ml-2 mr-1 pl-2 pr-1" color="secondary" @click="hideExpand">
                Hide
                <v-icon class="ml-1">mdi-arrow-left</v-icon>
              </v-btn>
            </v-col>
          </v-row>
          <!-- </v-col> -->
        </v-row>
      </div>
      <div>
        <v-row no-gutters class="mt-3">
          <v-col class="mt-2" cols="6" sm="2">
            <label class="text-h6 font-weight-bold">Total Price:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="text-h6 font-weight-regular">${{ saleItem.price }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="text-h6 font-weight-bold">Date Created:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="text-h6 font-weight-regular">{{ createdFormatted }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="text-h6 font-weight-bold">Quantity:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="text-h6 font-weight-regular">{{ saleItem.quantity }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="text-h6 font-weight-bold">Expiry Date:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="text-h6 font-weight-regular">{{ expiresFormatted }}</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="text-h6 font-weight-bold">More Info:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="text-h6 font-weight-regular">
              {{ saleItem.moreInfo }}
            </label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="2">
            <label class="text-h6 font-weight-bold">Closing Date:</label>
          </v-col>
          <v-col class="mt-2" cols="6" sm="4">
            <label class="text-h6 font-weight-regular">{{ createdFormatted }}</label>
          </v-col>
          <!-- change -->
          <v-col class="column" cols="6" sm="4">
            <label class="followingLabel">{{ closesFormatted }}</label>
          </v-col>
        </v-row>
        <div>
          <v-row no-gutters>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="text-h6 font-weight-bold">Best Before Date:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="text-h6 font-weight-regular">{{ bestBeforeFormatted }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="text-h6 font-weight-bold">Sell By Date:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="text-h6 font-weight-regular">{{ sellByFormatted }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="text-h6 font-weight-bold">Country:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="text-h6 font-weight-regular">{{ product.countryOfSale }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="text-h6 font-weight-bold">Manufacturer:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="text-h6 font-weight-regular">{{ product.manufacturer }}</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="2">
              <label class="text-h6 font-weight-bold">Original Name:</label>
            </v-col>
            <v-col class="mt-2" cols="6" sm="4">
              <label class="text-h6 font-weight-regular">{{ product.name }}</label>
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
import { setListingInterest, getListingInterest} from '../../api/internal';
import { formatDate, formatPrice } from '@/utils';

export default {
  name: "FullSaleListing",
  components: {
    ImageCarousel
  },
  data() {
    return {
      interestedCount: "",
      currency: {
        code: "",
        symbol: "",
      },
      extraDetails: false,
      isInterested: "",
    };
  },
  props: {
    saleItem: Object
  },
  mounted() {
    console.log(this.saleItem);
    this.interestedCount = this.saleItem.interestedCount;
  },
  computed: {
    imagesList() {
      return this.product.images;
    },
    userId() {
      return this.$store.state.user.id;
    },
    thumbIcon() {
      if (this.isInterested) {
        return "mdi-thumb-down";
      } else {
        return "mdi-thumb-up";
      }
    },
    thumbMessage() {
      if (this.isInterested) {
        return "Unlike";
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
    },
  },
  methods: {
    /**
     * Shows the business profile page
     */
    viewProfile() {
      this.$router.push("/business/" + this.$store.state.activeRole.id);
    },
    /** Change the user interest status on the listing (toggle)
     */
    async changeInterest() {
      await setListingInterest(this.saleItem.id, this.userId, !this.isInterested);
      await this.computeIsInterested();
      if(this.isInterested) this.interestedCount += 1;
      else this.interestedCount -= 1;
    },
    /**
     * Compute the lising isInterested
     */
    async computeIsInterested() {
      this.isInterested = await getListingInterest(this.saleItem.id, this.userId);
    },
    buy() {
      console.log(this.isInterested);
      console.log(this.saleItem);
    },
    /**
     * Computes the currency
     */
    computeCurrency() {
      this.currency = currencyFromCountry(this.product.countryOfSale);
    },
    hideExpand() {
      this.$emit('goBack');
    },
  },
  beforeMount() {
    this.computeCurrency();
    this.computeIsInterested();
  }
};
</script>
