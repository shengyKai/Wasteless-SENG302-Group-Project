<template>
    <v-container fluid>
      <v-row>
        <v-col cols="auto" md="3" sm="12">
          <!-- feed the productImages into the carousel child component -->
          <ProductImageCarousel :productImages="productImages"/>
        </v-col>
        <v-col>
          <v-row>
            <v-col class="auto pa-0" md="10" sm="10">
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
          <v-row/>
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
                  ${{ productRRP }}
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
</template>

<script>

export default {
  name: "ProductCatalogueItem",

}
</script>