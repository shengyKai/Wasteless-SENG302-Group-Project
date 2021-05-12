<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <!-- Image column -->
        <v-col align="center" justify="center" cols="auto" md="3" sm="12" style="width: 100%;" v-if="product.images.length === 0">
          <v-icon size="250">
            mdi-image
          </v-icon>
        </v-col>
        <v-col cols="auto" md="3" sm="12" v-else>
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel :productImages="product.images" :productId="product.id"/>
        </v-col>

        <!-- Info column -->
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
                <!-- shows the product code -->
                <v-card-text
                  :class="{ 'pb-0': $vuetify.breakpoint.mdAndUp }"
                  class="product-fields"
                >
                  <strong>Product Code</strong>
                  <br >
                  {{ product.id }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- if the description length is more than or equal to 50 without slicing any words, the "Read more..." link will
                appear which will lead the user to the FullProductDescription component  -->
                <span v-if="product.description.length >= 50">
                  <v-card-text
                    id="description"
                    class="pb-0 product-fields"
                  >
                    <strong>Description</strong>
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
                    <strong>Description</strong>
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
                  <strong>Manufacturer</strong>
                  <br >
                  {{ product.manufacturer }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col cols="auto" md="3" sm="12">
              <v-row>
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Quantity</strong>
                  <br >
                  {{ inventoryItem.quantity }}
                </v-card-text>
              </v-row>
              <v-row v-if="inventoryItem.pricePerItem !== undefined">
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Price per Item</strong>
                  <br >
                  {{ currency.symbol }}{{ inventoryItem.pricePerItem }} {{ currency.code }}
                </v-card-text>
              </v-row>
              <v-row v-if="inventoryItem.totalPrice !== undefined">
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Total Price</strong>
                  <br >
                  {{ currency.symbol }}{{ inventoryItem.totalPrice }} {{ currency.code }}
                </v-card-text>
              </v-row>
            </v-col>
          </v-row>
        </v-col>

        <!-- Timeline column -->
        <v-col cols="auto">
          <v-timeline
            clipped
            class="timeline"
          >
            <v-timeline-item v-if="inventoryItem.manufactured" small color="green" right>
              <template v-slot:opposite>
                <div class="timeline-label">Manufactured</div>
              </template>
              <div class="timeline-content">
                {{ inventoryItem.manufactured }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="product.created" small color="grey" right>
              <template v-slot:opposite>
                <div class="timeline-label">Created</div>
              </template>
              <div class="timeline-content">
                {{ product.created }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="inventoryItem.sellBy" small color="yellow" right>
              <template v-slot:opposite>
                <div class="timeline-label">Sell By</div>
              </template>
              <div class="timeline-content">
                {{ inventoryItem.sellBy }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="inventoryItem.bestBefore" small color="orange" right>
              <template v-slot:opposite>
                <div class="timeline-label">Best Before</div>
              </template>
              <div class="timeline-content">
                {{ inventoryItem.bestBefore }}
              </div>
            </v-timeline-item>
            <v-timeline-item v-if="inventoryItem.expires" small color="red" f right>
              <template v-slot:opposite>
                <div class="timeline-label">Expiry</div>
              </template>
              <div class="timeline-content">
                {{ inventoryItem.expires }}
              </div>
            </v-timeline-item>
          </v-timeline>
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
            created: "2021-05-12",
            images: [
            ],
            countryOfSale: "Japan",
          },
          quantity: 4,
          pricePerItem: 6.5,
          totalPrice: 21.99,
          manufactured: "2021-05-11",
          sellBy: "2021-05-13",
          bestBefore: "2021-05-14",
          expires: "2021-05-15"
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
    /**
     * Helper property for getting the product out of the inventoryItem
     */
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

<style>
.product-fields {
    padding-top: 0;
}

.timeline {
  height: 100%;
}

.timeline-content {
  margin-left: -20px;
}

.timeline-label {
  width: 90px;
  font-weight: bold;
}

</style>
