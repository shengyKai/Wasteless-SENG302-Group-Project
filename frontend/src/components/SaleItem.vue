<template>
  <v-card>
    <v-container>
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
      </v-row>
    </v-container>
  </v-card>
</template>

<script>
import ProductImageCarousel from "@/components/utils/ProductImageCarousel";
export default {
  name: "SaleItem",
  components: {ProductImageCarousel},
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
              "description": "Baked Beans as they should be.",
              "manufacturer": "Heinz Wattie's Limited",
              "recommendedRetailPrice": 2.2,
              "created": "2021-05-15T05:55:32.808Z",
              "images": [
                {
                  "id": 1234,
                  "filename": "/media/images/23987192387509-123908794328.png",
                  "thumbnailFilename": "/media/images/23987192387509-123908794328_thumbnail.png"
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
          "moreInfo": "Seller may be willing to consider near offers",
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
    product() {
      return this.saleItem.inventoryItem.product;
    },
    inventoryItem() {
      return this.saleItem.inventoryItem;
    }
  }
};
</script>

<style scoped>

</style>