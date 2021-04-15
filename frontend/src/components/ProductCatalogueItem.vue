<template>
  <v-card>
    <v-container fluid>
      <v-row>
        <v-col cols="3">
          <v-carousel
            v-model="model"
            show-arrows-on-hover
            hide-delimiters
            height="11.5rem"
          >
            <v-carousel-item v-for="(color, i) in colors" :key="color">
              <v-sheet :color="color" height="100%" tile>
                <v-row class="fill-height" align="center" justify="center">
                  <div class="display-3">Slide {{ i + 1 }}</div>
                </v-row>
              </v-sheet>
            </v-carousel-item>
          </v-carousel>
        </v-col>
        <v-col cols="6">
          <v-row align-center>
            {{ productName }}
          </v-row>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
export default {
  name: "ProductCatalogueItem",
  data() {
    return {
      //delete
      model: 0,
      colors: ["primary", "secondary", "yellow darken-2", "red", "orange"],
      //delete
      productName: "Some Product",
      productDescription: "",
      productDateAdded: "",
      productExpiryDate: "",
      productManufacturer: "",
      productRRP: null,
      productQuantity: null,
      productCode: "",
    };
  },
};
</script>

<style scoped>
.v-card {
  height: 12.5rem;
}

.v-carousel {
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  padding-left: 0.5rem;
}
</style>