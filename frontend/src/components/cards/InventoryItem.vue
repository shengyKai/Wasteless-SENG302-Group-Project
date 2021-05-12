<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col align="center" justify="center" cols="auto" md="3" sm="12" v-if="product.images.length === 0">
          <v-icon size="250">
            mdi-image
          </v-icon>
        </v-col>
        <v-col cols="auto" md="3" sm="12" v-else>
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel :productImages="product.images" :productId="product.id"/>
        </v-col>
        <v-col>
          <v-row>
            <v-col class="auto pa-0" md="10" sm="10">
              <v-card-title
                :class="{ 'pt-0': $vuetify.breakpoint.smAndDown }"
                class="pb-0"
              >
                <!-- shows product name -->
                {{ product.name }}
              </v-card-title>
            </v-col>
            <v-col cols="auto" md="2" sm="2">
              <v-card-actions
                :class="{ 'pt-0': $vuetify.breakpoint.smAndDown }"
                class="pb-0"
              />
            </v-col>
          </v-row>
          <v-row />
          <v-row>
            <v-col cols="auto" md="9" sm="12">
              <v-row>
                <!-- if the description length is more than or equal to 50 without slicing any words, the "Read more..." link will
                appear which will lead the user to the FullProductDescription component  -->
                <span v-if="product.description.length >= 50">
                  <v-card-text
                    id="description"
                    class="pb-0 product-fields"
                  >
                    <strong>Description: </strong>
                    <br >
                    {{
                      product.description.replace(
                        /^([\s\S]{50}\S*)[\s\S]*/,
                        "$1"
                      )
                    }}...
                    <!-- feed the productDescription into the dialog box child component -->
                    <FullProductDescription
                      :productDescription="product.description"
                    />
                  </v-card-text>
                </span>
                <!-- else just show the product description -->
                <span v-else>
                  <v-card-text class="pb-0 product-fields">
                    <strong>Description: </strong>
                    <br >
                    {{ product.description }}
                  </v-card-text>
                </span>
              </v-row>
              <v-row>
                <!-- shows the product manufacturer -->
                <v-card-text
                  :class="{
                    'pb-0': $vuetify.breakpoint.smAndDown,
                  }"
                  class="product-fields"
                >
                  <strong>Manufacturer: </strong>
                  <br >
                  {{ product.manufacturer }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col cols="auto" md="3" sm="12">
              <v-row>
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>RRP: </strong>
                  <br >
                  {{ currency.symbol }}{{ product.recommendedRetailPrice }} {{ currency.code }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product code -->
                <v-card-text
                  :class="{ 'pb-0': $vuetify.breakpoint.mdAndUp }"
                  class="product-fields"
                >
                  <strong>Product Code: </strong>
                  <br >
                  {{ product.id }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col>
              <div class="timeline-container">
                <v-timeline
                  dense
                  clipped
                  class="rotated-timeline"
                >
                  <v-timeline-item v-if="inventoryItem.expires" small color="red">
                    <div class="rotated-timeline-item">
                      <strong>Expires</strong>
                      <!--<br>-->
                      {{ inventoryItem.expires }}
                    </div>
                  </v-timeline-item>
                  <v-timeline-item v-if="inventoryItem.bestBefore" small color="orange">
                    <div class="rotated-timeline-item">
                      <strong>Best Before</strong>
                      <!--<br>-->
                      {{ inventoryItem.bestBefore }}
                    </div>
                  </v-timeline-item>
                  <v-timeline-item v-if="inventoryItem.sellBy" small color="yellow">
                    <div class="rotated-timeline-item">
                      <strong>Sell By</strong>
                      <!--<br>-->
                      {{ inventoryItem.sellBy }}
                    </div>
                  </v-timeline-item>
                  <v-timeline-item v-if="product.created" small color="grey">
                    <div class="rotated-timeline-item">
                      <strong>Created</strong>
                      <!--<br>-->
                      {{ product.created }}
                    </div>
                  </v-timeline-item>
                  <v-timeline-item v-if="inventoryItem.manufactured" small color="green">
                    <div class="rotated-timeline-item">
                      <strong>Manufactured</strong>
                      <!--<br>-->
                      {{ inventoryItem.manufactured }}
                    </div>
                  </v-timeline-item>
                </v-timeline>
              </div>
            </v-col>
          </v-row>
        </v-col>

      </v-row>
    </v-container>
  </v-card>
</template>

<script>
//This component requires two other custom components, one to display the product image, one to view more of the product's description
import FullProductDescription from "../utils/FullProductDescription.vue";
import ProductImageCarousel from "../utils/ProductImageCarousel.vue";
import { currencyFromCountry } from "@/api/currency";

export default {
  name: "InventoryItem",
  props: {
    inventoryItem: {
      default() {
        return {
          id: 101,
          product: {
            id: "WATT-420-BEANS",
            name: "Watties Baked Beans - 420g can",
            description: "Baked Beans as they should be.",
            manufacturer: "Heinz Wattie's Limited",
            recommendedRetailPrice: 2.2,
            created: "2021-05-11",
            images: [
              {
                "id": 1234,
                "filename": "/media/images/23987192387509-123908794328.png",
                "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
              }
            ],
            countryOfSale: "Japan",
          },
          quantity: 4,
          pricePerItem: 6.5,
          totalPrice: 21.99,
          manufactured: "2021-05-11",
          sellBy: "2021-05-11",
          bestBefore: "2021-05-11",
          expires: "2021-05-11"
        };
      },
    },
    //retrieved from Inventory page
    businessId: Number
  },
  components: {
    FullProductDescription,
    ProductImageCarousel,
  },
  data() {
    return {
      currency: {
        code: "",
        symbol: ""
      },
      showImageUploaderForm: false,
      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false
    };
  },
  async created() {
    // When the catalogue item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.product.countryOfSale);
  },
  computed: {
    product() {
      return this.inventoryItem.product;
    }
  },
  methods: {
    //if the "Read more..." link if clicked, readMoreActivated becomes true and the FullProductDescription dialog box will open
    activateReadMore() {
      this.readMoreActivated = true;
    },
  },
};
</script>

<style scoped>
.product-fields {
    padding-top: 0;
}

.timeline-container {
  /*height: 75px;
  width: 100%;*/
}

.rotated-timeline {
  display: inline-block;
  transform-origin: center center;
  /*transform: rotate(90deg) translate(-100%, -50%);*/
}
.rotated-timeline-item {
  margin: -20px -10px;
  text-align: center;
  display: inline-block;
  /*background-color: red;*/
  /*transform-origin: left center;
  transform: rotate(-90deg) translate(-50%, 0%);*/
}

.rotated-timeline-icon {
  /*transform-origin: center center;
  transform: rotate(-90deg);*/
}
</style>
