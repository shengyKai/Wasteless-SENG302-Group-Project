<template>
  <v-container>
    <v-card width="600px">
      <v-row>
        <v-col cols="8">
          <v-expand-transition>
            <div v-show="!moreInfo">
              <ProductImageCarousel :productImages="product.images" :productId="product.id" />
              <v-card-title>{{ saleItem.quantity + " x " + product.name }}</v-card-title>
              <v-card-subtitle>{{ retailPrice }}</v-card-subtitle>

            </div>
          </v-expand-transition>
          <v-expand-transition>
            <div v-show="moreInfo">
              <v-divider/>
              <v-card-subtitle>
                Product Description
              </v-card-subtitle>
              <v-card-text v-if="product.description.length >= 50">
                {{product.description.slice(0, 50) + "..."}}
                <FullProductDescription :product-description="product.description"/>
              </v-card-text>
              <v-card-text v-else>
                {{product.description}}
              </v-card-text>
              <div v-if="saleItem.moreInfo.length !== null && saleItem.moreInfo.length > 0">
                <v-card-subtitle>
                  Additional Sale Info
                </v-card-subtitle>
                <v-card-text v-if="saleItem.moreInfo.length >= 50">
                  {{saleItem.moreInfo.slice(0,50)}}
                  <FullProductDescription :product-description="saleItem.moreInfo"/>
                </v-card-text>
                <v-card-text v-else>
                  {{saleItem.moreInfo}}
                </v-card-text>
              </div>
            </div>
          </v-expand-transition>
        </v-col>
        <v-col cols="4">
          <v-timeline dense style="height: 100%; margin-left: -40%; margin-bottom: 10px">
            <v-timeline-item color="grey" small>
              <div style="margin-left: -25px">
                <strong>Created</strong>
                {{createdFormatted}}
              </div>
            </v-timeline-item>
            <v-timeline-item color="orange" small>
              <div style="margin-left: -25px">
                <strong>Expires</strong>
                {{expiresFormatted}}
              </div>
            </v-timeline-item>
            <v-timeline-item color="red" small>
              <div style="margin-left: -25px">
                <strong>Closes</strong>
                {{closesFormatted}}
              </div>
            </v-timeline-item>
          </v-timeline>
          <v-card-actions>
            <v-btn style="position: absolute; bottom: 10px; right: 10px" color="secondary" @click="moreInfo=!moreInfo">View {{moreInfo? 'Less' : 'More'}}</v-btn>
          </v-card-actions>
        </v-col>
      </v-row>
    </v-card>
  </v-container>
</template>

<script>
import ProductImageCarousel from "@/components/utils/ProductImageCarousel";
import FullProductDescription from "@/components/utils/FullProductDescription";
import {currencyFromCountry} from "@/api/currency";

export default {
  name: "SaleItem",
  components: {FullProductDescription, ProductImageCarousel},
  data() {
    return {
      moreInfo: false,
      currency: {
        code: "",
        symbol: ""
      },
    };
  },
  props: {
    saleItem: {
      default() {
        return {
          "id": 57,
          "inventoryItem": {
            "id": 101,
            "product": {
              "id": "WATT-420-BEANS",
              "name": "Watties Baked Beans - 420g can",
              "description": "Baked Beans as they should be. SOME LONG DESCRIPTION. SOME LONG DESCRIPTION. DESCRIPTION. SOME LONG DESCRIPTION. DESCRIPTION. SOME LONG DESCRIPTION. DESCRIPTION. SOME LONG DESCRIPTION. ",
              "manufacturer": "Heinz Wattie's Limited",
              "recommendedRetailPrice": 2.2,
              "created": "2021-05-15T05:55:32.808Z",
              "countryOfSale": "New Zealand",
              "images": [
                {
                  "id": 1234,
                  "filename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg",
                  "thumbnailFilename": "https://i.picsum.photos/id/357/300/300.jpg?hmac=GR6zE4y7iYz5d4y-W08ZaYhDGGrLHGon4wKEQp1eYkg"
                }
              ]
            },
            "quantity": 4,
            "pricePerItem": 6.5,
            "totalPrice": 21.99,
            "manufactured": "2021-05-15",
            "sellBy": "2021-05-15",
            "bestBefore": "2021-05-15",
            "expires": "2021-05-15"
          },
          "quantity": 3,
          "price": 17.99,
          "moreInfo": "Seller may be willing to consider near offers.  SOME LONG DESCRIPTION. SOME LONG DESCRIPTION. DESCRIPTION. SOME LONG DESCRIPTION. DESCRIPTION. SOME LONG DESCRIPTION. DESCRIPTIO",
          "created": "2021-07-14T11:44:00Z",
          "closes": "2021-07-21T23:59:00Z"
        };
      }
    },
    businessId: {
      default() {
        return 1;
      }
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
      return date.toDateString().slice(3);
    },
    /**
     * Creates a nicely formatted readable string for the sales expiry date
     * @returns {string} ExpiryDate
     */
    expiresFormatted() {
      let date = new Date(this.saleItem.inventoryItem.expires);
      return date.toDateString().slice(3);
    },
    /**
     * Creates a nicely formatted readable string for the sales close date
     * @returns {string} CloseDate
     */
    closesFormatted() {
      let date = new Date(this.saleItem.closes);
      return date.toDateString().slice(3);
    },
    /**
     * Creates a nicely formatted retail price, including the currency
     * @returns {string} RetailPrice
     */
    retailPrice() {
      if (!this.saleItem.price) {
        return "Not set";
      }
      return this.currency.symbol + this.saleItem.price + " " + this.currency.code;
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

</style>