<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col md="3" sm="12">
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel :productImages="productImages"/>
        </v-col>
        <v-col md="9" sm="12">
          <v-row>
            <v-col class="pa-0" md="11" sm="10">
              <v-card-title>
                <!-- shows product name -->
                {{ productName }}
              </v-card-title>
            </v-col>
            <v-col md="1" sm="2">
              <v-card-actions>
                <!-- shows the edit button for editing product details, which supposedly links to a form -->
                <a @click="editProductDetails">Edit</a>
              </v-card-actions>
            </v-col>
          </v-row>
          <v-row>
            <v-col md="9" sm="10">
              <v-row>
                <!-- if the description length is more than or equal to 50, the "Read more..." link will appear which will lead the
                user to the FullProductDescription component  -->
                <span v-if="productDescription.length >= 50">
                  <v-card-text id="description">
                    <strong>Description: </strong>
                    {{ productDescription.slice(0, 50) }}
                    <!-- feed the productDescription into the dialog box child component -->
                    <FullProductDescription :productDescription="productDescription"/>
                  </v-card-text>
                </span>
                <!-- else just show the product description -->
                <span v-else>
                  <v-card-text>
                    <strong>Description: </strong>
                    {{ productDescription }}
                  </v-card-text>
                </span>
              </v-row>
              <v-row>
                <!-- shows the date added for the product -->
                <v-card-text>
                  <strong>Date Added: </strong>
                  {{ productDateAdded }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the expiry date for the product -->
                <v-card-text>
                  <strong>Expiry Date: </strong>
                  {{ productExpiryDate }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product manufacturer -->
                <v-card-text>
                  <strong>Manufacturer: </strong>
                  {{ productManufacturer }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col md="3" sm="2">
              <v-row>
                <!-- shows the product price -->
                <v-card-text>
                  <strong>RRP: </strong>
                  ${{ productRRP }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product quantity -->
                <v-card-text>
                  <strong>Quantity: </strong>
                  {{ productQuantity }}
                </v-card-text>
              </v-row>
              <v-row>
                <!-- shows the product code -->
                <v-card-text>
                  <strong>Product Code: </strong>
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
        // {
        //   src: "https://cdn.vuetifyjs.com/images/carousel/squirrel.jpg",
        // },
        // {
        //   src: "https://cdn.vuetifyjs.com/images/carousel/sky.jpg",
        // },
        // {
        //   src: "https://cdn.vuetifyjs.com/images/carousel/bird.jpg",
        // },
        // {
        //   src: "https://cdn.vuetifyjs.com/images/carousel/planet.jpg",
        // },
      ],
      //The bottom 8 variables will all be shown as text/numbers in the component.
      productName: "",
      productDescription: "",
      productDateAdded: "",
      productExpiryDate: "",
      productManufacturer: "",
      productRRP: null,
      productQuantity: null,
      productCode: "",

      // try uncommenting the bottom variables to test out how it looks.
      // productName: "Some Product",
      // productDescription:
      //   "Some super long description Some super long description Some super long description Some super long description",
      // productDateAdded: "Some Date Added",
      // productExpiryDate: "Some Expired Date",
      // productManufacturer: "Some Manufacturer",
      // productRRP: 100,
      // productQuantity: 5,
      // productCode: "Some Code",

      //If readMoreActivated is false, the product description is less than 50 words, so it wont have to use the FullProductDescription
      //component. Else it will use it and the "Read more..." link will also be shown to lead to the FullProductDescription component
      readMoreActivated: false,
    };
  },
  methods: {
    //if the "Read more..." link if clicked, readMoreActivated becomes true and the FullProductDescription dialog box will open
    activateReadMore() {
      this.readMoreActivated = true;
    },
    //add the form to edit product details here
    editProductDetails() {
      alert("TODO");
    }
  },
};
</script>

<style scoped>
/* .v-card {
} */
</style>