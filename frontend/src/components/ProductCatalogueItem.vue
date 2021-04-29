<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col cols="auto" md="3" sm="12">
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel :productImages="productImages"/>
        </v-col>
        <v-col>
          <v-row>
            <v-col class="auto pa-0" md="11" sm="10">
              <v-card-title :class="{'pt-0': $vuetify.breakpoint.smAndDown}" class="pb-0">
                <!-- shows product name -->
                {{ productName }}
              </v-card-title>
            </v-col>
            <v-col cols="auto" md="1" sm="2">
              <v-card-actions :class="{'pt-0': $vuetify.breakpoint.smAndDown}" class="pb-0">
                <!-- shows the edit button for editing product details, which supposedly links to a form -->
                <a @click="editProductDetails">Edit</a>
              </v-card-actions>
            </v-col>
          </v-row>
          <v-row>
            <v-col cols="auto" md="9" sm="12">
              <v-row>
                <!-- if the description length is more than or equal to 50 without slicing any words, the "Read more..." link will
                appear which will lead the user to the FullProductDescription component  -->
                <span v-if="productDescription.length >= 50">
                  <v-card-text id="description" class="pb-0 product-fields">
                    <strong>Description: </strong>
                    <br>
                    {{ productDescription.replace(/^([\s\S]{50}\S*)[\s\S]*/, "$1") }}...
                    <!-- {{ productDescription.slice(0, 50) }}... -->
                    <!-- feed the productDescription into the dialog box child component -->
                    <FullProductDescription :productDescription="productDescription"/>
                  </v-card-text>
                </span>
                <!-- else just show the product description -->
                <span v-else>
                  <v-card-text class="pb-0 product-fields">
                    <strong>Description: </strong>
                    <br>
                    {{ productDescription }}
                  </v-card-text>
                </span>
              </v-row>
              <v-row>
                <!-- shows the date added for the product -->
                <v-card-text class="pb-0 product-fields">
                  <strong>Date Added: </strong>
                  <br>
                  {{ productDateAdded }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product manufacturer -->
                <v-card-text :class="{'pb-0': $vuetify.breakpoint.smAndDown}" class="product-fields">
                  <strong>Manufacturer: </strong>
                  <br>
                  {{ productManufacturer }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col cols="auto" md="3" sm="12">
              <v-row>
                <!-- shows the product price -->
                <v-card-text class="pb-0 product-fields">
                  <strong>RRP: </strong>
                  <br>
                  {{ currency.symbol }}{{ productRRP }} {{ currency.code }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product code -->
                <v-card-text :class="{'pb-0': $vuetify.breakpoint.mdAndUp}" class="product-fields">
                  <strong>Product Code: </strong>
                  <br>
                  {{ productCode }}
                </v-card-text>
              </v-row>
            </v-col>
          </v-row>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
//This component requires two other custom components, one to display the product image, one to view more of the product's description
import FullProductDescription from "./utils/FullProductDescription.vue";
import ProductImageCarousel from "./utils/ProductImageCarousel.vue";
import {currencyFromCountry} from "./utils/Methods/currency.ts";

export default {
  name: "ProductCatalogueItem",
  components: {
    FullProductDescription,
    ProductImageCarousel
  },
  data() {
    return {
      //insert the product images here, do note that the first index, at index 0, will be the primary image of the product.
      //The image MUST be labelled with the key "src" in order to be able to show in component
      productImages: [
        //commented out the images below for the moment, so that if you would like to test out the carousel, just uncomment it
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg",
        },
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/sky.jpg",
        },
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/bird.jpg",
        },
        {
          src: "https://cdn.vuetifyjs.com/images/carousel/planet.jpg",
        },
      ],
      //The bottom 8 variables will all be shown as text/numbers in the component.
      // productName: "",
      // productDescription: "",
      // productDateAdded: "",
      // productManufacturer: "",
      // productRRP: null,
      // productCode: "",
      // currency: {},

      // try uncommenting the bottom variables to test out how it looks.
      productName: "Some Product",
      productDescription:
        "Some super long description Some super long description Some super long description Some super long description",
      productDateAdded: "Some Date Added",
      productManufacturer: "Some Manufacturer",
      productRRP: 100,
      productCode: "Some Code",
      countryOfSale: "Japan",
      currency: {},

      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false,
    };
  },
  async created() {
    // When the catalogue item is created, the currency will be set to the currency of the country the product is being
    // sold in. It will default to New Zealand Dollars if no currency can be found from the country.
    this.currency = await currencyFromCountry(this.countryOfSale);
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