<template>
  <v-card class="d-flex">
    <v-container
      class="fill-height"
      fluid
    >
      <v-row>
        <v-col cols="3">
          <v-carousel
            show-arrows-on-hover
            hide-delimiters
            height="8rem"
          >
            <v-carousel-item
              v-for="(item, i) in items"
              :key="i"
              :src="item.src"
            />
          </v-carousel>
        </v-col>
        <v-col cols="8">
          <v-row>
            <v-text class="title">
              {{ productName }}
            </v-text>
          </v-row>
          <v-row>
            <v-col cols="8">
              <v-row>
                <span v-if="productDescription.length >= 50">
                  <v-text>
                    <b>Description: </b>
                    {{ productDescription.slice(0, 50) }}
                  </v-text>
                  <FullProductDescription :productDescription="productDescription"/>
                </span>
                <span v-else>
                  <v-text>
                    <b>Description: </b>
                    {{ productDescription }}
                  </v-text>
                </span>
              </v-row>
              <v-row>
                <v-text>
                  <b>Date Added: </b>
                  {{ productDateAdded }}
                </v-text>
              </v-row>
              <v-row>
                <v-text>
                  <b>Expiry Date: </b>
                  {{ productExpiryDate }}
                </v-text>
              </v-row>
              <v-row>
                <v-text>
                  <b>Manufacturer: </b>
                  {{ productManufacturer }}
                </v-text>
              </v-row>
            </v-col>
            <v-col cols="4">
              <v-row>
                <v-text>
                  <b>RRP: </b>
                  ${{ productRRP }}
                </v-text>
              </v-row>
              <v-row>
                <v-text>
                  <b>Quantity: </b>
                  {{ productQuantity }}
                </v-text>
              </v-row>
              <v-row>
                <v-text>
                  <b>Product Code: </b>
                  {{ productCode }}
                </v-text>
              </v-row>
            </v-col>
          </v-row>
        </v-col>
        <v-col cols="1">
          <a href="#">Edit</a>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
import FullProductDescription from "./FullProductDescription.vue";

export default {
  name: "ProductCatalogueItem",
  components: {
    FullProductDescription,
  },
  data() {
    return {
      //delete
      items: [
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
      //delete
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
  },
};
</script>

<style scoped>
.v-carousel {
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
  padding-left: 0.25rem;
}

</style>