<template>
  <v-container fluid>
    <v-row>
      <v-col cols="auto" md="3" sm="12">
        <!-- feed the productImages into the carousel child component -->
        <ProductImageCarousel :productImages="product.images" />
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
          <v-col cols="auto" md="1" sm="2">
            <v-card-actions
              :class="{ 'pt-0': $vuetify.breakpoint.smAndDown }"
              class="pb-0"
            >
              <!-- shows the edit button for editing product details, which supposedly links to a form -->
              <a @click="editProductDetails">Edit</a>
            </v-card-actions>
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
              <!-- shows the date added for the product -->
              <v-card-text class="pb-0 product-fields">
                <strong>Date Added: </strong>
                <br >
                {{ product.created }}
              </v-card-text>
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
        </v-row>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
//This component requires two other custom components, one to display the product image, one to view more of the product's description
import FullProductDescription from "./utils/FullProductDescription.vue";
import ProductImageCarousel from "./utils/ProductImageCarousel.vue";
import { currencyFromCountry } from "@/api/currency";

export default {
  name: "ProductCatalogueItem",
  props: {
    product: {
      default() {
        return {
          id: "Some Code",
          name: "Some Product",
          description:
                      "Some super long description Some super long description Some super long description Some super long description",
          recommendedRetailPrice: 100,
          manufacturer: "Some Manufacturer",
          created: "Some Date Added",
          images: [
            {
              filename: "https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg"
            },
            {
              filename: "https://cdn.vuetifyjs.com/images/carousel/sky.jpg"
            },
            {
              filename: "https://cdn.vuetifyjs.com/images/carousel/bird.jpg"
            },
            {
              filename: "https://cdn.vuetifyjs.com/images/carousel/planet.jpg"
            },
          ],
          countryOfSale: "Japan",
        };
      },
    },
  },
  components: {
    FullProductDescription,
    ProductImageCarousel,
  },
  data() {
    return {
      currency: {
        code: "",
        symbol: "",
      },

      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false,
    };
  },
  async created() {
    // When the catalogue item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will have blank fields if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.product.countryOfSale);
  },
  methods: {
    //if the "Read more..." link if clicked, readMoreActivated becomes true and the FullProductDescription dialog box will open
    activateReadMore() {
      this.readMoreActivated = true;
    },
    //add the form to edit product details here
    editProductDetails() {
      alert("TODO");
    },
  },
};
</script>

<style scoped>
.product-fields {
    padding-top: 0;
}
</style>
