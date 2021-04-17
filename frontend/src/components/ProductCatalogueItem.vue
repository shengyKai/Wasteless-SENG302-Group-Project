<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col md="3" sm="12">
          <ProductImageCarousel :itemImages="itemImages"/>
        </v-col>
        <v-col md="9" sm="12">
          <v-row>
            <v-col class="pa-0" md="11" sm="10">
              <v-card-title>
                {{ productName }}
              </v-card-title>
            </v-col>
            <v-col md="1" sm="2">
              <v-card-actions>
                <a @click="editProductDetails">Edit</a>
              </v-card-actions>
            </v-col>
          </v-row>
          <v-row>
            <v-col md="9" sm="10">
              <v-row>
                <span v-if="productDescription.length >= 50">
                  <v-card-text>
                    <b>Description: </b>
                    {{ productDescription.slice(0, 50) }}
                    <FullProductDescription :productDescription="productDescription"/>
                  </v-card-text>
                </span>
                <span v-else>
                  <v-card-text>
                    <b>Description: </b>
                    {{ productDescription }}
                  </v-card-text>
                </span>
              </v-row>
              <v-row>
                <v-card-text>
                  <b>Date Added: </b>
                  {{ productDateAdded }}
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text>
                  <b>Expiry Date: </b>
                  {{ productExpiryDate }}
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text>
                  <b>Manufacturer: </b>
                  {{ productManufacturer }}
                </v-card-text>
              </v-row>
            </v-col>
            <v-col md="3" sm="2">
              <v-row>
                <v-card-text>
                  <b>RRP: </b>
                  ${{ productRRP }}
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text>
                  <b>Quantity: </b>
                  {{ productQuantity }}
                </v-card-text>
              </v-row>
              <v-row>
                <v-card-text>
                  <b>Product Code: </b>
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
import FullProductDescription from "./FullProductDescription.vue";
import ProductImageCarousel from "./ProductImageCarousel.vue";

export default {
  name: "ProductCatalogueItem",
  components: {
    FullProductDescription,
    ProductImageCarousel
  },
  data() {
    return {
      itemImages: [
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
      productName: "Some Product",
      productDescription:
        "Some super long description Some super long description Some super long description Some super long description ",
      productDateAdded: "Some Date Added",
      productExpiryDate: "Some Expired Date",
      productManufacturer: "Some Manufacturer",
      productRRP: 100,
      productQuantity: 5,
      productCode: "Some Code",
      readMoreActivated: false,
    };
  },
  methods: {
    activateReadMore() {
      this.readMoreActivated = true;
    },
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